package id.my.hxa.musik.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Satu info pembaruan dari musik-final-update.json di Google Drive,
// formatnya sama persis kayak berkas versi aplikasi Musik sebelumnya.
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val fileId: String,
    val size: Long,
    val date: String,
    val notes: String
)

// Sistem pembaruan dalam aplikasi, ngikutin pola aplikasi sebelumnya:
// pas dibuka, aplikasi ngecek berkas JSON versi di Drive; kalau ada versi
// lebih baru, user ditawari dialog pembaruan. APK-nya diunduh pakai
// DownloadManager bawaan Android, sesudah selesai langsung prompt install.
object UpdateChecker {

    private const val UPDATE_JSON_URL =
        "https://drive.google.com/uc?export=download&id=13q0gOKZFKpXxA7mrWBLaTK8oeWUxqJPS"

    fun installedVersionCode(context: Context): Int {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) {
            pi.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            pi.versionCode
        }
    }

    fun installedVersionName(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""

    // null artinya tidak ada info yang bisa dipakai (offline, berkas belum
    // siap, atau isinya tidak valid); pemanggil yang membandingkan versinya
    suspend fun check(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(UPDATE_JSON_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("User-Agent", "Musik")
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val d = JSONObject(text)
            val fileId = d.optString("fileId")
            val vc = d.optInt("versionCode", 0)
            if (vc <= 0 || fileId.isEmpty() || fileId == "PENDING") return@withContext null
            UpdateInfo(
                versionCode = vc,
                versionName = d.optString("versionName"),
                fileId = fileId,
                size = d.optLong("size", 0),
                date = d.optString("date"),
                notes = d.optString("notes")
            )
        } catch (e: Exception) {
            null
        }
    }

    // mulai unduhan APK pembaruan; balikin id unduhannya buat dilacak
    // receiver penyelesaian di layar utama
    fun download(context: Context, info: UpdateInfo): Long {
        // pakai host usercontent: alamat uc biasa cuma ngasih halaman
        // peringatan scan virus buat berkas sebesar APK, sedangkan alamat
        // ini langsung ngasih berkasnya (terverifikasi byte-identik)
        val url = "https://drive.usercontent.google.com/download?id=${info.fileId}&export=download&confirm=t"
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle("Musik ${info.versionName}")
            .setDescription("Mengunduh pembaruan Musik")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Musik-${info.versionName}.apk")
            .setMimeType("application/vnd.android.package-archive")
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(req)
    }

    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

    fun openInstallSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // buka installer buat unduhan yang sudah selesai; URI-nya dari
    // DownloadManager langsung, jadi nggak perlu FileProvider
    fun installDownloaded(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = dm.getUriForDownloadedFile(downloadId) ?: return
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
