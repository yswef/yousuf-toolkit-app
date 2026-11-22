package feature.workouts

import core.design.EmptyIllustration
import core.design.rememberUndoHost
import core.design.SwipeActionRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import domain.model.Workout
import domain.model.workoutKeys
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val undo = rememberUndoHost()
    var adding by remember { mutableStateOf(false) }

    val logs = store.workouts.sortedByDescending { it.at }
    val total = store.workouts.sumOf { it.minutes }

    ScreenScaffold(
        title = stringResource(R.string.feature_workouts),
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
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column {
                            Text(
                                stringResource(R.string.workout_total),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(stringResource(R.string.workout_minutes_value, total), style = MaterialTheme.typography.headlineSmall)
                        }
                        Column {
                            Text(
                                stringResource(R.string.workout_count),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(store.workouts.size.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
            if (logs.isEmpty()) {
                item {
                    EmptyIllustration(R.drawable.empty_workouts, stringResource(R.string.workout_none))
                }
            }
            items(logs, key = { it.id }) { log ->
                val remove = { store.deleteWorkout(log.id); undo.deleted { store.restoreWorkout(log) } }
                SwipeActionRow(Modifier.animateItem(), onSwipeEnd = remove) { WorkoutRow(log = log, onDelete = remove) }
            }
        }
    }

    if (adding) {
        WorkoutDialog(
            onDismiss = { adding = false },
            onAdd = { kind, minutes -> store.addWorkout(kind, minutes) }
        )
    }
}

@Composable
private fun WorkoutRow(log: Workout, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(kindLabel(log.kind), style = MaterialTheme.typography.bodyLarge)
                Text(
                    dateText(log.at),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.workout_minutes_value, log.minutes),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var kind by remember { mutableStateOf(workoutKeys.first()) }
    var minutes by remember { mutableStateOf("30") }
    val parsed = minutes.toIntOrNull()

    FormDialog(
        title = stringResource(R.string.workout_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            parsed?.let { onAdd(kind, it) }
            onDismiss()
        },
        valid = parsed != null && parsed > 0
    ) {
        Text(stringResource(R.string.workout_kind), style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(workoutKeys, key = { it }) { key ->
                FilterChip(
                    selected = kind == key,
                    onClick = { kind = key },
                    label = { Text(kindLabel(key)) }
                )
            }
        }
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            label = { Text(stringResource(R.string.workout_minutes)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun kindLabel(key: String): String = when (key) {
    "strength" -> stringResource(R.string.workout_strength)
    "cardio" -> stringResource(R.string.workout_cardio)
    "stretch" -> stringResource(R.string.workout_stretch)
    else -> stringResource(R.string.workout_walk)
}

private fun dateText(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
