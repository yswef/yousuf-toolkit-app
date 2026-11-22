package feature.watch

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.yousuf.toolkit.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

object WatchNotifier {
    private const val CHANNEL = "y-dimension-watch"

    fun post(context: Context, id: Int, title: String, text: String) {
        if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(com.yousuf.toolkit.R.string.watch_channel), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        manager.notify(id, n)
    }
}

object WatchChecker {
    private const val PREFS = "ydim_watch"
    private const val LAST = "last_check"

    /** يفحص العناصر قيد المتابعة ويشعر بالحلقات الجديدة. يعيد عدد الإشعارات. */
    suspend fun run(context: Context, maxItems: Int = 12): Int {
        WatchRepo.init(context)
        var notified = 0
        val targets = WatchRepo.items.toList()
            .filter { it.status == "watching" || it.status == "plan" }
            .filterNot { it.airing == "finished" && it.total > 0 && it.aired >= it.total }
            .take(maxItems)
        for (item in targets) {
            val fresh = WatchApi.refresh(item) ?: continue
            val current = WatchRepo.find(item.key) ?: continue
            val merged = current.copy(
                total = fresh.total, airing = fresh.airing, aired = fresh.aired, nextAt = fresh.nextAt
            )
            WatchRepo.upsert(merged)
            if (merged.status == "watching" && merged.aired > merged.notified) {
                val fresher = merged.aired - merged.notified
                val text = if (fresher == 1) {
                    context.getString(com.yousuf.toolkit.R.string.watch_new_one, merged.aired)
                } else {
                    context.getString(com.yousuf.toolkit.R.string.watch_new_many, fresher, merged.aired)
                }
                WatchNotifier.post(context, merged.key.hashCode(), merged.title, text)
                WatchRepo.upsert(merged.copy(notified = merged.aired))
                notified++
            }
            delay(500)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(LAST, System.currentTimeMillis()).apply()
        return notified
    }

    fun runIfStale(context: Context, minGapMs: Long = 3 * 60 * 60 * 1000L) {
        WatchRepo.init(context)
        if (WatchRepo.items.none { it.status == "watching" }) return
        val last = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(LAST, 0L)
        if (System.currentTimeMillis() - last < minGapMs) return
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch { runCatching { run(app) } }
    }
}

class WatchCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(40_000L) { WatchChecker.run(app, maxItems = 5) }
            } finally {
                pending.finish()
            }
        }
    }
}

object WatchScheduler {
    /** يجدول فحصاً دورياً كل 6 ساعات تقريباً (يُعاد عند فتح التطبيق). */
    fun schedule(context: Context) {
        val app = context.applicationContext
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            app, 7421, Intent(app, WatchCheckReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + 30 * 60 * 1000L,
            6 * AlarmManager.INTERVAL_HOUR,
            pi
        )
    }

    fun ensure(context: Context) {
        WatchRepo.init(context)
        if (WatchRepo.items.any { it.status == "watching" }) {
            schedule(context)
            WatchChecker.runIfStale(context)
        }
    }
}
