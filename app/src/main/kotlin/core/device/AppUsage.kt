package core.device

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import java.time.LocalDate
import java.time.ZoneId

data class UsageEntry(val pkg: String, val label: String, val minutes: Int)

object AppUsage {
    fun hasAccess(context: Context): Boolean {
        val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = manager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun today(context: Context): List<UsageEntry> {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, System.currentTimeMillis())
            ?: return emptyList()
        val packageManager = context.packageManager
        return stats.asSequence()
            .filter { it.totalTimeInForeground > 60_000L }
            .groupBy { it.packageName }
            .map { entry ->
                val minutes = (entry.value.sumOf { it.totalTimeInForeground } / 60_000L).toInt()
                UsageEntry(entry.key, label(packageManager, entry.key), minutes)
            }
            .sortedByDescending { it.minutes }
    }

    fun label(packageManager: PackageManager, pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)
}
