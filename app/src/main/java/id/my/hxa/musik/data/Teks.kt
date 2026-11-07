package id.my.hxa.musik.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Tabel teks antarmuka dua bahasa. Kolom Indonesia ditulis
// dalam bahasa baku (keputusan user 7 Nov 2026: teks aplikasi
// jangan informal), kolom Inggris padanannya. UI membaca
// properti di sini; mengganti bahasa menggambar ulang
// tampilan karena [inggris] adalah state Compose.
object Teks {

    var inggris by mutableStateOf(false)
        private set

    fun atur(ing: Boolean) {
        inggris = ing
    }

    private fun p(id: String, en: String): String = if (inggris) en else id

    // ---- umum ----
    val tutup get() = p("Tutup", "Close")
    val batal get() = p("Batal", "Cancel")
    val oke get() = p("Oke", "OK")
    val simpan get() = p("Simpan", "Save")
    val hapus get() = p("Hapus", "Delete")
    val ganti get() = p("Ganti", "Change")
    val kembali get() = p("Kembali", "Back")
    val muatUlang get() = p("Muat ulang", "Reload")
    val salin get() = p("Salin", "Copy")
    val reset get() = p("Reset", "Reset")
    val buat get() = p("Buat", "Create")
    val nanti get() = p("Nanti", "Later")
    val nantiSaja get() = p("Nanti saja", "Later")
    val aktifkan get() = p("Aktifkan", "Enable")
    val mengerti get() = p("Mengerti", "Got it")
    val konfirmasi get() = p("Konfirmasi", "Confirmation")

    // ---- beranda dan pencarian ----
    val cariPlaceholder get() = p("Cari lagu / artis di YouTube…", "Search songs or artists…")
    val saringPlaceholder get() = p("Saring lagu lokal…", "Filter local songs…")
    val online get() = p("Online", "Online")
    val lokal get() = p("Lokal", "Local")
    val rekomendasi get() = p("Rekomendasi untuk kamu", "Recommended for you")
    val tidakAdaHasil get() = p("Tidak ada hasil. Coba judul atau artis lain.", "No results. Try another title or artist.")
    fun tidakAdaHasilUntuk(q: String) = p("Tidak ada hasil untuk \"$q\".", "No results for \"$q\".")
    fun pencarianGagal(msg: String) = p("Pencarian gagal: $msg", "Search failed: $msg")
    val menghubungiServer get() = p("Menghubungi server pencarian…", "Contacting the search server…")
    val mencari get() = p("Mencari…", "Searching…")
    val tidakAdaLaguLokalCocok get() = p("Tidak ada lagu lokal yang sesuai dengan saringan.", "No local songs match the filter.")
    val tidakAdaBerkasMusik get() = p(
        "Tidak ada berkas musik yang ditemukan di perangkat. Ketuk Pilih folder di atas untuk memindai folder tertentu secara langsung.",
        "No music files were found on this device. Tap Choose folder above to scan a specific folder directly."
    )
    val semuaFolder get() = p("Semua folder di perangkat", "All folders on this device")
    val pilihFolder get() = p("Pilih folder", "Choose folder")
    val folderLain get() = p("Folder lain", "Other folder")
    val putarSemua get() = p("Putar semua", "Play all")
    val sedangDiputar get() = p("Sedang diputar", "Now playing")
    fun jumlahLagu(n: Int) = p("$n lagu", "$n songs")

    // ---- unduhan ----
    val unduh get() = p("Unduh", "Download")
    val unduhLagu get() = p("Unduh lagu", "Download song")
    val unduhan get() = p("Unduhan", "Downloads")
    val mengunduhLagu get() = p("Mengunduh lagu", "Downloading song")
    fun mengunduhPersen(pct: Int) = p("Mengunduh… $pct%", "Downloading… $pct%")
    fun mengunduhJudul(judul: String) = p("Mengunduh \"$judul\"…", "Downloading \"$judul\"…")
    val menyiapkan get() = p("Menyiapkan…", "Preparing…")
    val sembunyikan get() = p("Sembunyikan", "Hide")
    val unduhanSelesai get() = p("Unduhan selesai.", "Download finished.")
    val sudahDiunduh get() = p("Sudah diunduh", "Already downloaded")
    val unduhanDitahanWifi get() = p("Unduhan ditahan: setelan hanya Wi-Fi sedang aktif.", "Download held: the Wi-Fi only setting is active.")
    val unduhanBerkasLokal get() = p("Unduhan • berkas lokal", "Downloads • local file")

