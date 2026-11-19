package core.device

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object Access {
    fun notificationListener(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        return enabled.split(':').any {
            ComponentName.unflattenFromString(it)?.packageName == context.packageName
        }
    }

    fun accessibility(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val expected = ComponentName(context, BlockingAccessibilityService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun installedApps(context: Context): List<Pair<String, String>> {
        val manager = context.packageManager
        return manager.getInstalledApplications(0)
            .asSequence()
            .filter { manager.getLaunchIntentForPackage(it.packageName) != null }
            .map { it.packageName to AppUsage.label(manager, it.packageName) }
            .sortedBy { it.second.lowercase() }
            .toList()
    }
}
