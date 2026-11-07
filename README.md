# Mousike

![Ikon Mousike](icon.png)

Mousike adalah aplikasi pemutar musik Android yang ditulis dengan Kotlin, Jetpack Compose, dan Media3. Nama Mousike diambil dari kata Yunani kuno *mousikē*, seni para Muses, yang menjadi asal kata "musik" di banyak bahasa.

Edisi sumber terbuka ini berdiri sendiri: tidak ada akun, tidak ada server backend khusus, dan tidak ada kunci layanan yang perlu diisi. Semua resolve audio dikerjakan langsung di perangkat.

## Fitur

- Pencarian lagu yang mengutamakan lagu resmi, dengan hasil tanpa batas yang dimuat bertahap saat menggulir
- Pemutaran streaming dengan ekstraksi lokal di perangkat memakai NewPipeExtractor; pencarian lewat instans Piped publik
- Perpustakaan lagu lokal di perangkat
- Lirik dari LRCLIB, dengan pencarian kandidat lirik secara manual
- Unduh lagu dengan pilihan kualitas, lengkap dengan opsi menanam sampul dan lirik ke berkas audio
- Ekspor dan impor library dalam berkas JSON
- Dua bahasa antarmuka: Indonesia dan Inggris

## Membangun dari sumber

Kebutuhan: JDK 17 dan Gradle 8.9 (atau Android Studio versi terbaru). Proyek ini tidak menyertakan Gradle Wrapper; hasilkan dengan perintah `gradle wrapper --gradle-version 8.9` atau buka langsung di Android Studio.

Tidak ada berkas konfigurasi layanan yang perlu disiapkan. Untuk build debug, langsung jalankan `assembleDebug`. Untuk build release, salin `keystore-final.properties.example` menjadi `keystore-final.properties` dan isi dengan keystore milikmu.

## Lisensi

Mousike dirilis dengan lisensi GNU General Public License v3.0. Lihat berkas [LICENSE](LICENSE).

Lisensi ini mengikuti pustaka NewPipeExtractor yang dipakai untuk ekstraksi di perangkat dan disertakan sebagai berkas JAR di `app/libs/`, bersama dependensinya (jsoup, nanojson, Rhino, dan protobuf-javalite) yang masing-masing tunduk pada lisensinya sendiri.

## Kredit

- [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) untuk ekstraksi
- [Piped](https://github.com/TeamPiped/Piped) untuk pencarian
- [LRCLIB](https://lrclib.net) untuk lirik
