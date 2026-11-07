package id.my.hxa.musik.data

import java.io.ByteArrayOutputStream
import java.io.File

// Penanam metadata ke berkas m4a (MP4): judul, artis, lirik, dan
// cover ditulis ke moov/udta/meta/ilst, sama kayak yang dilakukan
// mutagen di versi web dulu, tapi ini dikerjakan langsung di HP
// sesudah unduhan selesai. Strategi aman: seluruh berkas dibaca,
// box moov disusun ulang dengan ilst baru, offset potongan audio
// di stco/co64 digeser kalau moov-nya membesar dan letaknya
// sebelum mdat (kasus berkas YouTube), lalu berkas ditulis ulang
// lewat berkas sementara. Isi mdat tidak pernah diubah.
object Mp4Tagger {

    private val WADAH = setOf("moov", "trak", "mdia", "minf", "stbl", "udta", "meta")

    private fun u32(b: ByteArray, o: Int): Long =
        ((b[o].toLong() and 0xFF) shl 24) or ((b[o + 1].toLong() and 0xFF) shl 16) or
            ((b[o + 2].toLong() and 0xFF) shl 8) or (b[o + 3].toLong() and 0xFF)

    private fun u64(b: ByteArray, o: Int): Long = (u32(b, o) shl 32) or u32(b, o + 4)

    private fun tulisU32(out: ByteArrayOutputStream, v: Long) {
        out.write((v shr 24).toInt() and 0xFF)
        out.write((v shr 16).toInt() and 0xFF)
        out.write((v shr 8).toInt() and 0xFF)
        out.write(v.toInt() and 0xFF)
    }

    private fun tulisU32ke(b: ByteArray, o: Int, v: Long) {
        b[o] = (v shr 24).toByte()
        b[o + 1] = (v shr 16).toByte()
        b[o + 2] = (v shr 8).toByte()
        b[o + 3] = v.toByte()
    }

    private fun tulisU64ke(b: ByteArray, o: Int, v: Long) {
        tulisU32ke(b, o, v shr 32)
        tulisU32ke(b, o + 4, v and 0xFFFFFFFFL)
    }

    private class Kotak(val tipe: String, val awal: Int, val ukuran: Int, val kepala: Int) {
        val awalIsi get() = awal + kepala
        val akhir get() = awal + ukuran
    }

    // uraikan daftar box di rentang [dari, sampai); berhenti diam-diam
    // di box terakhir yang ukurannya tidak wajar
    private fun urai(b: ByteArray, dari: Int, sampai: Int): List<Kotak> {
        val hasil = ArrayList<Kotak>()
        var p = dari
        while (p + 8 <= sampai) {
            var ukuran = u32(b, p)
            val tipe = String(b, p + 4, 4, Charsets.ISO_8859_1)
            var kepala = 8
            if (ukuran == 1L) {
                ukuran = u64(b, p + 8)
                kepala = 16
            } else if (ukuran == 0L) {
                ukuran = (sampai - p).toLong()
            }
            if (ukuran < kepala || p + ukuran > sampai) break
            hasil.add(Kotak(tipe, p, ukuran.toInt(), kepala))
            p += ukuran.toInt()
        }
        return hasil
    }

    private fun buatBox(tipe: ByteArray, isi: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        tulisU32(out, (isi.size + 8).toLong())
        out.write(tipe)
        out.write(isi)
        return out.toByteArray()
    }

    // satu item ilst: isinya satu box data (4 byte jenis + 4 byte
    // locale + muatan). jenis 1 = teks UTF-8, 13 = gambar JPEG.
    private fun buatItem(tipe: ByteArray, jenisData: Int, muatan: ByteArray): ByteArray {
        val dataIsi = ByteArrayOutputStream()
        tulisU32(dataIsi, jenisData.toLong())
        tulisU32(dataIsi, 0)
        dataIsi.write(muatan)
        return buatBox(tipe, buatBox("data".toByteArray(Charsets.ISO_8859_1), dataIsi.toByteArray()))
    }

    // geser semua entri stco/co64 di dalam moov sebesar delta;
    // jalan rekursif menuruni box wadah saja
    private fun geserOffset(b: ByteArray, dari: Int, sampai: Int, delta: Long) {
        for (k in urai(b, dari, sampai)) {
            when {
                k.tipe == "stco" -> {
                    val n = u32(b, k.awalIsi + 4).toInt()
                    var o = k.awalIsi + 8
                    repeat(n) {
                        tulisU32ke(b, o, u32(b, o) + delta)
                        o += 4
                    }
                }
                k.tipe == "co64" -> {
                    val n = u32(b, k.awalIsi + 4).toInt()
                    var o = k.awalIsi + 8
                    repeat(n) {
                        tulisU64ke(b, o, u64(b, o) + delta)
                        o += 8
                    }
                }
                k.tipe in WADAH -> {
                    val isiMulai = if (k.tipe == "meta") k.awalIsi + 4 else k.awalIsi
                    geserOffset(b, isiMulai, k.akhir, delta)
                }
            }
        }
    }

