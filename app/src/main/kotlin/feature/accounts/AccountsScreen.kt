@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.accounts

import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import core.security.ImportedAccount
import core.security.PasswordTools
import core.security.Totp
import data.local.LocalStore
import domain.model.AccountDraft
import domain.model.AccountItem
import domain.model.accountCategories
import domain.model.accountLayers
import domain.model.relationKinds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    var tab by rememberSaveable { mutableStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AccountItem?>(null) }
    var relating by remember { mutableStateOf(false) }
    val tabs = listOf(
        R.string.accounts_tab_list,
        R.string.accounts_tab_codes,
        R.string.accounts_tab_security,
        R.string.accounts_tab_devices,
        R.string.accounts_tab_shots,
        R.string.accounts_tab_notes
    )
    ScreenScaffold(
        title = stringResource(R.string.feature_accounts),
        onBack = onBack,
        actions = {
            IconButton(onClick = { editing = null; adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.accounts_add))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
                }
            }
            when (tab) {
                0 -> AccountListTab(
                    onEdit = { editing = it; adding = true },
                    onRelate = { relating = true }
                )
                1 -> CodesTab()
                2 -> SecurityTab()
                3 -> DevicesTab()
                4 -> ShotsTab()
                else -> NotesTab()
            }
        }
    }
    if (adding) {
        AccountDialog(initial = editing, onDismiss = { adding = false; editing = null })
    }
    if (relating) {
        RelationDialog(onDismiss = { relating = false })
    }
}

@Composable
private fun AccountListTab(onEdit: (AccountItem) -> Unit, onRelate: () -> Unit) {
    val store = LocalStore.current
    var query by remember { mutableStateOf("") }
    var layer by remember { mutableStateOf("all") }
    var category by remember { mutableStateOf("all") }
    val filtered = store.accounts.filter { account ->
        (query.isBlank() || account.title.contains(query, true) || account.email.contains(query, true) || account.username.contains(query, true)) &&
            (layer == "all" || account.layer == layer) &&
            (category == "all" || account.category == category)
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.accounts_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(selected = layer == "all", onClick = { layer = "all" }, label = { Text(stringResource(R.string.accounts_all)) })
                }
                items(accountLayers) { value ->
                    FilterChip(selected = layer == value, onClick = { layer = value }, label = { Text(layerLabel(value)) })
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(selected = category == "all", onClick = { category = "all" }, label = { Text(stringResource(R.string.accounts_all)) })
                }
                items(accountCategories) { value ->
                    FilterChip(selected = category == value, onClick = { category = value }, label = { Text(categoryLabel(value)) })
                }
            }
        }
        if (store.accounts.isEmpty()) {
            item { InfoCard(text = stringResource(R.string.accounts_empty)) }
        }
        items(filtered, key = { it.id }) { account ->
            AccountCard(account = account, onEdit = { onEdit(account) })
        }
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            RelationSection(onAdd = onRelate)
        }
    }
}

