// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.learn

import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yousuf.toolkit.R
import data.local.LocalStore
import domain.model.LearningPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LearningPromptCard() {
    val clipboard = LocalClipboardManager.current
    val template = stringResource(R.string.learn_prompt_template)
    var subject by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf(listOf("")) }
    var minutes by remember { mutableStateOf("30") }
    var mode by remember { mutableStateOf("daily") }
    var prompt by remember { mutableStateOf("") }
    val modeText = stringResource(if (mode == "daily") R.string.learn_mode_daily else R.string.learn_mode_time)

    fun updateGoal(index: Int, value: String) {
        val next = goals.toMutableList()
        next[index] = value
        goals = next
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.learn_prompt), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.learn_prompt_subject)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text(stringResource(R.string.learn_prompt_level)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            goals.forEachIndexed { index, goal ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = goal, onValueChange = { updateGoal(index, it) }, label = { Text(stringResource(R.string.learn_goal, index + 1)) }, singleLine = true, modifier = Modifier.weight(1f))
                    if (goals.size > 1) TextButton(onClick = { goals = goals.filterIndexed { i, _ -> i != index } }) { Text(stringResource(R.string.learn_goal_remove)) }
                }
            }
            OutlinedButton(onClick = { goals = goals + "" }) { Text(stringResource(R.string.learn_goal_add)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == "daily", onClick = { mode = "daily" }, label = { Text(stringResource(R.string.learn_mode_daily)) })
                FilterChip(selected = mode == "time", onClick = { mode = "time" }, label = { Text(stringResource(R.string.learn_mode_time)) })
            }
            OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text(stringResource(R.string.learn_prompt_minutes)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { prompt = LearningPrompt.build(template, subject, level, goals, modeText, minutes) }) { Text(stringResource(R.string.learn_prompt_build)) }
                if (prompt.isNotBlank()) {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(prompt)) }) { Text(stringResource(R.string.learn_prompt_copy)) }
                }
            }
            if (prompt.isNotBlank()) Text(prompt, style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.learn_zip_hint), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LearningImportCard() {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { LearningZipImporter.parse(context, uri) }
            }
            result.onSuccess { draft ->
                val plan = store.importLearningPlan(draft)
                status = context.getString(R.string.learn_import_done, plan.title.ifBlank { context.getString(R.string.learn_unknown) })
            }.onFailure {
                status = context.getString(R.string.learn_import_failed)
            }
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.learn_import_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.learn_zip_hint), style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { picker.launch(arrayOf("application/zip")) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.learn_import_pick))
            }
            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LearningPlansCard() {
    val store = LocalStore.current
    var viewing by remember { mutableStateOf<LearningPlan?>(null) }
    val unknown = stringResource(R.string.learn_unknown)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.learn_plans_title), style = MaterialTheme.typography.titleMedium)
            if (store.learningPlans.isEmpty()) {
                Text(stringResource(R.string.learn_plans_empty), style = MaterialTheme.typography.bodySmall)
            } else {
                store.learningPlans.forEach { plan ->
                    Column {
                        Text(plan.title.ifBlank { unknown }, style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.learn_plan_category, plan.category.ifBlank { unknown }), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.learn_plan_lessons, plan.lessons.size), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.learn_status, stringResource(statusLabel(plan.status))), style = MaterialTheme.typography.bodySmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                TextButton(onClick = { viewing = plan }) { Text(stringResource(R.string.learn_plan_view_site)) }
                            }
                            items(plan.lessons, key = { it.id }) { lesson ->
                                TextButton(onClick = { store.toggleLearningLesson(plan.id, lesson.id) }) {
                                    Text(if (lesson.done) stringResource(R.string.learn_lesson_undo) else stringResource(R.string.learn_lesson_done))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    viewing?.let { plan -> LearningPlanViewerDialog(plan = plan, onDismiss = { viewing = null }) }
}

private fun statusLabel(status: String): Int = when (status) {
    "pending" -> R.string.learn_status_pending
    "approved" -> R.string.learn_status_approved
    "rejected" -> R.string.learn_status_rejected
    "ready" -> R.string.learn_status_ready
    else -> R.string.learn_status_local
}

@Composable
private fun LearningPlanViewerDialog(plan: LearningPlan, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) } },
        title = { Text(plan.title) },
        text = {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(520.dp),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadDataWithBaseURL(null, plan.siteHtml, "text/html", "UTF-8", null)
                    }
                }
            )
        }
    )
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
