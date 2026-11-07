package id.my.hxa.musik.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL

// Tujuan simpan unduhan, dipilih user di jendela konfirmasi sebelum unduh.
sealed class DlDestination {
    object Downloads : DlDestination()
    object Music : DlDestination()
    data class Custom(val treeUri: String, val label: String) : DlDestination()
}

// Pilihan khusus sekali unduh dari dialog unduh: kualitasOverride
// null = ikut setelan kualitas unduhan lalu kualitas putar,
// lirikTeks null = tidak menanam lirik ke berkasnya.
data class OpsiUnduh(
    val kualitasOverride: Int? = null,
    val tanamCover: Boolean = false,
    val lirikTeks: String? = null
)

// Unduhan lagu: file diambil langsung dari URL audio yang sama dengan
// yang dipakai memutar lagu (sudah dicache MusicApi), jadi tidak ada
// pengunduhan ulang di tempat lain. Hasilnya disimpan ke folder yang
// gampang ditemukan orang awam:
// folder Download atau Music bawaan HP, atau folder pilihan user sendiri.
// Registry-nya satu berkas JSON: metadata lagu + lokasi file (URI/path).
object DownloadStore {

    val downloads = MutableStateFlow<List<Track>>(emptyList())

    // id lagu -> persen 0..100 (negatif = lagi resolve / ukuran belum tahu)
    val progress = MutableStateFlow<Map<String, Int>>(emptyMap())
    // id lagu -> judul, buat ditampilkan di popup progres unduhan
    val judulAktif = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastError = MutableStateFlow<String?>(null)
    val destination = MutableStateFlow<DlDestination>(DlDestination.Downloads)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var appContext: Context? = null

