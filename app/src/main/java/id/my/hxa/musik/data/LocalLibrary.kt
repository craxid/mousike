package id.my.hxa.musik.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File

// Satu berkas musik lokal (tab Lokal di beranda). Sumbernya bisa
// indeks MediaStore perangkat, bisa juga folder pilihan user yang
// dipindai langsung per berkas.
data class LocalTrack(
    // kunci unik stabil buat id pemutar ("lokal:<kunci>")
    val kunci: String,
    val judul: String,
    val artis: String,
    val album: String,
    val durasiSec: Int,
    val uri: Uri,
    val albumArtUri: Uri?,
    val lossless: Boolean,
    val formatLabel: String
) {
    fun keTrack(): Track = Track("lokal:$kunci", judul, artis, durasiSec)
}

// Library musik lokal. Dua mode:
// 1) Semua perangkat: query MediaStore (IS_MUSIC, durasi >= 15
//    detik), cepat dan metadatanya lengkap, tapi cuma menemukan
//    berkas yang sudah terindeks Android.
// 2) Folder pilihan: user memilih folder lewat pemilih berkas
//    Android, folder itu dipindai rekursif per berkas pakai
//    MediaMetadataRetriever. Lebih lambat, tapi pasti terbaca
//    walau belum terindeks, dan tidak butuh izin media.
object LocalLibrary {

    val tracks = MutableStateFlow<List<LocalTrack>>(emptyList())
    val termuat = MutableStateFlow(false)
    val sedangMuat = MutableStateFlow(false)

    // folder pilihan yang lagi aktif; null = mode semua perangkat
    val folderUri = MutableStateFlow<Uri?>(null)
    val folderNama = MutableStateFlow("")

