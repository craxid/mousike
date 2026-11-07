package id.my.hxa.musik.data

// Semua nilai di berkas ini SENGAJA berupa contoh untuk salinan sumber
// terbuka. Isi dengan alamat layanan dan kunci milikmu sendiri sebelum
// membangun, atau fitur terkait tidak akan jalan:
//
// - RESA_*        alamat backend resa (unduh, stream, transliterasi)
// - SUPA_*        URL proyek Supabase dan publishable key-nya
// - QRIS_STATIS   payload QRIS statis untuk donasi (boleh dikosongkan
//                 kalau fitur donasinya tidak dipakai)
object Kunci {
    val RESA_AUDIO get() = "https://contoh.invalid/api/yt-audio"
    val RESA_STREAM get() = "https://contoh.invalid/api/yt-stream"
    val RESA_TRANSLIT get() = "https://contoh.invalid/api/translit"
    val SUPA_BASE get() = "https://proyek-kamu.supabase.co"
    val SUPA_PUBLISHABLE get() = "ISI_PUBLISHABLE_KEY_SUPABASE_KAMU"
    val QRIS_STATIS get() = ""
}
