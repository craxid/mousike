package id.my.hxa.musik.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Playlist(val id: String, val name: String, val tracks: List<Track>)

// Library lokal: riwayat putar, Disukai, dan Playlist, disimpan sebagai
// satu berkas JSON di penyimpanan internal. Bentuk datanya ngikutin versi
// web ({playlists, history, likes}) biar nanti gampang disinkronkan.
object LibraryStore {

    private const val MAX_HISTORY = 30 // sama kayak versi web

    val history = MutableStateFlow<List<Track>>(emptyList())
    val likes = MutableStateFlow<List<Track>>(emptyList())
    val disliked = MutableStateFlow<List<Track>>(emptyList())
    val playlists = MutableStateFlow<List<Playlist>>(emptyList())

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        load()
    }

    // ----- riwayat -----

    fun recordPlay(track: Track) {
        val updated = (listOf(track) + history.value.filter { it.id != track.id }).take(MAX_HISTORY)
        history.value = updated
        save()
    }

    fun removeFromHistory(trackId: String) {
        history.value = history.value.filter { it.id != trackId }
        save()
    }

    fun clearHistory() {
        history.value = emptyList()
        save()
    }

    // ----- disukai -----

    fun isLiked(trackId: String): Boolean = likes.value.any { it.id == trackId }

    // balikin status sesudah diubah, biar pemanggil tahu hasil akhirnya
    fun toggleLike(track: Track): Boolean {
        return if (isLiked(track.id)) {
            likes.value = likes.value.filter { it.id != track.id }
            save()
            false
        } else {
            likes.value = listOf(track) + likes.value
            save()
            true
        }
    }

    fun removeLike(trackId: String) {
        likes.value = likes.value.filter { it.id != trackId }
        save()
    }

    // ----- tidak disukai -----

    fun isDisliked(trackId: String): Boolean = disliked.value.any { it.id == trackId }

    // nandain tidak suka sekaligus ngeluarin dari Disukai (nggak mungkin
    // dua-duanya), balikin status sesudah diubah
    fun toggleDislike(track: Track): Boolean {
        return if (isDisliked(track.id)) {
            disliked.value = disliked.value.filter { it.id != track.id }
            save()
            false
        } else {
            disliked.value = listOf(track) + disliked.value
            if (isLiked(track.id)) {
                likes.value = likes.value.filter { it.id != track.id }
            }
            save()
            true
        }
    }

    // ----- playlist -----

    fun createPlaylist(name: String): Playlist {
        val p = Playlist(id = UUID.randomUUID().toString(), name = name, tracks = emptyList())
        playlists.value = playlists.value + p
        save()
        return p
    }

    fun deletePlaylist(playlistId: String) {
        playlists.value = playlists.value.filter { it.id != playlistId }
        save()
    }

    fun addToPlaylist(playlistId: String, track: Track) {
        playlists.value = playlists.value.map { p ->
            if (p.id == playlistId && p.tracks.none { it.id == track.id }) {
                p.copy(tracks = p.tracks + track)
            } else {
                p
            }
        }
        save()
    }

    fun removeFromPlaylist(playlistId: String, trackId: String) {
        playlists.value = playlists.value.map { p ->
            if (p.id == playlistId) p.copy(tracks = p.tracks.filter { it.id != trackId }) else p
        }
        save()
    }

    // ----- simpan & muat -----

    private fun trackToJson(t: Track): JSONObject = JSONObject()
        .put("id", t.id)
        .put("title", t.title)
        .put("author", t.author)
        .put("dur", t.durationSec)

    private fun trackFromJson(o: JSONObject): Track? {
        val id = o.optString("id")
        if (id.isEmpty()) return null
        return Track(
            id = id,
            title = o.optString("title"),
            author = o.optString("author"),
            durationSec = o.optInt("dur", 0)
        )
    }

    private fun tracksToJson(list: List<Track>): JSONArray {
        val arr = JSONArray()
        list.forEach { arr.put(trackToJson(it)) }
        return arr
    }

    private fun tracksFromJson(arr: JSONArray?): List<Track> {
        if (arr == null) return emptyList()
        val out = ArrayList<Track>()
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { trackFromJson(it)?.let { t -> out.add(t) } }
        }
        return out
    }

    private fun file(): File? = appContext?.let { File(it.filesDir, "library.json") }

    // bentuk JSON library saat ini, dipakai penyimpanan lokal
    // maupun ekspor berkas di Pengaturan
    private fun bangunJson(): JSONObject {
        val root = JSONObject()
            .put("history", tracksToJson(history.value))
            .put("likes", tracksToJson(likes.value))
            .put("disliked", tracksToJson(disliked.value))
        val pls = JSONArray()
        playlists.value.forEach { p ->
            pls.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("tracks", tracksToJson(p.tracks))
            )
        }
        root.put("playlists", pls)
        return root
    }

    fun exportJson(): String = bangunJson().toString(2)

    // pulihkan library dari berkas ekspor; true kalau berkasnya
    // kebaca dan bentuknya benar
    fun importJson(text: String): Boolean {
        return try {
            val root = JSONObject(text)
            terapkanJson(root)
            save()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun terapkanJson(root: JSONObject) {
        history.value = tracksFromJson(root.optJSONArray("history"))
        likes.value = tracksFromJson(root.optJSONArray("likes"))
        disliked.value = tracksFromJson(root.optJSONArray("disliked"))
        val pls = root.optJSONArray("playlists")
        val out = ArrayList<Playlist>()
        if (pls != null) {
            for (i in 0 until pls.length()) {
                val o = pls.optJSONObject(i) ?: continue
                val pid = o.optString("id")
                if (pid.isEmpty()) continue
                out.add(Playlist(pid, o.optString("name"), tracksFromJson(o.optJSONArray("tracks"))))
            }
        }
        playlists.value = out
    }

    private fun save() {
        val ctx = appContext ?: return
        // snapshot di utas pemanggil, tulisnya di latar biar UI nggak nunggu
        val text = bangunJson().toString()
        scope.launch {
            try {
                File(ctx.filesDir, "library.json").writeText(text)
            } catch (e: Exception) {
                // gagal simpan tidak boleh menjatuhkan aplikasi
            }
        }
    }

    private fun load() {
        try {
            val f = file() ?: return
            if (!f.exists()) return
            terapkanJson(JSONObject(f.readText()))
        } catch (e: Exception) {
            // berkas rusak: mulai dari library kosong, jangan crash
        }
    }
}
