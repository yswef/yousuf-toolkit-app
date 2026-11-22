package feature.cleaner

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.CountUpText
import core.design.ScreenScaffold
import core.design.SoundFx
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File

private data class Storage(val total: Long, val free: Long)

private fun readStorage(): Storage {
    val stat = StatFs(Environment.getDataDirectory().path)
    return Storage(stat.totalBytes, stat.availableBytes)
}

@Composable
fun CleanerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allFiles by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        allFiles = Environment.isExternalStorageManager()
    }
    var storage by remember { mutableStateOf(readStorage()) }
    var stage by remember { mutableStateOf<Int?>(null) }
    var results by remember { mutableStateOf<Map<CleanCat, List<Junk>>?>(null) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expanded by remember { mutableStateOf<CleanCat?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var cleaning by remember { mutableStateOf(false) }
    var freed by remember { mutableStateOf<Pair<Int, Long>?>(null) }

    val all = results?.values?.flatten().orEmpty()
    val chosen = all.filter { it.path in selected }
    val chosenSize = chosen.sumOf { it.size }

    fun startScan() {
        freed = null
        stage = R.string.clean_stage_cache
        scope.launch {
            try {
                val found = scanJunk(context, allFiles) { stage = it }
                results = found
                selected = found.entries.filter { it.key.defaultOn }.flatMap { e -> e.value.map { it.path } }.toSet()
                expanded = null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                results = emptyMap()
            } finally {
                stage = null
            }
        }
    }

    ScreenScaffold(title = stringResource(R.string.feature_cleaner), onBack = onBack) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(R.drawable.cleaner_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(24.dp))
                    )
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val used = (storage.total - storage.free).coerceAtLeast(0L)
                            Text(
                                stringResource(R.string.clean_storage, fmtSize(used), fmtSize(storage.total)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                progress = { if (storage.total > 0) used.toFloat() / storage.total else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                stringResource(R.string.clean_free, fmtSize(storage.free)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (!allFiles) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    stringResource(R.string.clean_perm_text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                OutlinedButton(onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    )
                                    permLauncher.launch(intent)
                                }) { Text(stringResource(R.string.clean_perm_btn)) }
                            }
                        }
                    }
                }
                item {
                    val busy = stage != null
                    if (busy) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(Modifier.height(28.dp))
                            Text(stringResource(stage ?: R.string.clean_stage_cache), style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Button(onClick = { storage = readStorage(); startScan() }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(if (results == null) R.string.clean_scan_all else R.string.clean_rescan))
                        }
                    }
                }
                freed?.let { (n, bytes) ->
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    CountUpText(
                                        value = (bytes / 1024L).toInt(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        suffix = " KB"
                                    )
                                    Text(
                                        stringResource(R.string.clean_done, fmtSize(bytes), n),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                val res = results
                if (res != null && all.isEmpty() && freed == null) {
                    item {
                        Text(
                            stringResource(R.string.clean_nothing),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                if (res != null) {
                    res.entries.filter { it.value.isNotEmpty() }.forEach { (cat, list) ->
                        item(key = cat.name) {
                            CategoryCard(
                                cat = cat,
                                list = list,
                                selected = selected,
                                open = expanded == cat,
                                onOpen = { expanded = if (expanded == cat) null else cat },
                                onToggleAll = {
                                    val paths = list.map { it.path }
                                    selected = if (paths.all { it in selected }) selected - paths.toSet() else selected + paths
                                },
                                onToggle = { p -> selected = if (p in selected) selected - p else selected + p }
                            )
                        }
                    }
                    item {
                        Text(
                            stringResource(R.string.clean_safe_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = chosen.isNotEmpty() && !cleaning,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { confirm = true },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) { Text(stringResource(R.string.clean_selected_btn, fmtSize(chosenSize))) }
                }
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.clean_confirm_title)) },
            text = { Text(stringResource(R.string.clean_confirm_text, chosen.size, fmtSize(chosenSize))) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    cleaning = true
                    val items = chosen
                    scope.launch {
                        val r = deleteJunk(items, cleanRoots(context, allFiles))
                        freed = r
                        SoundFx.play(SoundFx.Fx.CLEAN)
                        storage = readStorage()
                        results = results?.mapValues { (_, v) -> v.filter { File(it.path).exists() } }
                        selected = emptySet()
                        cleaning = false
                    }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CategoryCard(
    cat: CleanCat,
    list: List<Junk>,
    selected: Set<String>,
    open: Boolean,
    onOpen: () -> Unit,
    onToggleAll: () -> Unit,
    onToggle: (String) -> Unit
) {
    val pickedCount = list.count { it.path in selected }
    val state = when (pickedCount) {
        0 -> ToggleableState.Off
        list.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }
    val rotation by animateFloatAsState(if (open) 180f else 0f, label = "arrow")
    Card(Modifier.fillMaxWidth().animateContentSize()) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriStateCheckbox(state = state, onClick = onToggleAll)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(cat.title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.clean_items_size, list.size, fmtSize(list.sumOf { it.size })),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.rotate(rotation))
            }
            if (open) {
                Text(
                    stringResource(cat.desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                val shown = list.take(40)
                shown.forEach { j ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onToggle(j.path) }.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = j.path in selected, onCheckedChange = { onToggle(j.path) })
                        Column(Modifier.weight(1f)) {
                            Text(File(j.path).name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                File(j.path).parent.orEmpty().removePrefix(Environment.getExternalStorageDirectory().path),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(if (j.isDir) "" else fmtSize(j.size), style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (list.size > shown.size) {
                    Text(
                        stringResource(R.string.clean_more, list.size - shown.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
