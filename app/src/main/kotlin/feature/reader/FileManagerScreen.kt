// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.reader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.V3Chip
import core.files.FileItem
import core.files.FileManager
import core.files.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FileManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }
    var granted by remember { mutableStateOf(canReadFiles()) }
    var root by remember { mutableStateOf(runCatching { Environment.getExternalStorageDirectory() }.getOrNull() ?: context.filesDir) }
    var current by remember { mutableStateOf(root) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(SortMode.NAME) }
    var ascending by remember { mutableStateOf(true) }
    var grid by remember { mutableStateOf(false) }
    var clipboard by remember { mutableStateOf<Pair<File, Boolean>?>(null) }
    var docs by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var opened by remember { mutableStateOf<File?>(null) }
    var markdown by remember { mutableStateOf("") }
    var raw by remember { mutableStateOf(false) }
    var pdfPages by remember { mutableStateOf(0) }
    var pdfPage by remember { mutableStateOf(0) }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("") }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        granted = canReadFiles()
        docs = FileManager.scanDocuments(root)
    }

    fun openFile(file: File) {
        opened = file
        status = ""
        when (file.extension.lowercase()) {
            "pdf" -> {
                scope.launch {
                    val count = withContext(Dispatchers.IO) { pdfPageCount(file) }
                    if (count == null || count <= 0) status = context.getString(R.string.files_open_fail)
                    else {
                        pdfPages = count
                        pdfPage = 0
                        pdfBitmap = withContext(Dispatchers.IO) { renderPdfPage(file, 0, 1080)?.first }
                    }
                }
            }
            "md", "markdown", "txt" -> {
                markdown = runCatching { file.readText() }.getOrDefault("")
                raw = false
            }
            else -> status = context.getString(R.string.files_open_fail)
        }
    }

    if (!granted) {
        ScreenScaffold(title = stringResource(R.string.files_title), onBack = onBack) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoCard(text = stringResource(R.string.files_permission), tone = InfoTone.WARN)
                Button(onClick = {
                    runCatching {
                        permLauncher.launch(
                            Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    }
                }) { Text(stringResource(R.string.common_save)) }
            }
        }
        return
    }

    ScreenScaffold(title = stringResource(R.string.files_title), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                V3Chip(stringResource(R.string.files_browse), tab == 0, { tab = 0 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.files_library), tab == 1, { tab = 1; docs = FileManager.scanDocuments(root) }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.files_reader), tab == 2, { tab = 2 }, Modifier.weight(1f))
            }
            OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SortMode.entries.forEach { mode ->
                    V3Chip(
                        stringResource(
                            when (mode) {
                                SortMode.NAME -> R.string.files_sort_name
                                SortMode.DATE -> R.string.files_sort_date
                                SortMode.SIZE -> R.string.files_sort_size
                                SortMode.TYPE -> R.string.files_sort_type
                            }
                        ),
                        sort == mode,
                        { sort = mode; ascending = !ascending }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Chip(stringResource(R.string.files_list), !grid, { grid = false })
                V3Chip(stringResource(R.string.files_grid), grid, { grid = true })
                clipboard?.let { (file, move) ->
                    OutlinedButton(onClick = {
                        if (FileManager.copy(file, current, move)) {
                            status = context.getString(R.string.files_pasted)
                            clipboard = null
                        }
                    }) { Text(stringResource(R.string.files_paste)) }
                }
            }
            if (tab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { current = current.parentFile ?: root }) { Text(stringResource(R.string.files_up)) }
                    Text(current.absolutePath, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                }
                val items = FileManager.list(current, sort, ascending, query)
                if (items.isEmpty()) InfoCard(text = stringResource(R.string.files_empty), tone = InfoTone.INFO)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(items, key = { it.file.absolutePath }) { item ->
                        FileRow(item, grid) {
                            if (item.isDir) current = item.file else openFile(item.file)
                        }
                        if (item.isDir) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { clipboard = item.file to false }) { Text(stringResource(R.string.files_copy)) }
                                OutlinedButton(onClick = { clipboard = item.file to true }) { Text(stringResource(R.string.files_move)) }
                            }
                        }
                    }
                }
            } else if (tab == 1) {
                if (docs.isEmpty()) docs = FileManager.scanDocuments(root)
                if (docs.isEmpty()) InfoCard(text = stringResource(R.string.files_empty), tone = InfoTone.INFO)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(docs.filter { it.name.contains(query, ignoreCase = true) }, key = { it.file.absolutePath }) { item ->
                        FileRow(item, false) {
                            openFile(item.file)
                            tab = 2
                        }
                    }
                }
            } else {
                val file = opened
                if (file == null) {
                    InfoCard(text = stringResource(R.string.files_empty), tone = InfoTone.INFO)
                } else if (file.extension.lowercase() == "pdf") {
                    Text(stringResource(R.string.files_pdf_page, pdfPage + 1, pdfPages))
                    pdfBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(), contentDescription = file.name,
                            modifier = Modifier.fillMaxWidth(), filterQuality = FilterQuality.Medium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(enabled = pdfPage > 0, onClick = {
                            scope.launch {
                                pdfPage--
                                pdfBitmap = withContext(Dispatchers.IO) { renderPdfPage(file, pdfPage, 1080)?.first }
                            }
                        }) { Text(stringResource(R.string.files_prev)) }
                        OutlinedButton(enabled = pdfPage + 1 < pdfPages, onClick = {
                            scope.launch {
                                pdfPage++
                                pdfBitmap = withContext(Dispatchers.IO) { renderPdfPage(file, pdfPage, 1080)?.first }
                            }
                        }) { Text(stringResource(R.string.files_next)) }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        V3Chip(stringResource(R.string.files_visual), !raw, { raw = false })
                        V3Chip(stringResource(R.string.files_raw), raw, { raw = true })
                    }
                    Text(
                        if (raw) markdown else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!raw) {
                        Text(
                            text = FileManager.markdown(markdown),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
            if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.INFO)
        }
    }
}

@Composable
private fun FileRow(item: FileItem, grid: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().then(if (grid) Modifier.height(90.dp) else Modifier)) {
        Row(Modifier.padding(12.dp).clickable(onClick = onClick)) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
                Text(
                    if (item.isDir) "مجلد" else "${item.extension.uppercase()} · ${FileManager.sizeText(item.size)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun canReadFiles(): Boolean = runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)

private fun pdfPageCount(target: File): Int? = runCatching {
    ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { it.pageCount }
    }
}.getOrNull()

private fun renderPdfPage(target: File, index: Int, width: Int): Pair<Bitmap, Int>? = runCatching {
    ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            val safe = index.coerceIn(0, renderer.pageCount - 1)
            renderer.openPage(safe).use { page ->
                val height = (width.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                image.eraseColor(Color.WHITE)
                page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                image to safe
            }
        }
    }
}.getOrNull()
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
