package id.my.hxa.musik.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// Bantuan jaringan kecil: lagi Wi-Fi atau bukan, dan lagi
// online atau tidak. Dipakai setelan "hanya Wi-Fi" buat
// unduhan dan sinkronisasi, plus pemantau offline beranda.
object NetUtil {

    fun sedangOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun sedangWifi(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            false
        }
    }
}
