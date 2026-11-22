package feature.journal

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.JournalEntry
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val undo = rememberUndoHost()
    var adding by remember { mutableStateOf(false) }
    val entries = store.journal.sortedByDescending { it.at }

    ScreenScaffold(
        title = stringResource(R.string.feature_journal),
        onBack = onBack,
        actions = {
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyIllustration(R.drawable.empty_journal, stringResource(R.string.journal_none), Modifier.padding(padding).padding(8.dp))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val remove = { store.deleteEntry(entry.id); undo.deleted { store.restoreEntry(entry) } }
                    SwipeActionRow(Modifier.animateItem(), onSwipeEnd = remove) { EntryRow(entry = entry, onDelete = remove) }
                }
            }
        }
    }

    if (adding) {
        EntryDialog(
            onDismiss = { adding = false },
            onAdd = { title, body -> store.addEntry(title, body) }
        )
    }
}

@Composable
private fun EntryRow(entry: JournalEntry, onDelete: () -> Unit) {
    val heading = if (entry.title.isBlank()) stringResource(R.string.journal_untitled) else entry.title
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(heading, style = MaterialTheme.typography.titleMedium)
                    Text(
                        dateText(entry.at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            if (entry.body.isNotBlank()) {
                Text(
                    entry.body,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 0.dp, end = 12.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EntryDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    FormDialog(
        title = stringResource(R.string.journal_new),
        confirm = stringResource(R.string.save),
        onDismiss = onDismiss,
        onConfirm = {
            onAdd(title, body)
            onDismiss()
        },
        valid = title.isNotBlank() || body.isNotBlank()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.journal_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text(stringResource(R.string.journal_body)) },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun dateText(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
