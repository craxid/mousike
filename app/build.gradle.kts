import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "id.my.hxa.musik"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "id.my.hxa.musik"
        minSdk = 24
        targetSdk = 35
        // kode versi melanjutkan lini native (beta berakhir di 40)
        // dan WAJIB di atas 16: aplikasi web lama (paket sama)
        // berakhir di kode 16, kode yang lebih kecil ditolak
        // Android sebagai penurunan versi saat memasang
        versionCode = 64
        versionName = "1.2.0"
    }

    signingConfigs {
        create("beta") {
            val props = Properties()
            val f = rootProject.file("keystore.properties")
            if (f.exists()) {
                f.inputStream().use { props.load(it) }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
        // rilis final: keystore crax (keystore-final.properties,
        // kata sandinya dari berkas kata sandi di folder signing)
        create("final") {
            val props = Properties()
            val f = rootProject.file("keystore-final.properties")
            if (f.exists()) {
                f.inputStream().use { props.load(it) }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ""
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("final")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        // desugaring: extractor lokal memakai fungsi Java modern
        // (List.of dsb) yang di Android 10 ke bawah belum ada; tanpa
        // ini ekstraksinya mati diam-diam di perangkat lama
        // (terbukti di emulator Android 9, v1.0.13)
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("dev.chrisbanes.haze:haze:1.2.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    // login Google tahap 5: Credential Manager + Google ID
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // gambar QR buat donasi QRIS (digambar lokal di HP)
    implementation("com.google.zxing:core:3.5.3")
    // uji coba putar langsung dari HP: NewPipeExtractor tidak ada di
    // Maven Central (cuma JitPack), jadi dia + temannya disimpan
    // sebagai jar lokal di app/libs
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // FCM: notifikasi pengumuman dari owner (topik "semua")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-messaging")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
