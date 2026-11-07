package id.my.hxa.musik.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Mesin sinkron library ↔ musik_sync di Supabase. Format datanya
// PERSIS versi web biar dua-duanya bisa baca-tulis baris yang sama:
//   data = { playlists: {at, items: {nama: [lagu utuh]}},
//            history:   {at, items: [id, ...]},
//            likes:     {at, items: [id, ...]},
//            dislikes:  {at, items: [id, ...]},
//            trackmeta: {at, items: {id: {id,title,author,thumb,dur}}} }
// Aturan gabung juga ngikutin web: per bagian, yang cap waktunya
// lebih baru yang dipakai; playlist digabung gabungan per nama;
// trackmeta selalu gabungan (nggak ada metadata yang dibuang).
object SyncManager {

    private val SECTIONS = listOf("history", "likes", "dislikes")
    val status = MutableStateFlow("")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var mulai = false

    // keadaan sinkron yang disimpan ke berkas: cap waktu per bagian,
    // gabungan trackmeta terakhir, id cloud yang metadatanya nggak ada
    // (disimpan apa adanya biar nggak hilang pas aplikasi nge-push),
    // dan hash isi per bagian versi terakhir yang sudah tersinkron
    private val atMap = mutableMapOf<String, String>()
    private val metaMap = mutableMapOf<String, JSONObject>()
    private val remoteOnly = mutableMapOf<String, MutableList<String>>()
    private val hashMap = mutableMapOf<String, String>()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        muatState()
        mulaiAmati()
        if (SupaClient.session.value != null) {
            scope.launch { pullSekarang() }
        } else {
            siapDorong = true
        }
    }

    // dorongan pertama dalam satu sesi aplikasi hanya boleh jalan
    // sesudah satu tarikan berhasil, kalau tidak data lokal bisa
    // menimpa isi cloud yang belum sempat dibaca
    @Volatile
    private var siapDorong = false

    // ----- masuk Google -----

    // jalanin pemilih akun Google bawaan Android, tukar tokennya jadi
    // sesi Supabase, terus langsung tarik data. Balikin pesan error
    // buat ditampilkan, atau null kalau semua beres/dibatalkan diam-diam
    suspend fun masukDenganGoogle(context: Context): String? {
        if (SupaClient.WEB_CLIENT_ID.isEmpty()) return Teks.klienIdBelum
        val token: String = try {
            val cm = CredentialManager.create(context)
            val opsi = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(SupaClient.WEB_CLIENT_ID)
                .build()
            val req = GetCredentialRequest.Builder().addCredentialOption(opsi).build()
            val hasil = cm.getCredential(context, req)
            val cred = hasil.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(cred.data).idToken
            } else {
                return Teks.kredensialBukanGoogle
            }
        } catch (e: GetCredentialCancellationException) {
            return null // user nutup pemilih akun, nggak usah diributin
        } catch (e: NoCredentialException) {
            return Teks.tidakAdaAkunGoogle
        } catch (e: GetCredentialException) {
            return Teks.gagalPemilihAkun(e.message ?: Teks.cobaLagi)
        }
        status.value = Teks.menghubungkanSupabase
        val ok = SupaClient.signInWithIdToken(token)
        if (!ok) {
            status.value = ""
            return Teks.supabaseMenolakToken
        }
        pullSekarang()
        return null
    }

    fun keluar() {
        status.value = ""
    }

    // ----- pengamat perubahan lokal -----

    private fun mulaiAmati() {
        if (mulai) return
        mulai = true
        scope.launch {
            combine(
                LibraryStore.history,
                LibraryStore.likes,
                LibraryStore.disliked,
                LibraryStore.playlists
            ) { _, _, _, _ -> Unit }
                .collectLatest {
                    // debounce 2 detik kayak versi web: perubahan kecil
                    // beruntun cukup sekali tembak ke cloud
                    delay(2000)
                    pushJikaBerubah()
                }
        }
    }

    // ----- tarik + gabung -----

    suspend fun pullSekarang() {
        if (SupaClient.session.value == null) return
        status.value = Teks.mengambilCloud
        val (ok, cloud) = SupaClient.fetchSyncData()
        if (!ok) {
            status.value = Teks.gagalAmbilCloud
            return
        }
        if (cloud == null) {
            // baris belum ada: cloud masih kosong, unggah yang lokal
            status.value = Teks.cloudKosong
            siapDorong = true
            paksaSemuaKotor()
            pushJikaBerubah()
            return
        }
        gabung(cloud)
        siapDorong = true
        status.value = Teks.tersinkronPrefix + jamSekarang()
        // bagian lokal yang lebih baru dari cloud sekalian didorong
        pushJikaBerubah()
    }

    suspend fun sinkronSekarang() {
        pullSekarang()
    }

    private fun gabung(cloud: JSONObject) {
        // 1) trackmeta: gabungan tiga lapis, lokal paling atas
        val cloudMeta = cloud.optJSONObject("trackmeta")?.optJSONObject("items")
        if (cloudMeta != null) {
            val keys = cloudMeta.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                cloudMeta.optJSONObject(k)?.let { metaMap[k] = it }
            }
        }
        metaDariLibrary().forEach { (id, m) -> metaMap[id] = m }
        atMap["trackmeta"] = maksIso(
            atMap["trackmeta"],
            cloud.optJSONObject("trackmeta")?.optString("at")
        )

        var hist = LibraryStore.history.value
        var like = LibraryStore.likes.value
        var dislike = LibraryStore.disliked.value
        var pls = LibraryStore.playlists.value

        // 2) bagian berisi daftar id: yang cap waktunya lebih baru menang,
        //    lokal kosong selalu kalah (kasus HP baru / pasang ulang)
        for (k in SECTIONS) {
            val bagian = cloud.optJSONObject(k) ?: continue
            val cloudAt = bagian.optString("at")
            val localAt = atMap[k] ?: ""
            val lokalKosong = listLokal(k).isEmpty() && (remoteOnly[k].isNullOrEmpty())
            if (!lokalKosong && !isoSesudah(cloudAt, localAt)) continue
            val ids = mutableListOf<String>()
            val arr = bagian.optJSONArray("items")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optString(i).takeIf { it.isNotEmpty() }?.let { ids.add(it) }
                }
            }
            val ketemu = mutableListOf<Track>()
            val asing = mutableListOf<String>()
            for (id in ids) {
                val m = metaMap[id]
                if (m != null) {
                    ketemu.add(
                        Track(
                            id = id,
                            title = m.optString("title", id),
                            author = m.optString("author"),
                            durationSec = m.optInt("dur", 0)
                        )
                    )
                } else {
                    asing.add(id)
                }
            }
            when (k) {
                "history" -> hist = ketemu
                "likes" -> like = ketemu
                "dislikes" -> dislike = ketemu
            }
            remoteOnly[k] = asing
            if (cloudAt.isNotEmpty()) atMap[k] = cloudAt
            hashMap[k] = hashBagian(k, listLokalBaru(k, ketemu))
        }

        // 3) playlist: gabungan per nama, lagu digabung per id
        val cloudPl = cloud.optJSONObject("playlists")
        val cloudItems = cloudPl?.opt("items")
        if (cloudItems is JSONObject) {
            val gabungan = pls.toMutableList()
            val names = cloudItems.keys()
            while (names.hasNext()) {
                val nama = names.next()
                val arr = cloudItems.optJSONArray(nama) ?: continue
                val laguCloud = mutableListOf<Track>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id")
                    if (id.isEmpty()) continue
                    laguCloud.add(
                        Track(
                            id = id,
                            title = o.optString("title", id),
                            author = o.optString("author"),
                            durationSec = o.optInt("dur", 0)
                        )
                    )
                }
                val idx = gabungan.indexOfFirst { it.name == nama }
                if (idx >= 0) {
                    val ada = gabungan[idx].tracks.map { it.id }.toSet()
                    gabungan[idx] = gabungan[idx].copy(
                        tracks = gabungan[idx].tracks + laguCloud.filter { it.id !in ada }
                    )
                } else {
                    gabungan.add(Playlist("cloud-" + nama.hashCode().toString(36), nama, laguCloud))
                }
            }
            pls = gabungan
            atMap["playlists"] = maksIso(atMap["playlists"], cloudPl.optString("at"))
            hashMap["playlists"] = hashPlaylists(pls)
        }
        LibraryStore.applySynced(hist, like, dislike, pls)
        hashMap["trackmeta"] = hashMeta()
        hashKini = hitungHashKini()
        simpanState()
    }

    private fun hitungHashKini(): Map<String, String> = mapOf(
        "history" to hashBagian("history", LibraryStore.history.value),
        "likes" to hashBagian("likes", LibraryStore.likes.value),
        "dislikes" to hashBagian("dislikes", LibraryStore.disliked.value),
        "playlists" to hashPlaylists(LibraryStore.playlists.value),
        "trackmeta" to hashMeta()
    )

    // ----- dorong -----

    private fun paksaSemuaKotor() {
        hashMap.clear()
    }

    suspend fun pushJikaBerubah() {
        if (SupaClient.session.value == null) return
        if (!siapDorong) return
        // setelan sinkron hanya Wi-Fi: dorongan ditahan selama
        // koneksinya seluler; tarikan tetap dibolehkan
        if (SettingsStore.sinkronWifi.value) {
            val ctx = appContext
            if (ctx != null && !NetUtil.sedangWifi(ctx)) {
                status.value = Teks.sinkronDitahanWifi
                return
            }
        }
        sinkronkanHashDanWaktu()
        val kotor = listOf("playlists", "history", "likes", "dislikes", "trackmeta")
            .any { hashKini[it] != hashTersimpan(it) }
        if (!kotor) return
        status.value = "Menyinkronkan..."
        val data = bangunPayload()
        val sukses = SupaClient.pushSyncData(data, isoSekarang())
        if (sukses) {
            terapkanHashSekarang()
            simpanState()
            status.value = Teks.tersinkronPrefix + jamSekarang()
        } else {
            status.value = Teks.gagalSinkron
        }
    }

    // tandai bagian yang isinya berubah: cap waktunya diperbarui.
    // hash tersimpan sengaja dibiarkan versi lama biar pushJikaBerubah
    // tahu ada yang kotor; hash tersimpan baru disamakan sesudah push sukses
    private var hashKini = mapOf<String, String>()
    private fun sinkronkanHashDanWaktu() {
        val baru = hitungHashKini()
        for ((k, h) in baru) {
            if (hashMap[k] != null && hashMap[k] != h && hashKini[k] != h) {
                atMap[k] = isoSekarang()
            }
        }
        hashKini = baru
    }

    private fun hashTersimpan(k: String): String = hashMap[k] ?: ""

    private fun terapkanHashSekarang() {
        for ((k, h) in hashKini) hashMap[k] = h
    }

    private fun bangunPayload(): JSONObject {
        val root = JSONObject()
        // playlist: objek nama -> daftar lagu utuh
        val plObj = JSONObject()
        for (p in LibraryStore.playlists.value) {
            val arr = JSONArray()
            // lagu lokal perangkat tidak ikut ke cloud: id "lokal:"
            // dan sampul MediaStore-nya tidak dikenal versi web
            for (t in p.tracks) {
                if (!t.id.startsWith("lokal:")) arr.put(laguJson(t))
            }
            plObj.put(p.name, arr)
        }
        root.put("playlists", JSONObject().put("at", atMap["playlists"] ?: isoSekarang()).put("items", plObj))
        for (k in SECTIONS) {
            val arr = JSONArray()
            val idsAda = mutableSetOf<String>()
            for (t in listLokal(k)) {
                if (t.id.startsWith("lokal:")) continue
                arr.put(t.id)
                idsAda.add(t.id)
            }
            for (id in remoteOnly[k].orEmpty()) {
                if (id !in idsAda) arr.put(id)
            }
            root.put(k, JSONObject().put("at", atMap[k] ?: isoSekarang()).put("items", arr))
        }
        // trackmeta: gabungan simpanan + yang lagi ada di library
        val metaObj = JSONObject()
        val gabung = metaMap.toMutableMap()
        metaDariLibrary().forEach { (id, m) -> gabung[id] = m }
        for ((id, m) in gabung) {
            if (!id.startsWith("lokal:")) metaObj.put(id, m)
        }
        root.put("trackmeta", JSONObject().put("at", atMap["trackmeta"] ?: isoSekarang()).put("items", metaObj))
        return root
    }

    // ----- bantuan bentuk data -----

    private fun listLokal(k: String): List<Track> = when (k) {
        "history" -> LibraryStore.history.value
        "likes" -> LibraryStore.likes.value
        else -> LibraryStore.disliked.value
    }

    private fun listLokalBaru(k: String, baru: List<Track>): List<Track> = baru

    private fun laguJson(t: Track): JSONObject = JSONObject()
        .put("id", t.id)
        .put("title", t.title)
        .put("author", t.author)
        .put("dur", t.durationSec)
        .put("thumb", t.thumbUrl)

    private fun metaDariLibrary(): Map<String, JSONObject> {
        val out = mutableMapOf<String, JSONObject>()
        val semua = LibraryStore.history.value + LibraryStore.likes.value +
            LibraryStore.disliked.value + LibraryStore.playlists.value.flatMap { it.tracks }
        for (t in semua) out[t.id] = laguJson(t)
        return out
    }

    private fun hashBagian(k: String, list: List<Track>): String {
        val ids = list.joinToString(",") { it.id }
        val asing = remoteOnly[k].orEmpty().joinToString(",")
        return (ids + "|" + asing).hashCode().toString(36)
    }

    private fun hashPlaylists(pls: List<Playlist>): String =
        pls.joinToString(";") { p -> p.name + ":" + p.tracks.joinToString(",") { it.id } }
            .hashCode().toString(36)

    private fun hashMeta(): String {
        val lokal = metaDariLibrary()
        val ids = (metaMap.keys + lokal.keys).sorted().joinToString(",")
        return ids.hashCode().toString(36)
    }

    // ----- waktu ISO, format yang sama kayak toISOString() di web -----

    private fun isoSekarang(): String {
        val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        f.timeZone = TimeZone.getTimeZone("UTC")
        return f.format(Date())
    }

    private fun jamSekarang(): String {
        val f = SimpleDateFormat("HH:mm", Locale.US)
        f.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return "pukul " + f.format(Date()) + " WIB"
    }

    private fun isoSesudah(a: String?, b: String?): Boolean {
        if (a.isNullOrEmpty()) return false
        if (b.isNullOrEmpty()) return true
        return a > b // ISO UTC urutannya sama dengan urutan huruf
    }

    private fun maksIso(a: String?, b: String?): String =
        if (isoSesudah(a, b)) a!! else (b ?: a ?: isoSekarang())

    // ----- simpan & muat keadaan sinkron -----

    private fun berkas(): File? = appContext?.let { File(it.filesDir, "sync_state.json") }

    private fun simpanState() {
        val ctx = appContext ?: return
        val root = JSONObject()
        val at = JSONObject()
        atMap.forEach { (k, v) -> at.put(k, v) }
        root.put("at", at)
        val meta = JSONObject()
        metaMap.forEach { (k, v) -> meta.put(k, v) }
        root.put("meta", meta)
        val ro = JSONObject()
        remoteOnly.forEach { (k, v) ->
            val arr = JSONArray()
            v.forEach { arr.put(it) }
            ro.put(k, arr)
        }
        root.put("remoteOnly", ro)
        val h = JSONObject()
        hashMap.forEach { (k, v) -> h.put(k, v) }
        root.put("hash", h)
        val text = root.toString()
        scope.launch {
            try {
                File(ctx.filesDir, "sync_state.json").writeText(text)
            } catch (e: Exception) {
                // gagal simpan tidak boleh menjatuhkan aplikasi
            }
        }
    }

    private fun muatState() {
        try {
            val f = berkas() ?: return
            if (!f.exists()) return
            val root = JSONObject(f.readText())
            val at = root.optJSONObject("at")
            if (at != null) {
                val keys = at.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    atMap[k] = at.optString(k)
                }
            }
            val meta = root.optJSONObject("meta")
            if (meta != null) {
                val keys = meta.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    meta.optJSONObject(k)?.let { metaMap[k] = it }
                }
            }
            val ro = root.optJSONObject("remoteOnly")
            if (ro != null) {
                for (k in SECTIONS) {
                    val arr = ro.optJSONArray(k) ?: continue
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) arr.optString(i).takeIf { it.isNotEmpty() }?.let { list.add(it) }
                    remoteOnly[k] = list
                }
            }
            val h = root.optJSONObject("hash")
            if (h != null) {
                val keys = h.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    hashMap[k] = h.optString(k)
                }
            }
            hashKini = hitungHashKini()
        } catch (e: Exception) {
            // berkas rusak: mulai dari keadaan kosong
        }
    }
}
