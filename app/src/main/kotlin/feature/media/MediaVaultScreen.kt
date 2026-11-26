// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.media

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.V3Chip
import core.media.MediaEntry
import core.media.MediaVault
import java.io.File

@Composable
fun MediaVaultScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var granted by remember { mutableStateOf(hasPermission(context)) }
    var gallery by remember { mutableStateOf(MediaVault.list(context)) }
    var vault by remember { mutableStateOf(MediaVault.hidden(context)) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var status by remember { mutableStateOf("") }
    var viewer by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.all { it }
        gallery = MediaVault.list(context)
    }
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            gallery = MediaVault.list(context)
            vault = MediaVault.hidden(context)
            status = context.getString(R.string.media_hidden_ok, vault.size)
        }
    }

    fun refresh() {
        gallery = MediaVault.list(context)
        vault = MediaVault.hidden(context)
    }

    ScreenScaffold(title = stringResource(R.string.media_title), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Chip(stringResource(R.string.media_gallery), tab == 0, { tab = 0 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.media_vault), tab == 1, { tab = 1 }, Modifier.weight(1f))
            }
            if (!granted) {
                InfoCard(text = stringResource(R.string.media_permission), tone = InfoTone.WARN)
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
                    )
                }) { Text(stringResource(R.string.common_save)) }
            }
            if (tab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        selected = if (selected.size == gallery.size) emptySet()
                        else gallery.map { it.uri.toString() }.toSet()
                    }) { Text(stringResource(R.string.media_select_all)) }
                    Button(
                        onClick = {
                            val chosen = gallery.filter { it.uri.toString() in selected }
                            if (chosen.isEmpty()) return@Button
                            MediaVault.hide(context, chosen)
                            val pending = MediaStore.createDeleteRequest(
                                context.contentResolver, chosen.map { it.uri }
                            )
                            deleteLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                        },
                        enabled = selected.isNotEmpty()
                    ) { Text(stringResource(R.string.media_hide)) }
                }
                if (gallery.isEmpty()) InfoCard(text = stringResource(R.string.media_none), tone = InfoTone.INFO)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(gallery, key = { it.uri.toString() }) { entry ->
                        MediaRow(entry, entry.uri.toString() in selected) {
                            selected = if (it) selected - entry.uri.toString() else selected + entry.uri.toString()
                        }
                    }
                }
            } else {
                if (vault.isEmpty()) InfoCard(text = stringResource(R.string.media_none), tone = InfoTone.INFO)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vault, key = { it.absolutePath }) { file ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(file.name, style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { viewer = file }) {
                                        Text(stringResource(if (isVideo(file)) R.string.media_open_video else R.string.media_open_image))
                                    }
                                    OutlinedButton(onClick = {
                                        status = if (MediaVault.restore(context, file)) {
                                            file.delete()
                                            refresh()
                                            context.getString(R.string.media_restored)
                                        } else status
                                    }) { Text(stringResource(R.string.media_restore)) }
                                }
                            }
                        }
                    }
                }
            }
            if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.INFO)
        }
    }

    viewer?.let { file ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewer = null },
            confirmButton = {
                OutlinedButton(onClick = { viewer = null }) { Text(stringResource(R.string.common_close)) }
            },
            text = { MediaViewer(file) }
        )
    }
}

@Composable
private fun MediaRow(entry: MediaEntry, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.bodyLarge)
                Text(entry.mime, style = MaterialTheme.typography.labelSmall)
            }
            androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun MediaViewer(file: File) {
    if (isVideo(file)) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val controller = MediaController(ctx)
                    controller.setAnchorView(this)
                    setMediaController(controller)
                    setVideoPath(file.absolutePath)
                    setOnPreparedListener { it.start() }
                }
            },
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )
    } else {
        var scale by remember { mutableStateOf(1f) }
        val state = rememberTransformableState { zoom, _, _ -> scale = (scale * zoom).coerceIn(0.5f, 5f) }
        val bitmap = remember(file.absolutePath) { BitmapFactory.decodeFile(file.absolutePath) }
        Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = file.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .transformable(state = state)
                )
            } else Text(stringResource(R.string.files_open_fail))
        }
    }
}

private fun isVideo(file: File): Boolean = file.extension.lowercase() in setOf("mp4", "mkv", "webm", "3gp")

private fun hasPermission(context: Context): Boolean =
    context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) ==
        android.content.pm.PackageManager.PERMISSION_GRANTED
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
