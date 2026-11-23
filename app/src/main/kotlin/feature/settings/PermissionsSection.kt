package feature.settings

import android.Manifest
import android.app.AppOpsManager
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.yousuf.toolkit.R
import core.design.SoundFx
import core.device.Access
import data.local.LocalStore

private const val MISSION_POINTS = 10
private const val ALL_BONUS = 50

private class Perm(
    val key: String,
    @StringRes val title: Int,
    @StringRes val why: Int,
    val runtime: String? = null,
    val special: Boolean = false,
    val granted: (Context) -> Boolean,
    val settings: (Context) -> Intent
)

private fun usageGranted(c: Context): Boolean {
    val ops = c.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= 29)
        ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
    else @Suppress("DEPRECATION") ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun runtimeGranted(c: Context, p: String) =
    ContextCompat.checkSelfPermission(c, p) == PackageManager.PERMISSION_GRANTED

private fun appDetails(c: Context) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${c.packageName}"))

private val perms = listOf(
    Perm("notif", R.string.perm_notif_title, R.string.perm_notif_why, Manifest.permission.POST_NOTIFICATIONS,
        granted = { if (Build.VERSION.SDK_INT >= 33) runtimeGranted(it, Manifest.permission.POST_NOTIFICATIONS) else true },
        settings = { Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, it.packageName) }),
    Perm("alarm", R.string.perm_alarm_title, R.string.perm_alarm_why, special = true,
        granted = { if (Build.VERSION.SDK_INT >= 31) (it.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms() else true },
        settings = { if (Build.VERSION.SDK_INT >= 31) Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${it.packageName}")) else appDetails(it) }),
    Perm("files", R.string.perm_files_title, R.string.perm_files_why, special = true,
        granted = { if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager() else true },
        settings = { if (Build.VERSION.SDK_INT >= 30) Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${it.packageName}")) else appDetails(it) }),
    Perm("usage", R.string.perm_usage_title, R.string.perm_usage_why, special = true,
        granted = ::usageGranted, settings = { Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS) }),
    Perm("access", R.string.perm_access_title, R.string.perm_access_why, special = true,
        granted = Access::accessibility, settings = { Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS) }),
    Perm("listener", R.string.perm_listener_title, R.string.perm_listener_why, special = true,
        granted = Access::notificationListener, settings = { Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }),
    Perm("sms", R.string.perm_sms_title, R.string.perm_sms_why, Manifest.permission.READ_SMS,
        granted = { runtimeGranted(it, Manifest.permission.READ_SMS) }, settings = ::appDetails),
    Perm("contacts", R.string.perm_contacts_title, R.string.perm_contacts_why, Manifest.permission.READ_CONTACTS,
        granted = { runtimeGranted(it, Manifest.permission.READ_CONTACTS) }, settings = ::appDetails),
    Perm("loc", R.string.perm_loc_title, R.string.perm_loc_why, Manifest.permission.ACCESS_FINE_LOCATION,
        granted = { runtimeGranted(it, Manifest.permission.ACCESS_FINE_LOCATION) }, settings = ::appDetails),
    Perm("mic", R.string.perm_mic_title, R.string.perm_mic_why, Manifest.permission.RECORD_AUDIO,
        granted = { runtimeGranted(it, Manifest.permission.RECORD_AUDIO) }, settings = ::appDetails)
)

@Composable
fun PermissionsSection() {
    val context = LocalContext.current
    val store = LocalStore.current
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }

    @Suppress("UNUSED_EXPRESSION") tick
    val states = perms.map { it to it.granted(context) }
    LaunchedEffect(tick) {
        states.forEach { (p, ok) -> if (ok && store.claimMission("perm_${p.key}", MISSION_POINTS)) SoundFx.play(SoundFx.Fx.COIN) }
        if (states.all { it.second } && store.claimMission("perm_all", ALL_BONUS)) SoundFx.play(SoundFx.Fx.LEVEL_UP)
    }

    val done = states.count { it.second }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.perms_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.perms_summary, done, perms.size, MISSION_POINTS, ALL_BONUS),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        states.forEach { (p, ok) ->
            val claimed = store.missionClaimed("perm_${p.key}")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(p.title), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(p.why), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (claimed) Text(stringResource(R.string.perms_claimed, MISSION_POINTS), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (ok) {
                        OutlinedButton(onClick = { runCatching { context.startActivity(p.settings(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }) { Text(stringResource(R.string.perms_manage)) }
                    } else {
                        Button(onClick = {
                            val r = p.runtime
                            if (r != null && !p.special) {
                                launcher.launch(r)
                            } else runCatching { context.startActivity(p.settings(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        }) { Text(stringResource(R.string.perms_enable)) }
                    }
                }
            }
        }
    }
}