    // format lossless yang dikenali; berkas begini dapat tag HI-RES
    private val LOSSLESS_EXT = setOf("flac", "wav", "aif", "aiff", "ape", "dsf", "dff", "alac")
    private val LOSSLESS_MIME = setOf(
        "audio/flac", "audio/x-flac", "audio/wav", "audio/x-wav",
        "audio/aiff", "audio/x-aiff", "audio/ape", "audio/x-ape"
    )
    private val AUDIO_EXT = LOSSLESS_EXT + setOf(
        "mp3", "m4a", "m4b", "ogg", "oga", "opus", "aac", "wma", "mid", "midi"
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences("musikbeta", Context.MODE_PRIVATE)

    private fun folderTersimpan(context: Context): Uri? {
        val s = prefs(context).getString("set_folder_lokal", null) ?: return null
        return try {
            Uri.parse(s)
        } catch (e: Exception) {
            null
        }
    }

    // titik masuk pemuatan: folder pilihan didahulukan, kalau
    // tidak ada baru indeks perangkat
    suspend fun muat(context: Context) {
        val f = folderUri.value ?: folderTersimpan(context)
        if (f != null) {
            folderUri.value = f
            folderNama.value = namaFolder(context, f)
            pindaiFolder(context, f)
        } else {
            muatMediaStore(context)
        }
    }

    // ganti folder pilihan; null = balik ke mode semua perangkat
    suspend fun pakaiFolder(context: Context, uri: Uri?) {
        if (uri == null) {
            prefs(context).edit().remove("set_folder_lokal").apply()
            folderUri.value = null
            folderNama.value = ""
            tracks.value = emptyList()
            termuat.value = false
            muatMediaStore(context)
        } else {
            prefs(context).edit().putString("set_folder_lokal", uri.toString()).apply()
            folderUri.value = uri
            folderNama.value = namaFolder(context, uri)
            tracks.value = emptyList()
            termuat.value = false
            pindaiFolder(context, uri)
        }
    }

    private fun namaFolder(context: Context, treeUri: Uri): String {
        return try {
            DocumentFile.fromTreeUri(context, treeUri)?.name ?: Teks.folderPilihan
        } catch (e: Exception) {
            Teks.folderPilihan
        }
    }

    // ----- mode semua perangkat (MediaStore) -----

    private suspend fun muatMediaStore(context: Context) {
        if (sedangMuat.value) return
        sedangMuat.value = true
        try {
            tracks.value = withContext(Dispatchers.IO) { kueriMediaStore(context) }
            termuat.value = true
        } catch (e: Exception) {
            // izin belum ada: daftar dibiarkan, UI yang menanganinya
        } finally {
            sedangMuat.value = false
        }
    }

    private fun kueriMediaStore(context: Context): List<LocalTrack> {
        val hasil = ArrayList<LocalTrack>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val proyeksi = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val pilihan = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
        context.contentResolver.query(
            uri, proyeksi, pilihan, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val iJudul = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val iArtis = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val iAlbumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val iMime = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val iNama = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                val namaBerkas = c.getString(iNama).orEmpty()
                val ext = namaBerkas.substringAfterLast('.', "").lowercase()
                val mime = c.getString(iMime).orEmpty().lowercase()
                val artisMentah = c.getString(iArtis).orEmpty()
                hasil.add(
                    LocalTrack(
                        kunci = id.toString(),
                        judul = c.getString(iJudul)?.ifBlank { namaBerkas } ?: namaBerkas,
                        artis = if (artisMentah == "<unknown>") Teks.artisTidakDikenal else artisMentah,
                        album = c.getString(iAlbum).orEmpty(),
                        durasiSec = (c.getLong(iDur) / 1000).toInt(),
                        uri = ContentUris.withAppendedId(uri, id),
                        albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            c.getLong(iAlbumId)
                        ),
                        lossless = ext in LOSSLESS_EXT || mime in LOSSLESS_MIME,
                        formatLabel = ext.uppercase().ifEmpty { mime.removePrefix("audio/").uppercase() }
                    )
                )
            }
        }
        return hasil
    }

    // ----- mode folder pilihan (pindai langsung) -----

    private suspend fun pindaiFolder(context: Context, treeUri: Uri) {
        if (sedangMuat.value) return
        sedangMuat.value = true
        try {
            tracks.value = withContext(Dispatchers.IO) { jalanFolder(context, treeUri) }
            termuat.value = true
        } catch (e: Exception) {
            // izin folder lepas atau foldernya hilang: daftar kosong
            tracks.value = emptyList()
            termuat.value = true
        } finally {
            sedangMuat.value = false
        }
    }

    private fun jalanFolder(context: Context, treeUri: Uri): List<LocalTrack> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val berkas = ArrayList<DocumentFile>()
        fun kumpul(dir: DocumentFile) {
            for (f in dir.listFiles()) {
                if (f.isDirectory) {
                    kumpul(f)
                } else if (f.isFile) {
                    val ext = (f.name ?: "").substringAfterLast('.', "").lowercase()
                    val mime = f.type.orEmpty()
                    if (mime.startsWith("audio") || ext in AUDIO_EXT) {
                        berkas.add(f)
                    }
                }
            }
        }
        kumpul(root)
        val hasil = berkas.mapNotNull { bacaMeta(context, it) }
        return hasil.sortedBy { it.judul.lowercase() }
    }

    private fun bacaMeta(context: Context, f: DocumentFile): LocalTrack? {
        val namaBerkas = f.name ?: return null
        val ext = namaBerkas.substringAfterLast('.', "").lowercase()
        var judul = namaBerkas.substringBeforeLast('.')
        var artis = Teks.artisTidakDikenal
        var album = ""
        var durasiSec = 0
        var artUri: Uri? = null
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(context, f.uri)
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.ifBlank { null }?.let { judul = it }
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.ifBlank { null }?.let { artis = it }
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.let { album = it }
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()?.let { durasiSec = (it / 1000).toInt() }
            // sampul tertanam disimpan ke cache biar bisa ditampilkan
            // Coil tanpa membaca ulang berkasnya tiap kali
            val pic = mmr.embeddedPicture
            if (pic != null && pic.isNotEmpty()) {
                val dirArt = File(context.cacheDir, "localart").apply { mkdirs() }
                val berkasArt = File(dirArt, "f" + f.uri.toString().hashCode().toString(36) + ".jpg")
                if (!berkasArt.exists() || berkasArt.length() != pic.size.toLong()) {
                    berkasArt.writeBytes(pic)
                }
                artUri = Uri.fromFile(berkasArt)
            }
        } catch (e: Exception) {
            // metadata tidak kebaca: tetap tampilkan dari nama berkas
        } finally {
            try {
                mmr.release()
            } catch (e: Exception) {
                // abaikan
            }
        }
        // catatan suara dan bunyi pendek tidak ikut, sama kayak mode perangkat
        if (durasiSec in 1..14) return null
        return LocalTrack(
            kunci = "f" + f.uri.toString().hashCode().toString(36),
            judul = judul,
            artis = artis,
            album = album,
            durasiSec = durasiSec,
            uri = f.uri,
            albumArtUri = artUri,
            lossless = ext in LOSSLESS_EXT,
            formatLabel = ext.uppercase()
        )
    }
}
