package feature.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.SoundFx
import data.local.LocalStore
import data.local.Store

private fun lessonKey(path: LearnPath, index: Int) = "${path.id}:$index"

private fun doneCount(store: Store, path: LearnPath) =
    path.lessons.indices.count { store.lessonDone(lessonKey(path, it)) }

fun LazyListScope.learnPathsSection(store: Store, onOpen: (LearnPath) -> Unit) {
    item(key = "paths-head") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(R.string.learn_paths_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.learn_paths_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    learnPaths.forEach { path ->
        item(key = "path-" + path.id) {
            val done = doneCount(store, path)
            val total = path.lessons.size
            Card(onClick = { onOpen(path) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(path.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(path.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.path_meta, path.level, total, path.totalMinutes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { if (total > 0) done.toFloat() / total else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(stringResource(R.string.path_progress, done, total), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnPathSheet(path: LearnPath, onDismiss: () -> Unit) {
    val store = LocalStore.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val done = doneCount(store, path)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        LazyColumn(
            Modifier.navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(path.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(path.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LinearProgressIndicator(
                        progress = { if (path.lessons.isNotEmpty()) done.toFloat() / path.lessons.size else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(stringResource(R.string.path_progress, done, path.lessons.size), style = MaterialTheme.typography.labelMedium)
                }
            }
            itemsIndexed(path.lessons) { index, lesson ->
                val key = lessonKey(path, index)
                val checked = store.lessonDone(key)
                val toggle = {
                    val nowDone = store.toggleLesson(key)
                    val all = path.lessons.indices.all { store.lessonDone(lessonKey(path, it)) }
                    SoundFx.play(if (nowDone && all) SoundFx.Fx.LEVEL_UP else if (nowDone) SoundFx.Fx.SUCCESS else SoundFx.Fx.TAP)
                }
                Row(
                    Modifier.fillMaxWidth().clickable { toggle() }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(checked = checked, onCheckedChange = { toggle() })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "${index + 1}. ${lesson.title}",
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(lesson.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.path_lesson_min, lesson.minutes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
