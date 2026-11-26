// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.home

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.InfoCard
import core.design.V3Group
import core.design.V3QuickAction
import core.design.V3Row
import core.design.V3SectionHeader
import core.navigation.Routes
import core.apps.moodHiddenRoutes
import data.local.LocalStore

private data class ToolLink(val title: Int, val subtitle: Int, val route: String, val icon: ImageVector)

@Composable
fun ToolsTab(onFeature: (String) -> Unit, modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val context = LocalContext.current
    var opening by remember { mutableStateOf<MagicBook?>(null) }
    var query by remember { mutableStateOf("") }
    val hidden = store.hiddenRoutes + moodHiddenRoutes(store.mood)
    val allRoutes = bookSections.flatMap { it.books }.map { it.route }
    val order = (store.routeOrder.filter { it in allRoutes } + allRoutes.filter { it !in store.routeOrder }).distinct()
    val visibleSections = bookSections.mapNotNull { section ->
        val sectionTitle = stringResource(section.title)
        val books = section.books.filter { book ->
            book.route !in hidden && (
                query.isBlank() ||
                    stringResource(book.title).contains(query, ignoreCase = true) ||
                    sectionTitle.contains(query, ignoreCase = true) ||
                    book.route.contains(query, ignoreCase = true)
            )
        }.sortedBy { order.indexOf(it.route) }
        if (books.isEmpty()) null else section.copy(books = books)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "tools-head") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.v3_tools_clear))
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.tools_search)) }
                )
                Text(
                    stringResource(R.string.tools_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (query.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "tools-quick") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    V3QuickAction(
                        stringResource(R.string.feature_water), BookSymbol.DROP,
                        { onFeature(Routes.WATER) }, Modifier.weight(1f)
                    )
                    V3QuickAction(
                        stringResource(R.string.feature_sleep), BookSymbol.MOON,
                        { onFeature(Routes.SLEEP) }, Modifier.weight(1f)
                    )
                    V3QuickAction(
                        stringResource(R.string.feature_faith), BookSymbol.STAR,
                        { onFeature(Routes.FAITH) }, Modifier.weight(1f)
                    )
                    V3QuickAction(
                        stringResource(R.string.feature_library), BookSymbol.ORB,
                        { onFeature(Routes.LIBRARY) }, Modifier.weight(1f)
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "tools-groups") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    V3SectionHeader(title = stringResource(R.string.v3_tools_groups))
                    ToolGroupCard(
                        stringResource(R.string.v3_tools_money),
                        listOf(
                            ToolLink(R.string.feature_wallets, R.string.v3_tools_sub_wallets, Routes.WALLETS, Icons.Filled.ShoppingCart),
                            ToolLink(R.string.feature_money, R.string.v3_tools_sub_advanced, Routes.MONEYPRO, Icons.Filled.CheckCircle)
                        ),
                        onFeature
                    )
                    ToolGroupCard(
                        stringResource(R.string.v3_tools_health),
                        listOf(
                            ToolLink(R.string.feature_life, R.string.v3_tools_sub_life, Routes.HEALTHPRO, Icons.Filled.Person),
                            ToolLink(R.string.feature_workouts, R.string.v3_tools_sub_workouts, Routes.WORKOUTS, Icons.Filled.Info)
                        ),
                        onFeature
                    )
                    ToolGroupCard(
                        stringResource(R.string.v3_tools_device),
                        listOf(
                            ToolLink(R.string.feature_device, R.string.v3_tools_sub_device, Routes.DEVICE, Icons.Filled.Settings),
                            ToolLink(R.string.feature_vault, R.string.v3_tools_sub_vault, Routes.VAULT, Icons.Filled.List)
                        ),
                        onFeature
                    )
                }
            }
        }

        if (visibleSections.isEmpty() && query.isNotBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "tools-none") {
                InfoCard(text = stringResource(R.string.v3_tools_none))
            }
        }

        visibleSections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }, key = "section-${section.title}") {
                V3SectionHeader(title = stringResource(section.title))
            }
            items(items = section.books, key = { it.route }) { book ->
                MagicBookCard(book = book, onClick = { opening = book })
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = "sister-apps") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                V3SectionHeader(title = stringResource(R.string.v3_sister_apps))
                if (store.config.sisterApps.isEmpty()) {
                    InfoCard(text = stringResource(R.string.v3_sister_apps_empty))
                } else {
                    V3Group {
                        store.config.sisterApps.forEachIndexed { index, app ->
                            V3Row(
                                title = app.name,
                                subtitle = app.url,
                                icon = Icons.Filled.Info,
                                divider = index > 0,
                                onClick = {
                                    if (app.url.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.url)))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
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
private fun ToolGroupCard(title: String, links: List<ToolLink>, onFeature: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp))
        V3Group {
            links.forEachIndexed { index, link ->
                V3Row(
                    title = stringResource(link.title),
                    subtitle = stringResource(link.subtitle),
                    icon = link.icon,
                    divider = index > 0,
                    onClick = { onFeature(link.route) }
                )
            }
        }
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
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
