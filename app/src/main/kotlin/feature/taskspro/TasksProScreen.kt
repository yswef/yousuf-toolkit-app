@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.taskspro

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.EmptyState
import core.design.InfoCard
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Habit
import domain.model.Priority
import domain.model.Task
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TasksProScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    val tabs = listOf(
        R.string.taskspro_calendar,
        R.string.taskspro_matrix,
        R.string.taskspro_pomodoro,
        R.string.taskspro_habits,
        R.string.taskspro_lists
    )
    ScreenScaffold(
        title = stringResource(R.string.feature_tasks_more),
        onBack = onBack,
        actions = { TextButton(onClick = { adding = true }) { Text(stringResource(R.string.add)) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
                }
            }
            when (tab) {
                0 -> CalendarTab()
                1 -> MatrixTab()
                2 -> PomodoroTab()
                3 -> HabitsTab()
                else -> ListsTab()
            }
        }
    }
    if (adding) TaskDialog(onDismiss = { adding = false })
}

@Composable
private fun CalendarTab() {
    val store = LocalStore.current
    val today = LocalDate.now()
    val days = (0..6).map { today.plusDays(it.toLong()) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(days) { day ->
            val due = store.tasks.filter { !it.done && dayOf(it.dueAt) == day }
            val undated = if (day == today) store.tasks.filter { !it.done && it.dueAt == 0L } else emptyList()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(dayText(day), fontWeight = FontWeight.Bold)
                    if (due.isEmpty() && undated.isEmpty()) {
                        Text(stringResource(R.string.taskspro_no_due), style = MaterialTheme.typography.bodySmall)
                    }
                    (due + undated).forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.done, onCheckedChange = { store.toggleTask(task.id) })
                            Column(Modifier.weight(1f)) {
                                Text(task.title)
                                if (task.repeat.isNotBlank()) Text(repeatText(task.repeat), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixTab() {
    val store = LocalStore.current
    val open = store.tasks.filterNot { it.done }
    val quadrants = listOf(
        R.string.taskspro_quadrant_urgent to open.filter { isUrgent(it) && it.priority == Priority.HIGH },
        R.string.taskspro_quadrant_plan to open.filter { !isUrgent(it) && it.priority == Priority.HIGH },
        R.string.taskspro_quadrant_delegate to open.filter { isUrgent(it) && it.priority != Priority.HIGH },
        R.string.taskspro_quadrant_later to open.filter { !isUrgent(it) && it.priority != Priority.HIGH }
    )
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(quadrants) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(item.first), fontWeight = FontWeight.Bold)
                    if (item.second.isEmpty()) Text(stringResource(R.string.taskspro_empty), style = MaterialTheme.typography.bodySmall)
                    item.second.forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = false, onCheckedChange = { store.toggleTask(task.id) })
                            Text(task.title, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroTab() {
    var workMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }
    var seconds by remember { mutableStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(true) }
    var rounds by remember { mutableStateOf(0) }
    LaunchedEffect(running, working) {
        while (running) {
            delay(1000)
            if (seconds > 1) {
                seconds -= 1
            } else {
                if (working) rounds++
                working = !working
                seconds = (if (working) workMinutes else breakMinutes) * 60
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(
            progress = { seconds.toFloat() / ((if (working) workMinutes else breakMinutes) * 60).coerceAtLeast(1) },
            modifier = Modifier.padding(24.dp)
        )
        Text(formatTime(seconds), style = MaterialTheme.typography.displayMedium)
        Text(if (working) stringResource(R.string.taskspro_work_time, workMinutes) else stringResource(R.string.taskspro_break_time, breakMinutes))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                if (running) running = false else {
                    if (seconds <= 0) seconds = (if (working) workMinutes else breakMinutes) * 60
                    running = true
                }
            }) { Text(stringResource(if (running) R.string.taskspro_pause else R.string.taskspro_start)) }
            OutlinedButton(onClick = {
                running = false
                working = true
                seconds = workMinutes * 60
            }) { Text(stringResource(R.string.taskspro_reset)) }
        }
        Text(stringResource(R.string.taskspro_rounds, rounds))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { workMinutes = (workMinutes - 5).coerceAtLeast(5); seconds = workMinutes * 60 }) { Text("-5") }
            OutlinedButton(onClick = { workMinutes = (workMinutes + 5).coerceAtMost(90); seconds = workMinutes * 60 }) { Text("+5") }
        }
    }
}

