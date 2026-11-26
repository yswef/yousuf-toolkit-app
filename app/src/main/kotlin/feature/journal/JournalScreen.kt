// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.text.Html
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
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
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onBack: () -> Unit, onAdd: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val undo = rememberUndoHost()
    var adding by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf("") }
    val entries = store.journal.sortedByDescending { it.at }

    val sync = {
        scope.launch {
            val result = runCatching { JournalSync.push(store) }
            syncStatus = result.fold(
                onSuccess = { count -> context.getString(R.string.journal_sync_done, count) },
                onFailure = { context.getString(R.string.journal_sync_failed) }
            )
        }
    }

    ScreenScaffold(
        title = stringResource(R.string.feature_journal),
        onBack = onBack,
        actions = {
            TextButton(onClick = sync) { Text(stringResource(R.string.journal_sync_now)) }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MoodInsight(entries)
            if (syncStatus.isNotBlank()) Text(syncStatus, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
            if (entries.isEmpty()) {
                EmptyIllustration(R.drawable.empty_journal, stringResource(R.string.journal_none), Modifier.padding(8.dp))
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
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
    }
}

@Composable
private fun MoodInsight(entries: List<JournalEntry>) {
    val timed = entries.filter { it.hasTime && it.mood.isNotBlank() }
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            if (timed.isEmpty()) {
                Text(stringResource(R.string.journal_mood_empty), style = MaterialTheme.typography.bodySmall)
            } else {
                val mood = timed.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key.orEmpty()
                val hour = timed.groupingBy { Instant.ofEpochMilli(it.at).atZone(ZoneId.systemDefault()).hour }.eachCount().maxByOrNull { it.value }?.key ?: 0
                Text(stringResource(R.string.journal_mood_insight, mood, String.format(Locale.US, "%02d:00", hour)))
            }
        }
    }
}

@Composable
private fun EntryRow(entry: JournalEntry, onDelete: () -> Unit) {
    val heading = if (entry.title.isBlank()) stringResource(R.string.journal_untitled) else entry.title
    val names = stringArrayResource(R.array.journal_background_names)
    val backgroundIndex = JournalAssets.backgrounds.indexOfFirst { it.id == entry.background }.coerceAtLeast(0)
    val body = if (entry.richBody.isNotBlank()) Html.fromHtml(entry.richBody, Html.FROM_HTML_MODE_LEAN).toString() else entry.body
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(heading, style = MaterialTheme.typography.titleMedium)
                    Text(dateText(entry.at, entry.hasTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (entry.mood.isNotBlank()) Text(stringResource(R.string.journal_mood_value, entry.mood), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.journal_background_value, names.getOrNull(backgroundIndex).orEmpty()), style = MaterialTheme.typography.bodySmall)
                    if (entry.voicePath.isNotBlank()) Text(stringResource(R.string.journal_voice_attached), style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            if (body.isNotBlank()) {
                Text(body, style = MaterialTheme.typography.bodyMedium, maxLines = 6, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 12.dp, top = 4.dp))
            }
        }
    }
}

private fun dateText(at: Long, hasTime: Boolean): String {
    val value = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return if (hasTime) value.toString().replace('T', ' ') else value.toLocalDate().toString()
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
