// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.device

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import data.local.Store

class NotificationCollectorService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification: Notification = sbn.notification
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.map { it?.toString().orEmpty() }
            .orEmpty()
        val bodies = listOf(text, bigText).plus(lines).filter { it.isNotBlank() }.distinct()
        if (title.isBlank() && bodies.isEmpty()) return
        val combined = bodies.joinToString("\n")
        val store = Store.of(this)
        store.addNotification(sbn.packageName, title, combined)
        val parsed = WalletTxParser.parseAll(
            context = this,
            source = "notification",
            sender = sbn.packageName,
            title = title,
            text = combined,
            at = sbn.postTime
        )
        if (parsed.isNotEmpty()) store.importSms(parsed)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) = Unit
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
