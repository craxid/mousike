package id.my.hxa.musik

import android.app.Application
import android.content.Context
import android.util.Log
import java.io.File

// Penangkap kerusakan: stack trace disimpan ke berkas, lalu ditampilkan
// ke user pas aplikasi dibuka lagi. Buat keperluan beta biar laporan
// kerusakan nggak cuma "force close" doang tanpa keterangan.
object CrashLog {
    private fun file(context: Context) = File(context.filesDir, "last-crash.txt")

    fun saveSync(context: Context, error: Throwable) {
        try {
            file(context).writeText(Log.getStackTraceString(error))
        } catch (_: Throwable) {
            // lagi crash, nggak bisa ngapa-ngapain lagi
        }
    }

    fun read(context: Context): String? = try {
        val f = file(context)
        if (f.exists()) f.readText().take(8000) else null
    } catch (e: Exception) {
        null
    }

    fun clear(context: Context) {
        try {
            file(context).delete()
        } catch (_: Exception) {
        }
    }
}

// Pilihan tema aplikasi: 0 ikut sistem, 1 terang, 2 gelap. Disimpan lokal.
object ThemeStore {
    val mode = kotlinx.coroutines.flow.MutableStateFlow(0)
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.getSharedPreferences("musikbeta", Context.MODE_PRIVATE)
        prefs = p
        mode.value = p.getInt("theme_mode", 0)
    }

    fun setMode(m: Int) {
        mode.value = m
        prefs?.edit()?.putInt("theme_mode", m)?.apply()
    }
}

class MusikBetaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            CrashLog.saveSync(this, error)
            prev?.uncaughtException(thread, error)
        }
    }
}
