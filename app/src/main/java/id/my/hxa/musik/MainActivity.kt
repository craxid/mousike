package id.my.hxa.musik

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.graphics.Bitmap
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.WindowManager
import android.widget.Toast
import android.os.Build
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import id.my.hxa.musik.data.DlDestination
import id.my.hxa.musik.data.OpsiUnduh
import id.my.hxa.musik.data.DownloadStore
import id.my.hxa.musik.data.AudioOutput
import id.my.hxa.musik.data.LibraryStore
import id.my.hxa.musik.data.LocalLibrary
import id.my.hxa.musik.data.LocalTrack
import id.my.hxa.musik.data.LirikKandidat
import id.my.hxa.musik.data.Lyrics
import id.my.hxa.musik.data.MusicApi
import id.my.hxa.musik.data.Teks
import id.my.hxa.musik.data.Playlist
import id.my.hxa.musik.data.SettingsStore
import id.my.hxa.musik.data.Track
import id.my.hxa.musik.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// palet ngikutin versi web biar tampilannya kerasa sama
private val AccentLight = Color(0xFF2D6A4F)
private val AccentDark = Color(0xFF52B788)
private val BgLight = Color(0xFFF2F7F2)
private val BgDark = Color(0xFF000000)

// font ditanam di aplikasi biar tampilan konsisten di semua HP
// (diagnosis awal "font sistem gagal gambar teks tebal" terbukti salah:
// penyebab judul hilang adalah warna teks bawaan yang hitam, lihat MusikApp)
private val RobotoFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

private fun robotoTypography(): Typography {
    val t = Typography()
    fun TextStyle.r() = copy(fontFamily = RobotoFamily)
    return Typography(
        displayLarge = t.displayLarge.r(), displayMedium = t.displayMedium.r(), displaySmall = t.displaySmall.r(),
        headlineLarge = t.headlineLarge.r(), headlineMedium = t.headlineMedium.r(), headlineSmall = t.headlineSmall.r(),
        titleLarge = t.titleLarge.r(), titleMedium = t.titleMedium.r(), titleSmall = t.titleSmall.r(),
        bodyLarge = t.bodyLarge.r(), bodyMedium = t.bodyMedium.r(), bodySmall = t.bodySmall.r(),
        labelLarge = t.labelLarge.r(), labelMedium = t.labelMedium.r(), labelSmall = t.labelSmall.r()
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LibraryStore.init(applicationContext)
        SettingsStore.init(applicationContext)
        AudioOutput.init(applicationContext)
        DownloadStore.init(applicationContext)
        ThemeStore.init(applicationContext)
        // izin notifikasi (Android 13+): diminta sekali saja biar
        // notifikasi kontrol pemutaran bisa muncul; status dimintanya
        // disimpan supaya nggak ditanya berulang-ulang tiap buka
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            val prefsNotif = getSharedPreferences("musikbeta", MODE_PRIVATE)
            if (!prefsNotif.getBoolean("izin_notif_diminta", false)) {
                prefsNotif.edit().putBoolean("izin_notif_diminta", true).apply()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9911)
            }
        }
        // kalau sambungan pemutarnya yang bermasalah, aplikasi tetap dibuka
        // dan kesalahannya dicatat, bukan langsung force close tanpa jejak
        try {
            PlayerManager.init(applicationContext)
        } catch (e: Throwable) {
            CrashLog.saveSync(this, e)
        }
        enableEdgeToEdge()
        setContent { MusikApp() }
    }
}

@Composable
fun MusikApp() {
    val themeMode by ThemeStore.mode.collectAsState()
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val scheme = if (dark) {
        darkColorScheme(primary = AccentDark, onPrimary = Color.Black, secondary = Color(0xFF74C69D), tertiary = Color(0xFF95D5B2), background = BgDark, surface = BgDark)
    } else {
        lightColorScheme(primary = AccentLight, onPrimary = Color.White, secondary = Color(0xFF40916C), tertiary = Color(0xFF74C69D), background = BgLight, surface = BgLight)
    }
    // ikon status bar + bar navigasi ngikutin tema aplikasi,
    // bukan mode malam sistem HP: pas aplikasi terang ikonnya
    // ikut digelapkan biar tetap kelihatan di atas latar putih
    val viewSistem = LocalView.current
    val konteksSistem = LocalContext.current
    SideEffect {
        val aktivitas = konteksSistem as? android.app.Activity ?: return@SideEffect
        val kontroler = WindowCompat.getInsetsController(aktivitas.window, viewSistem)
        kontroler.isAppearanceLightStatusBars = !dark
        kontroler.isAppearanceLightNavigationBars = !dark
    }
    MaterialTheme(colorScheme = scheme, typography = robotoTypography()) {
        // Dari uji diagnostik v0.1.2 di HP user: teks TANPA warna eksplisit
        // tergambar hitam (LocalContentColor bawaannya hitam), jadi di mode
        // gelap judul hilang total. Warna konten dipasang eksplisit di sini.
        CompositionLocalProvider(LocalContentColor provides scheme.onSurface) {
            HomeScreen()
        }
    }
}

