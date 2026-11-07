package id.my.hxa.musik.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Ngobrol langsung ke Supabase versi web (proyek yang sama kayak
// /musik/ web) pakai REST mentah, nggak usah SDK biar APK tetap ramping.
// Yang dipakai cuma dua: Auth (tukar token Google jadi sesi Supabase)
// sama tabel musik_sync buat library.
object SupaClient {

    private val BASE get() = Kunci.SUPA_BASE

    // kunci publishable, memang buat dipasang di aplikasi publik
    // (pengganti anon key), bukan rahasia
    private val PUBLISHABLE get() = Kunci.SUPA_PUBLISHABLE

    // Client ID klien Web dari Google Cloud Console, yang sama dengan
    // yang terdaftar di Supabase Auth. Token Google dari Credential
    // Manager audiensnya harus ID ini. KOSONG = login belum bisa jalan,
    // tombol masuk di UI dinonaktifkan selama ini belum diisi.
    const val WEB_CLIENT_ID = "804186336285-doai54jg1m1526d8js3emnj0t17sf87v.apps.googleusercontent.com"

    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val expiresAtMs: Long,
        val userId: String,
        val email: String,
        val name: String,
        val avatar: String
    )

    val session = MutableStateFlow<Session?>(null)

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val p = prefs() ?: return
        val access = p.getString("access", null) ?: return
        session.value = Session(
            accessToken = access,
            refreshToken = p.getString("refresh", "") ?: "",
            expiresAtMs = p.getLong("expires", 0L),
            userId = p.getString("uid", "") ?: "",
            email = p.getString("email", "") ?: "",
            name = p.getString("name", "") ?: "",
            avatar = p.getString("avatar", "") ?: ""
        )
    }

    private fun prefs() = appContext?.getSharedPreferences("supa_session", Context.MODE_PRIVATE)

    private fun simpan(s: Session?) {
        val p = prefs() ?: return
        if (s == null) {
            p.edit().clear().apply()
            return
        }
        p.edit()
            .putString("access", s.accessToken)
            .putString("refresh", s.refreshToken)
            .putLong("expires", s.expiresAtMs)
            .putString("uid", s.userId)
            .putString("email", s.email)
            .putString("name", s.name)
            .putString("avatar", s.avatar)
            .apply()
    }

    // ----- HTTP kecil-kecilan, gaya yang sama kayak MusicApi -----

    private suspend fun http(method: String, url: String, body: String?, bearer: String?, extra: Map<String, String> = emptyMap()): Pair<Int, String> =
        withContext(Dispatchers.IO) {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = 20000
                conn.readTimeout = 20000
                conn.setRequestProperty("apikey", PUBLISHABLE)
                if (bearer != null) conn.setRequestProperty("Authorization", "Bearer $bearer")
                extra.forEach { (k, v) -> conn.setRequestProperty(k, v) }
                if (body != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
                code to text
            } finally {
                conn.disconnect()
            }
        }

    // ----- auth -----

    // tukar token ID Google (dari Credential Manager) jadi sesi Supabase
    suspend fun signInWithIdToken(idToken: String): Boolean {
        return try {
            val body = JSONObject()
                .put("provider", "google")
                .put("id_token", idToken)
                .toString()
            val (code, text) = http("POST", "$BASE/auth/v1/token?grant_type=id_token", body, PUBLISHABLE)
            if (code !in 200..299) return false
            sesiDari(JSONObject(text), session.value)
        } catch (e: Exception) {
            false
        }
    }

    private fun sesiDari(o: JSONObject, lama: Session?): Boolean {
        val access = o.optString("access_token")
        if (access.isEmpty()) return false
        val user = o.optJSONObject("user")
        val meta = user?.optJSONObject("user_metadata")
        val s = Session(
            accessToken = access,
            refreshToken = o.optString("refresh_token", lama?.refreshToken ?: ""),
            expiresAtMs = System.currentTimeMillis() + o.optLong("expires_in", 3600) * 1000,
            userId = user?.optString("id") ?: lama?.userId ?: "",
            email = user?.optString("email") ?: lama?.email ?: "",
            name = meta?.optString("full_name")?.ifEmpty { null }
                ?: meta?.optString("name")
                ?: lama?.name ?: "",
            avatar = meta?.optString("avatar_url")?.ifEmpty { null }
                ?: meta?.optString("picture")
                ?: lama?.avatar ?: ""
        )
        if (s.userId.isEmpty()) return false
        session.value = s
        simpan(s)
        return true
    }

    // pastikan access token masih segar; kalau mau habis, tukar pakai
    // refresh token. Balikin false kalau sesinya sudah mati beneran.
    suspend fun ensureSession(): Boolean {
        val s = session.value ?: return false
        if (System.currentTimeMillis() < s.expiresAtMs - 60_000) return true
        if (s.refreshToken.isEmpty()) return false
        return try {
            val body = JSONObject().put("refresh_token", s.refreshToken).toString()
            val (code, text) = http("POST", "$BASE/auth/v1/token?grant_type=refresh_token", body, PUBLISHABLE)
            if (code !in 200..299) {
                // refresh ditolak: sesi dianggap berakhir
                session.value = null
                simpan(null)
                false
            } else {
                sesiDari(JSONObject(text), s)
            }
        } catch (e: Exception) {
            // jaringan lagi jelek: jangan langsung vonis keluar,
            // biar pemanggil yang mutusin mau lanjut atau tidak
            true
        }
    }

    suspend fun signOut() {
        val s = session.value
        session.value = null
        simpan(null)
        if (s != null) {
            try {
                http("POST", "$BASE/auth/v1/logout", null, s.accessToken)
            } catch (e: Exception) {
                // abaikan, yang penting lokal sudah bersih
            }
        }
    }

    // ----- tabel musik_sync -----

    // ambil kolom data baris milik user. Bagian pertama hasil: panggilan
    // sukses atau tidak; bagian kedua: datanya (null = baris belum ada,
    // alias cloud masih kosong buat user ini)
    suspend fun fetchSyncData(): Pair<Boolean, JSONObject?> {
        if (!ensureSession()) return false to null
        val s = session.value ?: return false to null
        return try {
            val q = URLEncoder.encode("eq." + s.userId, "UTF-8")
            val (code, text) = http(
                "GET",
                "$BASE/rest/v1/musik_sync?select=data&user_id=$q",
                null,
                s.accessToken
            )
            if (code !in 200..299) return false to null
            val arr = org.json.JSONArray(text)
            if (arr.length() == 0) return true to null
            true to arr.optJSONObject(0)?.optJSONObject("data")
        } catch (e: Exception) {
            false to null
        }
    }

    // upsert satu baris (user_id kunci utama, pola yang sama kayak web)
    suspend fun pushSyncData(data: JSONObject, updatedAtIso: String): Boolean {
        if (!ensureSession()) return false
        val s = session.value ?: return false
        val body = JSONObject()
            .put("user_id", s.userId)
            .put("data", data)
            .put("updated_at", updatedAtIso)
            .toString()
        val (code, _) = http(
            "POST",
            "$BASE/rest/v1/musik_sync",
            body,
            s.accessToken,
            mapOf("Prefer" to "resolution=merge-duplicates")
        )
        return code in 200..299
    }

    // kirim laporan bug/saran ke tabel musik_feedback. Tabelnya
    // cuma menerima INSERT dari kunci publik (aturan RLS), isinya
    // cuma bisa dibaca pemilik proyek dari dashboard Supabase.
    suspend fun kirimFeedback(kategori: String, pesan: String, versiApp: String, perangkat: String): Boolean {
        return try {
            val body = JSONObject()
                .put("kategori", kategori)
                .put("pesan", pesan)
                .put("versi_app", versiApp)
                .put("perangkat", perangkat)
            val s = session.value
            if (s != null && s.email.isNotEmpty()) body.put("akun_email", s.email)
            val (code, _) = http(
                "POST",
                "$BASE/rest/v1/musik_feedback",
                body.toString(),
                s?.accessToken ?: PUBLISHABLE,
                mapOf("Prefer" to "return=minimal")
            )
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
