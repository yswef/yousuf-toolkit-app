@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import core.design.SoundFx
import core.design.SwipeActionRow
import kotlinx.coroutines.launch
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Priority
import domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class TaskFilter { ALL, TODAY, UPCOMING, OVERDUE, DONE }

@Composable
fun TasksScreen(onBack: () -> Unit = {}, onMore: () -> Unit = {}, embedded: Boolean = false) {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var query by remember { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val undoLabel = stringResource(R.string.undo)
    val deletedLabel = stringResource(R.string.task_deleted)
    val matches: (Task) -> Boolean = { t ->
        query.isBlank() || t.title.contains(query, ignoreCase = true) ||
            t.note.contains(query, ignoreCase = true) || t.list.contains(query, ignoreCase = true)
    }
    val open = store.tasks.filterNot { it.done }.filter(matches)
    val done = store.tasks.filter { it.done }.filter(matches)

    fun toggle(task: Task) {
        val was = task.done
        if (!store.toggleTask(task.id)) {
            denied = true
            SoundFx.play(SoundFx.Fx.ERROR)
        } else if (!was) {
            SoundFx.play(SoundFx.Fx.SUCCESS)
        } else {
            SoundFx.play(SoundFx.Fx.TAP)
        }
    }

    fun remove(task: Task) {
        store.deleteTask(task.id)
        SoundFx.play(SoundFx.Fx.DELETE)
        scope.launch {
            snack.currentSnackbarData?.dismiss()
            val result = snack.showSnackbar(deletedLabel, actionLabel = undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) store.restoreTask(task)
        }
    }
    val today = LocalDate.now()

    val content: @Composable (PaddingValues) -> Unit = { padding ->
      Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = if (embedded) 96.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.task_summary, open.size, done.size),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.task_energy_cost, store.config.energyCost),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "${if (store.tasks.isEmpty()) 0 else (done.size * 100 / store.tasks.size)}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (store.tasks.isEmpty()) 0f else done.size.toFloat() / store.tasks.size },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel)) }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.search_hint)) }
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        listOf(
                            TaskFilter.ALL to R.string.task_filter_all,
                            TaskFilter.TODAY to R.string.task_filter_today,
                            TaskFilter.UPCOMING to R.string.task_filter_upcoming,
                            TaskFilter.OVERDUE to R.string.task_filter_overdue,
                            TaskFilter.DONE to R.string.task_filter_done
                        ), key = { it.first.name }
                    ) { pair ->
                        FilterChip(
                            selected = filter == pair.first,
                            onClick = { filter = pair.first },
                            label = { Text(stringResource(pair.second)) }
                        )
                    }
                }
            }
            if (store.tasks.isEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            painter = painterResource(R.drawable.empty_tasks),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp))
                        )
                        Text(
                            stringResource(R.string.task_none),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            if (filter == TaskFilter.DONE) {
                items(done.sortedByDescending { it.createdAt }, key = { it.id }) { task ->
                    TaskRow(task, Modifier.animateItem(), onToggle = { toggle(task) }, onDelete = { remove(task) })
                }
            } else {
                val visible = when (filter) {
                    TaskFilter.TODAY -> open.filter { dayOf(it.dueAt) == today }
                    TaskFilter.UPCOMING -> open.filter { dayOf(it.dueAt)?.isAfter(today) == true }
                    TaskFilter.OVERDUE -> open.filter { dayOf(it.dueAt)?.isBefore(today) == true }
                    else -> open
                }
                val buckets = listOf(
                    R.string.taskspro_overdue to visible.filter { dayOf(it.dueAt)?.isBefore(today) == true },
                    R.string.taskspro_today to visible.filter { dayOf(it.dueAt) == today },
                    R.string.taskspro_tomorrow to visible.filter { dayOf(it.dueAt) == today.plusDays(1) },
                    R.string.task_upcoming to visible.filter { dayOf(it.dueAt)?.isAfter(today.plusDays(1)) == true },
                    R.string.taskspro_no_due to visible.filter { it.dueAt <= 0L }
                )
                buckets.forEach { bucket ->
                    if (bucket.second.isNotEmpty()) {
                        item(key = "head-${bucket.first}-${filter.name}") {
                            Text(
                                stringResource(bucket.first),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }
                        items(bucket.second, key = { "t-${it.id}-${filter.name}" }) { task ->
                            TaskRow(task, Modifier.animateItem(), onToggle = { toggle(task) }, onDelete = { remove(task) })
                        }
                    }
                }
            }
        }
        if (embedded) {
            SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
        }
      }
    }
    if (embedded) {
        content(PaddingValues(0.dp))
    } else {
        ScreenScaffold(
            title = stringResource(R.string.feature_tasks),
            onBack = onBack,
            snackbarHost = snack,
            actions = {
                TextButton(onClick = onMore) { Text(stringResource(R.string.tasks_pro)) }
                IconButton(onClick = { adding = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            },
            content = content
        )
    }

    if (adding) {
        TaskDialog(
            onDismiss = { adding = false },
            onAdd = { title, note, priority, dueAt, repeat, list ->
                store.addTask(title, note, priority, dueAt, repeat, list)
            }
        )
    }

    if (denied) {
        AlertDialog(
            onDismissRequest = { denied = false },
            title = { Text(stringResource(R.string.energy_title)) },
            text = { Text(stringResource(R.string.not_enough_energy)) },
            confirmButton = {
                TextButton(onClick = { denied = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }
}

@Composable
private fun TaskRow(task: Task, modifier: Modifier = Modifier, onToggle: () -> Unit, onDelete: () -> Unit) {
  SwipeActionRow(modifier = modifier, onSwipeStart = onToggle, onSwipeEnd = onDelete) {
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .padding(start = 10.dp)
                    .width(4.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor(task.priority))
            )
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
                if (task.note.isNotBlank()) {
                    Text(task.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(priorityLabel(task.priority), style = MaterialTheme.typography.labelSmall, color = priorityColor(task.priority))
                    if (task.dueAt > 0L) Text(dueLabel(task.dueAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (task.repeat.isNotBlank()) Text(task.repeat, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    if (task.list.isNotBlank()) Text("#" + task.list, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
  }
}

@Composable
private fun TaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Priority, Long, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var list by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var due by remember { mutableStateOf<LocalDate?>(null) }

    FormDialog(
        title = stringResource(R.string.task_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            val dueAt = due?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L
            onAdd(title, note, priority, dueAt, "", list)
            onDismiss()
        },
        valid = title.isNotBlank()
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.task_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.task_note)) }, maxLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = list, onValueChange = { list = it }, label = { Text(stringResource(R.string.taskspro_list)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.taskspro_due), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = due == LocalDate.now(), onClick = { due = LocalDate.now() }, label = { Text(stringResource(R.string.taskspro_today)) })
            FilterChip(selected = due == LocalDate.now().plusDays(1), onClick = { due = LocalDate.now().plusDays(1) }, label = { Text(stringResource(R.string.taskspro_tomorrow)) })
            FilterChip(selected = due == null, onClick = { due = null }, label = { Text(stringResource(R.string.taskspro_no_due)) })
        }
        Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.labelLarge)
        Priority.entries.forEach { option ->
            Row(Modifier.fillMaxWidth().clickable { priority = option }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = priority == option, onClick = { priority = option })
                Text(priorityLabel(option))
            }
        }
    }
}

@Composable
private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.LOW -> stringResource(R.string.priority_low)
    Priority.NORMAL -> stringResource(R.string.priority_normal)
    Priority.HIGH -> stringResource(R.string.priority_high)
}

@Composable
private fun priorityColor(priority: Priority) = when (priority) {
    Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    Priority.NORMAL -> MaterialTheme.colorScheme.primary
    Priority.HIGH -> MaterialTheme.colorScheme.error
}

@Composable
private fun dueLabel(at: Long): String {
    val day = dayOf(at) ?: return ""
    val today = LocalDate.now()
    return when (day) {
        today -> stringResource(R.string.taskspro_today)
        today.plusDays(1) -> stringResource(R.string.taskspro_tomorrow)
        else -> day.toString()
    }
}

private fun dayOf(at: Long): LocalDate? =
    if (at <= 0L) null else Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate()
