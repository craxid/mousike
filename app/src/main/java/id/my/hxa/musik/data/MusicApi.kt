package id.my.hxa.musik.data

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Satu lagu hasil pencarian, bentuknya disamain kayak versi web.
data class Track(val id: String, val title: String, val author: String, val durationSec: Int) {
    val thumbUrl: String get() = "https://i.ytimg.com/vi/$id/mqdefault.jpg"
    val thumbBigUrl: String get() = "https://i.ytimg.com/vi/$id/hqdefault.jpg"
    val thumbHdUrl: String get() = "https://i.ytimg.com/vi/$id/maxresdefault.jpg"
}

// Audio hasil resolve, plus info teknis buat ditampilin di full player.
data class ResolvedAudio(
    val url: String,
    val codec: String,
    val bitrateKbps: Int,
    val sampleRate: Int,
    val source: String = "API",
    val ext: String = "m4a"
)

// Satu baris lirik berkaraoke: detik dari awal lagu + teksnya.
data class LyricLine(val timeSec: Double, val text: String)

// Hasil lirik: lines terisi kalau ada versi tersinkron (karaoke),
// kalau tidak plain berisi teks polosnya.
data class Lyrics(
    val lines: List<LyricLine>?,
    val plain: String?,
    val source: String,
    val trackName: String,
    val artistName: String
)

// Satu pilihan di daftar hasil pencarian lirik manual: liriknya
// plus durasi versi itu, biar user bisa mencocokkan sendiri
// sama durasi lagu yang lagi diputar.
data class LirikKandidat(
    val lirik: Lyrics,
    val durasiSec: Int
)

// Lapisan data: pencarian via instance Piped publik, audio
// diekstrak langsung di perangkat.
object MusicApi {

    // URL yang siap diambil byte-nya buat hasil resolve ini. Semua
    // hasil resolve diekstrak lokal di perangkat, jadi alamatnya
    // bisa langsung diambil.
    fun urlByte(r: ResolvedAudio): String = r.url

    // urutan bisa berubah: instance yang terakhir berhasil dinaikkan ke depan
    private val piped = mutableListOf(
        "https://api.piped.private.coffee",
        "https://pipedapi.adminforge.de",
        "https://api.piped.projectsegfau.lt",
        "https://pipedapi.reallyaweso.me"
    )

    private fun httpGetText(url: String, timeoutMs: Int, ua: String = "Mozilla/5.0 (Linux; Android) Musik"): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        conn.setRequestProperty("User-Agent", ua)
        if (conn.responseCode !in 200..299) {
            // ambil alasan asli dari badan respons (resa ngirim {"detail": ...}),
            // biar user nggak cuma lihat "HTTP 400" tanpa keterangan
            val errText = try {
                conn.errorStream?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
            conn.disconnect()
            val detail = try {
                JSONObject(errText ?: "").optString("detail")
            } catch (e: Exception) {
                ""
            }
            throw IllegalStateException(if (detail.isNotEmpty()) cleanDetail(detail) else "HTTP ${conn.responseCode}")
        }
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        return text
    }

    private fun getJson(url: String, timeoutMs: Int): JSONObject =
        JSONObject(httpGetText(url, timeoutMs))

    private fun decodeHtml(s: String): String =
        Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString()

    // pesan error yt-dlp bentuknya "ERROR: [youtube] <id>: <alasan>",
    // buang bungkusnya biar yang tampil tinggal alasannya
    private fun cleanDetail(d: String): String {
        var s = d.removePrefix("ERROR:").trim()
        s = Regex("^\\[youtube\\] [A-Za-z0-9_-]{11}:\\s*").replace(s, "")
        return s
    }

