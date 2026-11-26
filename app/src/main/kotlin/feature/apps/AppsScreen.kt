// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.apps.AppEntry
import core.apps.isSocial
import core.apps.moodHiddenPackages
import core.apps.moodLabels
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.V3Chip
import data.local.LocalStore

@Composable
fun AppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = LocalStore.current
    val all = remember { installed(context) }
    var launcherOn by remember { mutableStateOf(launcherEnabled(context)) }
    val hidden = store.hiddenApps + moodHiddenPackages(store.mood, all.map { it.packageName })
    val visible = all.filter { it.packageName !in hidden }
    val hiddenList = all.filter { it.packageName in hidden }

    ScreenScaffold(title = stringResource(R.string.apps_title), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                InfoCard(text = stringResource(R.string.apps_sub), tone = InfoTone.INFO)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    moodLabels.forEach { (key, label) ->
                        V3Chip(label, store.mood == key, { store.updateMood(key) }, Modifier.weight(1f))
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.apps_launcher), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.apps_launcher_hint), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = {
                            launcherOn = toggleLauncher(context, !launcherOn)
                        }) { Text(if (launcherOn) "تعطيل" else "تفعيل") }
                    }
                }
            }
            item { Text(stringResource(R.string.apps_hidden, hidden.size), style = MaterialTheme.typography.titleMedium) }
            if (all.isEmpty()) {
                item { InfoCard(text = stringResource(R.string.apps_none), tone = InfoTone.WARN) }
            }
            items(visible, key = { it.packageName }) { app -> AppRow(app, false) { store.toggleHiddenApp(app.packageName) } }
            if (hiddenList.isNotEmpty()) {
                item { Text(stringResource(R.string.visibility_hidden), style = MaterialTheme.typography.titleMedium) }
                items(hiddenList, key = { it.packageName }) { app -> AppRow(app, true) { store.toggleHiddenApp(app.packageName) } }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppEntry, hidden: Boolean, onToggle: () -> Unit) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(app.name, style = MaterialTheme.typography.bodyLarge)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                if (isSocial(app.packageName)) Text("تواصل اجتماعي", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(onClick = {
                runCatching {
                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
                }
            }) { Text(stringResource(R.string.apps_open)) }
            OutlinedButton(onClick = onToggle) {
                Text(stringResource(if (hidden) R.string.apps_show else R.string.apps_hide))
            }
        }
    }
}

@Composable
fun LauncherHome(onExit: () -> Unit = {}) {
    val context = LocalContext.current
    val store = LocalStore.current
    val all = remember { installed(context) }
    val hidden = store.hiddenApps + moodHiddenPackages(store.mood, all.map { it.packageName })
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Y- Dimensions", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth()) {
            moodLabels.forEach { (key, label) ->
                V3Chip(label, store.mood == key, { store.updateMood(key) }, Modifier.weight(1f))
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(all.filter { it.packageName !in hidden }, key = { it.packageName }) { app ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(app.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = {
                            runCatching {
                                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
                            }
                        }) { Text(stringResource(R.string.apps_open)) }
                    }
                }
            }
        }
        OutlinedButton(onClick = onExit) { Text(stringResource(R.string.common_close)) }
    }
}

private fun installed(context: Context): List<AppEntry> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    return resolved.mapNotNull { r ->
        val pkg = r.activityInfo?.packageName ?: return@mapNotNull null
        if (pkg == context.packageName) return@mapNotNull null
        val name = runCatching { pm.getApplicationLabel(r.activityInfo.applicationInfo).toString() }.getOrDefault(pkg)
        AppEntry(name, pkg, false)
    }.distinctBy { it.packageName }.sortedBy { it.name }
}

private fun launcherEnabled(context: Context): Boolean =
    context.packageManager.getComponentEnabledSetting(
        ComponentName(context, "feature.apps.LauncherActivity")
    ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

private fun toggleLauncher(context: Context, enable: Boolean): Boolean {
    val component = ComponentName(context, "feature.apps.LauncherActivity")
    context.packageManager.setComponentEnabledSetting(
        component,
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
    return enable
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