    // ---- playlist ----
    val playlist get() = p("Playlist", "Playlist")
    val playlistBaru get() = p("Playlist baru", "New playlist")
    val namaPlaylist get() = p("Nama playlist", "Playlist name")
    val tambahKePlaylist get() = p("Tambah ke playlist", "Add to playlist")

    // ---- donasi ----
    val donasi get() = p("Donasi", "Donate")
    val dukungQris get() = p("Dukung pengembangan Musik melalui QRIS.", "Support Musik development via QRIS.")
    val qrisDonasiDesc get() = p("QRIS donasi", "Donation QRIS")
    val qrisBelumDibuat get() = p(
        "QRIS belum dibuat.\nPilih nominal terlebih dahulu di bawah, lalu tekan Buat QRIS.",
        "The QRIS has not been created yet.\nChoose an amount below, then press Create QRIS."
    )
    val nominalOtomatis get() = p(
        "Nominal akan terisi otomatis saat kamu memindai dengan e-wallet atau m-banking.",
        "The amount is filled in automatically when you scan with an e-wallet or mobile banking."
    )
    val pilihNominal get() = p("Pilih nominal", "Choose amount")
    val nominalLain get() = p("Nominal lain", "Other amount")
    val buatQris get() = p("Buat QRIS", "Create QRIS")
    val buatUlangQris get() = p("Buat Ulang QRIS", "Recreate QRIS")
    val qrDibuatDiPerangkat get() = p(
        "QR dibuat langsung di perangkat kamu, tidak melalui server. Terima kasih atas dukungannya.",
        "The QR is generated directly on your device, not through a server. Thank you for your support."
    )
    val tidakAdaAplikasiTelegram get() = p("Tidak ada aplikasi untuk membuka tautan Telegram.", "No app available to open the Telegram link.")
    val tidakAdaAplikasiGithub get() = p("Tidak ada aplikasi untuk membuka tautan GitHub.", "No app available to open the GitHub link.")

    // ---- pengaturan ----
    val pengaturan get() = p("Pengaturan", "Settings")
    val tema get() = p("Tema", "Theme")
    val ikutSistem get() = p("Ikut sistem", "System default")
    val terang get() = p("Terang", "Light")
    val gelap get() = p("Gelap", "Dark")
    val pemutaran get() = p("Pemutaran", "Playback")
    val pengaturWaktuTidur get() = p("Pengatur waktu tidur", "Sleep timer")
    val mati get() = p("Mati", "Off")
    fun menit(m: Int) = p("$m menit", "$m min")
    val akhirLaguIni get() = p("Akhir lagu ini", "End of this song")
    val kualitasAudio get() = p("Kualitas audio", "Audio quality")
    val otomatis get() = p("Otomatis", "Automatic")
    val hematData get() = p("Hemat data", "Data saver")
    val tertinggi get() = p("Tertinggi", "Highest")
    fun jalurLokalTerakhir(s: String) = p("Jalur lokal terakhir: $s", "Last local attempt: $s")
    val belumAdaPercobaan get() = p("belum ada percobaan", "no attempt yet")
    val lanjutkanSesi get() = p("Lanjutkan sesi terakhir", "Resume last session")
    val lanjutkanSesiSub get() = p(
        "Lagu terakhir dimuat kembali dalam keadaan jeda saat aplikasi dibuka.",
        "The last song is loaded again, paused, when the app opens."
    )
    val kualitasUnduhan get() = p("Kualitas unduhan", "Download quality")
    val unduhHanyaWifi get() = p("Unduh hanya saat Wi-Fi", "Download only on Wi-Fi")
    val lirik get() = p("Lirik", "Lyrics")
    val sumberLirik get() = p("Sumber lirik", "Lyrics source")
    val otomatisLrclibTermai get() = p("Otomatis (LRCLIB, lalu termai)", "Automatic (LRCLIB, then termai)")
    val lrclibSaja get() = p("LRCLIB saja", "LRCLIB only")
    val termaiSaja get() = p("termai saja", "termai only")
    val alihAksaraOtomatis get() = p("Alih aksara otomatis", "Automatic transliteration")
    val alihAksaraSub get() = p(
        "Lagu beraksara asing langsung ditampilkan dalam romaji atau latin.",
        "Songs in foreign scripts are shown directly in romaji or Latin."
    )
    val ukuranTeksLirik get() = p("Ukuran teks lirik", "Lyric text size")
    val kecil get() = p("Kecil", "Small")
    val sedang get() = p("Sedang", "Medium")
    val besar get() = p("Besar", "Large")
    val sinkronDanData get() = p("Sinkron dan data", "Sync and data")
    val sinkronHanyaWifi get() = p("Sinkron hanya saat Wi-Fi", "Sync only on Wi-Fi")
    val sinkronWifiSub get() = p(
        "Pengiriman ke cloud ditahan saat koneksi menggunakan data seluler.",
        "Uploads to the cloud are held while the connection uses mobile data."
    )
    val eksporLibrary get() = p("Ekspor library", "Export library")
    val imporLibrary get() = p("Impor library", "Import library")
    val cacheGambar get() = p("Cache gambar sampul", "Cover image cache")
    val menghitungUkuran get() = p("Menghitung ukuran...", "Calculating size...")
    fun ukuranSekarang(s: String) = p("Ukuran sekarang $s", "Current size $s")
    val bersihkan get() = p("Bersihkan", "Clear")
    val umum get() = p("Umum", "General")
    val bahasaLabel get() = p("Bahasa", "Language")
    val bahasaIndonesia get() = p("Indonesia", "Indonesian")
    val bahasaInggris get() = p("Inggris", "English")
    val cekPembaruanOtomatis get() = p("Cek pembaruan otomatis", "Automatic update checks")
    val cekPembaruanSub get() = p(
        "Memeriksa versi baru setiap kali aplikasi dibuka.",
        "Check for a new version every time the app opens."
    )
    val cekPembaruan get() = p("Cek pembaruan", "Check for updates")
    val sedangMemeriksa get() = p("Memeriksa…", "Checking…")
    val sudahTerbaru get() = p("Kamu sudah memakai versi terbaru.", "You are already on the latest version.")
    val gagalMemeriksa get() = p("Pemeriksaan gagal. Periksa koneksi internet kamu.", "The check failed. Please check your internet connection.")
    val dukungan get() = p("Dukungan", "Support")
    val tentang get() = p("Tentang", "About")
    val dibuatOleh get() = p("Dibuat oleh CraXID Project", "Created by CraXID Project")

