// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.InetAddress
import java.security.MessageDigest

object DiagnosticsCollector {

    fun collect(context: Context, symptoms: List<String>, environment: Map<String, String>): JSONObject {
        val app = context.applicationContext
        val root = JSONObject()
        root.put("device_info", deviceInfo())
        root.put("power", power(app))
        root.put("storage", storage(app))
        root.put("wifi", wifi(app))
        root.put("sim_network", sim(app))
        root.put("system_general", general(app, symptoms))
        root.put("user_symptoms", JSONArray().apply { symptoms.forEach { put(it) } })
        root.put("environment_context", JSONObject().apply {
            put("charger_age", environment["charger_age"] ?: "")
            put("power_source_notes", environment["power_source_notes"] ?: "")
            put("screen_protector", environment["screen_protector"] ?: "")
            put("humidity_notes", environment["humidity_notes"] ?: "")
        })
        return root
    }

    private fun deviceInfo(): JSONObject = JSONObject().apply {
        put("brand", Build.BRAND)
        put("model", Build.MODEL)
        put("android_version", Build.VERSION.RELEASE)
        put("rom_version", Build.DISPLAY)
        put("root_status", "unknown")
        put("unavailable", JSONArray().apply { put("root_status") })
        put("note", "حالة الروت لا تُخمّن؛ يحتاج فحصاً مباشراً من النظام.")
    }

