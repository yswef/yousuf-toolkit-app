package feature.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Priority
import domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(onBack: () -> Unit, onMore: () -> Unit = {}) {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }

    val open = store.tasks.filterNot { it.done }.sortedByDescending { it.createdAt }
    val done = store.tasks.filter { it.done }.sortedByDescending { it.createdAt }

    ScreenScaffold(
        title = stringResource(R.string.feature_tasks),
        onBack = onBack,
        actions = {
            TextButton(onClick = onMore) {
                Text(stringResource(R.string.taskspro_calendar))
            }
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
                Text(
                    stringResource(R.string.task_energy_cost, store.config.energyCost),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (store.tasks.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.task_none),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(open, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onToggle = { if (!store.toggleTask(task.id)) denied = true },
                    onDelete = { store.deleteTask(task.id) }
                )
            }
            if (done.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.task_done_section),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                items(done, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { if (!store.toggleTask(task.id)) denied = true },
                        onDelete = { store.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (adding) {
        TaskDialog(
            onDismiss = { adding = false },
            onAdd = { title, note, priority -> store.addTask(title, note, priority) }
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
private fun TaskRow(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
                if (task.note.isNotBlank()) {
                    Text(
                        task.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    priorityLabel(task.priority),
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor(task.priority)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
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
private fun TaskDialog(onDismiss: () -> Unit, onAdd: (String, String, Priority) -> Unit) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }

    FormDialog(
        title = stringResource(R.string.task_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            onAdd(title, note, priority)
            onDismiss()
        },
        valid = title.isNotBlank()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.task_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.task_note)) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.labelLarge)
        Priority.entries.forEach { option ->
            Row(
                Modifier.fillMaxWidth().clickable { priority = option },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = priority == option, onClick = { priority = option })
                Text(priorityLabel(option))
            }
        }
    }
}
