package core.device

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.yousuf.toolkit.R
import data.local.Store

class BlockingAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val store = Store.of(this)
        if (pkg !in store.blockedApps) return
        if (!store.isBlockingNow()) return
        performGlobalAction(GLOBAL_ACTION_HOME)
        Toast.makeText(this, R.string.blocker_blocked_toast, Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() = Unit
}
