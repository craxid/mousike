package id.my.hxa.musik.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.stream.AudioStream
import java.util.concurrent.TimeUnit
import okhttp3.Request as OkRequest
import org.schabi.newpipe.extractor.downloader.Request as NpRequest
import org.schabi.newpipe.extractor.downloader.Response as NpResponse

// Pengambil HTTP buat NewPipeExtractor, numpang OkHttp punya aplikasi.
// Polanya ngikutin OkHttpDownloader-nya aplikasi NewPipe.
private class OkDownloader : Downloader() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun execute(request: NpRequest): NpResponse {
        val builder = OkRequest.Builder().url(request.url())
        for ((nama, nilai) in request.headers()) {
            for (v in nilai) builder.addHeader(nama, v)
        }
        when (request.httpMethod()) {
            "POST" -> {
                val data = request.dataToSend() ?: ByteArray(0)
                val tipe = request.headers()["Content-Type"]?.firstOrNull()
                builder.post(data.toRequestBody(tipe?.toMediaTypeOrNull()))
            }
            "HEAD" -> builder.head()
            else -> builder.get()
        }
        client.newCall(builder.build()).execute().use { resp ->
            // 429 dari YouTube itu tantangan captcha, lempar sebagai
            // ReCaptchaException biar extractor yang mutusin langkahnya
            if (resp.code == 429) throw ReCaptchaException("reCaptcha Challenge requested", request.url())
            val body = resp.body?.string() ?: ""
            return NpResponse(
                resp.code,
                resp.message,
                resp.headers.toMultimap(),
                body,
                resp.request.url.toString()
            )
        }
    }
}

// Ekstrak stream audio langsung dari YouTube pakai IP HP ini sendiri.
// Ini jalur resolve satu-satunya sejak v1.0.13 (dulu eksperimen
// bersakelar di v1.0.11).
object LocalYoutube {

    @Volatile
    private var siap = false

    private fun pastikanSiap() {
        if (siap) return
        synchronized(this) {
            if (!siap) {
                NewPipe.init(OkDownloader())
                siap = true
            }
        }
    }

    // kualitas ngikutin arti di Pengaturan: 0 otomatis, 1 hemat, 2 tertinggi
    suspend fun resolve(videoId: String, kualitas: Int): ResolvedAudio = withContext(Dispatchers.IO) {
        pastikanSiap()
        val extractor = NewPipe.getService(ServiceList.YouTube.serviceId)
            .getStreamExtractor("https://www.youtube.com/watch?v=$videoId")
        extractor.fetchPage()
        val streams = extractor.audioStreams.filter { it.content.startsWith("http") }
        if (streams.isEmpty()) throw IllegalStateException("stream audio lokal kosong")

        // pilihan format: m4a didahulukan,
        // hemat ambil bitrate terkecil, tertinggi ambil bitrate terbesar
        val m4aDulu = compareBy<AudioStream> {
            if (it.format?.suffix.equals("m4a", ignoreCase = true)) 0 else 1
        }
        val picked = when (kualitas) {
            1 -> streams.minByOrNull { it.averageBitrate }
            2 -> streams.sortedWith(m4aDulu.thenByDescending { it.averageBitrate }).firstOrNull()
            else -> streams.sortedWith(m4aDulu.thenBy { it.averageBitrate }).firstOrNull()
        } ?: throw IllegalStateException(Teks.tidakAdaFormatAudioLokal)

        val ext = picked.format?.suffix?.lowercase()?.ifEmpty { "m4a" } ?: "m4a"
        ResolvedAudio(
            // URL hasil ekstrak lokal keikat IP HP ini, jadi bisa
            // diambil langsung
            url = picked.content,
            codec = when (ext) {
                "m4a" -> "mp4a"
                "webm", "opus" -> "opus"
                else -> ext
            },
            bitrateKbps = picked.averageBitrate.coerceAtLeast(0),
            // sample rate nggak dikasih extractor per stream; itag umum
            // ditebak dari tabel YouTube, sisanya 0 (nggak ditampilkan)
            sampleRate = when (picked.itag) {
                140, 141 -> 44100
                249, 250, 251 -> 48000
                else -> 0
            },
            source = "Lokal",
            ext = ext
        )
    }
}
