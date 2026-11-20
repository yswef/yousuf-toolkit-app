@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.alarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
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
import core.design.InfoCard
import core.design.ScreenScaffold
import core.device.Alarms
import data.local.LocalStore
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AlarmScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalTime.now().plusMinutes(5)) }
    var status by remember { mutableStateOf("") }
    val alarms = store.extras.filter { it.kind == "alarm" }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) status = context.getString(R.string.alarm_no_permission)
    }
    fun ensurePermission(): Boolean {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return false
        }
        return true
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.feature_alarm), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.alarm_title)) }, singleLine = true)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            TimePickerDialog(context, { _, hour, minute -> time = LocalTime.of(hour, minute) }, time.hour, time.minute, true).show()
                        }) { Text(stringResource(R.string.alarm_time) + " " + time.format(DateTimeFormatter.ofPattern("HH:mm"))) }
                    }
                    OutlinedButton(onClick = {
                        if (!ensurePermission()) return@OutlinedButton
                        val base = LocalDate.now().atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val at = if (base > System.currentTimeMillis()) base else base + 86_400_000L
                        if (alarmAt(at)) {
                            val id = store.addAlarm(title.ifBlank { context.getString(R.string.alarm_default) }, "", at)
                            Alarms.schedule(context, id, at, title.ifBlank { context.getString(R.string.alarm_default) }, "")
                            status = context.getString(R.string.alarm_added)
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.alarm_schedule)) }
                    if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.alarm_smart), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.alarm_smart_hint), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = {
                        if (!ensurePermission()) return@OutlinedButton
                        val at = LocalDate.now().atTime(20, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val target = if (at <= System.currentTimeMillis()) at + 86_400_000L else at
                        val text = context.getString(R.string.alarm_summary, store.tasks.count { !it.done }, store.water[store.today] ?: 0, store.config.sleepGoal)
                        if (alarmAt(target)) {
                            val id = store.addAlarm(context.getString(R.string.alarm_smart), text, target)
                            Alarms.schedule(context, id, target, context.getString(R.string.alarm_smart), text)
                            status = context.getString(R.string.alarm_smart_done)
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.alarm_smart_schedule)) }
                }
            }
        }
        if (alarms.isEmpty()) item { InfoCard(text = stringResource(R.string.alarm_none)) }
        items(alarms, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title)
                        Text(formatAt(item.dueAt), style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        Alarms.cancel(context, item.id)
                        store.deleteExtra(item.id)
                    }) { Text(stringResource(R.string.alarm_delete)) }
                }
            }
        }
    }
}

private fun alarmAt(at: Long): Boolean = at > System.currentTimeMillis()

private fun formatAt(at: Long): String =
    java.time.Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