    // id lagu -> lokasi file: content:// URI (MediaStore / folder pilihan)
    // atau path absolut (Android lama / unduhan internal versi sebelum 0.4.1)
    private val locById = HashMap<String, String>()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        loadDestination()
        load()
    }

    // ----- tujuan simpan -----

    private fun prefs() = appContext?.getSharedPreferences("musikbeta_dl", Context.MODE_PRIVATE)

    private fun loadDestination() {
        val p = prefs() ?: return
        destination.value = when (p.getInt("dest_kind", 0)) {
            1 -> DlDestination.Music
            2 -> {
                val uri = p.getString("dest_tree", null)
                if (uri != null) DlDestination.Custom(uri, p.getString("dest_label", Teks.folderPilihan) ?: Teks.folderPilihan)
                else DlDestination.Downloads
            }
            else -> DlDestination.Downloads
        }
    }

    fun setDestination(d: DlDestination) {
        destination.value = d
        val p = prefs()?.edit() ?: return
        when (d) {
            is DlDestination.Downloads -> p.putInt("dest_kind", 0)
            is DlDestination.Music -> p.putInt("dest_kind", 1)
            is DlDestination.Custom -> p.putInt("dest_kind", 2).putString("dest_tree", d.treeUri).putString("dest_label", d.label)
        }
        p.apply()
    }

    // label folder dari URI pohon hasil pemilih folder sistem
    fun treeLabel(uri: Uri): String {
        val path = Uri.decode(uri.toString())
        val tail = path.substringAfterLast('/').substringAfterLast(':')
        return tail.ifEmpty { Teks.folderPilihan }
    }

    // ----- nama file -----

    fun sanitizeFileName(name: String): String {
        var s = name.replace(Regex("[\\\\/:*?\"<>|]"), " ")
        s = s.replace(Regex("\\s+"), " ").trim().trim('.')
        if (s.length > 120) s = s.substring(0, 120).trim()
        return s.ifEmpty { "audio" }
    }

    fun defaultFileName(track: Track): String {
        val artist = track.author.removeSuffix(" - Topic").trim()
        val base = if (artist.isNotEmpty() && !track.title.contains(artist, ignoreCase = true)) {
            "${track.title} - $artist"
        } else {
            track.title
        }
        return sanitizeFileName(base)
    }

    // ----- status -----

    fun localUri(trackId: String): Uri? {
        val loc = synchronized(locById) { locById[trackId] } ?: return null
        return if (loc.startsWith("content://")) {
            Uri.parse(loc)
        } else {
            val f = File(loc)
            if (f.exists() && f.length() > 0) Uri.fromFile(f) else null
        }
    }

    fun isDownloaded(trackId: String): Boolean = downloads.value.any { it.id == trackId } && localUri(trackId) != null

    // ----- pilihan dialog unduh terakhir, diingat sebagai bawaan -----

    fun kualitasUnduhTerakhir(): Int = prefs()?.getInt("dl_kualitas", 0) ?: 0
    fun tanamCoverTerakhir(): Boolean = prefs()?.getBoolean("dl_cover", true) ?: true
    fun tanamLirikTerakhir(): Boolean = prefs()?.getBoolean("dl_lirik", true) ?: true

    fun simpanPilihanUnduh(kualitas: Int, cover: Boolean, lirik: Boolean) {
        prefs()?.edit()
            ?.putInt("dl_kualitas", kualitas)
            ?.putBoolean("dl_cover", cover)
            ?.putBoolean("dl_lirik", lirik)
            ?.apply()
    }

    // ----- unduh -----

    fun download(track: Track, fileName: String, dest: DlDestination, opsi: OpsiUnduh = OpsiUnduh()) {
        if (progress.value.containsKey(track.id) || isDownloaded(track.id)) return
        lastError.value = null
        progress.value = progress.value + (track.id to -1)
        judulAktif.value = judulAktif.value + (track.id to track.title)
        scope.launch {
            try {
                val loc = downloadBlocking(track, sanitizeFileName(fileName), dest, opsi)
                synchronized(locById) { locById[track.id] = loc }
                downloads.value = listOf(track) + downloads.value.filter { it.id != track.id }
                save()
            } catch (e: Exception) {
                lastError.value = Teks.unduhanGagal(track.title, e.message ?: "")
            } finally {
                progress.value = progress.value - track.id
                judulAktif.value = judulAktif.value - track.id
            }
        }
    }

    private suspend fun downloadBlocking(track: Track, baseName: String, dest: DlDestination, opsi: OpsiUnduh): String {
        val ctx = appContext ?: throw IllegalStateException(Teks.sebabPenyimpananTidakSiap)
        val tmpDir = File(ctx.filesDir, "dl-tmp").apply { mkdirs() }
        val tmp = File(tmpDir, "${track.id}.part")
        tmp.delete()

        // ambil URL audio: yang sudah pernah diputar langsung dari cache,
        // yang belum ya resolve sekali (sama seperti mau memutar lagunya)
        var resolved = MusicApi.resolveAudio(track.id, opsi.kualitasOverride ?: kualitasUnduh())
        var attempt = 0
        while (true) {
            try {
                // URL hasil ekstrak lokal keikat IP HP ini, jadi
                // bisa diambil langsung
                streamToFile(MusicApi.urlByte(resolved), tmp, track.id)
                break
            } catch (e: Exception) {
                // URL googlevideo bisa kedaluwarsa (403/410) atau putus di
                // tengah jalan: buang cache, resolve ulang, ulangi dari awal.
                // Kesempatan kedua adalah yang terakhir.
                val retryable = e is java.io.IOException ||
                    (e is StreamHttpException && (e.code == 403 || e.code == 410))
                if (!retryable || attempt >= 1) throw e
                attempt++
                MusicApi.invalidateAudio(track.id)
                resolved = MusicApi.resolveAudio(track.id, opsi.kualitasOverride ?: kualitasUnduh())
                tmp.delete()
                progress.value = progress.value + (track.id to -1)
            }
        }
        if (!tmp.exists() || tmp.length() == 0L) {
            tmp.delete()
            throw IllegalStateException(Teks.sebabBerkasKosong)
        }

        val ext = resolved.ext.lowercase().takeIf { it in setOf("m4a", "mp3", "opus", "webm", "ogg", "aac") } ?: "m4a"
        val mime = when (ext) {
            "mp3" -> "audio/mpeg"
            "opus" -> "audio/opus"
            "webm" -> "audio/webm"
            "ogg" -> "audio/ogg"
            "aac" -> "audio/aac"
            else -> "audio/mp4"
        }
        // tanam cover dan/atau lirik ke berkasnya selagi masih di
        // tmp, kayak versi web dulu tapi dikerjakan langsung di HP;
        // gagal menanam tidak boleh menggagalkan unduhannya
        if (ext == "m4a" && (opsi.tanamCover || opsi.lirikTeks != null)) {
            try {
                val cover = if (opsi.tanamCover) ambilCoverPersegi(track) else null
                Mp4Tagger.tanam(tmp, track.title, track.author, opsi.lirikTeks, cover)
            } catch (t: Throwable) {
                // abaikan: berkas tanpa tanaman tetap sah
            }
        }
        val finalName = "$baseName.$ext"
        try {
            return copyToDestination(ctx, tmp, finalName, mime, dest)
        } finally {
            tmp.delete()
        }
    }

    private class StreamHttpException(val code: Int) : Exception("HTTP $code")

    private fun openConn(url: String, range: String? = null): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout = 120_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) Musik")
        if (range != null) conn.setRequestProperty("Range", range)
        return conn
    }

    // Unduh per potongan Range 1 MB, meniru cara yt-dlp: satu koneksi GET
    // biasa ke googlevideo dibatasi YouTube ke kisaran 30 KB/s (terukur di
    // vps3), sedangkan potongan Range 1 MB lewat di kisaran 460 KB/s karena
    // pembatasannya kena per koneksi dan tiap potongan mulai dari awal.
    // Server yang tidak menghormati Range ditangani dengan unduh polos.
    private fun streamToFile(url: String, target: File, trackId: String) {
        // intip total ukuran dulu lewat potongan 1 byte
        val probe = openConn(url, "bytes=0-0")
        val probeCode = probe.responseCode
        if (probeCode == 403 || probeCode == 410) {
            probe.disconnect()
            throw StreamHttpException(probeCode)
        }
        var total = -1L
        if (probeCode == 206) {
            val cr = probe.getHeaderField("Content-Range") ?: ""
            total = cr.substringAfterLast('/').toLongOrNull() ?: -1L
            try { probe.inputStream.use { it.readBytes() } } catch (e: Exception) { /* abaikan */ }
        }
        if (total <= 0) {
            // Range tidak dihormati: badan respons 200 ini sudah berisi
            // seluruh file, sekalian saja dialirkan seperti unduhan polos
            if (probeCode !in 200..299) {
                probe.disconnect()
                throw StreamHttpException(probeCode)
            }
            val len = probe.contentLengthLong
            var done = 0L
            var lastPct = -2
            probe.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        done += n
                        if (len > 0) {
                            val pct = (done * 100 / len).toInt()
                            if (pct != lastPct) {
                                lastPct = pct
                                progress.value = progress.value + (trackId to pct)
                            }
                        }
                    }
                }
            }
            probe.disconnect()
            if (done == 0L) throw IllegalStateException(Teks.sebabBerkasKosong)
            return
        }
        probe.disconnect()

        val chunkSize = 1_048_576L
        var pos = 0L
        var lastPct = -2
        target.outputStream().use { output ->
            val buf = ByteArray(64 * 1024)
            while (pos < total) {
                val end = minOf(pos + chunkSize - 1, total - 1)
                val conn = openConn(url, "bytes=$pos-$end")
                val code = conn.responseCode
                if (code == 403 || code == 410) {
                    conn.disconnect()
                    throw StreamHttpException(code)
                }
                if (code != 206) {
                    conn.disconnect()
                    throw java.io.IOException("potongan unduhan ditolak (HTTP $code)")
                }
                var chunkDone = 0L
                conn.inputStream.use { input ->
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        chunkDone += n
                        val pct = ((pos + chunkDone) * 100 / total).toInt()
                        if (pct != lastPct) {
                            lastPct = pct
                            progress.value = progress.value + (trackId to pct)
                        }
                    }
                }
                conn.disconnect()
                if (chunkDone != end - pos + 1) {
                    throw java.io.IOException(Teks.sebabPotonganTidakUtuh)
                }
                pos = end + 1
            }
        }
        if (target.length() != total) throw java.io.IOException(Teks.sebabUkuranTidakCocok)
    }

    private fun copyToDestination(ctx: Context, src: File, finalName: String, mime: String, dest: DlDestination): String {
        val resolver = ctx.contentResolver
        when (dest) {
            is DlDestination.Custom -> {
                val tree = Uri.parse(dest.treeUri)
                val parent = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
                val doc = DocumentsContract.createDocument(resolver, parent, mime, finalName)
                    ?: throw IllegalStateException(Teks.sebabFolderTidakBisaDitulis)
                resolver.openOutputStream(doc, "w")?.use { out -> src.inputStream().use { it.copyTo(out) } }
                    ?: throw IllegalStateException(Teks.sebabFolderTidakBisaDitulis)
                return doc.toString()
            }
            is DlDestination.Downloads, is DlDestination.Music -> {
                val dirName = if (dest is DlDestination.Music) Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_DOWNLOADS
                if (Build.VERSION.SDK_INT >= 29) {
                    val collection = if (dest is DlDestination.Music) {
                        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    } else {
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    }
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, finalName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mime)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, dirName)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val uri = resolver.insert(collection, values)
                        ?: throw IllegalStateException(Teks.sebabFolderNamaTidakBisaDitulis(dirName))
                    try {
                        resolver.openOutputStream(uri, "w")?.use { out -> src.inputStream().use { it.copyTo(out) } }
                            ?: throw IllegalStateException(Teks.sebabFolderNamaTidakBisaDitulis(dirName))
                        val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                        resolver.update(uri, done, null, null)
                    } catch (e: Exception) {
                        try { resolver.delete(uri, null, null) } catch (_: Exception) {}
                        throw e
                    }
                    return uri.toString()
                } else {
                    // Android 9 ke bawah: tulis langsung ke folder publik,
                    // izin penyimpanan diminta UI sebelum sampai sini
                    val dir = Environment.getExternalStoragePublicDirectory(dirName).apply { mkdirs() }
                    val target = uniqueFile(dir, finalName)
                    src.copyTo(target, overwrite = false)
                    return target.absolutePath
                }
            }
        }
    }

    private fun uniqueFile(dir: File, name: String): File {
        var f = File(dir, name)
        if (!f.exists()) return f
        val base = name.substringBeforeLast('.')
        val ext = name.substringAfterLast('.', "")
        var i = 1
        while (f.exists()) {
            f = File(dir, if (ext.isEmpty()) "$base ($i)" else "$base ($i).$ext")
            i++
        }
        return f
    }

    fun delete(trackId: String) {
        val ctx = appContext
        val loc = synchronized(locById) { locById.remove(trackId) }
        if (ctx != null && loc != null) {
            try {
                if (loc.startsWith("content://")) {
                    val uri = Uri.parse(loc)
                    if (DocumentsContract.isDocumentUri(ctx, uri)) {
                        DocumentsContract.deleteDocument(ctx.contentResolver, uri)
                    } else {
                        ctx.contentResolver.delete(uri, null, null)
                    }
                } else {
                    File(loc).delete()
                }
            } catch (e: Exception) {
                // berkas sudah hilang / izin lepas: registry tetap dibersihkan
            }
        }
        // sisa unduhan internal dari versi sebelum 0.4.1 sekalian dibersihkan
        try {
            ctx?.let { File(File(it.filesDir, "downloads"), "$trackId.m4a").delete() }
        } catch (e: Exception) {
            // abaikan
        }
        downloads.value = downloads.value.filter { it.id != trackId }
        save()
    }

    // ----- registry -----

    private fun save() {
        val ctx = appContext ?: return
        val arr = JSONArray()
        downloads.value.forEach { t ->
            val o = JSONObject()
                .put("id", t.id)
                .put("title", t.title)
                .put("author", t.author)
                .put("dur", t.durationSec)
            synchronized(locById) { locById[t.id] }?.let { o.put("loc", it) }
            arr.put(o)
        }
        val text = JSONObject().put("items", arr).toString()
        scope.launch {
            try {
                File(ctx.filesDir, "downloads.json").writeText(text)
            } catch (e: Exception) {
                // gagal simpan registry tidak boleh menjatuhkan aplikasi
            }
        }
    }

    private fun load() {
        try {
            val ctx = appContext ?: return
            val f = File(ctx.filesDir, "downloads.json")
            if (!f.exists()) return
            val arr = JSONObject(f.readText()).optJSONArray("items") ?: return
            val out = ArrayList<Track>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val id = o.optString("id")
                if (id.isEmpty()) continue
                var loc = o.optString("loc")
                if (loc.isEmpty()) {
                    // unduhan internal dari versi sebelum 0.4.1
                    val file = File(File(ctx.filesDir, "downloads"), "$id.m4a")
                    if (!file.exists() || file.length() == 0L) continue
                    loc = file.absolutePath
                } else if (loc.startsWith("content://")) {
                    // pastikan berkasnya masih ada; izin yang lepas atau file
                    // yang dihapus dari pengelola berkas bikin entri ini basi
                    try {
                        ctx.contentResolver.openInputStream(Uri.parse(loc))?.close()
                            ?: continue
                    } catch (e: FileNotFoundException) {
                        continue
                    } catch (e: Exception) {
                        // kesalahan lain (mis. izin sesaat): pertahankan entrinya
                    }
                } else {
                    val file = File(loc)
                    if (!file.exists() || file.length() == 0L) continue
                }
                synchronized(locById) { locById[id] = loc }
                out.add(Track(id, o.optString("title"), o.optString("author"), o.optInt("dur", 0)))
            }
            downloads.value = out
        } catch (e: Exception) {
            // registry rusak: mulai kosong saja
        }
    }
    // ambil thumbnail lagu lalu potong persegi dari tengahnya buat
    // ditanam sebagai cover; resolusi HD didahulukan, yang biasa
    // jadi cadangan. Gagal di titik mana pun hasilnya null dan
    // unduhan tetap lanjut tanpa cover.
    private fun ambilCoverPersegi(track: Track): ByteArray? {
        for (url in listOf(track.thumbHdUrl, track.thumbBigUrl)) {
            try {
                val conn = openConn(url)
                val bytes = try {
                    if (conn.responseCode !in 200..299) null
                    else conn.inputStream.use { it.readBytes() }
                } finally {
                    conn.disconnect()
                }
                if (bytes == null) continue
                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: continue
                // gambar pengganti dari YouTube pas resolusi HD tidak
                // ada ukurannya kecil; yang begitu jangan dipakai
                if (bmp.width < 300) continue
                val sisi = minOf(bmp.width, bmp.height)
                val potong = android.graphics.Bitmap.createBitmap(
                    bmp, (bmp.width - sisi) / 2, (bmp.height - sisi) / 2, sisi, sisi
                )
                val out = java.io.ByteArrayOutputStream()
                potong.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, out)
                return out.toByteArray()
            } catch (e: Exception) {
                // coba sumber berikutnya
            }
        }
        return null
    }

    // kualitas khusus unduhan dari Pengaturan: null = ikut kualitas putar
    private fun kualitasUnduh(): Int? = when (SettingsStore.formatUnduh.value) {
        1 -> 1
        2 -> 2
        else -> null
    }
}