    // ---- akun ----
    val akun get() = p("Akun", "Account")
    val masukGooglePanjang get() = p(
        "Masuk dengan akun Google agar library kamu tersinkron ke cloud: Playlist, Disukai, Tidak Disukai, dan Riwayat. Datanya terhubung dengan versi web, jadi berganti perangkat atau memasang ulang tidak menghilangkan koleksimu.",
        "Sign in with a Google account so your library syncs to the cloud: Playlists, Liked, Disliked, and History. The data is shared with the web version, so switching devices or reinstalling will not erase your collection."
    )
    val sinkronisasiAktif get() = p("Sinkronisasi aktif.", "Sync is active.")
    val sinkronkan get() = p("Sinkronkan", "Sync now")
    val sinkron get() = p("Sinkron", "Sync")
    val pengguna get() = p("Pengguna", "User")
    val masukDenganGoogle get() = p("Masuk dengan Google", "Sign in with Google")
    val keluar get() = p("Keluar", "Sign out")

    // ---- pembaruan ----
    val pembaruanTersedia get() = p("Pembaruan tersedia", "Update available")
    fun musikVersiTersedia(v: String) = p("Musik v$v sudah tersedia.", "Musik v$v is available.")
    fun versiKamu(inst: String, tgl: String, uk: String) = p(
        "Versi kamu: v$inst\nTanggal rilis: $tgl\nUkuran: $uk",
        "Your version: v$inst\nRelease date: $tgl\nSize: $uk"
    )
    val pembaruanDiunduh get() = p(
        "Pembaruan sedang diunduh. Tunggu sampai selesai, installer akan muncul dengan sendirinya.",
        "The update is being downloaded. Wait until it finishes, the installer will appear on its own."
    )
    val unduhanSelesaiInstaller get() = p(
        "Unduhan selesai. Aktifkan izin pemasangan, lalu ketuk berkas Musik di folder Download.",
        "Download finished. Allow the install permission, then tap the Musik file in the Download folder."
    )
    fun baruDiMusik(v: String) = p("Baru di Musik v$v", "New in Musik v$v")

    // ---- notifikasi ----
    val notifikasiBelumAktif get() = p("Notifikasi belum aktif", "Notifications are not enabled")