@Composable
private fun HabitsTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.taskspro_habits), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.taskspro_habit_add)) }
            }
        }
        if (store.habits.isEmpty()) item { InfoCard(text = stringResource(R.string.taskspro_empty)) }
        items(store.habits, key = { it.id }) { habit ->
            HabitCard(habit = habit, onCheck = { store.checkHabit(habit.id) }, onDelete = { store.deleteHabit(habit.id) })
        }
    }
    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.taskspro_habit_add)) },
            text = { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.moneypro_title)) }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = title.isNotBlank(), onClick = {
                    store.addHabit(title)
                    title = ""
                    adding = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun HabitCard(habit: Habit, onCheck: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(habit.title, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.taskspro_habit_streak, habit.streak))
            Text(stringResource(R.string.taskspro_habit_best, habit.best), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onCheck) { Text(stringResource(R.string.taskspro_habit_check)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun ListsTab() {
    val store = LocalStore.current
    val noList = stringResource(R.string.taskspro_no_due)
    val groups = store.tasks.filterNot { it.done }.groupBy { it.list.ifBlank { noList } }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (groups.isEmpty()) item { InfoCard(text = stringResource(R.string.taskspro_empty)) }
        items(groups.keys.toList()) { key ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(key, fontWeight = FontWeight.Bold)
                    groups[key].orEmpty().forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = false, onCheckedChange = { store.toggleTask(task.id) })
                            Column(Modifier.weight(1f)) {
                                Text(task.title)
                                if (task.dueAt > 0L) Text(dayText(dayOf(task.dueAt)), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskDialog(onDismiss: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var repeat by remember { mutableStateOf("") }
    var list by remember { mutableStateOf("") }
    var due by remember { mutableStateOf<LocalDate?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.moneypro_title)) }, singleLine = true) }
                item { OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.moneypro_detail)) }, maxLines = 3) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Priority.values().toList()) { value ->
                            FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value.name) })
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = {
                        val base = due ?: LocalDate.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                due = LocalDate.of(year, month + 1, day)
                            },
                            base.year,
                            base.monthValue - 1,
                            base.dayOfMonth
                        ).show()
                    }) {
                        Text(due?.let { dayText(it) } ?: stringResource(R.string.taskspro_due))
                    }
                }
                item { OutlinedTextField(value = list, onValueChange = { list = it }, label = { Text(stringResource(R.string.taskspro_list)) }, singleLine = true) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("", "daily", "weekly", "monthly")) { value ->
                            FilterChip(selected = repeat == value, onClick = { repeat = value }, label = { Text(if (value.isBlank()) stringResource(R.string.accounts_all) else repeatText(value)) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                val dueAt = due?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L
                store.addTask(title, note, priority, dueAt, repeat, list)
                onDismiss()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun repeatText(value: String): String = stringResource(
    when (value) {
        "daily" -> R.string.taskspro_repeat_daily
        "weekly" -> R.string.taskspro_repeat_weekly
        "monthly" -> R.string.taskspro_repeat_monthly
        else -> R.string.accounts_all
    }
)

private fun dayOf(at: Long): LocalDate? =
    if (at <= 0L) null else Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate()

private fun isUrgent(task: Task): Boolean {
    if (task.dueAt <= 0L) return false
    return task.dueAt <= System.currentTimeMillis() + 2L * 86_400_000L
}

@Composable
private fun dayText(day: LocalDate?): String {
    if (day == null) return stringResource(R.string.taskspro_no_due)
    val today = LocalDate.now()
    return when (day) {
        today -> stringResource(R.string.taskspro_today)
        today.plusDays(1) -> stringResource(R.string.taskspro_tomorrow)
        else -> day.toString()
    }
}

private fun formatTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}
