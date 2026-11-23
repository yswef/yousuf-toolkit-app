@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.adhkar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import java.util.Locale

@Composable
fun AdhkarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val categories = remember { AdhkarRepo.load(context) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<DhikrCategory?>(null) }
    val filtered = if (query.isBlank()) categories else categories.filter { category ->
        category.title.contains(query, true) || category.items.any { it.text.contains(query, true) }
    }
    ScreenScaffold(
        title = selected?.title ?: stringResource(R.string.feature_adhkar),
        onBack = { if (selected != null) selected = null else onBack() }
    ) { padding ->
        val current = selected
        if (current != null) {
            DhikrList(category = current, modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(R.string.adhkar_search)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (filtered.isEmpty()) {
                    item { InfoCard(text = stringResource(R.string.adhkar_none)) }
                }
                items(filtered, key = { it.id }) { category ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(category.title, fontWeight = FontWeight.Bold)
                                Text(
                                    stringResource(R.string.adhkar_items_count, category.items.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { selected = category }) {
                                Text(stringResource(R.string.adhkar_open))
                            }
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.adhkar_source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DhikrList(category: DhikrCategory, modifier: Modifier) {
    val clipboard = LocalClipboardManager.current
    LazyColumn(
        modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(category.items, key = { it.id }) { dhikr ->
            var count by remember(dhikr.id) { mutableIntStateOf(0) }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(dhikr.text, style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (dhikr.count > 1) stringResource(R.string.adhkar_repeat, dhikr.count) else stringResource(R.string.adhkar_once),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (dhikr.count > 1) {
                            Text(
                                String.format(Locale.US, "%d/%d", count.coerceAtMost(dhikr.count), dhikr.count),
                                style = MaterialTheme.typography.labelLarge
                            )
                            TextButton(onClick = { if (count < dhikr.count) count++ }) {
                                Text(stringResource(if (count >= dhikr.count) R.string.adhkar_done else R.string.adhkar_tap))
                            }
                        }
                        TextButton(onClick = { clipboard.setText(AnnotatedString(dhikr.text)) }) {
                            Text(stringResource(R.string.accounts_copy))
                        }
                    }
                }
            }
        }
    }
}