    suspend fun search(query: String, filter: String = "videos"): List<Track> = withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        var responded = false
        for (base in piped.toList()) {
            try {
                val q = URLEncoder.encode(query, "UTF-8")
                val d = getJson("$base/search?q=$q&filter=$filter", 12_000)
                responded = true
                val out = parseHasil(d)
                if (out.isEmpty()) continue
                // instance ini yang paling sehat sekarang, taruh paling depan
                piped.remove(base)
                piped.add(0, base)
                return@withContext out
            } catch (e: Exception) {
                lastError = e
            }
        }
        // ada server yang menjawab tapi hasilnya kosong: beneran tidak ketemu
        if (responded) return@withContext emptyList()
        throw lastError ?: IllegalStateException(Teks.semuaServerMati)
    }

    // Lagu terkait buat tab Terkait di full player: Piped nyediain daftar
    // relatedStreams di rincian videonya, isinya lagu-lagu sejenis dari
    // YouTube. Lagu yang sedang diputar sendiri dibuang, video super
    // pendek (di bawah 30 detik, biasanya Shorts) juga.
    suspend fun fetchRelated(trackId: String): List<Track> = withContext(Dispatchers.IO) {
        for (base in piped.toList()) {
            try {
                val d = getJson("$base/streams/$trackId", 12_000)
                val items = d.optJSONArray("relatedStreams") ?: continue
                val out = ArrayList<Track>()
                for (i in 0 until items.length()) {
                    val it = items.optJSONObject(i) ?: continue
                    if (it.optString("type") != "stream") continue
                    val m = Regex("v=([A-Za-z0-9_-]{11})").find(it.optString("url")) ?: continue
                    val id = m.groupValues[1]
                    if (id == trackId) continue
                    val dur = it.optInt("duration", 0)
                    if (dur in 1..29) continue
                    out.add(
                        Track(
                            id = id,
                            title = decodeHtml(it.optString("title")),
                            author = decodeHtml(it.optString("uploaderName")),
                            durationSec = dur
                        )
                    )
                }
                if (out.isEmpty()) continue
                piped.remove(base)
                piped.add(0, base)
                return@withContext out
            } catch (e: Exception) {
                // pindah instance berikutnya
            }
        }
        emptyList()
    }

    // Rekomendasi buat beranda, disusun dari selera user sendiri:
    // artis ditimbang dari Disukai (bobot 3 per lagu) dan riwayat putar
    // (makin baru diputar makin berat). Tiga artis teratas dicarikan
    // lagunya pakai filter music_songs, hasilnya dicampur bergantian
    // biar nggak monoton, dan lagu yang sudah ada di library dibuang.
    // Library masih kosong? Isi dari pencarian musik umum (bukan tren
    // YouTube umum, isinya kebanyakan bukan lagu).
    suspend fun fetchRecommendations(history: List<Track>, likes: List<Track>, disliked: List<Track> = emptyList()): List<Track> = withContext(Dispatchers.IO) {
        val knownIds = HashSet<String>()
        history.forEach { knownIds.add(it.id) }
        likes.forEach { knownIds.add(it.id) }
        // yang ditandai tidak suka jangan dimunculin lagi di rekomendasi
        disliked.forEach { knownIds.add(it.id) }

        fun artistOf(t: Track): String =
            splitArtistTitle(cleanTitle(t.title))?.first ?: cleanArtist(t.author)

        val scores = HashMap<String, Int>()
        for (t in likes) {
            val a = artistOf(t)
            if (a.isNotEmpty() && !a.equals("YouTube", ignoreCase = true)) {
                scores[a] = (scores[a] ?: 0) + 3
            }
        }
        history.forEachIndexed { i, t ->
            val a = artistOf(t)
            if (a.isNotEmpty() && !a.equals("YouTube", ignoreCase = true)) {
                scores[a] = (scores[a] ?: 0) + (history.size - i).coerceAtMost(10)
            }
        }

        val out = ArrayList<Track>()
        val seen = HashSet<String>()
        val topArtists = scores.entries.sortedByDescending { it.value }.take(3).map { it.key }
        if (topArtists.isNotEmpty()) {
            val perArtist = ArrayList<List<Track>>()
            for (a in topArtists) {
                val found = try {
                    search(a, "music_songs")
                } catch (e: Exception) {
                    emptyList()
                }
                perArtist.add(found.filter { it.id !in knownIds && it.durationSec in 60..1200 })
            }
            var idx = 0
            var progressed = true
            while (out.size < 12 && progressed) {
                progressed = false
                for (list in perArtist) {
                    val t = list.getOrNull(idx) ?: continue
                    progressed = true
                    if (seen.add(t.id)) out.add(t)
                    if (out.size >= 12) break
                }
                idx++
            }
        }
        if (out.isEmpty()) {
            // pencarian artisnya kosong/gagal juga: isi dari pencarian
            // musik umum, tetap pakai filter music_songs
            val seeds = listOf("lagu indonesia terbaru", "lagu barat terbaru", "pop indonesia")
            val perSeed = ArrayList<List<Track>>()
            for (s in seeds) {
                val found = try {
                    search(s, "music_songs")
                } catch (e: Exception) {
                    emptyList()
                }
                perSeed.add(found.filter { it.id !in knownIds && it.durationSec in 60..1200 })
            }
            var j = 0
            var jalan = true
            while (out.size < 12 && jalan) {
                jalan = false
                for (list in perSeed) {
                    val t = list.getOrNull(j) ?: continue
                    jalan = true
                    if (seen.add(t.id)) out.add(t)
                    if (out.size >= 12) break
                }
                j++
            }
        }
        out
    }

    // URL googlevideo umurnya pendek, tapi dalam satu sesi aplikasi
    // hasil resolve dipakai bareng pemutar dan unduhan biar lagu yang
    // sudah diputar nggak perlu resolve ulang pas diunduh.
    private val audioCache = HashMap<String, ResolvedAudio>()

    // hasil percobaan jalur lokal terakhir (berhasil / gagal + sebab),
    // ditampilkan di Pengaturan. Fallback ke server itu diam-diam,
    // jadi tanpa ini kegagalan lokal nggak kelihatan sama sekali
    // (pelajaran uji emulator Android 9 di v1.0.13).
    val statusLokal = MutableStateFlow(Teks.belumAdaPercobaan)

    fun invalidateAudio(videoId: String) {
        // kunci cache bentuknya "videoId|kualitas", jadi yang dibuang
        // semua entri yang awalan kuncinya video ini (dulu cuma remove
        // (videoId) polos, nggak pernah kena, bikin resolve ulang pas
        // pemulihan galat diam-diam pakai hasil lama)
        synchronized(audioCache) {
            audioCache.keys.removeAll { it.startsWith("$videoId|") }
        }
    }

    suspend fun resolveAudio(videoId: String, kualitasOverride: Int? = null): ResolvedAudio = withContext(Dispatchers.IO) {
        // kunci cache ikut kualitas yang dipakai: ganti setelan = resolve ulang.
        // kualitasOverride dipakai unduhan biar bisa beda dari kualitas putar
        val kualitas = kualitasOverride ?: SettingsStore.kualitasAudio.value
        val kunciCache = "$videoId|$kualitas"
        synchronized(audioCache) { audioCache[kunciCache] }?.let { return@withContext it }
        // ambil stream langsung dari HP (jalur satu-satunya sejak
        // v1.0.13). Berlaku buat putar DAN unduhan.
        // Tangkapnya Throwable, BUKAN Exception: galat kelas hilang
        // (NoClassDefFoundError) itu Error, lolos dari catch Exception
        // dan sempat bikin aplikasi tertutup sendiri di v1.0.11.
        var gagalLokal: String? = null
        val lokal = try {
            withTimeoutOrNull(30_000) { LocalYoutube.resolve(videoId, kualitas) }
        } catch (e: Throwable) {
            gagalLokal = "${e::class.simpleName}: ${e.message?.take(90) ?: "tanpa pesan"}"
            null
        }
        if (lokal != null) {
            statusLokal.value = Teks.statusBerhasil("${lokal.codec}, ${lokal.bitrateKbps} kbps")
            synchronized(audioCache) { audioCache[kunciCache] = lokal }
            return@withContext lokal
        }
        statusLokal.value = Teks.statusGagal(gagalLokal ?: Teks.batasWaktu30)
        throw IllegalStateException(gagalLokal ?: Teks.batasWaktu30)
    }


    // pengurai satu halaman hasil Piped jadi daftar lagu
    private fun parseHasil(d: JSONObject): List<Track> {
        val items = d.optJSONArray("items") ?: return emptyList()
        val out = ArrayList<Track>()
        for (i in 0 until items.length()) {
            val it = items.optJSONObject(i) ?: continue
            if (it.optString("type") != "stream") continue
            val m = Regex("v=([A-Za-z0-9_-]{11})").find(it.optString("url")) ?: continue
            out.add(
                Track(
                    id = m.groupValues[1],
                    title = decodeHtml(it.optString("title")),
                    author = decodeHtml(it.optString("uploaderName")),
                    durationSec = it.optInt("duration", 0)
                )
            )
        }
        return out
    }

    // satu halaman hasil: daftar lagu + token halaman berikutnya
    // (null = halaman terakhir)
    private class HalamanCari(val lagu: List<Track>, val tokenBerikut: String?)

    // sesi pencarian utama yang berhalaman: halaman musik
    // dihabiskan dulu, sesudah itu baru halaman video. Token
    // halaman cuma berlaku di instance yang mengeluarkannya,
    // makanya instancenya disimpan di sesi.
    class SesiCari(val query: String) {
        var basis: String? = null
        var tokenMusik: String? = null
        var tokenVideo: String? = null
        var musikHabis = false
        var videoAktif = false
        var videoHabis = false
        val habis: Boolean get() = musikHabis && videoHabis
    }

    private suspend fun ambilHalaman(base: String, query: String, filter: String, token: String?): HalamanCari {
        val q = URLEncoder.encode(query, "UTF-8")
        val url = if (token == null) {
            "$base/search?q=$q&filter=$filter"
        } else {
            "$base/nextpage/search?nextpage=${URLEncoder.encode(token, "UTF-8")}&q=$q&filter=$filter"
        }
        val d = getJson(url, 12_000)
        return HalamanCari(parseHasil(d), d.optString("nextpage").ifEmpty { null })
    }

    // halaman pertama pencarian utama: katalog musik didahulukan
    // biar hasil dan sampulnya berupa lagu resmi; video biasa
    // melengkapi halaman pertama kalau hasil musiknya sedikit,
    // halaman-halaman musik tetap didahulukan pas dimuat lanjut
    // (keputusan user 8 Nov: hasil dibuat tanpa batas, lazy load)
    suspend fun cariPertama(query: String): Pair<List<Track>, SesiCari> = withContext(Dispatchers.IO) {
        val sesi = SesiCari(query)
        var lastError: Exception? = null
        for (base in piped.toList()) {
            try {
                val hal = ambilHalaman(base, query, "music_songs", null)
                piped.remove(base)
                piped.add(0, base)
                sesi.basis = base
                sesi.tokenMusik = hal.tokenBerikut
                if (hal.tokenBerikut == null) sesi.musikHabis = true
                var gabungan = hal.lagu
                if (gabungan.size < 5) {
                    sesi.videoAktif = true
                    val hv = try {
                        ambilHalaman(base, query, "videos", null)
                    } catch (e: Exception) {
                        null
                    }
                    if (hv != null) {
                        sesi.tokenVideo = hv.tokenBerikut
                        if (hv.tokenBerikut == null) sesi.videoHabis = true
                        val ada = gabungan.map { it.id }.toHashSet()
                        gabungan = gabungan + hv.lagu.filter { it.id !in ada }
                    } else {
                        sesi.videoHabis = true
                    }
                }
                return@withContext gabungan to sesi
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException(Teks.semuaServerMati)
    }

    // halaman berikutnya dari sesi yang sama; daftar kosong
    // berarti semua halaman dari kedua filter sudah habis.
    // Instance Piped menutup rantai halamannya dengan eror
    // (HTTP 400) alih-alih halaman terakhir yang bersih, jadi
    // eror di tengah jalan diperlakukan sebagai ujung filter
    // itu; halaman kosong yang tokennya masih ada dilompati
    // sekalian dalam panggilan yang sama.
    suspend fun cariBerikutnya(sesi: SesiCari): List<Track> = withContext(Dispatchers.IO) {
        val base = sesi.basis ?: return@withContext emptyList()
        var percobaan = 0
        while (percobaan < 5) {
            if (!sesi.musikHabis) {
                percobaan++
                val hal = try {
                    ambilHalaman(base, sesi.query, "music_songs", sesi.tokenMusik)
                } catch (e: Exception) {
                    sesi.musikHabis = true
                    continue
                }
                sesi.tokenMusik = hal.tokenBerikut
                if (hal.tokenBerikut == null) sesi.musikHabis = true
                if (hal.lagu.isNotEmpty()) return@withContext hal.lagu
                continue
            }
            if (sesi.videoHabis) break
            percobaan++
            val hv = try {
                ambilHalaman(base, sesi.query, "videos", sesi.tokenVideo)
            } catch (e: Exception) {
                sesi.videoHabis = true
                break
            }
            sesi.videoAktif = true
            sesi.tokenVideo = hv.tokenBerikut
            if (hv.tokenBerikut == null) sesi.videoHabis = true
            if (hv.lagu.isNotEmpty()) return@withContext hv.lagu
        }
        emptyList()
    }

    // ----- lirik: LRCLIB utama, cadangan pencarian LRCLIB.
    // Logikanya port dari versi web (cleanTitle, splitArtistTitle, parseLRC).

    private const val LRCLIB_UA = "Musik/1.0"
    private val lyricsCache = HashMap<String, Lyrics?>()

    private fun cleanTitle(t: String): String {
        var s = t
        s = Regex("\\s*[\\(\\[].*?[\\)\\]]\\s*$").replace(s, "")
        s = Regex("\\s*[\\(\\[].*?(official|lyric|lirik|video|audio|\\bmv\\b|m/v|visualizer|live|cover|remix|\\bhd\\b|4k|\\bpv\\b).*?$", RegexOption.IGNORE_CASE).replace(s, " ")
        s = Regex("\\s+\\(?\\s*(full version|official video|official audio|official mv|lyric video|music video)\\s*\\)?\\s*$", RegexOption.IGNORE_CASE).replace(s, "")
        s = Regex("\\s*[\\|｜].*$").replace(s, "")
        return Regex("\\s{2,}").replace(s, " ").trim()
    }

    // pecah "Artis - Judul" dari judul video; artis dari sini lebih akurat daripada nama channel
    private fun splitArtistTitle(title: String): Pair<String, String>? {
        val m = Regex("^(.+?)\\s+[-–—:]\\s+(.+)$").find(title) ?: return null
        val a = m.groupValues[1].trim()
        val ti = m.groupValues[2].trim()
        if (a.isEmpty() || ti.isEmpty() || a.length > 60) return null
        return a to ti
    }

    private fun cleanArtist(a: String): String {
        var s = Regex("\\s*-\\s*Topic$", RegexOption.IGNORE_CASE).replace(a, "")
        s = Regex("\\s*[\\(\\[].*?[\\)\\]]\\s*").replace(s, " ")
        s = Regex("\\s{2,}").replace(s, " ").trim()
        return s.ifEmpty { "YouTube" }
    }

    private fun parseLRC(lrc: String?): List<LyricLine> {
        if (lrc.isNullOrEmpty()) return emptyList()
        val out = ArrayList<LyricLine>()
        val re = Regex("\\[(\\d+):(\\d+(?:\\.\\d+)?)\\](.*)")
        for (ln in lrc.split("\n")) {
            val m = re.find(ln) ?: continue
            val txt = m.groupValues[3].trim()
            if (txt.isNotEmpty()) {
                out.add(LyricLine(m.groupValues[1].toInt() * 60 + m.groupValues[2].toDouble(), txt))
            }
        }
        out.sortBy { it.timeSec }
        return out
    }


    private class LrcNotFound : Exception()

    // GET khusus LRCLIB: servernya sekarang sering balas 503/429 pas lagi
    // penuh, lengkap dengan header Retry-After. Sekali tembak langsung
    // gagal total (itu biang liriknya dulu sering error), jadi di sini
    // diulang sampai 5x sambil nurut jeda yang diminta server.
    private fun httpGetLrclib(url: String): String {
        var lastErr: Exception? = null
        for (attempt in 1..5) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15_000
                conn.readTimeout = 15_000
                conn.setRequestProperty("User-Agent", LRCLIB_UA)
                val code = conn.responseCode
                if (code in 200..299) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    return text
                }
                val retryAfter = conn.getHeaderField("Retry-After")?.toLongOrNull()
                conn.disconnect()
                if (code == 404) throw LrcNotFound()
                if (code != 429 && code != 503) throw IllegalStateException("HTTP $code")
                lastErr = IllegalStateException("HTTP $code")
                if (attempt < 5) {
                    val tunggu = (retryAfter ?: (attempt * 2L)).coerceIn(1, 15) * 1000 + (0..400).random()
                    Thread.sleep(tunggu)
                }
            } catch (e: LrcNotFound) {
                throw e
            } catch (e: Exception) {
                lastErr = e
                if (attempt < 5) Thread.sleep(attempt * 1500L + (0..400).random())
            }
        }
        throw lastErr ?: IllegalStateException(Teks.lrclibTidakMerespons)
    }

    private fun candFromJson(d: JSONObject, src: String): Lyrics? {
        val syncedRaw = d.optString("syncedLyrics").ifEmpty { d.optString("lyrics") }
        val plainRaw = d.optString("plainLyrics").ifEmpty { d.optString("lyrics") }
        val lines = parseLRC(syncedRaw)
        if (lines.isEmpty() && plainRaw.isEmpty()) return null
        return Lyrics(
            lines = lines.ifEmpty { null },
            plain = plainRaw.ifEmpty { null },
            source = src,
            trackName = d.optString("trackName").ifEmpty { d.optString("track_name") },
            artistName = d.optString("artistName").ifEmpty { d.optString("artist_name") }
        )
    }

    suspend fun fetchLyrics(track: Track): Lyrics? = withContext(Dispatchers.IO) {
        if (lyricsCache.containsKey(track.id)) return@withContext lyricsCache[track.id]
        val result = try {
            fetchLyricsUncached(track)
        } catch (e: Exception) {
            null
        }
        lyricsCache[track.id] = result
        result
    }

    // saran isi kolom pencarian manual: judul dan artis hasil
    // pembersihan yang sama dengan yang dipakai jalur otomatis
    fun saranLirik(track: Track): Pair<String, String> {
        val titleC = cleanTitle(track.title)
        val split = splitArtistTitle(titleC)
        val channelArtist = cleanArtist(track.author)
        var titleOnly = split?.second ?: titleC
        if (split == null && titleOnly.lowercase().startsWith(channelArtist.lowercase())) {
            titleOnly = Regex("^[\\s\\-–—:]+").replace(titleOnly.substring(channelArtist.length), "").trim()
        }
        return titleOnly to (split?.first ?: channelArtist)
    }

    // pencarian lirik manual dari panel lirik: judul (dan artis)
    // diketik sendiri oleh user karena judul YouTube kadang beda
    // dari judul resmi lagunya. Bedanya dari jalur otomatis: di
    // sini SEMUA kandidat dikembalikan sebagai daftar, user yang
    // memilih sendiri mana yang benar. Dulu hasil terbaik dipilih
    // otomatis dan pernah salah pasang ke lagu lain.
    suspend fun cariKandidatLirik(durasiTrack: Int, judul: String, artis: String): List<LirikKandidat> = withContext(Dispatchers.IO) {
        val j = judul.trim()
        val a = artis.trim()
        if (j.isEmpty()) return@withContext emptyList()
        val kueri = listOf(a, j).filter { it.isNotEmpty() }.joinToString(" ")
        val out = ArrayList<LirikKandidat>()
        val seen = HashSet<String>()
        fun tambah(l: Lyrics, dur: Int) {
            val kunci = (l.trackName + "|" + l.artistName + "|" + dur).lowercase()
            if (seen.add(kunci)) out.add(LirikKandidat(l, dur))
        }
        // pasangan persis judul + artis didahulukan
        if (a.isNotEmpty()) {
            try {
                val q = "artist_name=" + URLEncoder.encode(a, "UTF-8") +
                    "&track_name=" + URLEncoder.encode(j, "UTF-8") +
                    if (durasiTrack > 0) "&duration=$durasiTrack" else ""
                candFromJson(JSONObject(httpGetLrclib("https://lrclib.net/api/get?$q")), "LRCLIB")
                    ?.let { tambah(it, durasiTrack) }
            } catch (e: Exception) {
                // lanjut ke pencarian bebas
            }
        }
        // pencarian bebas LRCLIB: semua hasil, diurutkan dari yang
        // durasinya paling dekat sama lagu yang lagi diputar
        try {
            val q = URLEncoder.encode(kueri, "UTF-8")
            val arr = JSONArray(httpGetLrclib("https://lrclib.net/api/search?q=$q"))
            val tmp = ArrayList<LirikKandidat>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val c = candFromJson(o, "LRCLIB") ?: continue
                tmp.add(LirikKandidat(c, o.optDouble("duration", 0.0).toInt()))
            }
            tmp.sortBy { k ->
                if (durasiTrack > 0 && k.durasiSec > 0) kotlin.math.abs(k.durasiSec - durasiTrack) else 9999
            }
            tmp.forEach { tambah(it.lirik, it.durasiSec) }
        } catch (e: Exception) {
            // pencarian bebas gagal, pakai kandidat yang sudah ada
        }
        out.take(10)
    }

    // pasang lirik pilihan user ke cache lagu ini
    fun terapkanLirik(track: Track, l: Lyrics) {
        lyricsCache[track.id] = l
    }

    // buang lirik yang kepasang buat lagu ini; panel balik ke
    // keadaan tidak ditemukan dan pencarian otomatis jalan lagi
    // pas pemutar lagu ini dibuka ulang
    fun hapusLirikCache(track: Track) {
        lyricsCache.remove(track.id)
    }

    private fun fetchLyricsUncached(track: Track): Lyrics? {
        val titleC = cleanTitle(track.title)
        val split = splitArtistTitle(titleC)
        val channelArtist = cleanArtist(track.author)
        var titleOnly = split?.second ?: titleC
        if (split == null && titleOnly.lowercase().startsWith(channelArtist.lowercase())) {
            titleOnly = Regex("^[\\s\\-–—:]+").replace(titleOnly.substring(channelArtist.length), "").trim()
        }
        val artists = ArrayList<String>()
        split?.first?.let { artists.add(it) }
        if (channelArtist.isNotEmpty() && (split == null || !split.first.equals(channelArtist, ignoreCase = true))) {
            artists.add(channelArtist)
        }
        if (artists.isEmpty()) artists.add("YouTube")
        val artist = artists[0]

        // jalur utama: /api/get per kandidat artis
        for (a in artists) {
            try {
                val q = "artist_name=" + URLEncoder.encode(a, "UTF-8") +
                    "&track_name=" + URLEncoder.encode(titleOnly, "UTF-8") +
                    if (track.durationSec > 0) "&duration=${track.durationSec}" else ""
                val d = JSONObject(httpGetLrclib("https://lrclib.net/api/get?$q"))
                candFromJson(d, "LRCLIB")?.let { return it }
            } catch (e: Exception) {
                // lanjut ke artis berikutnya
            }
        }
        // cadangan 1: pencarian LRCLIB, pilih kandidat paling cocok
        try {
            val q = URLEncoder.encode("$artist $titleOnly", "UTF-8")
            val arr = JSONArray(httpGetLrclib("https://lrclib.net/api/search?q=$q"))
            var best: Lyrics? = null
            var bestScore = Int.MAX_VALUE
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val c = candFromJson(o, "LRCLIB") ?: continue
                val dd = o.optDouble("duration", 0.0)
                val durDiff = if (track.durationSec > 0 && dd > 0) kotlin.math.abs(dd - track.durationSec).toInt() else 999
                val score = (if (c.lines != null) 0 else 1000) + durDiff
                if (score < bestScore) {
                    bestScore = score
                    best = c
                }
            }
            if (best != null) return best
        } catch (e: Exception) {
            // semua jalur habis
        }
        return null
    }
}