    private fun power(context: Context): JSONObject = JSONObject().apply {
        val sticky = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        if (sticky == null) {
            put("unavailable", JSONArray().apply { put("all") })
            return@apply
        }
        val level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val voltage = sticky.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val temp = sticky.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val plugged = sticky.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = sticky.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val healthCode = sticky.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        put("is_charging", charging)
        put("charge_type", when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "WIRELESS"
            else -> "NONE"
        })
        if (level >= 0 && scale > 0) put("battery_level", level * 100 / scale)
        else put("battery_level", JSONObject.NULL)
        if (voltage > 0) put("voltage_mv", voltage) else put("voltage_mv", JSONObject.NULL)

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentUa = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: Int.MIN_VALUE
        val chargeCounter = (bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: -1).toLong()
        val unavailable = JSONArray()
        if (currentUa != Int.MIN_VALUE) {
            put("current_ua", currentUa)
            put("current_ma", currentUa / 1000.0)
        } else {
            put("current_ua", JSONObject.NULL)
            put("current_ma", JSONObject.NULL)
            unavailable.put("current_ua").put("current_ma")
        }
        unavailable.put("average_current_ma")
        if (chargeCounter > 0) put("charge_counter_uah", chargeCounter)
        else { put("charge_counter_uah", JSONObject.NULL); unavailable.put("charge_counter_uah") }
        unavailable.put("capacity_mah_design").put("capacity_mah_actual")
        if (temp != Int.MIN_VALUE) put("temperature_c", temp / 10.0)
        else { put("temperature_c", JSONObject.NULL); unavailable.put("temperature_c") }
        put("health", when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER_VOLTAGE"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "UNSPECIFIED_FAILURE"
            BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
            else -> "UNKNOWN"
        })
        val watts = if (voltage > 0 && currentUa != Int.MIN_VALUE) {
            voltage / 1000.0 * kotlin.math.abs(currentUa) / 1_000_000.0
        } else null
        put("fast_charging_status", when {
            watts == null -> "unsupported"
            watts >= 15.0 -> "active"
            else -> "inactive"
        })
        if (charging && currentUa <= 0) {
            put("note", "القراءة صفر/سالبة أثناء التوصيل: الحساس أو sysfs لا يوفر تياراً مباشراً دقيقاً على هذا الجهاز.")
        } else if (watts != null) {
            put("note", "القدرة التقريبية %.1f W من الفولتية والتيار اللحظي؛ قد تكون مقيدة بحسب الشركة المصنعة والـROM.".format(watts))
        }
        put("unavailable", unavailable)
    }

    private fun storage(context: Context): JSONObject = JSONObject().apply {
        val fs = runCatching { StatFs(context.filesDir.absolutePath) }.getOrNull()
        val unavailable = JSONArray()
        if (fs != null) {
            val total = fs.blockCountLong * fs.blockSizeLong
            val free = fs.availableBlocksLong * fs.blockSizeLong
            put("internal_total_gb", gb(total))
            put("internal_free_gb", gb(free))
        } else {
            unavailable.put("internal_total_gb").put("internal_free_gb")
        }
        val speeds = benchmark(context)
        if (speeds != null) {
            put("internal_read_speed_mbps", speeds.first)
            put("internal_write_speed_mbps", speeds.second)
        } else {
            unavailable.put("internal_read_speed_mbps").put("internal_write_speed_mbps")
        }
        val dirs = context.getExternalFilesDirs(null)
        val sd = dirs.getOrNull(1)
        put("external_sd_present", sd != null)
        if (sd != null) {
            val sfs = runCatching { StatFs(sd.absolutePath) }.getOrNull()
            if (sfs != null) {
                put("external_sd_total_gb", gb(sfs.blockCountLong * sfs.blockSizeLong))
                put("external_sd_free_gb", gb(sfs.availableBlocksLong * sfs.blockSizeLong))
            } else unavailable.put("external_sd_total_gb").put("external_sd_free_gb")
            put("external_sd_health", "unknown")
            unavailable.put("external_sd_health")
        } else {
            put("note", "لا توجد ذاكرة خارجية مكتشفة؛ لم يُطلب من المستخدم أي تفاصيل عنها.")
        }
        var cacheBytes = 0L
        var junkBytes = 0L
        runCatching {
            context.cacheDir.walkTopDown().forEach { f ->
                if (f.isFile) {
                    cacheBytes += f.length()
                    if (f.length() > 20L * 1024 * 1024) junkBytes += f.length()
                }
            }
        }
        put("cache_size_mb", cacheBytes / (1024.0 * 1024.0))
        put("large_junk_files_mb", junkBytes / (1024.0 * 1024.0))
        put("io_errors_detected", false)
        put("unavailable", unavailable)
    }

    private fun benchmark(context: Context): Pair<Double, Double>? = runCatching {
        val file = File(context.cacheDir, "diag_bench.tmp")
        val bytes = ByteArray(1024 * 1024)
        val startW = System.nanoTime()
        file.outputStream().use { out -> repeat(4) { out.write(bytes) } }
        val write = 4.0 / ((System.nanoTime() - startW) / 1e9)
        val startR = System.nanoTime()
        file.inputStream().use { input -> while (input.read(bytes) > 0) Unit }
        val read = 4.0 / ((System.nanoTime() - startR) / 1e9)
        file.delete()
        read to write
    }.getOrNull()

    private fun wifi(context: Context): JSONObject = JSONObject().apply {
        val manager = runCatching {
            context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        }.getOrNull()
        val info = runCatching { manager?.connectionInfo }.getOrNull()
        val unavailable = JSONArray()
        val connected = info != null && info.networkId != -1 && info.ssid != null
        put("connected", connected)
        if (!connected) {
            unavailable.put("ssid_hash").put("frequency_band").put("signal_dbm")
                .put("link_speed_mbps").put("channel")
            put("note", "غير متصل بشبكة واي فاي حالياً.")
        } else {
            val ssid = info.ssid?.trim('"').orEmpty()
            if (ssid.isBlank() || ssid.contains("unknown", ignoreCase = true)) {
                put("ssid_hash", JSONObject.NULL); unavailable.put("ssid_hash")
            } else {
                put("ssid_hash", sha256(ssid).take(16))
            }
            val freq = info.frequency
            put("frequency_band", when {
                freq >= 5900 -> "6GHz"
                freq >= 4900 -> "5GHz"
                freq > 0 -> "2.4GHz"
                else -> "unknown"
            })
            put("signal_dbm", info.rssi)
            put("link_speed_mbps", info.linkSpeed)
            put("channel", when {
                freq in 2412..2472 -> (freq - 2412) / 5 + 1
                freq in 5000..5900 -> (freq - 5000) / 5
                freq >= 5955 -> (freq - 5955) / 5 + 1
                else -> 0
            })
        }
        unavailable.put("channel_congestion").put("packet_loss_percent").put("frequent_disconnects")
        val dns = runCatching {
            val t = System.nanoTime()
            InetAddress.getByName("one.one.one.one")
            ((System.nanoTime() - t) / 1e6).toLong()
        }.getOrNull()
        if (dns != null) put("dns_latency_ms", dns) else { put("dns_latency_ms", JSONObject.NULL); unavailable.put("dns_latency_ms") }
        put("note", "جودة القنوات وفقد الحزم وتكرار الانقطاع تحتاج صلاحيات مسح/سجلاتsystem؛ لم تُخمّن.")
        put("unavailable", unavailable)
    }

    private fun sim(context: Context): JSONObject = JSONObject().apply {
        val telephony = runCatching {
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        }.getOrNull()
        val unavailable = JSONArray()
        if (telephony == null) {
            put("sim_present", false)
            put("note", "لا توجد خدمة اتصالات متاحة على هذا الجهاز.")
            put("unavailable", JSONArray().apply { put("all") })
            return@apply
        }
        val ready = telephony.simState == TelephonyManager.SIM_STATE_READY
        put("sim_present", ready)
        val simType = runCatching {
            val subs = SubscriptionManager.from(context).activeSubscriptionInfoList
            val first = subs?.firstOrNull()
            when {
                first == null -> null
                first.isEmbedded -> "eSIM"
                else -> "physical"
            }
        }.getOrNull()
        if (simType != null) put("sim_type", simType) else { put("sim_type", JSONObject.NULL); unavailable.put("sim_type") }
        val carrier = telephony.networkOperatorName.orEmpty()
        if (carrier.isNotBlank()) put("carrier_name", carrier) else { put("carrier_name", JSONObject.NULL); unavailable.put("carrier_name") }
        val networkType = runCatching { telephony.dataNetworkType }.getOrDefault(TelephonyManager.NETWORK_TYPE_UNKNOWN)
        put("network_type", when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT -> "2G"
            else -> "unknown"
        })
        val dbm = runCatching {
            telephony.allCellInfo?.firstOrNull()?.cellSignalStrength?.dbm
        }.getOrNull()
        if (dbm != null && dbm != Int.MAX_VALUE) put("signal_strength_dbm", dbm)
        else { put("signal_strength_dbm", JSONObject.NULL); unavailable.put("signal_strength_dbm") }
        put("roaming", telephony.isNetworkRoaming)
        put("network_lock_status", "unknown")
        put("note", "قفل الشبكة يحتاج قراءة مباشرة من النظام (مثل *#*#INFO#*#* أو إعدادات الشبكة)؛ لم تُجزَم حالته. قراءة الإشارة تقريبية حسب الـROM.")
        put("unavailable", unavailable)
    }

    private fun general(context: Context, symptoms: List<String>): JSONObject = JSONObject().apply {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val mem = ActivityManager.MemoryInfo()
        val unavailable = JSONArray()
        if (manager != null) {
            manager.getMemoryInfo(mem)
            put("ram_total_mb", mem.totalMem / (1024.0 * 1024.0))
            put("ram_free_mb", mem.availMem / (1024.0 * 1024.0))
        } else {
            unavailable.put("ram_total_mb").put("ram_free_mb")
        }
        val cpu = readCpuTemp()
        if (cpu != null) put("cpu_temp_c", cpu) else { put("cpu_temp_c", JSONObject.NULL); unavailable.put("cpu_temp_c") }
        put("screen_touch_issue_reported", symptoms.any {
            it.contains("لمس") || it.contains("كيبورد") || it.contains("تعليق")
        })
        val processes = runCatching { manager?.runningAppProcesses?.size }.getOrNull()
        if (processes != null) put("running_background_apps_count", processes)
        else { put("running_background_apps_count", JSONObject.NULL); unavailable.put("running_background_apps_count") }
        put("note", "عدد العمليات الخلفية تقريبي، وحرارة المعالج مقيدة بملفات thermal المتاحة للـKernel.")
        put("unavailable", unavailable)
    }

    private fun readCpuTemp(): Double? = runCatching {
        for (i in 0..12) {
            val f = File("/sys/class/thermal/thermal_zone$i/temp")
            if (!f.isFile || !f.canRead()) continue
            val raw = f.readText().trim().toDoubleOrNull() ?: continue
            return@runCatching if (raw > 200) raw / 1000.0 else raw
        }
        null
    }.getOrNull()

    private fun gb(bytes: Long): Double = bytes / 1_000_000_000.0

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