    // ---- pemutar ----
    val suka get() = p("Suka", "Like")
    val tidakSuka get() = p("Tidak suka", "Dislike")
    val jeda get() = p("Jeda", "Pause")
    val putar get() = p("Putar", "Play")
    val sebelumnya get() = p("Sebelumnya", "Previous")
    val berikutnya get() = p("Berikutnya", "Next")
    val ulangi get() = p("Ulangi", "Repeat")
    val menu get() = p("Menu", "Menu")
    val mencariLirik get() = p("Mencari lirik…", "Looking up lyrics…")
    val memproses get() = p("Memproses…", "Processing…")
    val polos get() = p("Polos", "Plain")
    val pilihLirikBenar get() = p("Pilih lirik yang benar:", "Choose the correct lyrics:")
    fun terpasangSekarang(s: String) = p("Terpasang sekarang: $s", "Currently set: $s")
    val streamingYt get() = p("Streaming • YouTube", "Streaming • YouTube")

    // ---- edit berkas lokal ----
    val judulLagu get() = p("Judul lagu", "Song title")
    val artisBolehKosong get() = p("Artis, boleh dikosongkan", "Artist, may be left empty")
    val namaFile get() = p("Nama file", "File name")
    val simpanDi get() = p("Simpan di", "Save in")