    fun tanam(
        berkas: File,
        judul: String?,
        artis: String?,
        lirik: String?,
        coverJpeg: ByteArray?
    ): Boolean {
        if (judul == null && artis == null && lirik == null && coverJpeg == null) return false
        val asal = berkas.readBytes()
        val atas = urai(asal, 0, asal.size)
        val moov = atas.firstOrNull { it.tipe == "moov" } ?: return false
        val mdat = atas.firstOrNull { it.tipe == "mdat" } ?: return false

        // ----- susun ilst baru, pertahankan item lama yang tidak diganti -----
        val tipeDiganti = HashSet<String>()
        if (judul != null) tipeDiganti.add("©nam")
        if (artis != null) tipeDiganti.add("©ART")
        if (lirik != null) tipeDiganti.add("©lyr")
        if (coverJpeg != null) tipeDiganti.add("covr")

        val anakMoov = urai(asal, moov.awalIsi, moov.akhir)
        val udtaLama = anakMoov.firstOrNull { it.tipe == "udta" }
        var metaLama: Kotak? = null
        var ilstLama: Kotak? = null
        var anakUdtaLain = emptyList<Kotak>()
        var anakMetaLain = emptyList<Kotak>()
        var benderaMeta = byteArrayOf(0, 0, 0, 0)
        if (udtaLama != null) {
            val anakUdta = urai(asal, udtaLama.awalIsi, udtaLama.akhir)
            anakUdtaLain = anakUdta.filter { it.tipe != "meta" }
            metaLama = anakUdta.firstOrNull { it.tipe == "meta" }
            if (metaLama != null) {
                benderaMeta = asal.copyOfRange(metaLama.awalIsi, metaLama.awalIsi + 4)
                val anakMeta = urai(asal, metaLama.awalIsi + 4, metaLama.akhir)
                anakMetaLain = anakMeta.filter { it.tipe != "ilst" }
                ilstLama = anakMeta.firstOrNull { it.tipe == "ilst" }
            }
        }
        val isiIlst = ByteArrayOutputStream()
        if (ilstLama != null) {
            for (item in urai(asal, ilstLama.awalIsi, ilstLama.akhir)) {
                if (item.tipe !in tipeDiganti) {
                    isiIlst.write(asal, item.awal, item.ukuran)
                }
            }
        }
        if (judul != null) isiIlst.write(buatItem(byteArrayOf(0xA9.toByte(), 'n'.code.toByte(), 'a'.code.toByte(), 'm'.code.toByte()), 1, judul.toByteArray(Charsets.UTF_8)))
        if (artis != null) isiIlst.write(buatItem(byteArrayOf(0xA9.toByte(), 'A'.code.toByte(), 'R'.code.toByte(), 'T'.code.toByte()), 1, artis.toByteArray(Charsets.UTF_8)))
        if (lirik != null) isiIlst.write(buatItem(byteArrayOf(0xA9.toByte(), 'l'.code.toByte(), 'y'.code.toByte(), 'r'.code.toByte()), 1, lirik.toByteArray(Charsets.UTF_8)))
        if (coverJpeg != null) isiIlst.write(buatItem("covr".toByteArray(Charsets.ISO_8859_1), 13, coverJpeg))
        val ilstBaru = buatBox("ilst".toByteArray(Charsets.ISO_8859_1), isiIlst.toByteArray())

        // ----- susun ulang meta, udta, lalu moov -----
        val isiMeta = ByteArrayOutputStream()
        isiMeta.write(benderaMeta)
        if (metaLama == null) {
            // meta baru wajib punya hdlr (penangan 'mdir'),
            // isinya nol semua kecuali jenis penangannya
            val isiHdlr = ByteArrayOutputStream()
            tulisU32(isiHdlr, 0)
            tulisU32(isiHdlr, 0)
            isiHdlr.write("mdir".toByteArray(Charsets.ISO_8859_1))
            repeat(3) { tulisU32(isiHdlr, 0) }
            isiHdlr.write(0)
            isiMeta.write(buatBox("hdlr".toByteArray(Charsets.ISO_8859_1), isiHdlr.toByteArray()))
        }
        for (k in anakMetaLain) isiMeta.write(asal, k.awal, k.ukuran)
        isiMeta.write(ilstBaru)
        val metaBaru = buatBox("meta".toByteArray(Charsets.ISO_8859_1), isiMeta.toByteArray())

        val isiUdta = ByteArrayOutputStream()
        for (k in anakUdtaLain) isiUdta.write(asal, k.awal, k.ukuran)
        isiUdta.write(metaBaru)
        val udtaBaru = buatBox("udta".toByteArray(Charsets.ISO_8859_1), isiUdta.toByteArray())

        val isiMoov = ByteArrayOutputStream()
        var udtaTerpasang = false
        for (k in anakMoov) {
            if (k.tipe == "udta") {
                isiMoov.write(udtaBaru)
                udtaTerpasang = true
            } else {
                isiMoov.write(asal, k.awal, k.ukuran)
            }
        }
        if (!udtaTerpasang) isiMoov.write(udtaBaru)
        val moovBaru = buatBox("moov".toByteArray(Charsets.ISO_8859_1), isiMoov.toByteArray())

        // ----- geser offset potongan audio bila moov membesar dan
        // letaknya sebelum mdat -----
        val delta = (moovBaru.size - moov.ukuran).toLong()
        if (delta != 0L && moov.awal < mdat.awal) {
            geserOffset(moovBaru, 8, moovBaru.size, delta)
        }

        // ----- tulis ulang berkas lewat berkas sementara -----
        val out = ByteArrayOutputStream(asal.size + moovBaru.size)
        for (k in atas) {
            if (k.tipe == "moov") out.write(moovBaru) else out.write(asal, k.awal, k.ukuran)
        }
        val sementara = File(berkas.parentFile, berkas.name + ".tag")
        return try {
            sementara.writeBytes(out.toByteArray())
            if (!sementara.renameTo(berkas)) {
                berkas.writeBytes(out.toByteArray())
                sementara.delete()
            }
            true
        } catch (e: Exception) {
            sementara.delete()
            false
        }
    }
}
