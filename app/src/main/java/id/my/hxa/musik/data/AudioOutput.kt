package id.my.hxa.musik.data

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow

// Info keluaran audio yang lagi dipakai: speaker HP, kabel 3.5mm,
// USB DAC, Bluetooth, dsb. Android tidak ngasih tahu rute persis
// per lagu ke aplikasi, jadi penentuannya dari daftar perangkat
// keluaran yang terhubung: perangkat yang terakhir tersambung
// diutamakan (itu juga yang biasanya dipilih sistem), sisanya
// jatuh ke urutan USB > HDMI > kabel > Bluetooth > speaker.
object AudioOutput {

    enum class Jenis { SPEAKER, KABEL, USB, BLUETOOTH, HDMI, LAIN }

    data class Info(val label: String, val laju: String, val jenis: Jenis)

    val info = MutableStateFlow<Info?>(null)

    private var appContext: Context? = null
    private var terdaftar = false

    // id perangkat yang terakhir tersambung menurut callback
    @Volatile
    private var terakhirSambungId = -1

    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            addedDevices.lastOrNull { it.isSink }?.let { terakhirSambungId = it.id }
            segarkan()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (removedDevices.any { it.id == terakhirSambungId }) {
                terakhirSambungId = -1
            }
            segarkan()
        }
    }

    fun init(context: Context) {
        if (terdaftar) return
        appContext = context.applicationContext
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            am.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
            terdaftar = true
        } catch (e: Exception) {
            // callback gagal: info tetap dihitung sekali di bawah
        }
        segarkan()
    }

    fun segarkan() {
        val ctx = appContext ?: return
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        info.value = try {
            hitung(am)
        } catch (e: Exception) {
            null
        }
    }

    private fun jenisDari(type: Int): Jenis? = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Jenis.SPEAKER
        AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_LINE_ANALOG -> Jenis.KABEL
        AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> Jenis.USB
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> Jenis.BLUETOOTH
        AudioDeviceInfo.TYPE_HDMI, AudioDeviceInfo.TYPE_HDMI_ARC,
        AudioDeviceInfo.TYPE_HDMI_EARC -> Jenis.HDMI
        else -> null
    }

    private fun labelDari(d: AudioDeviceInfo, jenis: Jenis): String {
        val nama = try {
            d.productName?.toString()?.trim().orEmpty()
        } catch (e: Exception) {
            ""
        }
        return when (jenis) {
            Jenis.SPEAKER -> Teks.speakerHp
            Jenis.KABEL -> if (d.type == AudioDeviceInfo.TYPE_LINE_ANALOG) "Line out" else Teks.kabel35
            // nama produk USB/Bluetooth itu nama perangkat aslinya
            // (nama DAC, nama earbuds), tampilkan apa adanya
            Jenis.USB -> nama.ifEmpty { "USB DAC" }
            Jenis.BLUETOOTH -> nama.ifEmpty { "Bluetooth" }
            Jenis.HDMI -> "HDMI"
            Jenis.LAIN -> nama.ifEmpty { Teks.keluaranLain }
        }
    }

    private fun hitung(am: AudioManager): Info? {
        val sinks = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter { it.isSink }
        if (sinks.isEmpty()) return null
        val berjenis = sinks.mapNotNull { d -> jenisDari(d.type)?.let { d to it } }
        if (berjenis.isEmpty()) return null

        // perangkat terakhir tersambung menang; kalau sudah tidak
        // ada, pakai urutan prioritas umum
        var pilih = berjenis.firstOrNull { it.first.id == terakhirSambungId }
        if (pilih == null) {
            val urutan = listOf(Jenis.USB, Jenis.HDMI, Jenis.KABEL, Jenis.BLUETOOTH, Jenis.SPEAKER, Jenis.LAIN)
            pilih = urutan.firstNotNullOfOrNull { j -> berjenis.firstOrNull { it.second == j } }
        }
        val (dev, jenis) = pilih ?: return null

        // laju sampel keluaran sistem yang lagi jalan, mis. 48000
        val lajuSistem = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            ?.toIntOrNull() ?: 0
        val maksPerangkat = try {
            dev.sampleRates.maxOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
        val laju = buildString {
            if (lajuSistem > 0) append("${lajuSistem / 1000} kHz")
            // buat USB, kemampuan maks DAC-nya menarik ditampilkan
            if (jenis == Jenis.USB && maksPerangkat > lajuSistem && maksPerangkat > 0) {
                if (isNotEmpty()) append(" • ")
                append("${Teks.maksLabel} ${maksPerangkat / 1000} kHz")
            }
        }
        return Info(labelDari(dev, jenis), laju, jenis)
    }
}
