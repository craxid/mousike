package id.my.hxa.musik.player

import id.my.hxa.musik.data.Teks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import id.my.hxa.musik.data.DownloadStore
import id.my.hxa.musik.data.LibraryStore
import id.my.hxa.musik.data.LocalTrack
import id.my.hxa.musik.data.MusicApi
import id.my.hxa.musik.data.ResolvedAudio
import id.my.hxa.musik.data.SettingsStore
import id.my.hxa.musik.data.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

// Otak pemutar: pegang antrean (hasil pencarian), urus resolve URL audio
// per lagu, lalu mainkan lewat ExoPlayer bersama.
//
// Player dipegang langsung di sini, BUKAN lewat MediaController/SessionToken:
// pencarian SessionToken lewat PackageManager gagal di sebagian ROM
// (kasus nyata: HP realme user, v0.2.0 force close pas dibuka dengan
// "Failed to resolve SessionToken"). Karena service jalan satu proses
// dengan aplikasi, player yang sama dipakai bareng: UI mengendalikan dari
// sini, PlaybackService membungkusnya jadi MediaSession buat notifikasi
// dan tombol media. Satu lagu dimuat sekali waktu, lagu berikutnya
// di-resolve pas mau diputar (URL googlevideo umurnya pendek).
object PlayerManager {

    val queue = MutableStateFlow<List<Track>>(emptyList())
    val currentIndex = MutableStateFlow(-1)
    val currentTrack = MutableStateFlow<Track?>(null)
    val isPlaying = MutableStateFlow(false)
    val isBuffering = MutableStateFlow(false)
    val isResolving = MutableStateFlow(false)
    val audioInfo = MutableStateFlow<ResolvedAudio?>(null)
    val errorMessage = MutableStateFlow<String?>(null)
    val shuffleOn = MutableStateFlow(false)
    // mode ulangi: 0 = mati, 1 = ulangi semua, 2 = ulangi satu lagu ini
    val repeatMode = MutableStateFlow(0)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val urlCache = HashMap<String, ResolvedAudio>()

    @Volatile
    private var exoPlayer: ExoPlayer? = null
    @Volatile
    private var appContext: Context? = null

