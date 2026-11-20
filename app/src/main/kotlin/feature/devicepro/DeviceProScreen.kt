@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.devicepro

import android.Manifest
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.ContactsContract
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DeviceProScreen(onBack: () -> Unit, onOpen: (String) -> Unit = {}, initialTab: Int = 0) {
    var tab by rememberSaveable { mutableStateOf(initialTab.coerceIn(0, 3)) }
    val tabs = listOf(
        R.string.devpro_performance,
        R.string.devpro_sd,
        R.string.devpro_download,
        R.string.devpro_contacts
    )
    ScreenScaffold(title = stringResource(R.string.feature_device_pro), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
                }
            }
            when (tab) {
                0 -> PerformanceTab()
                1 -> StorageTab()
                2 -> DownloadTab()
                else -> ContactsTab()
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onOpen(core.navigation.Routes.CLEANER) }) { Text(stringResource(R.string.feature_cleaner)) }
                TextButton(onClick = { onOpen(core.navigation.Routes.CONTACTS) }) { Text(stringResource(R.string.feature_contacts)) }
                TextButton(onClick = { onOpen(core.navigation.Routes.UPDATER) }) { Text(stringResource(R.string.feature_updater)) }
            }
        }
    }
}

@Composable
private fun PerformanceTab() {
    val context = LocalContext.current
    val activity = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo().also { activity.getMemoryInfo(it) }
    val battery = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val level = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val stat = StatFs(Environment.getDataDirectory().path)
    val available = stat.availableBytes / 1024 / 1024
    val total = stat.totalBytes / 1024 / 1024
    val cores = Runtime.getRuntime().availableProcessors()
    val rx = android.net.TrafficStats.getTotalRxBytes().coerceAtLeast(0) / 1024 / 1024
    val tx = android.net.TrafficStats.getTotalTxBytes().coerceAtLeast(0) / 1024 / 1024
    val used = (memory.totalMem - memory.availMem) / 1024 / 1024
    val totalMem = memory.totalMem / 1024 / 1024
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.devpro_performance), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.devpro_memory, used.toInt(), totalMem.toInt()))
                    Text(stringResource(R.string.devpro_battery, level))
                    Text(stringResource(R.string.devpro_storage, available.toInt(), total.toInt()))
                    Text(stringResource(R.string.devpro_cores, cores))
                    Text(stringResource(R.string.devpro_traffic, rx.toString() + " MB", tx.toString() + " MB"))
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.devpro_usage), style = MaterialTheme.typography.titleMedium)
                    Text(android.text.format.Formatter.formatFileSize(context, rx * 1024 * 1024) + " / " + android.text.format.Formatter.formatFileSize(context, tx * 1024 * 1024))
                }
            }
        }
    }
}

@Composable
private fun StorageTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tree by remember { mutableStateOf<Uri?>(null) }
    var status by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            tree = uri
            status = ""
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.devpro_sd), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { picker.launch(null) }) { Text(stringResource(R.string.devpro_pick_sd)) }
        if (tree == null) {
            InfoCard(text = stringResource(R.string.devpro_sd_none))
        } else {
            OutlinedButton(onClick = {
                val uri = tree ?: return@OutlinedButton
                scope.launch {
                    status = withContext(Dispatchers.IO) { runTest(context, uri) }
                }
            }) { Text(stringResource(R.string.devpro_sd_test)) }
            if (status.isNotBlank()) Text(status)
        }
    }
}

private fun runTest(context: Context, tree: Uri): String {
    val resolver = context.contentResolver
    val root = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
    val test = DocumentsContract.createDocument(resolver, root, "application/octet-stream", "yd-test.bin")
        ?: return "error"
    val buffer = ByteArray(1024 * 1024) { 7 }
    val writeStart = System.nanoTime()
    resolver.openOutputStream(test)?.use { it.write(buffer) }
    val writeMs = (System.nanoTime() - writeStart) / 1_000_000.0
    val readStart = System.nanoTime()
    resolver.openInputStream(test)?.use { input ->
        val data = ByteArray(8192)
        while (input.read(data) > 0) Unit
    }
    val readMs = (System.nanoTime() - readStart) / 1_000_000.0
    DocumentsContract.deleteDocument(resolver, test)
    val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
    var count = 0
    var size = 0L
    resolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_SIZE), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
        while (cursor.moveToNext()) {
            count++
            if (index >= 0) size += cursor.getLong(index)
        }
    }
    val writeSpeed = 1.0 / (writeMs / 1000.0).coerceAtLeast(0.001)
    val readSpeed = 1.0 / (readMs / 1000.0).coerceAtLeast(0.001)
    return String.format(Locale.US, "%.1f MB/s | %.1f MB/s | %d | %d MB", writeSpeed, readSpeed, count, size / 1024 / 1024)
}

@Composable
private fun DownloadTab() {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val ok = stringResource(R.string.devpro_download_done)
    val fail = stringResource(R.string.devpro_download_fail)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.devpro_download), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(R.string.devpro_url)) }, singleLine = true)
        OutlinedButton(onClick = {
            val target = runCatching { Uri.parse(url) }.getOrNull()
            if (target == null || target.scheme?.startsWith("http") != true) {
                status = fail
            } else {
                runCatching {
                    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(
                        DownloadManager.Request(target)
                            .setTitle(target.lastPathSegment ?: "download")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    )
                }
                status = ok
            }
        }) { Text(stringResource(R.string.devpro_download_btn)) }
        if (status.isNotBlank()) Text(status)
        Text(stringResource(R.string.devpro_download_none), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ContactsTab() {
    val context = LocalContext.current
    var groups by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) status = context.getString(R.string.devpro_permission)
    }
    fun scan() {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val map = HashMap<String, MutableList<String>>()
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null, null, null
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex).orEmpty()
                        val number = cursor.getString(numberIndex).orEmpty().filter { it.isDigit() }.takeLast(9)
                        if (number.length >= 7) map.getOrPut(number) { ArrayList() }.add(name.ifBlank { number })
                    }
                }
                map.values.filter { it.size > 1 }.map { it.distinct() }
            }
            groups = result
            status = context.getString(if (result.isEmpty()) R.string.devpro_contacts_none else R.string.devpro_contacts_dupes, result.size)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.devpro_contacts), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                scan()
            } else {
                permission.launch(Manifest.permission.READ_CONTACTS)
            }
        }) { Text(stringResource(R.string.devpro_contacts_scan)) }
        OutlinedButton(onClick = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)) }
        }) { Text(stringResource(R.string.devpro_contacts_open)) }
        if (status.isNotBlank()) InfoCard(text = status)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(groups) { group ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        group.forEach { Text(it, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
