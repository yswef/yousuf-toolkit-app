package feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.SleepLog
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }

    val today = store.sleepToday()
    val goal = store.config.sleepGoal.toFloat()
    val ratio = if (goal > 0f) (today.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val average = store.sleep.take(7).map { it.hours }.average().takeIf { !it.isNaN() } ?: 0.0
    val logs = store.sleep.sortedByDescending { it.at }

    ScreenScaffold(
        title = stringResource(R.string.feature_sleep),
        onBack = onBack,
        actions = {
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(R.string.sleep_today), style = MaterialTheme.typography.labelLarge)
                        Text(
                            stringResource(R.string.sleep_hours_value, hoursText(today)),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(R.string.sleep_goal, store.config.sleepGoal),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.sleep_average, hoursText(average)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (logs.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.sleep_none),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(logs, key = { it.id }) { log ->
                SleepRow(log = log, onDelete = { store.deleteSleep(log.id) })
            }
        }
    }

    if (adding) {
        SleepDialog(
            onDismiss = { adding = false },
            onAdd = { hours -> store.addSleep(hours) }
        )
    }
}

@Composable
private fun SleepRow(log: SleepLog, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.sleep_hours_value, hoursText(log.hours)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    dateText(log.at),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun SleepDialog(onDismiss: () -> Unit, onAdd: (Double) -> Unit) {
    var hours by remember { mutableStateOf("") }
    val parsed = hours.toDoubleOrNull()

    FormDialog(
        title = stringResource(R.string.sleep_new),
        confirm = stringResource(R.string.save),
        onDismiss = onDismiss,
        onConfirm = {
            parsed?.let { onAdd(it) }
            onDismiss()
        },
        valid = parsed != null && parsed > 0.0 && parsed <= 24.0
    ) {
        OutlinedTextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text(stringResource(R.string.sleep_hours)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun hoursText(hours: Double): String = String.format(Locale.US, "%.1f", hours)

private fun dateText(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
