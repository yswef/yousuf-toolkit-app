package feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import data.local.LocalStore

@Composable
fun ToolsTab(onFeature: (String) -> Unit, modifier: Modifier = Modifier) {
    var opening by remember { mutableStateOf<MagicBook?>(null) }
    var query by remember { mutableStateOf("") }
    val visibleSections = bookSections.mapNotNull { section ->
        val books = section.books.filter { book ->
            query.isBlank() || stringResource(book.title).contains(query, ignoreCase = true)
        }
        if (books.isEmpty()) null else section.copy(books = books)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "tools-head") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.tools_search)) }
                )
                Text(
                    stringResource(R.string.tools_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        visibleSections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "section-${section.title}") {
                Text(
                    stringResource(section.title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(items = section.books, key = { it.route }) { book ->
                MagicBookCard(book = book, onClick = { opening = book })
            }
        }
    }

    opening?.let { book ->
        MagicBookOpen(
            book = book,
            onCancel = { opening = null },
            onOpened = {
                opening = null
                onFeature(book.route)
            }
        )
    }
}

@Composable
fun DataManagement() {
    val store = LocalStore.current
    val context = LocalContext.current
    var status by remember { mutableStateOf<String?>(null) }

    fun read(uri: Uri): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }.getOrNull()

    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(store.backupText().toByteArray()) } != null
            }.getOrDefault(false)
            status = context.getString(if (ok) R.string.tools_backup_done else R.string.tools_backup_failed)
        }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = read(uri)
            val ok = text != null && store.restoreBackup(text)
            status = context.getString(if (ok) R.string.tools_restore_done else R.string.tools_restore_failed)
        }
    }
    val moneyCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = read(uri).orEmpty()
            val count = store.importMoneyCsv(text)
            status = context.getString(if (count > 0) R.string.tools_csv_done else R.string.tools_csv_none, count)
        }
    }
    val taskCsv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = read(uri).orEmpty()
            val count = store.importTaskCsv(text)
            status = context.getString(if (count > 0) R.string.tools_csv_done else R.string.tools_csv_none, count)
        }
    }

            GlassCard {
                Text(
                    stringResource(R.string.tools_data),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.tools_data_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { backup.launch("yousuf-backup.json") }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.tools_backup))
                    }
                    OutlinedButton(
                        onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.tools_restore))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { moneyCsv.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.tools_csv_money))
                    }
                    OutlinedButton(
                        onClick = { taskCsv.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.tools_csv_tasks))
                    }
                }
                Text(
                    stringResource(R.string.tools_csv_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                status?.let { InfoCard(text = it) }
            }
}