    // pengatur waktu tidur: pekerjaan hitung mundur + bendera
    // "berhenti di akhir lagu ini"
    private var sleepJob: Job? = null
    @Volatile
    private var tidurDiAkhirLagu = false
    val tidurAktif = MutableStateFlow(false)

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying.value = playing
        }

        override fun onPlaybackStateChanged(state: Int) {
            isBuffering.value = state == Player.STATE_BUFFERING
            if (state == Player.STATE_ENDED) next(auto = true)
        }

        // tanpa ini, error dari ExoPlayer (mis. server audio menolak)
        // hilang diam-diam: lagu kelihatan berhenti tanpa penjelasan
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            isBuffering.value = false
            errorMessage.value = Teks.gagalMemutar
        }
    }

    // Player bersama, dibuat sekali. PlaybackService memakai yang ini juga.
    fun player(context: Context): ExoPlayer {
        return exoPlayer ?: synchronized(this) {
            exoPlayer ?: ExoPlayer.Builder(context.applicationContext)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
                .also {
                    it.addListener(listener)
                    exoPlayer = it
                }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        player(context)
        // amati setelan waktu tidur: tiap nilainya berubah, hitung
        // mundurnya diatur ulang dari nol
        scope.launch {
            SettingsStore.waktuTidur.collect { m ->
                sleepJob?.cancel()
                tidurDiAkhirLagu = m == -1
                tidurAktif.value = m != 0
                if (m > 0) {
                    sleepJob = scope.launch {
                        delay(m * 60_000L)
                        exoPlayer?.pause()
                        SettingsStore.setWaktuTidur(0)
                    }
                }
            }
        }
        // layanan media dinyalakan biar notifikasi dan tombol headset siap;
        // kegagalan di sini jangan sampai menjatuhkan aplikasi
        try {
            context.startService(Intent(context, PlaybackService::class.java))
        } catch (e: Exception) {
            // biarin, putar dari UI tetap jalan tanpa layanan
        }
    }

    fun playQueue(tracks: List<Track>, index: Int) {
        queue.value = tracks
        playAt(index)
    }

    // peta berkas lokal perangkat (tab Lokal): id "lokal:<kunci>"
    // ke URI MediaStore, sampul album, dan label formatnya
    private var localUris = HashMap<String, Uri>()
    private var localArt = HashMap<String, Uri>()
    private var localFormat = HashMap<String, String>()
    private var localLossless = HashMap<String, Boolean>()

    fun artLokal(id: String): Uri? = localArt[id]

    // lagu ini lossless? (buat stiker Hi-Res di sampul pemutar)
    fun hiRes(id: String): Boolean = localLossless[id] == true

    fun playLocalQueue(list: List<LocalTrack>, index: Int) {
        localUris = HashMap()
        localArt = HashMap()
        localFormat = HashMap()
        localLossless = HashMap()
        list.forEach { lt ->
            val key = "lokal:${lt.kunci}"
            localUris[key] = lt.uri
            lt.albumArtUri?.let { localArt[key] = it }
            localFormat[key] = lt.formatLabel
            localLossless[key] = lt.lossless
        }
        queue.value = list.map { it.keTrack() }
        playAt(index)
    }

    // detail teknis berkas yang lagi diputar (format, laju sampel,
    // kedalaman bit, bitrate, kanal), dibaca dari berkas aslinya
    // pakai MediaExtractor; cuma buat berkas lokal dan unduhan
    data class AudioDetail(
        val format: String,
        val sampleRate: Int,
        val bitDepth: Int,
        val bitrateKbps: Int,
        val channels: Int
    )

    val audioDetail = MutableStateFlow<AudioDetail?>(null)

    private fun hitungDetail(track: Track, uri: Uri, formatLabel: String) {
        val ctx = appContext ?: return
        scope.launch(Dispatchers.IO) {
            val hasil = try {
                val ex = android.media.MediaExtractor()
                try {
                    ex.setDataSource(ctx, uri, null)
                    if (ex.trackCount == 0) {
                        null
                    } else {
                        val f = ex.getTrackFormat(0)
                        val sr = if (f.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE)) {
                            f.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE)
                        } else 0
                        val ch = if (f.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
                            f.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT)
                        } else 0
                        // kunci "bits-per-sample" tidak punya konstanta
                        // resmi di MediaFormat, makanya ditulis polos
                        val bd = if (f.containsKey("bits-per-sample")) {
                            f.getInteger("bits-per-sample")
                        } else 0
                        var br = if (f.containsKey(android.media.MediaFormat.KEY_BIT_RATE)) {
                            f.getInteger(android.media.MediaFormat.KEY_BIT_RATE)
                        } else 0
                        // bitrate kosong dari format (sering di FLAC):
                        // perkirakan dari ukuran berkas dibagi durasi
                        if (br <= 0 && track.durationSec > 0) {
                            val panjang = try {
                                ctx.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                            if (panjang > 0) {
                                br = (panjang * 8 / track.durationSec).toInt()
                            }
                        }
                        AudioDetail(formatLabel, sr, bd, br / 1000, ch)
                    }
                } finally {
                    ex.release()
                }
            } catch (e: Exception) {
                null
            }
            // jangan timpa detail lagu lain kalau user sudah pindah
            if (currentTrack.value?.id == track.id) {
                audioDetail.value = hasil
            }
        }
    }

    private fun mediaItemFor(track: Track, uri: Uri): MediaItem =
        MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.author)
                    .setArtworkUri(localArt[track.id] ?: Uri.parse(track.thumbBigUrl))
                    .build()
            )
            .build()

    private fun playAt(index: Int, putarOtomatis: Boolean = true, posisiAwal: Long = 0, catatRiwayat: Boolean = true) {
        val tracks = queue.value
        if (index !in tracks.indices) return
        val track = tracks[index]
        currentIndex.value = index
        currentTrack.value = track
        errorMessage.value = null
        audioDetail.value = null

        // berkas musik dari perangkat (tab Lokal): mainkan langsung
        // dari URI MediaStore, dan jangan dicatat ke riwayat online
        // karena id "lokal:" tidak dikenal di sana
        val uriLokal = localUris[track.id]
        if (uriLokal != null) {
            audioInfo.value = ResolvedAudio(
                url = "",
                codec = localFormat[track.id].orEmpty().lowercase(),
                bitrateKbps = 0,
                sampleRate = 0,
                source = "Lokal"
            )
            exoPlayer?.apply {
                setMediaItem(mediaItemFor(track, uriLokal))
                prepare()
                if (posisiAwal > 0) seekTo(posisiAwal)
                if (putarOtomatis) play()
            }
            hitungDetail(track, uriLokal, localFormat[track.id].orEmpty().ifEmpty { "Audio" })
            simpanSesi()
            return
        }

        // sudah diunduh? putar dari berkas lokal, langsung bunyi tanpa server
        val localUri = DownloadStore.localUri(track.id)
        if (localUri != null) {
            audioInfo.value = ResolvedAudio(url = "", codec = "m4a", bitrateKbps = 0, sampleRate = 0, source = "Unduhan")
            exoPlayer?.apply {
                setMediaItem(mediaItemFor(track, localUri))
                prepare()
                if (posisiAwal > 0) seekTo(posisiAwal)
                if (putarOtomatis) play()
            }
            hitungDetail(track, localUri, localUri.lastPathSegment?.substringAfterLast('.', "m4a")?.uppercase() ?: "M4A")
            if (catatRiwayat) LibraryStore.recordPlay(track)
            simpanSesi()
            prefetchNext(index)
            return
        }

        scope.launch {
            isResolving.value = true
            try {
                val resolved = urlCache[track.id] ?: MusicApi.resolveAudio(track.id).also { urlCache[track.id] = it }
                audioInfo.value = resolved
                exoPlayer?.apply {
                    // URL hasil ekstrak lokal diambil langsung.
                    // urlByte yang mutusin alamat finalnya.
                    setMediaItem(mediaItemFor(track, Uri.parse(MusicApi.urlByte(resolved))))
                    prepare()
                    if (posisiAwal > 0) seekTo(posisiAwal)
                    if (putarOtomatis) play()
                }
                if (catatRiwayat) LibraryStore.recordPlay(track)
                simpanSesi()
                prefetchNext(index)
            } catch (e: Exception) {
                errorMessage.value = Teks.gagalMemutarSebab(e.message ?: "")
            } finally {
                isResolving.value = false
            }
        }
    }

    // ----- sesi terakhir (buat setelan "lanjutkan sesi terakhir") -----

    private fun trackKeJson(t: Track): JSONObject = JSONObject()
        .put("id", t.id)
        .put("title", t.title)
        .put("author", t.author)
        .put("dur", t.durationSec)

    // simpan antrean + lagu + posisi sekarang ke berkas, ringan saja
    // dipanggil tiap ganti lagu, jeda, dan geser posisi
    fun simpanSesi() {
        val ctx = appContext ?: return
        val track = currentTrack.value ?: return
        val root = JSONObject()
            .put("track", trackKeJson(track))
            .put("index", currentIndex.value)
            .put("pos", exoPlayer?.currentPosition ?: 0L)
        val arr = JSONArray()
        queue.value.forEach { arr.put(trackKeJson(it)) }
        root.put("queue", arr)
        val text = root.toString()
        scope.launch(Dispatchers.IO) {
            try {
                File(ctx.filesDir, "session.json").writeText(text)
            } catch (e: Exception) {
                // gagal simpan sesi tidak boleh mengganggu pemutaran
            }
        }
    }

    // muat sesi terakhir dalam keadaan jeda (tidak langsung bunyi,
    // tidak dicatat sebagai putaran baru di riwayat)
    fun pulihkanSesi() {
        if (currentTrack.value != null) return
        val ctx = appContext ?: return
        scope.launch {
            try {
                val f = File(ctx.filesDir, "session.json")
                if (!f.exists()) return@launch
                val root = JSONObject(withContext(Dispatchers.IO) { f.readText() })
                // sesi lagu lokal tidak dipulihkan: peta URI-nya
                // sudah tidak ada sesudah aplikasi ditutup
                if (root.optJSONObject("track")?.optString("id")?.startsWith("lokal:") == true) return@launch
                val arr = root.optJSONArray("queue") ?: return@launch
                val q = ArrayList<Track>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id")
                    if (id.isEmpty()) continue
                    q.add(Track(id, o.optString("title"), o.optString("author"), o.optInt("dur", 0)))
                }
                val idx = root.optInt("index", 0)
                if (q.isEmpty() || idx !in q.indices) return@launch
                queue.value = q
                playAt(idx, putarOtomatis = false, posisiAwal = root.optLong("pos", 0), catatRiwayat = false)
            } catch (e: Exception) {
                // berkas sesi rusak: abaikan, aplikasi tetap mulai bersih
            }
        }
    }

    // lagu berikutnya disiapkan diam-diam biar perpindahannya nggak nunggu lama
    private fun prefetchNext(index: Int) {
        val nextTrack = queue.value.getOrNull(index + 1) ?: return
        if (localUris.containsKey(nextTrack.id)) return
        if (urlCache.containsKey(nextTrack.id)) return
        scope.launch {
            try {
                urlCache[nextTrack.id] = MusicApi.resolveAudio(nextTrack.id)
            } catch (e: Exception) {
                // biarin, nanti dicoba lagi pas lagunya benar-benar diputar
            }
        }
    }

    fun togglePlay() {
        val p = exoPlayer ?: return
        if (p.isPlaying) {
            p.pause()
            simpanSesi()
            return
        }
        // sesudah error, player balik ke IDLE dan play() saja tidak cukup:
        // muat ulang lagu yang sedang dipilih biar tombol putar berfungsi
        if (p.playbackState == Player.STATE_IDLE && currentIndex.value >= 0) {
            playAt(currentIndex.value)
        } else {
            p.play()
        }
    }

    // riwayat lompatan pas mode acak nyala, biar tombol sebelumnya
    // beneran balik ke lagu yang tadi diputar, bukan urutan antrean
    private val shuffleHistory = mutableListOf<Int>()

    fun toggleShuffle() {
        shuffleOn.value = !shuffleOn.value
        if (!shuffleOn.value) shuffleHistory.clear()
    }

    fun cycleRepeat() {
        repeatMode.value = (repeatMode.value + 1) % 3
    }

    // lompat langsung ke satu posisi di antrean (dari tab Berikutnya)
    fun jumpTo(index: Int) {
        playAt(index)
    }

    fun next(auto: Boolean = false) {
        val tracks = queue.value
        if (tracks.isEmpty()) return
        // waktu tidur "akhir lagu": lagu ini selesai = berhenti di sini,
        // jangan lanjut ke lagu berikutnya (mengalahkan mode ulangi juga)
        if (auto && tidurDiAkhirLagu) {
            tidurDiAkhirLagu = false
            SettingsStore.setWaktuTidur(0)
            exoPlayer?.pause()
            return
        }
        val cur = currentIndex.value
        // ulangi satu cuma berlaku pas lagu selesai sendiri; user pencet
        // tombol berikutnya ya tetap lanjut kayak biasa
        if (auto && repeatMode.value == 2 && cur in tracks.indices) {
            exoPlayer?.seekTo(0)
            exoPlayer?.play()
            return
        }
        if (shuffleOn.value && tracks.size > 1) {
            if (cur in tracks.indices) shuffleHistory.add(cur)
            var n = cur
            while (n == cur) n = tracks.indices.random()
            playAt(n)
            return
        }
        val nextIndex = cur + 1
        if (nextIndex in tracks.indices) {
            playAt(nextIndex)
        } else if (repeatMode.value == 1) {
            playAt(0)
        }
    }

    fun prev() {
        val p = exoPlayer
        // sama kayak pemutar lain: kalau sudah lewat 3 detik, ulang dari awal dulu
        if (p != null && p.currentPosition > 3000) {
            p.seekTo(0)
            return
        }
        if (shuffleOn.value && shuffleHistory.isNotEmpty()) {
            playAt(shuffleHistory.removeAt(shuffleHistory.lastIndex))
            return
        }
        val prevIndex = currentIndex.value - 1
        if (prevIndex in queue.value.indices) playAt(prevIndex) else p?.seekTo(0)
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        simpanSesi()
    }

    fun positionMs(): Long = exoPlayer?.currentPosition ?: 0L
    fun durationMs(): Long = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L
}
