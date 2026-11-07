package id.my.hxa.musik.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// Layanan media buat putar latar belakang: membungkus ExoPlayer bersama
// milik PlayerManager jadi MediaSession, biar sistem menampilkan kontrol
// di notifikasi, layar kunci, dan tombol headset. Player-nya TIDAK dibuat
// atau dilepas di sini karena yang punya PlayerManager (satu proses).
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val session = MediaSession.Builder(this, PlayerManager.player(this)).build()
        mediaSession = session
        addSession(session)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