@Composable
private fun AccountCard(account: AccountItem, onEdit: () -> Unit) {
    val store = LocalStore.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var reveal by remember(account.id) { mutableStateOf(false) }
    val password = remember(account.id, account.updatedAt) { store.accountPassword(account) }
    val totpSecret = remember(account.id, account.updatedAt) { store.accountTotp(account) }
    val code = remember(account.id, totpSecret) { if (totpSecret.isBlank()) null else Totp.code(totpSecret) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(account.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (account.email.isNotBlank()) Text(account.email, style = MaterialTheme.typography.bodySmall)
                    if (account.username.isNotBlank()) Text(account.username, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(layerLabel(account.layer), style = MaterialTheme.typography.labelSmall)
                    Text(categoryLabel(account.category), style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (password.isBlank()) stringResource(R.string.accounts_secret_missing)
                    else if (reveal) password else "••••••••••••",
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.Monospace
                )
                if (password.isNotBlank()) {
                    TextButton(onClick = { reveal = !reveal }) {
                        Text(stringResource(if (reveal) R.string.accounts_hide else R.string.accounts_show))
                    }
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(password))
                        android.widget.Toast.makeText(context, context.getString(R.string.accounts_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }) { Text(stringResource(R.string.accounts_copy)) }
                }
            }
            if (code != null) {
                Text("TOTP  $code", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text(stringResource(R.string.accounts_edit)) }
                TextButton(onClick = { store.deleteAccount(account.id) }) { Text(stringResource(R.string.delete)) }
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.accounts_days_ago, daysSince(account.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RelationSection(onAdd: () -> Unit) {
    val store = LocalStore.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.accounts_relations), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onAdd) { Text(stringResource(R.string.accounts_relation_add)) }
        }
        if (store.accountRelations.isEmpty()) {
            Text(stringResource(R.string.accounts_relation_empty), style = MaterialTheme.typography.bodySmall)
        }
        store.accountRelations.forEach { relation ->
            val from = store.accounts.firstOrNull { it.id == relation.fromId }?.title ?: relation.fromId.toString()
            val to = store.accounts.firstOrNull { it.id == relation.toId }?.title ?: relation.toId.toString()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("$from ↔ $to", modifier = Modifier.weight(1f))
                Text(relationLabel(relation.kind), style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = { store.deleteAccountRelation(relation.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun CodesTab() {
    val store = LocalStore.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val withCodes = store.accounts.filter { store.accountTotp(it).isNotBlank() }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (withCodes.isEmpty()) {
            item { InfoCard(text = stringResource(R.string.accounts_codes_empty)) }
        }
        items(withCodes, key = { it.id }) { account ->
            val secret = store.accountTotp(account)
            val code = Totp.code(secret, now) ?: "------"
            val seconds = Totp.secondsLeft(now)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(account.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        code.chunked(3).joinToString(" "),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.accounts_code_valid, seconds), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SecurityTab() {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var length by remember { mutableStateOf(18f) }
    var symbols by remember { mutableStateOf(true) }
    var generated by remember { mutableStateOf(PasswordTools.generate(18, true)) }
    var status by remember { mutableStateOf("") }
    var scanNote by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val plain = store.accounts.map { it to store.accountPassword(it) }
    val weak = plain.count { it.second.isNotBlank() && PasswordTools.strength(it.second) < 3 }
    val reused = plain.filter { it.second.isNotBlank() }.groupBy { it.second }.count { it.value.size > 1 }
    val old = store.accounts.count { daysSince(it.updatedAt) >= 180 }
    val noRecovery = store.accounts.count { it.recoveryEmail.isBlank() && it.recoveryPhone.isBlank() }
    val noTotp = store.accounts.count { it.totp.isNullOrBlank() }
    val score = (100 - weak * 8 - reused * 10 - old * 5 - noRecovery * 6 - noTotp * 4).coerceIn(0, 100)
    val rows = plain.map { (account, password) ->
        ImportedAccount(account.title, account.username, password, account.email)
    }

    fun exportCsv(uri: android.net.Uri?) {
        if (uri == null) return
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(PasswordTools.csv(rows).toByteArray(Charsets.UTF_8))
                    } ?: error("no stream")
                }.isSuccess
            }
            status = context.getString(if (ok) R.string.accounts_export_done else R.string.accounts_export_fail)
        }
    }

    fun exportHtml(uri: android.net.Uri?) {
        if (uri == null) return
        scope.launch {
            val html = buildString {
                append("<html><head><meta charset=\"utf-8\"><title>Y-Dimension</title></head><body>")
                append("<h1>").append("Y-Dimension").append("</h1><table border=\"1\" cellspacing=\"0\" cellpadding=\"6\">")
                append("<tr><th>Name</th><th>Url</th><th>Username</th><th>Password</th></tr>")
                rows.forEach {
                    append("<tr><td>").append(escape(it.title)).append("</td><td>").append(escape(it.url))
                        .append("</td><td>").append(escape(it.username)).append("</td><td>").append(escape(it.password)).append("</td></tr>")
                }
                append("</table></body></html>")
            }
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(html.toByteArray()) } }
                    .isSuccess
            }
            status = context.getString(if (ok) R.string.accounts_export_done else R.string.accounts_export_fail)
        }
    }

    fun exportPdf(uri: android.net.Uri?) {
        if (uri == null) return
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val doc = android.graphics.pdf.PdfDocument()
                    val paint = android.graphics.Paint().apply {
                        textSize = 10f
                        color = android.graphics.Color.BLACK
                    }
                    var pageNumber = 1
                    var page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                    var y = 36f
                    val lines = ArrayList<String>()
                    rows.forEach {
                        lines.add(it.title + " | " + it.username + " | " + it.password)
                    }
                    lines.forEach { line ->
                        if (y > 800f) {
                            doc.finishPage(page)
                            pageNumber++
                            page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                            y = 36f
                        }
                        page.canvas.drawText(line, 28f, y, paint)
                        y += 16f
                    }
                    doc.finishPage(page)
                    context.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }
                    doc.close()
                }.isSuccess
            }
            status = context.getString(if (ok) R.string.accounts_export_done else R.string.accounts_export_fail)
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { exportCsv(it) }
    val htmlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { exportHtml(it) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { exportPdf(it) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty() }.getOrDefault("")
            }
            val imported = PasswordTools.parseCsv(text)
            var added = 0
            imported.forEach { row ->
                if (store.saveAccount(0, AccountDraft(row.title, row.username, row.url, row.password, "", "normal", "other", "", "", "", ""))) added++
            }
            status = context.getString(if (added > 0) R.string.accounts_import_done else R.string.accounts_import_fail, added)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.accounts_security_score), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.accounts_security_score_value, score), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.accounts_issue_weak, weak))
                    Text(stringResource(R.string.accounts_issue_reused, reused))
                    Text(stringResource(R.string.accounts_issue_old, old))
                    Text(stringResource(R.string.accounts_issue_recovery, noRecovery))
                    Text(stringResource(R.string.accounts_issue_totp, noTotp))
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.accounts_guard), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.accounts_guard_text), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = {
                        scanNote = if (score >= 80) context.getString(R.string.accounts_guard_ok)
                        else context.getString(R.string.accounts_issue_weak, weak) + " · " + context.getString(R.string.accounts_issue_recovery, noRecovery)
                    }) { Text(stringResource(R.string.accounts_guard_scan)) }
                    if (scanNote.isNotBlank()) Text(scanNote)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.accounts_generator), style = MaterialTheme.typography.titleMedium)
                    Text(generated, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.accounts_length, length.toInt()))
                    Slider(value = length, onValueChange = { length = it }, valueRange = 8f..40f, steps = 31)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.accounts_symbols), modifier = Modifier.weight(1f))
                        Switch(checked = symbols, onCheckedChange = { symbols = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { generated = PasswordTools.generate(length.toInt(), symbols) }) {
                            Text(stringResource(R.string.accounts_generate))
                        }
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(generated))
                        }) { Text(stringResource(R.string.accounts_copy)) }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.accounts_import), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.accounts_import_hint), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { importLauncher.launch("text/*") }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.accounts_import))
                    }
                    OutlinedButton(onClick = { csvLauncher.launch("passwords.csv") }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.accounts_export_csv))
                    }
                    OutlinedButton(onClick = { htmlLauncher.launch("passwords.html") }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.accounts_export_html))
                    }
                    OutlinedButton(onClick = { pdfLauncher.launch("passwords.pdf") }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.accounts_export_pdf))
                    }
                    if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text(stringResource(R.string.accounts_rotate_title), style = MaterialTheme.typography.titleMedium) }
        val due = store.accounts.filter { daysSince(it.updatedAt) >= 180 }
        if (due.isEmpty()) {
            item { Text(stringResource(R.string.accounts_guard_ok), style = MaterialTheme.typography.bodySmall) }
        }
        items(due, key = { it.id }) { account ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.accounts_rotate_due, account.title, daysSince(account.updatedAt)), modifier = Modifier.weight(1f))
                    TextButton(onClick = { store.touchAccount(account.id) }) {
                        Text(stringResource(R.string.accounts_rotate_now))
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { store.ensureCurrentDevice(Build.MANUFACTURER + " " + Build.MODEL) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.accounts_tab_devices), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { adding = true }) { Text(stringResource(R.string.accounts_device_add)) }
        }
        Text(stringResource(R.string.accounts_device_hint), style = MaterialTheme.typography.bodySmall)
        if (store.accountDevices.isEmpty()) {
            InfoCard(text = stringResource(R.string.accounts_devices_empty))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(store.accountDevices, key = { it.id }) {  device ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(device.name, fontWeight = FontWeight.Bold)
                            Text(device.platform, style = MaterialTheme.typography.bodySmall)
                            if (device.current) Text(stringResource(R.string.accounts_device_current), style = MaterialTheme.typography.labelSmall)
                        }
                        if (!device.current) {
                            TextButton(onClick = { store.removeAccountDevice(device.id) }) {
                                Text(stringResource(R.string.accounts_device_revoke))
                            }
                        }
                    }
                }
            }
        }
    }
    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.accounts_device_add)) },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.accounts_device_name)) }, singleLine = true)
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    store.addAccountDevice(name, "android")
                    name = ""
                    adding = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ShotsTab() {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf("") }
    var asking by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.readBytes() }.getOrNull()
            }
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ok = bytes != null && store.addScreenshot(label, mime, bytes)
            status = context.getString(if (ok) R.string.accounts_shot_saved else R.string.accounts_shot_fail)
            label = ""
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.accounts_tab_shots), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { asking = true }) { Text(stringResource(R.string.accounts_shots_add)) }
            }
        }
        if (status.isNotBlank()) item { InfoCard(text = status) }
        if (store.screenshots.isEmpty()) item { InfoCard(text = stringResource(R.string.accounts_shots_empty)) }
        items(store.screenshots, key = { it.id }) { shot ->
            val bytes = remember(shot.id) { store.screenshotBytes(shot) }
            val bitmap = remember(shot.id) { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(shot.label, modifier = Modifier.weight(1f))
                        IconButton(onClick = { store.deleteScreenshot(shot.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = shot.label,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.accounts_shots_add)) },
            text = {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text(stringResource(R.string.accounts_shot_label)) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = { asking = false; picker.launch("image/*") }) {
                    Text(stringResource(R.string.accounts_pick_image))
                }
            },
            dismissButton = { TextButton(onClick = { asking = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun NotesTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.accounts_tab_notes), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.accounts_notes_add)) }
            }
        }
        if (store.linkedNotes.isEmpty()) item { InfoCard(text = stringResource(R.string.accounts_notes_empty)) }
        items(store.linkedNotes, key = { it.id }) { note ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(note.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { store.deleteLinkedNote(note.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                    if (note.body.isNotBlank()) Text(note.body)
                    if (note.tags.isNotBlank()) Text(note.tags, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.accounts_notes_add)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.accounts_note_title)) }, singleLine = true)
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text(stringResource(R.string.accounts_note_body)) }, minLines = 3, maxLines = 6)
                    OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text(stringResource(R.string.accounts_note_tags)) }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = title.isNotBlank() || body.isNotBlank(), onClick = {
                    store.addLinkedNote(title, body, tags)
                    title = ""; body = ""; tags = ""
                    adding = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