    // ---- tambahan hasil sapuan kedua ----
    val gantiLirik get() = p("Ganti lirik", "Change lyrics")
    val cariLirik get() = p("Cari lirik", "Search lyrics")
    val lirikTidakDitemukan get() = p("Lirik tidak ditemukan.", "Lyrics not found.")
    fun lirikOleh(s: String) = p("Lirik oleh $s", "Lyrics by $s")
    val masihKosong get() = p("Masih kosong.", "Still empty.")
    val disukai get() = p("Disukai", "Liked")
    val lagu get() = p("Lagu", "Songs")
    val hapusSemua get() = p("Hapus semua", "Delete all")
    val hapusSemuaRiwayat get() = p("Hapus semua riwayat putar?", "Delete all play history?")
    val hapusPlaylist get() = p("Hapus playlist", "Delete playlist")
    fun hapusPlaylistNama(nama: String) = p("Hapus playlist \"$nama\"?", "Delete playlist \"$nama\"?")
    val hapusLirikIni get() = p("Hapus lirik ini", "Delete these lyrics")
    val hapusDanLanjut get() = p("Hapus dan lanjut", "Delete and continue")
    val jejakKerusakan get() = p(
        "Ini adalah jejak kerusakan dari terakhir kali aplikasi menutup sendiri. Salin atau tangkap layar isinya, lalu kirim ke pengembang.",
        "This is the crash trace from the last time the app closed by itself. Copy it or take a screenshot of it, then send it to the developer."
    )
    val bukaPengaturan get() = p("Buka Pengaturan", "Open Settings")
    val notifIsi get() = p(
        "Lagu yang sedang diputar tidak akan muncul di panel cepat, layar kunci, dan kapsul dinamis selama notifikasi Musik belum diaktifkan. Aktifkan dari pengaturan aplikasi, cukup sekali.",
        "The currently playing song will not appear in the quick panel, lock screen, and dynamic capsule while Musik notifications are not enabled. Enable them from the app settings, just once."
    )
    val izinkanAksesMusik get() = p("Izinkan akses musik", "Allow music access")
    val izinMusikBelum get() = p("Izin musik belum diberikan, tab Lokal masih kosong.", "Music permission has not been granted, the Local tab is still empty.")
    val folderKosongMusik get() = p("Folder ini tidak berisi berkas musik yang bisa dibaca.", "This folder contains no readable music files.")
    val dariIndeks get() = p("Dari indeks musik Android", "From the Android music index")
    val dipindaiLangsung get() = p("Dipindai langsung dari folder ini", "Scanned directly from this folder")
    val ekstensiInfo get() = p("Ekstensi mengikuti format audio aslinya (umumnya .m4a).", "The extension follows the original audio format (usually .m4a).")
    val eksporGagal get() = p("Ekspor gagal.", "Export failed.")
    val libraryDiekspor get() = p("Library diekspor.", "Library exported.")
    val libraryDipulihkan get() = p("Library dipulihkan dari berkas.", "Library restored from file.")
    val cacheDibersihkan get() = p("Cache gambar dibersihkan.", "Image cache cleared.")
    val izinkanInstall get() = p(
        "Izinkan pemasangan dari aplikasi ini terlebih dahulu, lalu ketuk Unduh lagi.",
        "Allow installs from this app first, then tap Download again."
    )
    val judulYtBerbeda get() = p(
        "Judul dari YouTube kadang berbeda dari judul resmi lagunya. Coba cari sendiri dengan judul dan artis yang benar, lalu pilih hasil yang cocok.",
        "YouTube titles sometimes differ from the official song title. Try searching yourself with the correct title and artist, then pick the matching result."
    )
    val hematBerkas get() = p("Hemat (berkas terkecil)", "Economy (smallest file)")
    val ikutKualitasPutar get() = p("Ikut kualitas putar", "Follow playback quality")
    fun jumlahHasil(n: Int) = p("$n hasil", "$n results")
    val sedangMemuat get() = p("Memuat…", "Loading…")
    val semuaHasil get() = p("Semua hasil sudah ditampilkan.", "All results are shown.")
    val folderMusik get() = p("Folder Music", "Music folder")
    val folderDownload get() = p("Folder Download", "Download folder")
    val ikutiPengaturan get() = p("Ikuti pengaturan", "Follow settings")
    val tanamCover get() = p("Tanam cover ke berkas", "Embed cover into the file")
    val tanamLirik get() = p("Tanam lirik ke berkas", "Embed lyrics into the file")
    val lirikSiapTanam get() = p("Lirik ditemukan dan siap ditanam.", "Lyrics found and ready to embed.")
    val lirikTakKetemuUnduh get() = p("Lirik tidak ditemukan otomatis.", "Lyrics were not found automatically.")
    val dariBerkas get() = p("Dari berkas", "From file")
    val lirikDariBerkasSiap get() = p("Lirik dari berkas siap ditanam.", "Lyrics from the file are ready to embed.")
    val lirikDariCariSiap get() = p("Lirik pilihan siap ditanam.", "The selected lyrics are ready to embed.")
    val berkasLirikTakTerbaca get() = p("Berkas lirik tidak bisa dibaca.", "The lyrics file could not be read.")
    val laporkanBugSaran get() = p("Laporkan Bug & Saran", "Report a Bug or Suggestion")
    val laporkanSub get() = p(
        "Bug, saran, atau pesan lain untuk developer",
        "Bugs, suggestions, or any other message for the developer"
    )
    val kategori get() = p("Kategori", "Category")
    val bugKata get() = p("Bug", "Bug")
    val saranKata get() = p("Saran", "Suggestion")
    val lainnyaKata get() = p("Lainnya", "Other")
    val pesanKamu get() = p("Pesan kamu", "Your message")
    val infoLampiranOtomatis get() = p(
        "Versi aplikasi dan model perangkat ikut terlampir otomatis.",
        "The app version and device model are attached automatically."
    )
    val kirim get() = p("Kirim", "Send")
    val laporanTerkirim get() = p("Laporan terkirim. Terima kasih!", "Report sent. Thank you!")
    val laporanGagal get() = p(
        "Laporan gagal terkirim. Periksa koneksi lalu coba lagi.",
        "The report failed to send. Check your connection and try again."
    )
    fun unduhanGagal(judul: String, msg: String) = p("Unduhan gagal ($judul): $msg", "Download failed ($judul): $msg")
    val berhasilMasuk get() = p("Berhasil masuk, library disinkronkan.", "Signed in, library synced.")
    fun ditambahkanKe(judul: String, nama: String) = p("\"$judul\" ditambahkan ke $nama", "\"$judul\" was added to $nama")
    val semuaServerMati get() = p("semua server pencarian mati", "all search servers are down")
    val klienIdBelum get() = p("Client ID belum dipasang di aplikasi.", "The Client ID has not been set in the app.")
    val kredensialBukanGoogle get() = p("Kredensial yang dipilih bukan akun Google.", "The chosen credential is not a Google account.")
    val tidakAdaAkunGoogle get() = p("Tidak ada akun Google yang dapat dipakai di perangkat ini.", "No Google account on this device can be used.")
    fun gagalPemilihAkun(msg: String) = p("Gagal membuka pemilih akun Google: $msg", "Failed to open the Google account picker: $msg")
    val menghubungkanSupabase get() = p("Menghubungkan ke Supabase…", "Connecting to Supabase…")
    val supabaseMenolakToken get() = p("Supabase menolak token Google. Periksa kembali Client ID di Supabase Auth.", "Supabase rejected the Google token. Double-check the Client ID in Supabase Auth.")
    val mengambilCloud get() = p("Mengambil data dari cloud…", "Fetching data from the cloud…")
    val gagalAmbilCloud get() = p("Gagal mengambil data dari cloud.", "Failed to fetch data from the cloud.")
    val cloudKosong get() = p("Cloud masih kosong, mengunggah library…", "The cloud is still empty, uploading the library…")
    val sinkronDitahanWifi get() = p("Sinkron ditahan: menunggu Wi-Fi", "Sync held: waiting for Wi-Fi")
    val gagalSinkron get() = p("Gagal menyinkronkan, dicoba lagi nanti.", "Sync failed, it will be retried later.")
    val tersinkronPrefix get() = p("Tersinkron ", "Synced ")
    val cobaLagi get() = p("coba lagi", "try again")
    fun statusBerhasil(x: String) = p("berhasil ($x)", "succeeded ($x)")
    fun statusGagal(x: String) = p("gagal ($x), server yang melayani", "failed ($x), the server served instead")
    val batasWaktu30 get() = p("lewat batas 30 detik", "over the 30 second limit")
    val izinDitolakPilihLain get() = p(
        "Izin penyimpanan ditolak. Pilih \"Folder lain\" pada jendela unduh agar tetap dapat mengunduh.",
        "Storage permission denied. Choose \"Other folder\" in the download window so you can keep downloading."
    )