fun fmtDur(totalSec: Int): String {
    if (totalSec <= 0) return "0:00"
    val m = totalSec / 60
    val s = totalSec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

private fun namaVersiTerpasang(context: Context): String =
    try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" } catch (e: Exception) { "" }

private fun kodeVersiTerpasang(context: Context): Int {
    val pi = context.packageManager.getPackageInfo(context.packageName, 0)
    return if (android.os.Build.VERSION.SDK_INT >= 28) {
        pi.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        pi.versionCode
    }
}

// format angka ikut bahasa aplikasi, bukan bahasa HP:
// Inggris pakai titik (44.1), Indonesia pakai koma (44,1)
private fun localeAngka(): java.util.Locale =
    if (Teks.inggris) java.util.Locale.US else java.util.Locale("id", "ID")

// latar kotak-kotak: garis 1px tiap 42dp,
// putih samar (5,5%) di tema gelap, slate samar di tema terang.
// Digambar di belakang isi, di atas warna latar polosnya.
@Composable
fun Modifier.latarKotak(): Modifier {
    val gelap = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val warna = if (gelap) {
        Color.White.copy(alpha = 0.055f)
    } else {
        Color(0xFF111827).copy(alpha = 0.055f)
    }
    return this.drawBehind {
        val langkah = 42.dp.toPx()
        var x = langkah
        while (x < size.width) {
            drawLine(warna, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += langkah
        }
        var y = langkah
        while (y < size.height) {
            drawLine(warna, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += langkah
        }
    }
}

// ----- potongan kecil buat dialog Pengaturan -----

@Composable
private fun JudulBagianSetelan(teks: String) {
    Text(
        teks,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp)
    )
}

@Composable
private fun LabelSetelan(teks: String) {
    Text(
        teks,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

// satu baris per setelan pilihan: label di kiri, tombol
// dropdown nilai terpilih di kanan (gaya ringkas v1.0.7,
// menggantikan deretan radio yang bikin dialog kepanjangan)
@Composable
private fun DropdownSetelan(label: String, pilihan: List<Pair<Int, String>>, terpilih: Int, ikon: Map<Int, Int> = emptyMap(), onPilih: (Int) -> Unit) {
    var terbuka by remember { mutableStateOf(false) }
    val labelTerpilih = pilihan.firstOrNull { it.first == terpilih }?.second ?: ""
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Text(
            label,
            fontSize = 14.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Box {
            OutlinedButton(
                onClick = { terbuka = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // ikon pilihan terpilih (mis. bendera bahasa)
                // tampil di kiri teks tombolnya
                val ikonTerpilih = ikon[terpilih]
                if (ikonTerpilih != null) {
                    Image(
                        painter = painterResource(ikonTerpilih),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp, 16.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                // teks tombol dipendekkan: keterangan dlm kurung
                // cuma tampil lengkap di dalam menu pilihannya
                Text(
                    labelTerpilih.substringBefore(" ("),
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = terbuka, onDismissRequest = { terbuka = false }) {
                pilihan.forEach { (nilai, teks) ->
                    DropdownMenuItem(
                        text = { Text(teks, fontSize = 14.sp) },
                        leadingIcon = ikon[nilai]?.let { res ->
                            {
                                Image(
                                    painter = painterResource(res),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp, 16.dp).clip(RoundedCornerShape(3.dp))
                                )
                            }
                        },
                        onClick = { onPilih(nilai); terbuka = false },
                        trailingIcon = if (nilai == terpilih) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SakelarSetelan(label: String, keterangan: String?, nyala: Boolean, onUbah: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onUbah(!nyala) }
            .padding(vertical = 5.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
            if (keterangan != null) {
                Text(keterangan, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = nyala, onCheckedChange = onUbah)
    }
}


// Bungkus dialog bersama buat semua dialog aplikasi.
// AlertDialog bawaan Material jarak internalnya longgar
// (padding 24dp ditumpuk jarak judul, isi, dan tombol),
// bikin bagian bawah dialog kayak kebanyakan ruang kosong
// pas isinya pendek. Di sini jaraknya dirapatkan, dan tinggi
// area isi dibatasi biar dialog yang isinya panjang (catatan
// rilis, donasi) menggulir, bukan meregang setinggi layar.
@Composable
fun DialogMusik(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    title()
                }
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .heightIn(max = 520.dp)
                ) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        text()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(Modifier.width(8.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}

// unduhan yang tertahan izin tulis (Android 9 ke bawah),
// dilanjutkan persis dengan pilihan yang sama sesudah izin diputus
private data class UnduhanTertahan(
    val track: Track,
    val nama: String,
    val dest: DlDestination,
    val opsi: OpsiUnduh
)

// Baris pilihan folder di dialog unduh. RadioButton bawaan
// tingginya 48dp jadi barisnya kegedean; di sini lingkarannya
// digambar pakai ikon 22dp dan tinggi barisnya cukup 42dp.
@Composable
private fun BarisPilihanFolder(
    terpilih: Boolean,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit,
    aksiKanan: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            if (terpilih) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (terpilih) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            if (sublabel != null) {
                Text(sublabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (aksiKanan != null) aksiKanan()
    }
}

// Catatan rilis versi yang sedang dibangun. Tampil sebagai
// popup SEKALI tiap ganti versi (pengganti spanduk menetap
// di bawah beranda), jadi isinya diganti tiap rilis.
private const val CATATAN_RILIS_INI =
    "Pencarian sekarang mengutamakan lagu resmi dan hasilnya tanpa batas, dimuat bertahap saat kamu menggulir."

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Track>?>(null) }
    var sesiCari by remember { mutableStateOf<MusicApi.SesiCari?>(null) }
    var memuatLagi by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var pickerTrack by remember { mutableStateOf<Track?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var pendingAdd by remember { mutableStateOf<Track?>(null) }
    var confirmAction by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var openPlaylistId by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    // mode beranda: online atau lokal (berkas musik di perangkat)
    var modeLokal by remember { mutableStateOf(false) }

    // status koneksi langsung. Dulu aplikasi tidak punya ini
    // sama sekali, jadi pas perangkat offline ada jalur jaringan
    // yang melempar eror tanpa penangkap dan aplikasi menutup
    // sendiri. Sekarang offline terdeteksi: ada popup dan tab
    // Online dikunci sampai koneksi kembali.
    var online by remember { mutableStateOf(id.my.hxa.musik.data.NetUtil.sedangOnline(context)) }
    var tampilOffline by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val cb = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                online = true
            }
            override fun onLost(network: android.net.Network) {
                online = id.my.hxa.musik.data.NetUtil.sedangOnline(context)
            }
            override fun onCapabilitiesChanged(network: android.net.Network, caps: android.net.NetworkCapabilities) {
                online = caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        try { cm?.registerDefaultNetworkCallback(cb) } catch (e: Exception) { }
        onDispose { try { cm?.unregisterNetworkCallback(cb) } catch (e: Exception) { } }
    }
    LaunchedEffect(online) {
        if (!online) {
            tampilOffline = true
            if (!modeLokal) modeLokal = true
        } else {
            tampilOffline = false
        }
    }
    val izinAudio = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val izinMusikLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { dikasih ->
        if (dikasih) {
            scope.launch { LocalLibrary.muat(context) }
        } else {
            Toast.makeText(context, Teks.izinMusikBelum, Toast.LENGTH_LONG).show()
        }
    }
    // pemilih folder buat tab Lokal: izin bacanya disimpan persisten
    // biar foldernya tetap bisa dipindai sesudah aplikasi ditutup
    val folderLokalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // izin persisten gagal: pindai tetap dicoba pakai
                // izin sementara dari pemilih berkas
            }
            scope.launch { LocalLibrary.pakaiFolder(context, uri) }
        }
    }
    var downloadTarget by remember { mutableStateOf<Track?>(null) }
    var dlName by remember { mutableStateOf("") }
    var dlChoice by remember { mutableStateOf<DlDestination>(DlDestination.Downloads) }
    var pendingDl by remember { mutableStateOf<UnduhanTertahan?>(null) }
    var dlKualitas by remember { mutableIntStateOf(DownloadStore.kualitasUnduhTerakhir()) }
    var dlTanamCover by remember { mutableStateOf(DownloadStore.tanamCoverTerakhir()) }
    var dlTanamLirik by remember { mutableStateOf(DownloadStore.tanamLirikTerakhir()) }
    var dlLirikTeks by remember { mutableStateOf<String?>(null) }
    // status lirik tanaman: 0 belum dicari, 1 lagi mencari, 2 ketemu
    // otomatis, 3 tidak ketemu, 4 dari berkas, 5 dari pencarian
    var dlLirikStatus by remember { mutableIntStateOf(0) }
    var dlCariLirik by remember { mutableStateOf(false) }
    var recommendations by remember { mutableStateOf<List<Track>>(emptyList()) }
    val installedName = remember { namaVersiTerpasang(context) }
    // state buat efek blur mini player ngambang: kolom utama ini
    // sumbernya, kartu mini player yang nge-blur tampilan di belakangnya
    val hazeState = remember { HazeState() }

    val currentTrack by PlayerManager.currentTrack.collectAsState()
    val currentIndex by PlayerManager.currentIndex.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val isResolving by PlayerManager.isResolving.collectAsState()
    val isBuffering by PlayerManager.isBuffering.collectAsState()
    val playError by PlayerManager.errorMessage.collectAsState()
    val queue by PlayerManager.queue.collectAsState()
    val historyList by LibraryStore.history.collectAsState()
    val likesList by LibraryStore.likes.collectAsState()
    val dislikedList by LibraryStore.disliked.collectAsState()
    val playlistsList by LibraryStore.playlists.collectAsState()
    val dlList by DownloadStore.downloads.collectAsState()
    val dlError by DownloadStore.lastError.collectAsState()
    val dlDestSaved by DownloadStore.destination.collectAsState()
    val dlProgress by DownloadStore.progress.collectAsState()
    val dlJudul by DownloadStore.judulAktif.collectAsState()
    // popup progres unduhan: muncul sendiri pas ada unduhan BARU yang
    // jalan; kalau user menyembunyikannya, unduhan yang sama nggak
    // memunculkannya lagi (unduhan berikutnya tetap memunculkan)
    var popupUnduh by remember { mutableStateOf(false) }
    var idUnduhDikenal by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(dlProgress) {
        val baru = dlProgress.keys - idUnduhDikenal
        if (baru.isNotEmpty()) popupUnduh = true
        idUnduhDikenal = dlProgress.keys
        if (dlProgress.isEmpty() && popupUnduh) {
            if (dlError == null) statusMsg = Teks.unduhanSelesai
            popupUnduh = false
        }
    }
    val themeMode by ThemeStore.mode.collectAsState()
    val darkNow = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    // izin notifikasi buat kontrol pemutar di panel notifikasi (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    // ekspor/impor library buat Pengaturan: user milih sendiri
    // berkasnya lewat pemilih berkas Android
    val setScope = rememberCoroutineScope()
    val eksporLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            setScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(LibraryStore.exportJson().toByteArray(Charsets.UTF_8))
                        }
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                Toast.makeText(context, if (ok) Teks.libraryDiekspor else Teks.eksporGagal, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val imporLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            setScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val teks = context.contentResolver.openInputStream(uri)
                            ?.bufferedReader()?.use { it.readText() }
                        teks != null && LibraryStore.importJson(teks)
                    } catch (e: Exception) {
                        false
                    }
                }
                Toast.makeText(
                    context,
                    if (ok) Teks.libraryDipulihkan else Teks.eksporTidakValid,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ----- catatan rilis: popup sekali lihat tiap versi -----
    val kodeTerpasang = remember { kodeVersiTerpasang(context) }
    val prefsUmum = remember { context.getSharedPreferences("musikbeta", Context.MODE_PRIVATE) }
    var tampilCatatan by remember { mutableStateOf(false) }
    var tampilIzinNotif by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (prefsUmum.getInt("catatan_versi_terlihat", 0) < kodeTerpasang) {
            tampilCatatan = true
        }
    }
    if (tampilCatatan) {
        DialogMusik(
            onDismissRequest = {
                tampilCatatan = false
                prefsUmum.edit().putInt("catatan_versi_terlihat", kodeTerpasang).apply()
            },
            title = { Text(Teks.baruDiMusik(installedName)) },
            text = { Text(CATATAN_RILIS_INI, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = {
                    tampilCatatan = false
                    prefsUmum.edit().putInt("catatan_versi_terlihat", kodeTerpasang).apply()
                }) { Text(Teks.oke) }
            }
        )
    }

    // ----- panduan izin notifikasi buat kontrol media -----
    // panel cepat dan kapsul dinamis membaca notifikasi media;
    // tanpa izin ini, lagu yang diputar tidak muncul di sana.
    // Ingatkan sekali saja, pas user sedang memutar lagu.
    LaunchedEffect(isPlaying) {
        if (isPlaying && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            prefsUmum.getInt("catatan_versi_terlihat", 0) >= kodeTerpasang &&
            !prefsUmum.getBoolean("ingat_izin_notif", false)
        ) {
            tampilIzinNotif = true
        }
    }
    if (tampilIzinNotif) {
        DialogMusik(
            onDismissRequest = {
                tampilIzinNotif = false
                prefsUmum.edit().putBoolean("ingat_izin_notif", true).apply()
            },
            title = { Text(Teks.notifikasiBelumAktif) },
            text = {
                Text(
                    Teks.notifIsi,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    prefsUmum.edit().putBoolean("ingat_izin_notif", true).apply()
                    tampilIzinNotif = false
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    } catch (e: Exception) {
                        // pengaturan nggak kebuka juga nggak apa-apa
                    }
                }) { Text(Teks.bukaPengaturan) }
            },
            dismissButton = {
                TextButton(onClick = {
                    tampilIzinNotif = false
                    prefsUmum.edit().putBoolean("ingat_izin_notif", true).apply()
                }) { Text(Teks.nantiSaja) }
            }
        )
    }

    // setelan lanjutkan sesi: muat lagu terakhir dalam keadaan jeda
    LaunchedEffect(Unit) {
        if (SettingsStore.lanjutSesi.value) {
            PlayerManager.pulihkanSesi()
        }
    }

    // rekomendasi beranda: disusun ulang tiap lagu teratas di riwayat
    // atau jumlah Disukai berubah
    LaunchedEffect(historyList.firstOrNull()?.id, likesList.size, dislikedList.size) {
        // ditangkap di sini: pas offline semua server pencarian
        // gagal dan pemanggilan ini melempar eror; tanpa tangkapan
        // ini aplikasi menutup sendiri (laporan user 7 Nov)
        recommendations = try {
            MusicApi.fetchRecommendations(historyList, likesList, dislikedList)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // pemilih folder sistem buat tujuan unduhan "Folder lain"
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // izin persisten gagal: folder tetap dipakai buat unduhan ini
            }
            val custom = DlDestination.Custom(uri.toString(), DownloadStore.treeLabel(uri))
            dlChoice = custom
            DownloadStore.setDestination(custom)
        }
    }

    // pemilih berkas lirik (.lrc/.txt) buat ditanam pas lirik
    // otomatis tidak ketemu
    val berkasLirikLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val teks = try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }?.trim()
            } catch (e: Exception) {
                null
            }
            if (!teks.isNullOrEmpty()) {
                dlLirikTeks = teks.take(20_000)
                dlLirikStatus = 4
            } else {
                Toast.makeText(context, Teks.berkasLirikTakTerbaca, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // lirik tanaman dicari otomatis tiap dialog unduh dibuka dengan
    // centang liriknya menyala; hasil dan statusnya tampil di dialog
    LaunchedEffect(downloadTarget?.id, dlTanamLirik) {
        val dt = downloadTarget ?: return@LaunchedEffect
        if (!dlTanamLirik) return@LaunchedEffect
        dlLirikStatus = 1
        dlLirikTeks = null
        val lirik = try {
            MusicApi.fetchLyrics(dt)
        } catch (e: Exception) {
            null
        }
        if (downloadTarget?.id != dt.id) return@LaunchedEffect
        val teks = lirik?.let { lirikKeTeks(it) }
        if (teks != null) {
            dlLirikTeks = teks
            dlLirikStatus = 2
        } else {
            dlLirikStatus = 3
        }
    }

    // Android 9 ke bawah nulis ke folder Download/Music publik butuh izin ini;
    // unduhan yang tertahan di sini dilanjutkan begitu izinnya diputuskan
    val storagePermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val p = pendingDl
        pendingDl = null
        if (p != null) {
            if (granted) {
                DownloadStore.download(p.track, p.nama, p.dest, p.opsi)
            } else {
                statusMsg = Teks.izinDitolakPilihLain
            }
        }
    }

    fun startDownload(track: Track, name: String, dest: DlDestination, opsi: OpsiUnduh) {
        DownloadStore.setDestination(dest)
        if (Build.VERSION.SDK_INT < 29 && dest !is DlDestination.Custom &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDl = UnduhanTertahan(track, name, dest, opsi)
            storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            DownloadStore.download(track, name, dest, opsi)
        }
    }

    // catatan kerusakan dari sesi sebelumnya ditampilkan paling depan
    var crashNote by remember { mutableStateOf(CrashLog.read(context)) }
    val note = crashNote
    if (note != null) {
        val clipboard = LocalClipboardManager.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Text(Teks.adaKerusakan, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = RobotoFamily, color = MaterialTheme.colorScheme.onSurface)
            Text(
                Teks.jejakKerusakan,
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { clipboard.setText(AnnotatedString(note)) }) { Text(Teks.salin) }
                Button(onClick = { CrashLog.clear(context); crashNote = null }) { Text(Teks.hapusDanLanjut) }
            }
        }
        return
    }

    fun doSearch() {
        // di mode lokal kolom pencarian cuma menyaring daftar
        // perangkat, tidak jalan ke server
        if (modeLokal) return
        val q = query.trim()
        if (q.isEmpty() || searching) return
        searching = true
        statusMsg = Teks.menghubungiServer
        scope.launch {
            try {
                val (found, sesi) = MusicApi.cariPertama(q)
                sesiCari = sesi
                results = found
                statusMsg = if (found.isEmpty()) Teks.tidakAdaHasilUntuk(q) else Teks.jumlahHasil(found.size)
            } catch (e: Exception) {
                statusMsg = Teks.pencarianGagal(e.message ?: "")
            } finally {
                searching = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .latarKotak()
            .hazeSource(hazeState)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
    ) {
        // header: judul + pill tombol cepat, sama kyk web
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Musik", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, fontFamily = RobotoFamily, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    if (darkNow) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = Teks.tema,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { ThemeStore.setMode(if (darkNow) 1 else 2) }
                        .padding(6.dp)
                        .size(20.dp)
                )
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = Teks.pengaturan,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showSettings = true }
                        .padding(6.dp)
                        .size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // kolom pencarian model pill, sengaja dibuat lebih
        // ramping dari tinggi bawaan Material (keluhan user)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(if (modeLokal) Teks.saringPlaceholder else Teks.cariPlaceholder, fontSize = 14.5.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(21.dp)) },
            trailingIcon = {
                if (searching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.5.sp),
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { doSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
        statusMsg?.let {
            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
        }

        // pemisah mode beranda: Online atau Lokal
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                .padding(4.dp)
        ) {
            listOf(false to Teks.online, true to Teks.lokal).forEach { (lokal, label) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (modeLokal == lokal) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable(enabled = lokal || online) {
                            modeLokal = lokal
                            if (lokal) {
                                results = null
                                statusMsg = null
                            }
                        }
                        .padding(vertical = 7.dp)
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (modeLokal == lokal) {
                            MaterialTheme.colorScheme.onPrimary
                        } else if (!lokal && !online) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        if (modeLokal) {
            // ----- tab Lokal: berkas musik di perangkat -----
            val lokalList by LocalLibrary.tracks.collectAsState()
            val lokalTermuat by LocalLibrary.termuat.collectAsState()
            val lokalMuat by LocalLibrary.sedangMuat.collectAsState()
            val folderAktif by LocalLibrary.folderUri.collectAsState()
            val namaFolderAktif by LocalLibrary.folderNama.collectAsState()
            val izinMusik = ContextCompat.checkSelfPermission(context, izinAudio) == PackageManager.PERMISSION_GRANTED
            LaunchedEffect(izinMusik, modeLokal) {
                LocalLibrary.muat(context)
            }
            // baris sumber: semua perangkat atau satu folder pilihan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        if (folderAktif != null) namaFolderAktif.ifEmpty { Teks.folderPilihan } else Teks.semuaFolder,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (folderAktif != null) Teks.dipindaiLangsung else Teks.dariIndeks,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { folderLokalLauncher.launch(null) }) {
                    Text(if (folderAktif != null) Teks.ganti else Teks.pilihFolder)
                }
                if (folderAktif != null) {
                    TextButton(onClick = { scope.launch { LocalLibrary.pakaiFolder(context, null) } }) {
                        Text(Teks.reset)
                    }
                }
            }
            if (!izinMusik && folderAktif == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        Teks.izinMusikInfo,
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { izinMusikLauncher.launch(izinAudio) },
                        modifier = Modifier.padding(top = 14.dp)
                    ) { Text(Teks.izinkanAksesMusik) }
                }
            } else {
                val saring = query.trim().lowercase()
                val tampil = if (saring.isEmpty()) {
                    lokalList
                } else {
                    lokalList.filter {
                        it.judul.lowercase().contains(saring) || it.artis.lowercase().contains(saring)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(
                        if (folderAktif != null) Teks.jumlahLaguFolder(lokalList.size) else Teks.jumlahLaguPerangkat(lokalList.size),
                        fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { scope.launch { LocalLibrary.muat(context) } }) {
                        Icon(Icons.Filled.Refresh, contentDescription = Teks.muatUlang, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                when {
                    lokalMuat && lokalList.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    tampil.isEmpty() -> {
                        Text(
                            if (!lokalTermuat || lokalList.isEmpty()) {
                                if (folderAktif != null) {
                                    Teks.folderKosongMusik
                                } else {
                                    Teks.tidakAdaBerkasMusik
                                }
                            } else {
                                Teks.tidakAdaLaguLokalCocok
                            },
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = if (currentTrack != null) 104.dp else 8.dp)
                        ) {
                            itemsIndexed(tampil, key = { _, lt -> lt.kunci }) { index, lt ->
                                LocalTrackRow(
                                    lt = lt,
                                    isCurrent = currentTrack?.id == "lokal:${lt.kunci}",
                                    isPlaying = isPlaying,
                                    onClick = { PlayerManager.playLocalQueue(tampil, index) }
                                )
                            }
                        }
                    }
                }
            }
        }

        val res = results
        if (!modeLokal && res != null) {
            // mode hasil pencarian: tombol kembali + daftar lagu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(CircleShape)
                    .clickable {
                        results = null
                        sesiCari = null
                        statusMsg = null
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Teks.kembali, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(Teks.kembali, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
            }
            val stateHasil = rememberLazyListState()
            // lazy load: pas guliran mendekati ujung, ambil halaman
            // berikutnya dari sesi yang sama (musik dulu, lalu video)
            LaunchedEffect(stateHasil) {
                snapshotFlow { stateHasil.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
                    .collect { terakhir ->
                        val sesi = sesiCari ?: return@collect
                        val kini = results ?: return@collect
                        if (terakhir >= kini.size - 4 && !sesi.habis && !memuatLagi && !searching) {
                            memuatLagi = true
                            try {
                                val tambahan = MusicApi.cariBerikutnya(sesi)
                                val ada = kini.map { it.id }.toHashSet()
                                val baru = tambahan.filter { it.id !in ada }
                                if (baru.isNotEmpty()) {
                                    val gabungan = kini + baru
                                    results = gabungan
                                    statusMsg = Teks.jumlahHasil(gabungan.size)
                                }
                            } catch (e: Exception) {
                                // gagal memuat lanjutan tidak ditampilkan;
                                // menggulir lagi akan mencoba ulang sendiri
                            } finally {
                                memuatLagi = false
                            }
                        }
                    }
            }
            LazyColumn(state = stateHasil, modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = if (currentTrack != null) 104.dp else 8.dp)) {
                itemsIndexed(res) { index, track ->
                    TrackRow(
                        track = track,
                        isCurrent = currentTrack?.id == track.id,
                        isPlaying = isPlaying,
                        onClick = { PlayerManager.playQueue(res, index) },
                        onAdd = { pickerTrack = track }
                    )
                }
                if (memuatLagi) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                Teks.sedangMemuat,
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                } else if (sesiCari?.habis == true && res.size >= 20) {
                    item {
                        Text(
                            Teks.semuaHasil,
                            fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                }
            }
        } else if (!modeLokal) {
            // beranda online: rekomendasi dari algoritma selera, lalu library
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = if (currentTrack != null) 104.dp else 8.dp)) {
                if (recommendations.isNotEmpty()) {
                    item { SectionHeader(title = Teks.rekomendasi) }
                    itemsIndexed(recommendations, key = { _, t -> "rec-" + t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            onClick = { PlayerManager.playQueue(recommendations, index) },
                            onAdd = { pickerTrack = track }
                        )
                    }
                }
                item {
                    SectionHeader(
                        title = Teks.playlist,
                        actionLabel = Teks.buat,
                        onAction = {
                            pendingAdd = null
                            newPlaylistName = ""
                            showCreate = true
                        }
                    )
                }
                if (playlistsList.isEmpty()) {
                    item { EmptyText() }
                } else {
                    items(playlistsList, key = { "pl-" + it.id }) { p ->
                        PlaylistRow(playlist = p, onClick = { openPlaylistId = p.id })
                    }
                }
                item { SectionHeader(title = Teks.disukai) }
                if (likesList.isEmpty()) {
                    item { EmptyText() }
                } else {
                    itemsIndexed(likesList, key = { _, t -> "like-" + t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            onClick = { PlayerManager.playQueue(likesList, index) },
                            onAdd = { pickerTrack = track },
                            onDelete = { LibraryStore.removeLike(track.id) }
                        )
                    }
                }
                item {
                    SectionHeader(
                        title = Teks.baruDiputar,
                        actionLabel = if (historyList.isNotEmpty()) Teks.hapusSemua else null,
                        onAction = {
                            confirmAction = Teks.hapusSemuaRiwayat to { LibraryStore.clearHistory() }
                        }
                    )
                }
                if (historyList.isEmpty()) {
                    item { EmptyText() }
                } else {
                    itemsIndexed(historyList, key = { _, t -> "hist-" + t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            onClick = { PlayerManager.playQueue(historyList, index) },
                            onAdd = { pickerTrack = track },
                            onDelete = { LibraryStore.removeFromHistory(track.id) }
                        )
                    }
                }
                item { SectionHeader(title = Teks.unduhan) }
                if (dlList.isEmpty()) {
                    item { EmptyText() }
                } else {
                    itemsIndexed(dlList, key = { _, t -> "dl-" + t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isCurrent = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            onClick = { PlayerManager.playQueue(dlList, index) },
                            onDelete = { DownloadStore.delete(track.id) }
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        (playError ?: dlError)?.let {
            Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 6.dp))
        }

        Spacer(Modifier.height(16.dp))
    }

    // mini player sekarang ngambang di atas daftar lagu, nggak lagi
    // nempel di alur bawah kolom; kartunya dikasih efek blur kaca
    if (currentTrack != null && !showFullPlayer) {
        MiniPlayer(
            track = currentTrack!!,
            isPlaying = isPlaying,
            busy = isResolving || isBuffering,
            hasNext = currentIndex in 0 until (queue.size - 1),
            onToggle = { PlayerManager.togglePlay() },
            onNext = { PlayerManager.next() },
            onOpen = { showFullPlayer = true },
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }

    // ----- lapisan detail playlist -----
    // digambar di dalam Box utama (di atas daftar dan mini player,
    // di bawah pemutar penuh), bukan jendela dialog lagi
    val openId = openPlaylistId
    if (openId != null) {
        val playlist = playlistsList.find { it.id == openId }
        if (playlist == null) {
            openPlaylistId = null
        } else {
            PlaylistDetail(
                playlist = playlist,
                currentTrackId = currentTrack?.id,
                isPlaying = isPlaying,
                onClose = { openPlaylistId = null },
                onPlayAll = { if (playlist.tracks.isNotEmpty()) PlayerManager.playQueue(playlist.tracks, 0) },
                onPlayAt = { index -> PlayerManager.playQueue(playlist.tracks, index) },
                onRemoveTrack = { trackId -> LibraryStore.removeFromPlaylist(playlist.id, trackId) },
                onDeletePlaylist = {
                    confirmAction = Teks.hapusPlaylistNama(playlist.name) to {
                        LibraryStore.deletePlaylist(playlist.id)
                        openPlaylistId = null
                    }
                }
            )
        }
    }

    if (showFullPlayer && currentTrack != null) {
        FullPlayer(
            track = currentTrack!!,
            isPlaying = isPlaying,
            busy = isResolving || isBuffering,
            hasNext = currentIndex in 0 until (queue.size - 1),
            hasPrev = currentIndex > 0,
            onToggle = { PlayerManager.togglePlay() },
            onNext = { PlayerManager.next() },
            onPrev = { PlayerManager.prev() },
            onAddToPlaylist = { pickerTrack = currentTrack },
            onDownloadRequest = {
                val t = currentTrack!!
                dlName = DownloadStore.defaultFileName(t)
                dlChoice = dlDestSaved
                downloadTarget = t
            },
            onClose = { showFullPlayer = false }
        )
    }
    }


    // ----- dialog: pengaturan -----
    if (showSettings) {
        val kualitasAudio by SettingsStore.kualitasAudio.collectAsState()
        val waktuTidur by SettingsStore.waktuTidur.collectAsState()
        val lanjutSesi by SettingsStore.lanjutSesi.collectAsState()
        val kualitasUnduh by SettingsStore.formatUnduh.collectAsState()
        val unduhWifi by SettingsStore.unduhWifi.collectAsState()
        val sumberLirik by SettingsStore.sumberLirik.collectAsState()
        val ukuranLirik by SettingsStore.ukuranLirik.collectAsState()
        val bahasa by SettingsStore.bahasa.collectAsState()
        val statusLokal by MusicApi.statusLokal.collectAsState()
        val loaderGambar = remember { context.imageLoader }
        var ukuranCache by remember { mutableStateOf(-1L) }
        LaunchedEffect(Unit) {
            ukuranCache = withContext(Dispatchers.IO) { loaderGambar.diskCache?.size ?: 0L }
        }
        BackHandler { showSettings = false }
        // Pengaturan sekarang halaman layar penuh, bukan dialog:
        // dialog lamanya meregang setinggi layar dan menyisakan
        // ruang kosong besar di bagian bawah (keluhan user).
        // Jarak aman bar sistem wajib dipasang sendiri di sini:
        // tanpa itu bar judulnya naik ketutup status bar.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 6.dp)
                ) {
                    Text(
                        Teks.pengaturan,
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showSettings = false }) {
                        Icon(Icons.Filled.Close, contentDescription = Teks.tutup)
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    DropdownSetelan(
                        Teks.tema,
                        listOf(0 to Teks.ikutSistem, 1 to Teks.terang, 2 to Teks.gelap),
                        themeMode
                    ) { ThemeStore.setMode(it) }

                    JudulBagianSetelan(Teks.pemutaran)
                    DropdownSetelan(
                        Teks.pengaturWaktuTidur,
                        listOf(0 to Teks.mati, 15 to Teks.durasiMenit(15), 30 to Teks.durasiMenit(30), 45 to Teks.durasiMenit(45), 60 to Teks.durasiMenit(60), -1 to Teks.akhirLaguIni),
                        waktuTidur
                    ) { SettingsStore.setWaktuTidur(it) }
                    DropdownSetelan(
                        Teks.kualitasAudio,
                        listOf(0 to Teks.otomatis, 1 to Teks.hematData, 2 to Teks.tertinggi),
                        kualitasAudio
                    ) { SettingsStore.setKualitasAudio(it) }
                    Text(
                        Teks.jalurLokalTerakhir(statusLokal),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    SakelarSetelan(
                        Teks.lanjutkanSesi,
                        Teks.lanjutkanSesiSub,
                        lanjutSesi
                    ) { SettingsStore.setLanjutSesi(it) }

                    JudulBagianSetelan(Teks.unduhan)
                    DropdownSetelan(
                        Teks.kualitasUnduhan,
                        listOf(0 to Teks.ikutKualitasPutar, 1 to Teks.hematBerkas, 2 to Teks.tertinggi),
                        kualitasUnduh
                    ) { SettingsStore.setFormatUnduh(it) }
                    SakelarSetelan(Teks.unduhHanyaWifi, null, unduhWifi) {
                        SettingsStore.setUnduhWifi(it)
                    }

                    JudulBagianSetelan(Teks.lirik)
                    DropdownSetelan(
                        Teks.sumberLirik,
                        listOf(0 to Teks.otomatisLrclib, 1 to Teks.lrclibSaja),
                        sumberLirik
                    ) { SettingsStore.setSumberLirik(it) }
                    DropdownSetelan(
                        Teks.ukuranTeksLirik,
                        listOf(0 to Teks.kecil, 1 to Teks.sedang, 2 to Teks.besar),
                        ukuranLirik
                    ) { SettingsStore.setUkuranLirik(it) }

                    JudulBagianSetelan(Teks.data)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        OutlinedButton(onClick = { eksporLauncher.launch("musik-library.json") }) {
                            Text(Teks.eksporLibrary)
                        }
                        OutlinedButton(onClick = { imporLauncher.launch(arrayOf("application/json")) }) {
                            Text(Teks.imporLibrary)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(Teks.cacheGambar, fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (ukuranCache < 0) {
                                    Teks.menghitungUkuran
                                } else if (ukuranCache >= 1024 * 1024) {
                                    Teks.ukuranSekarang(String.format(localeAngka(), "%.1f", ukuranCache / 1048576.0) + " MB")
                                } else {
                                    Teks.ukuranSekarang((ukuranCache / 1024).toString() + " KB")
                                },
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = {
                            setScope.launch {
                                withContext(Dispatchers.IO) {
                                    loaderGambar.memoryCache?.clear()
                                    loaderGambar.diskCache?.clear()
                                }
                                ukuranCache = 0L
                                Toast.makeText(context, Teks.cacheDibersihkan, Toast.LENGTH_SHORT).show()
                            }
                        }) { Text(Teks.bersihkan) }
                    }

                    JudulBagianSetelan(Teks.umum)
                    DropdownSetelan(
                        Teks.bahasaLabel,
                        listOf(0 to Teks.bahasaIndonesia, 1 to Teks.bahasaInggris),
                        bahasa,
                        ikon = mapOf(0 to R.drawable.ic_bendera_id, 1 to R.drawable.ic_bendera_gb)
                    ) { SettingsStore.setBahasa(it) }
                    JudulBagianSetelan(Teks.tentang)
                    Text(
                        Teks.dibuatOleh,
                        fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/ClanDare"))
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, Teks.tidakAdaAplikasiTelegram, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_telegram),
                            contentDescription = null,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                            Text("Telegram", fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "@ClanDare",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/craxid"))
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, Teks.tidakAdaAplikasiGithub, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_github),
                            contentDescription = null,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                            Text("GitHub", fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "@craxid",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }


                    Text(
                        "Musik v$installedName",
                        fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 14.dp, bottom = 20.dp)
                    )
                }
            }
        }
    }

    // ----- dialog: offline -----
    if (tampilOffline) {
        DialogMusik(
            onDismissRequest = { tampilOffline = false },
            title = { Text(Teks.offlineJudul) },
            text = { Text(Teks.offlineIsi, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { tampilOffline = false }) { Text(Teks.mengerti) }
            }
        )
    }

    // ----- dialog: konfirmasi unduh (ganti nama + pilih folder simpan) -----
    // ditulis sesudah FullPlayer: dialog ini dibuka DARI full player,
    // jadi tumpukannya harus di atasnya (aturan urutan dialog di Compose)
    val dt = downloadTarget
    if (dt != null) {
        DialogMusik(
            onDismissRequest = { downloadTarget = null },
            title = { Text(Teks.unduhLagu) },
            text = {
                Column {
                    Text(dt.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(dt.author, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                    OutlinedTextField(
                        value = dlName,
                        onValueChange = { dlName = it },
                        label = { Text(Teks.namaFile) },
                        trailingIcon = {
                            if (dlName.isNotEmpty()) {
                                IconButton(onClick = { dlName = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        Teks.ekstensiInfo,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )
                    Text(Teks.simpanDi, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    listOf<DlDestination>(DlDestination.Downloads, DlDestination.Music).forEach { opt ->
                        BarisPilihanFolder(
                            terpilih = dlChoice == opt,
                            label = if (opt is DlDestination.Music) Teks.folderMusik else Teks.folderDownload,
                            onClick = { dlChoice = opt }
                        )
                    }
                    val savedCustom = dlDestSaved as? DlDestination.Custom
                    val activeCustom = dlChoice as? DlDestination.Custom
                    BarisPilihanFolder(
                        terpilih = activeCustom != null,
                        label = Teks.folderLain,
                        sublabel = activeCustom?.label ?: savedCustom?.label,
                        onClick = {
                            if (savedCustom != null) dlChoice = savedCustom
                            else folderLauncher.launch(null)
                        },
                        aksiKanan = if (savedCustom != null) {
                            { TextButton(onClick = { folderLauncher.launch(null) }) { Text(Teks.ganti) } }
                        } else null
                    )
                    Text(
                        Teks.kualitasUnduhan,
                        fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    listOf(
                        0 to Teks.ikutiPengaturan,
                        1 to Teks.hematBerkas,
                        2 to Teks.tertinggi
                    ).forEach { (nilai, label) ->
                        BarisPilihanFolder(
                            terpilih = dlKualitas == nilai,
                            label = label,
                            onClick = { dlKualitas = nilai }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { dlTanamCover = !dlTanamCover }
                    ) {
                        Checkbox(checked = dlTanamCover, onCheckedChange = { dlTanamCover = it })
                        Text(Teks.tanamCover, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { dlTanamLirik = !dlTanamLirik }
                    ) {
                        Checkbox(checked = dlTanamLirik, onCheckedChange = { dlTanamLirik = it })
                        Text(Teks.tanamLirik, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (dlTanamLirik) {
                        val gayaStatus = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        when (dlLirikStatus) {
                            1 -> Text(Teks.mencariLirik, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = gayaStatus)
                            2 -> Text(Teks.lirikSiapTanam, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = gayaStatus)
                            3 -> {
                                Text(Teks.lirikTakKetemuUnduh, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
                                Row {
                                    TextButton(onClick = { berkasLirikLauncher.launch(arrayOf("*/*")) }) { Text(Teks.dariBerkas) }
                                    TextButton(onClick = { dlCariLirik = true }) { Text(Teks.cariLirik) }
                                }
                            }
                            4 -> Text(Teks.lirikDariBerkasSiap, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = gayaStatus)
                            5 -> Text(Teks.lirikDariCariSiap, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = gayaStatus)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = dlName.isNotBlank(),
                    onClick = {
                        // setelan unduh hanya Wi-Fi: tahan dulu dan kasih
                        // tahu, dialognya jangan ditutup biar bisa dicoba
                        // lagi pas sudah pindah ke Wi-Fi
                        if (SettingsStore.unduhWifi.value && !id.my.hxa.musik.data.NetUtil.sedangWifi(context)) {
                            Toast.makeText(context, Teks.unduhanDitahanWifi, Toast.LENGTH_LONG).show()
                        } else {
                            val opsi = OpsiUnduh(
                                kualitasOverride = if (dlKualitas == 0) null else dlKualitas,
                                tanamCover = dlTanamCover,
                                lirikTeks = if (dlTanamLirik) dlLirikTeks else null
                            )
                            DownloadStore.simpanPilihanUnduh(dlKualitas, dlTanamCover, dlTanamLirik)
                            startDownload(dt, dlName, dlChoice, opsi)
                            downloadTarget = null
                            statusMsg = Teks.mengunduhJudul(dt.title)
                        }
                    }
                ) { Text(Teks.unduh) }
            },
            dismissButton = {
                TextButton(onClick = { downloadTarget = null }) { Text(Teks.batal) }
            }
        )
    }
    // ----- dialog: cari lirik buat ditanam ke unduhan -----
    // numpuk di atas dialog unduh; formulirnya sama persis dengan
    // yang dipakai panel lirik, bedanya hasil pilihan di sini
    // disimpan sebagai teks tanaman, bukan dipasang ke pemutar
    val dtc = downloadTarget
    if (dlCariLirik && dtc != null) {
        val saran = remember(dtc.id) { MusicApi.saranLirik(dtc) }
        DialogMusik(
            onDismissRequest = { dlCariLirik = false },
            title = { Text(Teks.cariLirik) },
            text = {
                FormCariLirik(
                    awalJudul = saran.first,
                    awalArtis = saran.second,
                    durasiTrack = dtc.durationSec,
                    onDark = false,
                    onTerapkan = { lirik ->
                        val teks = lirikKeTeks(lirik)
                        if (teks != null) {
                            dlLirikTeks = teks
                            dlLirikStatus = 5
                        }
                        dlCariLirik = false
                    },
                    onBatal = { dlCariLirik = false }
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dlCariLirik = false }) { Text(Teks.batal) }
            }
        )
    }
    // ----- popup: progres unduhan -----
    // Permintaan user 7 Nov: pas mengunduh harus kelihatan popup
    // "Mengunduh... NN%" lengkap dengan progress bar, bukan cuma
    // baris status. Sembunyikan cuma menutup popupnya, unduhan
    // tetap lanjut di belakang.
    if (popupUnduh && dlProgress.isNotEmpty()) {
        DialogMusik(
            onDismissRequest = { popupUnduh = false },
            title = { Text(Teks.mengunduhLagu) },
            text = {
                Column {
                    for ((id, pct) in dlProgress) {
                        Text(
                            dlJudul[id] ?: Teks.lagu,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        if (pct >= 0) {
                            Text(
                                Teks.mengunduhPersen(pct),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp)
                            )
                        } else {
                            Text(
                                Teks.menyiapkan,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { popupUnduh = false }) { Text(Teks.sembunyikan) }
            }
        )
    }
    // ----- dialog: buat playlist -----
    if (showCreate) {
        DialogMusik(
            onDismissRequest = { showCreate = false },
            title = { Text(Teks.playlistBaru) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text(Teks.namaPlaylist) },
                    trailingIcon = {
                        if (newPlaylistName.isNotEmpty()) {
                            IconButton(onClick = { newPlaylistName = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPlaylistName.isNotBlank(),
                    onClick = {
                        val p = LibraryStore.createPlaylist(newPlaylistName.trim())
                        pendingAdd?.let {
                            LibraryStore.addToPlaylist(p.id, it)
                            statusMsg = Teks.ditambahkanKe(it.title, p.name)
                        }
                        pendingAdd = null
                        newPlaylistName = ""
                        showCreate = false
                    }
                ) { Text(Teks.buat) }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text(Teks.batal) }
            }
        )
    }

    // ----- dialog: konfirmasi hapus -----
    val ca = confirmAction
    if (ca != null) {
        DialogMusik(
            onDismissRequest = { confirmAction = null },
            title = { Text(Teks.konfirmasi) },
            text = { Text(ca.first) },
            confirmButton = {
                TextButton(onClick = { ca.second(); confirmAction = null }) {
                    Text(Teks.hapus, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) { Text(Teks.batal) }
            }
        )
    }

    // ----- dialog: pilih playlist tujuan -----
    val pt = pickerTrack
    if (pt != null) {
        DialogMusik(
            onDismissRequest = { pickerTrack = null },
            title = { Text(Teks.tambahKePlaylist) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (playlistsList.isEmpty()) {
                        Text(Teks.belumAdaPlaylist, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    playlistsList.forEach { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    LibraryStore.addToPlaylist(p.id, pt)
                                    statusMsg = Teks.ditambahkanKe(pt.title, p.name)
                                    pickerTrack = null
                                }
                                .padding(vertical = 7.dp, horizontal = 6.dp)
                        ) {
                            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(p.name, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).padding(start = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(Teks.jumlahLagu(p.tracks.size), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                pendingAdd = pt
                                pickerTrack = null
                                newPlaylistName = ""
                                showCreate = true
                            }
                            .padding(vertical = 7.dp, horizontal = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(Teks.playlistBaru, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerTrack = null }) { Text(Teks.tutup) }
            }
        )
    }

}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = RobotoFamily, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                if (actionLabel == Teks.buat) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(actionLabel, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EmptyText() {
    Text(
        Teks.masihKosong,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
        ) {
            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(playlist.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(Teks.jumlahLagu(playlist.tracks.size), fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun PlaylistDetail(
    playlist: Playlist,
    currentTrackId: String?,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onPlayAll: () -> Unit,
    onPlayAt: (Int) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    // sekarang lapisan layar penuh di dalam aktivitas, bukan Dialog
    // lagi: jendela dialog bawa inset dan warna bilah status sendiri
    // yang dulu bikin pemutar penuh bermasalah di HP user
    BackHandler(onBack = onClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .latarKotak()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Teks.kembali, tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(playlist.name, fontSize = 21.sp, fontWeight = FontWeight.Bold, fontFamily = RobotoFamily, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                IconButton(onClick = onDeletePlaylist) {
                    Icon(Icons.Filled.Delete, contentDescription = Teks.hapusPlaylist, tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(Teks.jumlahLagu(playlist.tracks.size), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, top = 2.dp))
            Spacer(Modifier.height(12.dp))
            Button(onClick = onPlayAll, enabled = playlist.tracks.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(Teks.putarSemua, modifier = Modifier.padding(start = 6.dp))
            }
            Spacer(Modifier.height(10.dp))
            if (playlist.tracks.isEmpty()) {
                EmptyText()
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(playlist.tracks, key = { _, t -> "plt-" + t.id }) { index, track ->
                        TrackRow(
                            track = track,
                            isCurrent = currentTrackId == track.id,
                            isPlaying = isPlaying,
                            onClick = { onPlayAt(index) },
                            onDelete = { onRemoveTrack(track.id) }
                        )
                    }
                }
            }
    }
}

// baris lagu lokal di tab Lokal: sampul dari album art MediaStore,
// tag HI-RES hijau buat berkas lossless + label formatnya
@Composable
fun LocalTrackRow(
    lt: LocalTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        AsyncImage(
            model = lt.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                lt.judul,
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(
                listOf(lt.artis, fmtDur(lt.durasiSec)).filter { it.isNotEmpty() && it != "0:00" }.joinToString(" • "),
                fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (lt.lossless) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    "HI-RES",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                lt.formatLabel,
                fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp)
            )
        } else if (lt.formatLabel.isNotEmpty()) {
            Text(
                lt.formatLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onAdd: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val titleColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        AsyncImage(
            model = track.thumbUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                track.title,
                fontSize = 15.sp, fontWeight = FontWeight.Medium, color = titleColor,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(
                listOf(track.author, fmtDur(track.durationSec)).filter { it.isNotEmpty() && it != "0:00" }.joinToString(" • "),
                fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (onAdd != null) {
            IconButton(onClick = onAdd, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.PlaylistAdd, contentDescription = Teks.tambahKePlaylist, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp))
            }
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = Teks.hapus, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        if (isCurrent) {
            Icon(
                if (isPlaying) Icons.Filled.GraphicEq else Icons.Filled.Pause,
                contentDescription = if (isPlaying) Teks.sedangDiputar else Teks.jeda,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp).size(22.dp)
            )
        }
    }
}

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    busy: Boolean,
    hasNext: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    // kartu ngambang di atas daftar: bayangan biar kelihatan melayang,
    // terus efek blur kaca dari Haze buat nge-blur isi daftar yang
    // lewat di belakangnya (di Android di bawah 12, Haze jatuh ke
    // tint pekat sebagai cadangannya)
    val warnaKaca = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val warnaCadangan = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    val latarKaca = MaterialTheme.colorScheme.background
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = latarKaca,
                    tint = HazeTint(warnaKaca),
                    blurRadius = 24.dp,
                    noiseFactor = 0f,
                    fallbackTint = HazeTint(warnaCadangan)
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable { onOpen() }
            .padding(10.dp)
    ) {
        AsyncImage(
            model = PlayerManager.artLokal(track.id) ?: track.thumbUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(track.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1200, repeatDelayMillis = 1800, velocity = 28.dp))
            Text(track.author, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
        } else {
            IconButton(onClick = onToggle) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) Teks.jeda else Teks.putar,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        IconButton(onClick = onNext, enabled = hasNext) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = Teks.berikutnya,
                tint = if (hasNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// Potong thumbnail tepat ke sampul lagunya. Thumbnail YouTube itu bingkai
// video: varian 4:3 (hqdefault) berisi gambar 16:9 yang diapit bingkai
// hitam atas-bawah, dan sampul albumnya sendiri persegi di tengah bingkai
// itu. Crop kotak biasa cuma membuang sisi luar, bingkainya tetap ikut;
// di sini yang diambil memang persegi tengah dari area gambarnya saja.
class CoverCropTransformation : Transformation {
    override val cacheKey: String = "cover_crop_v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val w = input.width
        val h = input.height
        if (w <= 0 || h <= 0) return input
        val aspect = w.toFloat() / h.toFloat()
        if (aspect in 0.9f..1.1f) return input // sudah persegi, jangan dipotong lagi
        // area gambar betulan dianggap 16:9 di dalam bingkai sumbernya
        val picH = minOf(h, (w * 9f / 16f).toInt())
        val picW = minOf(w, (h * 16f / 9f).toInt())
        val side = minOf(picW, picH)
        if (side <= 0 || (side >= w && side >= h)) return input
        val x = (w - side) / 2
        val y = (h - side) / 2
        return Bitmap.createBitmap(input, x, y, side, side)
    }
}

// Bilah progres ala YouTube Music: garis tipis dengan kenop bulat.
// Dibikin manual karena Slider bawaan material3 kenopnya garis tegak,
// bentuknya nggak mirip sama sekali.
@Composable
fun ProgressBarYtm(
    fraction: Float,
    onScrub: (Float) -> Unit,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val shown = (dragFraction ?: fraction).coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectTapGestures { off ->
                    onSeekFraction((off.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { off ->
                        val f = (off.x / size.width).coerceIn(0f, 1f)
                        dragFraction = f
                        onScrub(f)
                    },
                    onDragEnd = {
                        dragFraction?.let { onSeekFraction(it) }
                        dragFraction = null
                    },
                    onDragCancel = { dragFraction = null },
                    onHorizontalDrag = { change, _ ->
                        val f = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragFraction = f
                        onScrub(f)
                    }
                )
            }
    ) {
        val knob = 13.dp
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(maxWidth * shown)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth - knob) * shown)
                .size(knob)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// Satu segmen tombol di pill gabungan Suka | Lirik | Tidak suka:
// ikon doang tanpa teks. Keadaan aktif cukup ditandai warna ikon
// (hijau aksen), TANPA bulatan latar (sesuai permintaan user).
// Pas ditekan segmennya meloncat: menyusut sesaat terus memantul
// balik pakai spring.
@Composable
private fun SegmenPillAksi(
    ikon: ImageVector,
    label: String,
    aktif: Boolean,
    ukIkon: Dp,
    onClick: () -> Unit
) {
    val interaksi = remember { MutableInteractionSource() }
    val ditekan by interaksi.collectIsPressedAsState()
    val skala by animateFloatAsState(
        targetValue = if (ditekan) 0.76f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "skalaSegmen"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(skala)
            .clip(CircleShape)
            .clickable(interactionSource = interaksi, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Icon(
            ikon,
            contentDescription = label,
            tint = if (aktif) AccentDark else Color.White,
            modifier = Modifier.size(ukIkon)
        )
    }
}

// garis tegak pemisah antar segmen pill gabungan
@Composable
private fun PemisahPill() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Color.White.copy(alpha = 0.30f))
    )
}

// Stiker Hi-Res AUDIO: gambar logo aslinya (berkas
// drawable-nodpi/hires_logo.png, 400x400), ditempel apa adanya
// di sampul dengan bayangan tipis biar kelihatan kayak stiker
// fisik. Versi gambar Compose sebelumnya (v0.7.2) dinilai user
// jelek, makanya diganti gambar beneran.
@Composable
fun HiResSticker(modifier: Modifier = Modifier) {
    Image(
        painter = androidx.compose.ui.res.painterResource(R.drawable.hires_logo),
        contentDescription = "Hi-Res AUDIO",
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .size(28.dp)
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(6.dp), clip = false)
            .clip(RoundedCornerShape(6.dp))
    )
}

// Sampul versi CD: bulat, muter selama lagu jalan (sudut terakhir
// disimpan pas jeda biar lanjutannya nyambung), ada lubang kecil
// di tengahnya biar kelihatan kayak cakram beneran.
@Composable
private fun SampulCd(
    request: ImageRequest,
    side: Dp,
    isPlaying: Boolean,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sudut by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var terakhir = withFrameMillis { it }
            while (true) {
                withFrameMillis { t ->
                    sudut += (t - terakhir) * 0.03f
                    terakhir = t
                }
            }
        }
    }
    Box(modifier = modifier.size(side), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { onError() },
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .graphicsLayer { rotationZ = sudut }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        // lubang tengah cakram + porosnya
        Box(
            modifier = Modifier
                .size(side * 0.20f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.88f))
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(side * 0.055f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.75f))
        )
    }
}

@Composable
fun FullPlayer(
    track: Track,
    isPlaying: Boolean,
    busy: Boolean,
    hasNext: Boolean,
    hasPrev: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownloadRequest: () -> Unit,
    onClose: () -> Unit
) {
    val audioInfo by PlayerManager.audioInfo.collectAsState()
    val likesList by LibraryStore.likes.collectAsState()
    val liked = likesList.any { it.id == track.id }
    val dislikedList by LibraryStore.disliked.collectAsState()
    val disliked = dislikedList.any { it.id == track.id }
    val dlList by DownloadStore.downloads.collectAsState()
    val dlProgress by DownloadStore.progress.collectAsState()
    val shuffleOn by PlayerManager.shuffleOn.collectAsState()
    val repeatMode by PlayerManager.repeatMode.collectAsState()
    // panel yang lagi menggantikan gambar album: sampul atau lirik.
    // Panel Berikutnya dan Terkait sudah dihapus atas permintaan user;
    // lirik sekarang dibuka dari pill Lirik di baris aksi.
    var showLyrics by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var lyrics by remember { mutableStateOf<Lyrics?>(null) }
    var lyricsLoading by remember { mutableStateOf(false) }
    var lyricsFor by remember { mutableStateOf<String?>(null) }
    var followPos by remember { mutableStateOf(0L) }
    var followDur by remember { mutableStateOf(0L) }
    var sliderPos by remember { mutableStateOf<Float?>(null) }
    // teks info audio buat bar paling bawah pemutar: sumber, codec,
    // sample rate, bitrate (dulu tampil kecil di bawah nama artis)
    val aiNow = audioInfo
    val infoAudio = when {
        aiNow != null -> {
            val khz = if (aiNow.sampleRate > 0) {
                val v = aiNow.sampleRate / 1000.0
                if (v % 1.0 == 0.0) "${v.toInt()} kHz" else "${String.format(localeAngka(), "%.1f", v)} kHz"
            } else ""
            listOf(if (aiNow.source == "Lokal") Teks.lokal else aiNow.source, aiNow.codec, khz, if (aiNow.bitrateKbps > 0) "${aiNow.bitrateKbps} kbps" else "")
                .filter { it.isNotEmpty() }.joinToString(" • ")
        }
        dlList.any { it.id == track.id } -> Teks.unduhanBerkasLokal
        else -> Teks.streamingYt
    }
    val artContext = LocalContext.current
    // sampul pakai versi HD (maxres) kalau videonya punya; kalau
    // YouTube balas 404, jatuh ke hq seperti sebelumnya
    var hdGagal by remember(track.id) { mutableStateOf(false) }
    val artRequest = remember(track.id, hdGagal) {
        ImageRequest.Builder(artContext)
            .data(
                // lagu lokal: sampulnya dari album art MediaStore
                if (track.id.startsWith("lokal:")) {
                    PlayerManager.artLokal(track.id) ?: track.thumbBigUrl
                } else if (hdGagal) {
                    track.thumbBigUrl
                } else {
                    track.thumbHdUrl
                }
            )
            .transformations(CoverCropTransformation())
            .build()
    }
    // mode tampilan sampul: persegi biasa atau bulat kayak CD yang
    // muter pas lagu jalan; beralih cukup ketuk gambarnya
    var modeCd by remember { mutableStateOf(true) }

    LaunchedEffect(track.id) {
        while (true) {
            followPos = PlayerManager.positionMs()
            followDur = PlayerManager.durationMs()
            delay(500)
        }
    }

    // lirik diambil begitu pemutar dibuka buat lagu ini (baris lirik
    // terkini di atas judul ikut makai), sesudah itu pakai hasil
    // yang sudah ada
    LaunchedEffect(track.id) {
        if (lyricsFor != track.id) {
            lyricsLoading = true
            lyrics = try {
                MusicApi.fetchLyrics(track)
            } catch (e: Exception) {
                null
            }
            lyricsFor = track.id
            lyricsLoading = false
        }
    }

    // lirik pilihan user dari daftar kandidat pencarian manual
    // dipasang lewat sini; hapus membuang lirik yang kepasang
    val terapkanLirik: (Lyrics) -> Unit = { l ->
        MusicApi.terapkanLirik(track, l)
        lyrics = l
    }
    val hapusLirik: () -> Unit = {
        MusicApi.hapusLirikCache(track)
        lyrics = null
    }
    val saranLirik = remember(track.id) { MusicApi.saranLirik(track) }

    // baris yang lagi jalan menurut posisi lagu; sebelum baris
    // pertama mulai, belum ada yang ditampilkan
    val barisKini: String? = run {
        val ls = lyrics?.lines
        if (ls.isNullOrEmpty()) {
            null
        } else {
            val idx = ls.indexOfLast { it.timeSec * 1000 <= followPos }
            if (idx < 0) null
            else ls[idx].text
        }
    }
    // efek percikan baris lirik: tiap barisnya berganti, partikel
    // kecil menyembur beterbangan dari sekitar teksnya lalu
    // memudar (permintaan user 7 Nov; efeknya di partikelnya,
    // bukan di teksnya)
    val warnaAksenPercik = MaterialTheme.colorScheme.primary
    var daftarPercik by remember { mutableStateOf<List<PercikanBaris>>(emptyList()) }
    val jalanPercik = remember { Animatable(0f) }
    LaunchedEffect(barisKini) {
        if (barisKini != null) {
            val acak = kotlin.random.Random.Default
            daftarPercik = List(18) {
                PercikanBaris(
                    x0 = acak.nextFloat(),
                    y0 = 0.25f + acak.nextFloat() * 0.5f,
                    sudut = acak.nextDouble(0.0, 2.0 * kotlin.math.PI),
                    jarak = 24f + acak.nextFloat() * 56f,
                    ukuran = 1.3f + acak.nextFloat() * 2.3f,
                    hijau = acak.nextFloat() < 0.45f,
                    bintang = acak.nextFloat() < 0.25f
                )
            }
            jalanPercik.snapTo(0f)
            jalanPercik.animateTo(1f, tween(780, easing = LinearOutSlowInEasing))
        }
    }

    // layar jangan mati selama pemutar penuh kebuka, biar lirik
    // dan sampul CD bisa ditonton terus; flagnya dicabut otomatis
    // pas pemutar ditutup
    val konteksLayar = LocalContext.current
    DisposableEffect(Unit) {
        val jendela = (konteksLayar as? Activity)?.window
        jendela?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { jendela?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    // Full player ini lapisan layar penuh di dalam aktivitas, bukan
    // Dialog: jendela dialog di HP realme user ngasih inset dan
    // warna status bar sendiri, bikin tampilan
    // kedorong ke bawah, bar tab kepotong keluar layar, dan status bar
    // tetap berpita (kejadian v0.4.7 dan v0.4.8). Di dalam aktivitas,
    // status bar transparan ngikutin layar utama yang sudah terbukti.
    BackHandler(onBack = onClose)
    // latar ala YouTube Music: gambar album diburamkan memenuhi layar,
    // ditimpa scrim gelap biar teks dan tombolnya tetap kebaca jelas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
    ) {
            AsyncImage(
                model = if (hdGagal) track.thumbBigUrl else track.thumbHdUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { hdGagal = true },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = 1.35f; scaleY = 1.35f }
                    .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(64.dp) else Modifier)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.42f), Color.Black.copy(alpha = 0.78f))
                        )
                    )
            )
            // ukuran elemen ngikutin ukuran layar: di HP kecil tombol,
            // teks, dan jaraknya ngecil biar nggak kegedean dan bar
            // bawahnya tetap muat (aturan responsif dari user)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val kecil = maxWidth < 360.dp || maxHeight < 660.dp
            val ukPutar = if (kecil) 58.dp else 68.dp
            val ukIkonPutar = if (kecil) 32.dp else 38.dp
            val ukSamping = if (kecil) 46.dp else 52.dp
            val ukIkonSamping = if (kecil) 30.dp else 34.dp
            val ukJudul = if (kecil) 18.sp else 21.sp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = if (kecil) 18.dp else 24.dp)
            ) {
            // bar atas: panah turun di kiri, info keluaran
            // audio di tengah (punya ruang sendiri), menu titik
            // tiga di kanan (isinya Unduh dan Tambah ke playlist).
            // Ikon kado donasi sempat di sini di v1.0.7-1.0.8,
            // dihapus user krn bar atas sudah penuh; donasi
            // sekarang cuma dari Pengaturan.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = Teks.tutup, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // info keluaran audio di tengah atas, sejajar gaya
                        // teks info sumber di paling bawah pemutar
                        LaunchedEffect(Unit) { AudioOutput.segarkan() }
                        val infoKeluaran by AudioOutput.info.collectAsState()
                        val ik = infoKeluaran
                        if (ik != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (ik.jenis) {
                                        AudioOutput.Jenis.USB -> Icons.Filled.Usb
                                        AudioOutput.Jenis.BLUETOOTH -> Icons.Filled.Bluetooth
                                        AudioOutput.Jenis.KABEL -> Icons.Filled.Headphones
                                        else -> Icons.Filled.Speaker
                                    },
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (ik.laju.isEmpty()) ik.label else "${ik.label} • ${ik.laju}",
                                    fontSize = 12.5.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = Teks.menu, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            val pct = dlProgress[track.id]
                            val downloaded = dlList.any { it.id == track.id }
                            when {
                                pct != null -> DropdownMenuItem(
                                    text = { Text(if (pct >= 0) Teks.mengunduhPersen(pct) else Teks.menyiapkan) },
                                    enabled = false,
                                    onClick = { }
                                )
                                downloaded -> DropdownMenuItem(
                                    text = { Text(Teks.sudahDiunduh) },
                                    leadingIcon = { Icon(Icons.Filled.DownloadDone, contentDescription = null) },
                                    enabled = false,
                                    onClick = { }
                                )
                                else -> DropdownMenuItem(
                                    text = { Text(Teks.unduhLagu) },
                                    leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null) },
                                    onClick = { showMenu = false; onDownloadRequest() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(Teks.tambahKePlaylist) },
                                leadingIcon = { Icon(Icons.Filled.PlaylistAdd, contentDescription = null) },
                                onClick = { showMenu = false; onAddToPlaylist() }
                            )
                        }
                    }
            }
            Spacer(Modifier.height(8.dp))
            // area panel: sampul album atau isi tab yang lagi dipilih
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (showLyrics) {
                    LyricsPanel(
                        lyrics = lyrics,
                        loading = lyricsLoading,
                        onDark = true,
                        positionMs = followPos,
                        onSeek = { sec -> PlayerManager.seekTo((sec * 1000).toLong()) },
                        saranJudul = saranLirik.first,
                        saranArtis = saranLirik.second,
                        durasiTrack = track.durationSec,
                        onTerapkanLirik = terapkanLirik,
                        onHapusLirik = hapusLirik,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        // sisi sampul dibatasi lebar dan tinggi area panel
                        // sekaligus: di v0.4.7 sampulnya bikin tata letak
                        // kedorong sampai bar tab di bawah hilang dari layar
                        val side = minOf(maxWidth * 0.94f, maxHeight)
                        if (modeCd) {
                            SampulCd(
                                request = artRequest,
                                side = side,
                                isPlaying = isPlaying,
                                onError = { hdGagal = true },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .clickable { modeCd = false }
                            )
                        } else {
                            AsyncImage(
                                model = artRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                onError = { hdGagal = true },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .size(side)
                                    .shadow(
                                        elevation = 18.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        clip = false,
                                        ambientColor = Color.Black.copy(alpha = 0.5f),
                                        spotColor = Color.Black.copy(alpha = 0.5f)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { modeCd = true }
                            )
                        }
                        // stiker Hi-Res AUDIO buat lagu lossless,
                        // nempel di kanan bawah sampul
                        if (PlayerManager.hiRes(track.id)) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter).size(side)) {
                                HiResSticker(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(
                                            end = if (modeCd) 14.dp else 10.dp,
                                            bottom = if (modeCd) 14.dp else 10.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
            // sampulnya nempel ke bawah area panel, jadi jarak ke
            // detail di bawahnya tetap rapat tapi nggak dempet
            Spacer(Modifier.height(if (kecil) 16.dp else 20.dp))
            // baris lirik yang lagi jalan, tampil di atas judul pas
            // panel lirik lagi ketutup; bergantinya dibuat lembut.
            // Tingginya dikunci setara dua baris biar sampul di
            // atasnya diam nggak geser-geser tiap baris liriknya
            // ganti panjang (permintaan user)
            if (!showLyrics) {
                Box(modifier = Modifier.fillMaxWidth().height(if (kecil) 48.dp else 54.dp)) {
                    AnimatedContent(
                        targetState = barisKini,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 }) togetherWith
                                fadeOut(tween(160))
                        },
                        label = "barisLirikKini"
                    ) { teks ->
                        if (teks != null) {
                            Text(
                                teks,
                                fontSize = if (kecil) 14.sp else 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.92f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // lapisan percikan menggambar di atas teksnya;
                    // partikel lahir pas baris berganti (keadaannya
                    // disiapkan di blok LaunchedEffect barisKini)
                    val partikelKini = daftarPercik
                    Canvas(Modifier.fillMaxSize()) {
                        val t = jalanPercik.value
                        if (t > 0f && t < 1f && partikelKini.isNotEmpty()) {
                            val ease = 1f - (1f - t) * (1f - t)
                            val pudar = (1f - t) * 0.95f
                            partikelKini.forEach { p ->
                                val ox = p.x0 * size.width * 0.8f
                                val oy = p.y0 * size.height
                                val tempuh = p.jarak * density * ease
                                val cx = ox + (kotlin.math.cos(p.sudut) * tempuh).toFloat()
                                val cy = oy + (kotlin.math.sin(p.sudut) * tempuh).toFloat() +
                                    10f * density * ease * ease
                                val warnaDasar = if (p.hijau) warnaAksenPercik else Color.White
                                val r = p.ukuran * density * (1f - t * 0.45f)
                                if (p.bintang) {
                                    val l = r * 2.6f
                                    drawLine(
                                        warnaDasar.copy(alpha = pudar),
                                        Offset(cx - l, cy), Offset(cx + l, cy),
                                        strokeWidth = density * 0.9f
                                    )
                                    drawLine(
                                        warnaDasar.copy(alpha = pudar),
                                        Offset(cx, cy - l), Offset(cx, cy + l),
                                        strokeWidth = density * 0.9f
                                    )
                                } else {
                                    drawCircle(
                                        warnaDasar.copy(alpha = pudar),
                                        radius = r,
                                        center = Offset(cx, cy)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Text(track.title, fontSize = ukJudul, fontWeight = FontWeight.Bold, fontFamily = RobotoFamily, color = Color.White, maxLines = 1, modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1200, repeatDelayMillis = 1800, velocity = 28.dp))
            Text(track.author, fontSize = if (kecil) 14.sp else 15.sp, color = Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(if (kecil) 12.dp else 16.dp))
            // baris aksi sekarang SATU pill gabungan: Suka | Lirik |
            // Tidak suka, ikon doang tanpa teks, dipisah garis tegak
            // (sesuai permintaan user). Segmen yang aktif latarnya
            // putih, ikonnya hitam. Yang ditandai tidak suka dikeluarin
            // dari Disukai, lagunya dilompati, dan nggak dimunculin
            // lagi di rekomendasi.
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.45f),
                            spotColor = Color.Black.copy(alpha = 0.45f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.21f),
                                    Color.White.copy(alpha = 0.11f)
                                )
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    SegmenPillAksi(
                        ikon = Icons.Filled.ThumbUp,
                        label = Teks.suka,
                        aktif = liked,
                        ukIkon = if (kecil) 18.dp else 19.dp
                    ) { LibraryStore.toggleLike(track) }
                    PemisahPill()
                    SegmenPillAksi(
                        ikon = Icons.Filled.Lyrics,
                        label = Teks.lirik,
                        aktif = showLyrics,
                        ukIkon = if (kecil) 18.dp else 19.dp
                    ) { showLyrics = !showLyrics }
                    PemisahPill()
                    SegmenPillAksi(
                        ikon = Icons.Filled.ThumbDown,
                        label = Teks.tidakSuka,
                        aktif = disliked,
                        ukIkon = if (kecil) 18.dp else 19.dp
                    ) {
                        val nowDisliked = LibraryStore.toggleDislike(track)
                        if (nowDisliked) PlayerManager.next()
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val shownPos = sliderPos?.toLong() ?: followPos
            ProgressBarYtm(
                fraction = if (followDur > 0) (shownPos.toFloat() / followDur.toFloat()).coerceIn(0f, 1f) else 0f,
                onScrub = { frac -> sliderPos = frac * followDur },
                onSeekFraction = { frac ->
                    PlayerManager.seekTo((frac * followDur).toLong())
                    sliderPos = null
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(fmtDur((shownPos / 1000).toInt()), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.weight(1f))
                Text(fmtDur((followDur / 1000).toInt()), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(6.dp))
            // kontrol lima tombol: acak, sebelumnya, putar (lingkaran putih
            // besar ikon hitam), berikutnya, ulangi
            val playInteraction = remember { MutableInteractionSource() }
            val playPressed by playInteraction.collectIsPressedAsState()
            val playScale by animateFloatAsState(if (playPressed) 0.9f else 1f, label = "skalaPutar")
            val nextEnabled = hasNext || shuffleOn || repeatMode == 1
            val prevEnabled = hasPrev || followPos > 3000
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { PlayerManager.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Acak",
                        tint = if (shuffleOn) AccentDark else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(23.dp)
                    )
                }
                IconButton(onClick = onPrev, enabled = prevEnabled, modifier = Modifier.size(ukSamping)) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = Teks.sebelumnya,
                        tint = if (prevEnabled) Color.White else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(ukIkonSamping)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(ukPutar)
                        .scale(playScale)
                        .shadow(
                            elevation = 14.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(interactionSource = playInteraction, indication = null) { onToggle() }
                ) {
                    if (busy) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                    } else {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) Teks.jeda else Teks.putar,
                            tint = Color.Black,
                            modifier = Modifier.size(ukIkonPutar)
                        )
                    }
                }
                IconButton(onClick = onNext, enabled = nextEnabled, modifier = Modifier.size(ukSamping)) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = Teks.berikutnya,
                        tint = if (nextEnabled) Color.White else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(ukIkonSamping)
                    )
                }
                IconButton(onClick = { PlayerManager.cycleRepeat() }) {
                    Icon(
                        if (repeatMode == 2) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = Teks.ulangi,
                        tint = if (repeatMode != 0) AccentDark else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // bar paling bawah isinya info audio lagu yang lagi
            // diputar, TANPA latar alias transparan (sesuai permintaan
            // user); tab Berikutnya/Lirik/Terkait sudah dihapus, lirik
            // pindah ke pill di baris aksi
            // baris bawah SELALU satu baris (dulu mode lokal dua
            // baris: info sumber + detail, bikin tombolnya naik
            // sendiri dibanding mode online). Buat berkas lokal
            // dan unduhan, info sumbernya digabung sama detail
            // audio rinci dari berkasnya dalam satu baris ini.
            val detailAudio by PlayerManager.audioDetail.collectAsState()
            val da = detailAudio
            val barisBawah = if (da == null) {
                infoAudio
            } else {
                val teksDetail = buildString {
                    append(da.format.uppercase())
                    if (da.sampleRate > 0) {
                        append(" • ")
                        val sr = da.sampleRate
                        append(
                            if (sr % 1000 == 0) {
                                "${sr / 1000} kHz"
                            } else {
                                "${sr / 1000}${if (Teks.inggris) "." else ","}${(sr % 1000) / 100} kHz"
                            }
                        )
                    }
                    if (da.bitDepth > 0) append(" • ${da.bitDepth} bit")
                    if (da.bitrateKbps > 0) append(" • ${da.bitrateKbps} kbps")
                    if (da.channels > 0) {
                        append(
                            when (da.channels) {
                                1 -> " • Mono"
                                2 -> " • Stereo"
                                else -> " • ${da.channels} kanal"
                            }
                        )
                    }
                }
                infoAudio.substringBefore(" • ") + " • " + teksDetail
            }
            Text(
                barisBawah,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            }
            }
        }
}

// Panel lirik di full player: versi tersinkron tampil per baris dengan
// baris aktif mengikuti posisi lagu (ketuk baris buat loncat ke detik itu),
// versi polos tampil sebagai teks biasa.
@Composable
fun LyricsPanel(
    lyrics: Lyrics?,
    loading: Boolean,
    positionMs: Long,
    onSeek: (Double) -> Unit,
    saranJudul: String,
    saranArtis: String,
    durasiTrack: Int,
    onTerapkanLirik: (Lyrics) -> Unit,
    onHapusLirik: () -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    // di full player latarnya selalu gelap (blur dari gambar album),
    // jadi warna teksnya dipaksa terang biar tetap kebaca
    val teksUtama = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface
    val teksRedup = if (onDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
    // ukuran teks lirik dari Pengaturan: baris aktif besar, sisanya
    // selalu 6sp di bawahnya
    val ukuranSetelan by SettingsStore.ukuranLirik.collectAsState()
    val ukuranAktifLirik = when (ukuranSetelan) {
        0 -> 18.sp
        2 -> 25.sp
        else -> 21.sp
    }
    val ukuranPasifLirik = when (ukuranSetelan) {
        0 -> 12.sp
        2 -> 19.sp
        else -> 15.sp
    }
    // mode mengganti lirik yang sudah kepasang (salah pasang
    // misalnya): panel beralih ke formulir pencarian kandidat
    var modeGanti by remember(lyrics) { mutableStateOf(false) }
    // tombol masuk mode ganti lirik, tampil di kepala panel
    // selama ada lirik yang kepasang
    val tombolGanti: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (onDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .clickable { modeGanti = true }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                Teks.ganti,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = teksUtama
            )
        }
    }
    Box(modifier = modifier) {
        when {
            loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(color = if (onDark) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                    Text(Teks.mencariLirik, fontSize = 13.sp, color = teksRedup, modifier = Modifier.padding(top = 10.dp))
                }
            }
            modeGanti && lyrics != null -> {
                // mengganti lirik yang kepasang: formulir yang sama
                // kayak pencarian manual, isinya diawali dari lirik
                // yang lagi terpasang biar gampang dikoreksi
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    Text(Teks.gantiLirik, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = teksUtama)
                    Text(
                        Teks.terpasangSekarang("${lyrics.source} • ${lyrics.trackName.ifEmpty { "-" }}") +
                            if (lyrics.artistName.isNotEmpty()) " — ${lyrics.artistName}" else "",
                        fontSize = 12.sp, color = teksRedup,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )
                    FormCariLirik(
                        awalJudul = lyrics.trackName.ifEmpty { saranJudul },
                        awalArtis = lyrics.artistName.ifEmpty { saranArtis },
                        durasiTrack = durasiTrack,
                        onDark = onDark,
                        onTerapkan = { l -> onTerapkanLirik(l); modeGanti = false },
                        onBatal = { modeGanti = false },
                        onHapus = { onHapusLirik(); modeGanti = false }
                    )
                }
            }
            lyrics == null -> {
                // lirik nggak ketemu otomatis: kasih kesempatan user
                // nyari sendiri, soalnya judul dari YouTube sering
                // beda dari judul resmi lagunya
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    Text(Teks.lirikTidakDitemukan, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = teksUtama)
                    Text(
                        Teks.judulYtBerbeda,
                        fontSize = 12.5.sp, color = teksRedup, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )
                    FormCariLirik(
                        awalJudul = saranJudul,
                        awalArtis = saranArtis,
                        durasiTrack = durasiTrack,
                        onDark = onDark,
                        onTerapkan = onTerapkanLirik
                    )
                }
            }
            lyrics.lines != null -> {
                val lines = lyrics.lines
                val activeIdx = lines.indexOfLast { it.timeSec * 1000 <= positionMs }
                val listState = rememberLazyListState()
                // baris aktif selalu digulir ke TENGAH panel: daftarnya
                // dikasih bantalan atas/bawah setengah tinggi area
                // (dihitung di BoxWithConstraints di bawah), jadi baris
                // pertama dan terakhir pun bisa berhenti di tengah
                LaunchedEffect(activeIdx) {
                    if (activeIdx >= 0) listState.animateScrollToItem(activeIdx)
                }
                // pas barisnya berganti, baris yang baru MENYEMBUL
                // sesaat: skala memantul keluar plus bayangan lembut
                val animPop = remember { Animatable(1f) }
                LaunchedEffect(activeIdx) {
                    if (activeIdx >= 0) {
                        animPop.snapTo(1.18f)
                        animPop.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    val srcLabel = buildString {
                        append(Teks.lirikOleh(lyrics.source))
                        if (lyrics.trackName.isNotEmpty()) {
                            append(" • ")
                            append(lyrics.trackName)
                            if (lyrics.artistName.isNotEmpty()) {
                                append(" — ")
                                append(lyrics.artistName)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(srcLabel, fontSize = 12.sp, color = teksRedup, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        tombolGanti()
                        Spacer(Modifier.width(6.dp))
                    }
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        val padTengah = (maxHeight / 2 - 24.dp).coerceAtLeast(0.dp)
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(top = padTengah, bottom = padTengah),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(lines) { i, line ->
                                val active = i == activeIdx
                                val teksBaris = line.text
                                Text(
                                    teksBaris,
                                    fontSize = if (active) ukuranAktifLirik else ukuranPasifLirik,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        active -> if (onDark) Color.White else MaterialTheme.colorScheme.primary
                                        i < activeIdx -> teksUtama.copy(alpha = 0.45f)
                                        else -> teksUtama.copy(alpha = 0.85f)
                                    },
                                    style = TextStyle(
                                        shadow = if (active) Shadow(
                                            color = Color.Black.copy(alpha = 0.55f),
                                            offset = Offset(0f, 4f),
                                            blurRadius = 10f
                                        ) else Shadow.None
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSeek(line.timeSec) }
                                        .padding(vertical = 5.dp, horizontal = 4.dp)
                                        .then(
                                            if (active) Modifier.graphicsLayer {
                                                scaleX = animPop.value
                                                scaleY = animPop.value
                                            } else Modifier
                                        )
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(Teks.lirikOleh(lyrics.source), fontSize = 12.sp, color = teksRedup, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        tombolGanti()
                        Spacer(Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            lyrics.plain ?: "",
                            fontSize = 15.sp, color = teksUtama
                        )
                    }
                }
            }
        }
    }
}

// Satu partikel percikan baris lirik: posisi awal relatif di
// kotak teks (x0, y0 dalam 0..1), arah sembur, jarak tempuh
// (dp), ukuran dasar (dp), warna dan bentuknya.
data class PercikanBaris(
    val x0: Float,
    val y0: Float,
    val sudut: Double,
    val jarak: Float,
    val ukuran: Float,
    val hijau: Boolean,
    val bintang: Boolean
)

// ubah hasil lirik jadi teks LRC buat ditanam ke berkas musik:
// versi tersinkron ditulis balik dengan cap waktunya, versi polos
// dipakai apa adanya
fun lirikKeTeks(lirik: Lyrics): String? {
    val lines = lirik.lines
    if (!lines.isNullOrEmpty()) {
        return lines.joinToString("\n") { baris ->
            val totalMs = (baris.timeSec * 1000).toLong()
            String.format(
                java.util.Locale.US, "[%02d:%02d.%02d]%s",
                totalMs / 60000, (totalMs % 60000) / 1000, (totalMs % 1000) / 10, baris.text
            )
        }
    }
    return lirik.plain?.takeIf { it.isNotBlank() }
}

// Formulir pencarian lirik manual plus daftar kandidatnya.
// Dipakai panel lirik pas lirik tidak ketemu otomatis dan pas
// user mengganti lirik yang sudah kepasang: hasil pencarian
// ditampilkan sebagai daftar untuk dipilih sendiri, bukan
// dipasang otomatis kayak dulu (pernah salah lagu).
@Composable
fun FormCariLirik(
    awalJudul: String,
    awalArtis: String,
    durasiTrack: Int,
    onDark: Boolean,
    onTerapkan: (Lyrics) -> Unit,
    onBatal: (() -> Unit)? = null,
    onHapus: (() -> Unit)? = null
) {
    val teksUtama = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface
    val teksRedup = if (onDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
    var fldJudul by remember(awalJudul) { mutableStateOf(awalJudul) }
    var fldArtis by remember(awalArtis) { mutableStateOf(awalArtis) }
    var kandidat by remember { mutableStateOf<List<LirikKandidat>>(emptyList()) }
    var lagiCari by remember { mutableStateOf(false) }
    var sudahCari by remember { mutableStateOf(false) }
    val lingkup = rememberCoroutineScope()
    val warnaKolom = if (onDark) OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = teksRedup,
        cursorColor = MaterialTheme.colorScheme.primary
    ) else OutlinedTextFieldDefaults.colors()
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fldJudul,
            onValueChange = { fldJudul = it },
            label = { Text(Teks.judulLagu) },
            trailingIcon = {
                if (fldJudul.isNotEmpty()) {
                    IconButton(onClick = { fldJudul = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            colors = warnaKolom,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = fldArtis,
            onValueChange = { fldArtis = it },
            label = { Text(Teks.artisBolehKosong) },
            trailingIcon = {
                if (fldArtis.isNotEmpty()) {
                    IconButton(onClick = { fldArtis = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            colors = warnaKolom,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    lingkup.launch {
                        lagiCari = true
                        sudahCari = true
                        kandidat = try {
                            MusicApi.cariKandidatLirik(durasiTrack, fldJudul, fldArtis)
                        } catch (e: Exception) {
                            emptyList()
                        }
                        lagiCari = false
                    }
                },
                enabled = fldJudul.isNotBlank() && !lagiCari
            ) {
                Text(Teks.cariLirik)
            }
            if (onBatal != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onBatal) {
                    Text(Teks.batal, color = teksRedup)
                }
            }
        }
        if (lagiCari) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                CircularProgressIndicator(
                    color = if (onDark) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp), strokeWidth = 2.dp
                )
                Text(Teks.mencari, fontSize = 13.sp, color = teksRedup, modifier = Modifier.padding(start = 10.dp))
            }
        } else if (sudahCari) {
            if (kandidat.isEmpty()) {
                Text(
                    Teks.tidakAdaHasil,
                    fontSize = 12.5.sp, color = teksRedup,
                    modifier = Modifier.padding(top = 14.dp)
                )
            } else {
                Text(
                    Teks.pilihLirikBenar,
                    fontSize = 12.5.sp, color = teksRedup,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                )
                kandidat.forEach { k ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTerapkan(k.lirik) }
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            k.lirik.trackName.ifEmpty { fldJudul },
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = teksUtama,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            listOf(
                                k.lirik.artistName,
                                if (k.durasiSec > 0) fmtDur(k.durasiSec) else "",
                                k.lirik.source,
                                if (k.lirik.lines != null) Teks.sinkron else Teks.polos
                            ).filter { it.isNotEmpty() }.joinToString(" • "),
                            fontSize = 12.sp, color = teksRedup,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        if (onHapus != null) {
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = onHapus) {
                Text(Teks.hapusLirikIni, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
