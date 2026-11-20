@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.reader

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfReaderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var file by remember { mutableStateOf<File?>(null) }
    var page by remember { mutableStateOf(0) }
    var pages by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val target = File(context.cacheDir, "reader.pdf")
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        file = target
        page = 0
        status = ""
        runCatching {
            ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    pages = renderer.pageCount
                }
            }
        }.onFailure { status = context.getString(R.string.pdf_fail) }
    }
    fun render(index: Int) {
        val target = file ?: return
        if (target.length() == 0L) return
        runCatching {
            ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    val safe = index.coerceIn(0, renderer.pageCount - 1)
                    renderer.openPage(safe).use { pdfPage ->
                        val width = 1080
                        val height = (width.toFloat() * pdfPage.height / pdfPage.width).toInt().coerceAtLeast(1)
                        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        image.eraseColor(Color.WHITE)
                        pdfPage.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap = image
                        page = safe
                    }
                }
            }
        }.onFailure { status = context.getString(R.string.pdf_fail) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.feature_pdf), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { picker.launch("application/pdf") }) { Text(stringResource(R.string.pdf_pick)) }
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(R.string.pdf_url)) }, singleLine = true)
                OutlinedButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                }) { Text(stringResource(R.string.pdf_open_url)) }
                if (status.isNotBlank()) InfoCard(text = status)
            }
        }
        if (bitmap == null) {
            InfoCard(text = stringResource(R.string.pdf_none))
        } else {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = stringResource(R.string.feature_pdf),
                modifier = Modifier.fillMaxWidth(),
                filterQuality = FilterQuality.Medium
            )
            Text(stringResource(R.string.pdf_page, page + 1, pages))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = page > 0, onClick = { render(page - 1) }) { Text(stringResource(R.string.pdf_prev)) }
                OutlinedButton(enabled = page + 1 < pages, onClick = { render(page + 1) }) { Text(stringResource(R.string.pdf_next)) }
            }
        }
    }
}