    // ---- offline ----
    val offlineJudul get() = p("Tidak ada koneksi internet", "No internet connection")
    val offlineIsi get() = p(
        "Perangkat kamu sedang offline. Tab Online tidak dapat digunakan sampai koneksi kembali. Lagu lokal tetap dapat diputar.",
        "Your device is offline. The Online tab cannot be used until the connection returns. Local songs can still be played."
    )

    // ---- pelengkapan terjemahan (v1.1.2) ----
    val eksporTidakValid get() = p("Berkas itu bukan ekspor library yang valid.", "That file is not a valid library export.")
    val adaKerusakan get() = p("Ada kerusakan tercatat", "A crash was recorded")
    val izinMusikInfo get() = p(
        "Untuk menampilkan lagu yang ada di HP kamu, aplikasi memerlukan izin membaca berkas musik.",
        "To show the songs on your phone, the app needs permission to read music files."
    )
    fun jumlahLaguFolder(n: Int) = p("$n lagu di folder ini", "$n songs in this folder")
    fun jumlahLaguPerangkat(n: Int) = p("$n lagu di perangkat", "$n songs on this device")
    val baruDiputar get() = p("Baru diputar", "Recently played")
    fun durasiMenit(n: Int) = p("$n menit", "$n minutes")
    val belumAdaPlaylist get() = p("Belum ada playlist.", "No playlists yet.")
    val asli get() = p("Asli", "Original")
    val gagalMemutar get() = p(
        "Gagal memutar lagu. Ketuk tombol putar untuk mencoba lagi.",
        "Could not play the song. Tap the play button to try again."
    )
    fun gagalMemutarSebab(x: String) = p("Gagal memutar lagu: $x", "Could not play the song: $x")
    val sebabPenyimpananTidakSiap get() = p("penyimpanan tidak siap", "storage is not ready")
    val sebabBerkasKosong get() = p("berkas kosong", "empty file")
    val sebabPotonganTidakUtuh get() = p("potongan unduhan tidak utuh", "downloaded pieces are incomplete")
    val sebabUkuranTidakCocok get() = p("ukuran berkas tidak cocok", "file size does not match")
    val sebabFolderTidakBisaDitulis get() = p("folder tujuan tidak bisa ditulis", "the destination folder is not writable")
    fun sebabFolderNamaTidakBisaDitulis(nama: String) = p("folder $nama tidak bisa ditulis", "the $nama folder is not writable")
    val artisTidakDikenal get() = p("Artis tidak dikenal", "Unknown artist")
    val folderPilihan get() = p("Folder pilihan", "Chosen folder")
    val keluaranLain get() = p("Keluaran lain", "Other output")
    val speakerHp get() = p("Speaker HP", "Phone speaker")
    val kabel35 get() = p("Kabel 3,5mm", "3.5mm cable")
    val maksLabel get() = p("maks", "max")
    val responsAudioTidakDikenal get() = p("respons audio tidak dikenal", "unrecognized audio response")
    val tidakAdaFormatAudio get() = p("tidak ada format audio", "no audio format available")
    val tidakAdaFormatAudioLokal get() = p("tidak ada format audio lokal", "no local audio format available")
    val lrclibTidakMerespons get() = p("LRCLIB tidak merespons", "LRCLIB is not responding")
}
