package id.my.hxa.musik.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

// Semua setelan lanjutan aplikasi, disimpan di SharedPreferences yang
// sama dengan ThemeStore ("musikbeta"). Satu nilai = satu StateFlow
// biar UI dan pemutar tinggal mengamati.
object SettingsStore {

    // pemutaran
    val kualitasAudio = MutableStateFlow(0) // 0 otomatis, 1 hemat, 2 tertinggi
    val waktuTidur = MutableStateFlow(0) // 0 mati, -1 akhir lagu, selain itu menit
    val lanjutSesi = MutableStateFlow(false)

    // unduhan. Catatan: berkas unduhan mengikuti format sumbernya
    // (umumnya m4a) karena aplikasi tidak punya jalur transkode mp3,
    // jadi setelan "format unduhan" diwujudkan sebagai kualitas
    // unduhan: 0 ikut kualitas putar, 1 hemat, 2 tertinggi
    val formatUnduh = MutableStateFlow(0)
    val unduhWifi = MutableStateFlow(false)

    // lirik
    val sumberLirik = MutableStateFlow(0) // 0 otomatis, 1 lrclib saja
    val alihOtomatis = MutableStateFlow(false)
    val ukuranLirik = MutableStateFlow(1) // 0 kecil, 1 sedang, 2 besar

    // data dan umum
    val sinkronWifi = MutableStateFlow(false)
    val cekUpdate = MutableStateFlow(true)

    // bahasa antarmuka: 0 Indonesia, 1 Inggris
    val bahasa = MutableStateFlow(0)

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.getSharedPreferences("musikbeta", Context.MODE_PRIVATE)
        prefs = p
        kualitasAudio.value = p.getInt("set_kualitas_audio", 0)
        waktuTidur.value = p.getInt("set_waktu_tidur", 0)
        lanjutSesi.value = p.getBoolean("set_lanjut_sesi", false)
        formatUnduh.value = p.getInt("set_format_unduh", 0)
        unduhWifi.value = p.getBoolean("set_unduh_wifi", false)
        sumberLirik.value = p.getInt("set_sumber_lirik", 0)
        alihOtomatis.value = p.getBoolean("set_alih_otomatis", false)
        ukuranLirik.value = p.getInt("set_ukuran_lirik", 1)
        sinkronWifi.value = p.getBoolean("set_sinkron_wifi", false)
        cekUpdate.value = p.getBoolean("set_cek_update", true)
        bahasa.value = p.getInt("set_bahasa", 0)
        Teks.atur(bahasa.value == 1)
    }

    private fun simpanInt(kunci: String, nilai: Int) {
        prefs?.edit()?.putInt(kunci, nilai)?.apply()
    }

    private fun simpanBool(kunci: String, nilai: Boolean) {
        prefs?.edit()?.putBoolean(kunci, nilai)?.apply()
    }

    fun setKualitasAudio(v: Int) {
        kualitasAudio.value = v
        simpanInt("set_kualitas_audio", v)
    }

    fun setWaktuTidur(v: Int) {
        waktuTidur.value = v
        simpanInt("set_waktu_tidur", v)
    }

    fun setLanjutSesi(v: Boolean) {
        lanjutSesi.value = v
        simpanBool("set_lanjut_sesi", v)
    }

    fun setFormatUnduh(v: Int) {
        formatUnduh.value = v
        simpanInt("set_format_unduh", v)
    }

    fun setUnduhWifi(v: Boolean) {
        unduhWifi.value = v
        simpanBool("set_unduh_wifi", v)
    }

    fun setSumberLirik(v: Int) {
        sumberLirik.value = v
        simpanInt("set_sumber_lirik", v)
    }

    fun setAlihOtomatis(v: Boolean) {
        alihOtomatis.value = v
        simpanBool("set_alih_otomatis", v)
    }

    fun setUkuranLirik(v: Int) {
        ukuranLirik.value = v
        simpanInt("set_ukuran_lirik", v)
    }

    fun setSinkronWifi(v: Boolean) {
        sinkronWifi.value = v
        simpanBool("set_sinkron_wifi", v)
    }

    fun setCekUpdate(v: Boolean) {
        cekUpdate.value = v
        simpanBool("set_cek_update", v)
    }

    fun setBahasa(v: Int) {
        bahasa.value = v
        simpanInt("set_bahasa", v)
        Teks.atur(v == 1)
    }
}
