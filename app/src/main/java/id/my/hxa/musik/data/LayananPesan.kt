package id.my.hxa.musik.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.my.hxa.musik.MainActivity
import id.my.hxa.musik.R

// Notifikasi dari owner lewat FCM: owner nulis pesan dari Firebase
// Console, semua HP yang langganan topik "semua" nerima push-nya.
// Pas aplikasi lagi dibuka, pesannya ditampilin manual dari sini;
// pas aplikasi ketutup, sistem yang nampilin otomatis pakai setelan
// bawaan di AndroidManifest.
class LayananPesan : FirebaseMessagingService() {

    override fun onMessageReceived(pesan: RemoteMessage) {
        val judul = pesan.notification?.title ?: pesan.data["judul"] ?: pesan.data["title"]
        val isi = pesan.notification?.body ?: pesan.data["isi"] ?: pesan.data["body"] ?: ""
        if (judul.isNullOrBlank() && isi.isBlank()) return
        tampilkan(judul?.takeIf { it.isNotBlank() } ?: "Musik", isi)
    }

    override fun onNewToken(token: String) {
        // langganannya pakai topik, bukan token per HP, jadi token
        // barunya nggak perlu disimpan atau dikirim ke mana-mana
    }

    private fun tampilkan(judul: String, isi: String) {
        try {
            val buka = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pending = PendingIntent.getActivity(
                this, judul.hashCode(), buka,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notif = NotificationCompat.Builder(this, SALURAN)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(judul)
                .setContentText(isi)
                .setStyle(NotificationCompat.BigTextStyle().bigText(isi))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify((System.currentTimeMillis() and 0x7fffffff).toInt(), notif)
        } catch (_: Throwable) {
            // notif gagal tampil bukan alasan aplikasi ikut mati
        }
    }

    companion object {
        const val SALURAN = "pengumuman_owner"
        const val TOPIK = "semua"

        // Bikin saluran notifnya + langganan topik owner, dipanggil
        // sekali dari Application. Semuanya dibungkus Throwable
        // soalnya di HP tanpa layanan Google (emulator dsb) Firebase
        // bisa gagal init, dan itu nggak boleh bikin aplikasi force
        // close.
        fun siapkan(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (nm.getNotificationChannel(SALURAN) == null) {
                        val saluran = NotificationChannel(
                            SALURAN, "Pengumuman", NotificationManager.IMPORTANCE_DEFAULT
                        ).apply {
                            description = "Pesan dan pengumuman dari CraXID Project"
                        }
                        nm.createNotificationChannel(saluran)
                    }
                }
            } catch (_: Throwable) {
            }
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIK)
            } catch (_: Throwable) {
            }
        }
    }
}
