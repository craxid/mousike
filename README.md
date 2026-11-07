# Mousike

![Ikon Mousike](icon.png)

Mousike adalah aplikasi pemutar musik Android yang ditulis dengan Kotlin, Jetpack Compose, dan Media3. Nama Mousike diambil dari kata Yunani kuno *mousikē*, seni para Muses, yang menjadi asal kata "musik" di banyak bahasa.

## Fitur

- Pencarian lagu yang mengutamakan lagu resmi, dengan hasil tanpa batas yang dimuat bertahap saat menggulir
- Pemutaran streaming lewat instans Piped, dengan ekstraksi lokal di perangkat memakai NewPipeExtractor sebagai jalur utama
- Perpustakaan lagu lokal di perangkat
- Lirik dari LRCLIB, termasuk romaji untuk lagu Jepang
- Unduh lagu dengan pilihan kualitas, lengkap dengan opsi menanam sampul dan lirik ke berkas audio
- Masuk dengan Google dan sinkronisasi data lewat Supabase
- Laporan bug dan saran dari dalam aplikasi
- Notifikasi pengumuman lewat Firebase Cloud Messaging
- Pemeriksaan pembaruan di dalam aplikasi
- Dua bahasa antarmuka: Indonesia dan Inggris

## Membangun dari sumber

Kebutuhan: JDK 17 dan Gradle 8.9 (atau Android Studio versi terbaru). Proyek ini tidak menyertakan Gradle Wrapper; hasilkan dengan perintah `gradle wrapper --gradle-version 8.9` atau buka langsung di Android Studio.

Sebelum membangun, siapkan tiga hal berikut yang memang tidak disertakan di repo ini:

1. Berkas `app/google-services.json` dari proyek Firebase milikmu sendiri. Nama paket aplikasinya adalah `id.my.hxa.musik`.
2. Nilai layanan di `app/src/main/java/id/my/hxa/musik/data/Kunci.kt`. Berkas itu berisi contoh untuk alamat backend resa dan proyek Supabase; ganti dengan milikmu.
3. Untuk build release, salin `keystore-final.properties.example` menjadi `keystore-final.properties` dan isi dengan keystore milikmu. Build debug berjalan tanpa berkas ini.

Saluran pembaruan di dalam aplikasi membaca berkas JSON dari Google Drive. Tautan di `UpdateChecker.kt` menunjuk ke saluran pembaruan resmi Mousike; ganti dengan berkas milikmu bila kamu menerbitkan varian sendiri.

## Lisensi

Mousike dirilis dengan lisensi GNU General Public License v3.0. Lihat berkas [LICENSE](LICENSE).

Lisensi ini mengikuti pustaka NewPipeExtractor yang dipakai untuk ekstraksi di perangkat dan disertakan sebagai berkas JAR di `app/libs/`, bersama dependensinya (jsoup, nanojson, Rhino, dan protobuf-javalite) yang masing-masing tunduk pada lisensinya sendiri.

## Kredit

- [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) untuk ekstraksi
- [Piped](https://github.com/TeamPiped/Piped) untuk pencarian dan streaming
- [LRCLIB](https://lrclib.net) untuk lirik
