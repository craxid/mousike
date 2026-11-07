package id.my.hxa.musik.data

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

// QRIS donasi: payload statis punya CraXID Project diubah jadi
// dinamis pas pengguna milih nominal, port persis dari plugin
// buatqris bot WA (pustaka qris-statis-to-dinamis). Hasil
// rakitannya sudah dicocokkan byte per byte dengan keluaran
// pustaka aslinya, jadi harusnya aman dipindai e-wallet.
// Semua proses lokal di HP, tidak ada server yang dilibatkan.
object QrisDonasi {

    const val NAMA_PENERIMA = "CraXID Project"
    const val NOMINAL_MIN = 5_000L

    private val QRIS_STATIS get() = Kunci.QRIS_STATIS

    // CRC16-CCITT (init FFFF) sama kayak yang dipakai standar EMVCo
    private fun crc16(str: String): String {
        var crc = 0xFFFF
        for (c in str) {
            crc = crc xor (c.code shl 8)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
            }
        }
        return (crc and 0xFFFF).toString(16).uppercase().padStart(4, '0')
    }

    fun buatPayloadDinamis(nominal: Long): String {
        // buang CRC lama, tandai dinamis, sisipkan tag 54 (nominal)
        // tepat sebelum tag 58 (kode negara), lalu CRC dihitung ulang
        val tanpaCrc = QRIS_STATIS.dropLast(4).replaceFirst("010211", "010212")
        val angka = nominal.toString()
        val tagNominal = "54" + angka.length.toString().padStart(2, '0') + angka
        val potong = tanpaCrc.indexOf("5802ID")
        val isi = tanpaCrc.substring(0, potong) + tagNominal + tanpaCrc.substring(potong)
        return isi + crc16(isi)
    }

    fun buatBitmap(payload: String, ukuran: Int = 720): Bitmap {
        val matriks = QRCodeWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            ukuran,
            ukuran,
            mapOf(EncodeHintType.MARGIN to 2)
        )
        val bmp = Bitmap.createBitmap(ukuran, ukuran, Bitmap.Config.ARGB_8888)
        val hitam = 0xFF000000.toInt()
        val putih = 0xFFFFFFFF.toInt()
        for (x in 0 until ukuran) {
            for (y in 0 until ukuran) {
                bmp.setPixel(x, y, if (matriks.get(x, y)) hitam else putih)
            }
        }
        return bmp
    }
}
