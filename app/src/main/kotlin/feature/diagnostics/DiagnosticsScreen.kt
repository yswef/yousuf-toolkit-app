// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.diagnostics.DiagnosticEngine
import core.diagnostics.DiagnosticsCollector
import core.net.Api
import data.local.LocalStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val symptomOptions = listOf(
    "تعليق الكيبورد/تكرار اللمسات أثناء الشحن",
    "بطارية تسخن أثناء الشحن",
    "الشحن بطيء",
    "الواي فاي يفصل كثيراً",
    "الإنترنت بطيء",
    "الجهاز يعلّق بشكل عام",
    "ضعف إشارة الشريحة",
    "امتلاء التخزين"
)

@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = LocalStore.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var chargerAge by remember { mutableStateOf("") }
    var powerNotes by remember { mutableStateOf("") }
    var protector by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<JSONObject?>(null) }
    var status by remember { mutableStateOf("") }
    var showRaw by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<JSONObject?>(null) }
    var known by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val confirmTitle = stringResource(R.string.diag_confirm_title)
    val confirmBody = stringResource(R.string.diag_confirm_body)
    val confirmOk = stringResource(R.string.diag_confirm_ok)
    val sentText = stringResource(R.string.diag_sent)
    val fixedText = stringResource(R.string.diag_fixed)

    fun collect() {
        scope.launch {
            status = ""
            val env = mapOf(
                "charger_age" to chargerAge,
                "power_source_notes" to powerNotes,
                "screen_protector" to protector,
                "humidity_notes" to humidity
            )
            val result = withContext(Dispatchers.IO) {
                val input = DiagnosticsCollector.collect(context, selected.toList(), env)
                DiagnosticEngine.analyze(input)
            }
            output = result
            val base = Api.normalizeBase(store.serverUrl)
            val issues = result.optJSONArray("issues")
            val map = LinkedHashMap<String, String>()
            if (issues != null && store.authToken.isNotBlank()) {
                for (i in 0 until issues.length()) {
                    val sig = issues.optString(i)
                    val solution = runCatching { Api.diagnosticsMatch(base, store.authToken, sig) }.getOrNull()
                    if (!solution.isNullOrBlank()) map[sig] = solution
                }
            }
            known = map
        }
    }

    fun sendReports() {
        val result = output ?: return
        scope.launch {
            val base = Api.normalizeBase(store.serverUrl)
            val reports = result.optJSONArray("server_reports") ?: JSONArray()
            var sent = 0
            for (i in 0 until reports.length()) {
                val report = reports.optJSONObject(i) ?: continue
                val sig = report.optString("issue_signature")
                if (known.containsKey(sig)) continue
                runCatching { Api.diagnosticsReport(base, store.authToken, report) }
                    .onSuccess { if (it) sent++ }
            }
            status = sentText + " ($sent)"
        }
    }

    fun applyFix(action: JSONObject) {
        val dir = context.cacheDir
        var freed = 0L
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.name != "diag_bench.tmp") {
                    val size = if (f.isFile) f.length() else f.walkTopDown().sumOf { if (it.isFile) it.length() else 0L }
                    freed += size
                    if (f.isDirectory) f.deleteRecursively() else f.delete()
                }
            }
        }
        val result = output ?: return
        val attempted = JSONArray().apply { put(action.optString("id")) }
        val arr = result.optJSONArray("server_reports") ?: JSONArray()
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.put("attempted_fix_actions", attempted)
        }
        val report = result.optString("user_report_markdown") +
            "\n\n## ما تم إصلاحه تلقائياً\n- ${action.optString("description_ar")} — نحو %.1f MB.".format(freed / 1024.0 / 1024.0)
        result.put("user_report_markdown", report)
        output = JSONObject(result.toString())
        status = fixedText
    }

    ScreenScaffold(title = stringResource(R.string.diagnostics_title), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoCard(text = stringResource(R.string.diagnostics_desc), tone = InfoTone.INFO)
            Text(stringResource(R.string.diag_symptoms), style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                symptomOptions.forEach { symptom ->
                    FilterChip(
                        selected = symptom in selected,
                        onClick = { selected = if (symptom in selected) selected - symptom else selected + symptom },
                        label = { Text(symptom) }
                    )
                }
            }
            OutlinedTextField(value = chargerAge, onValueChange = { chargerAge = it }, label = { Text(stringResource(R.string.diag_charger_age)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = powerNotes, onValueChange = { powerNotes = it }, label = { Text(stringResource(R.string.diag_power_notes)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = protector, onValueChange = { protector = it }, label = { Text(stringResource(R.string.diag_protector)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = humidity, onValueChange = { humidity = it }, label = { Text(stringResource(R.string.diag_humidity)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { collect() }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.diag_run)) }
                OutlinedButton(onClick = { showRaw = !showRaw }) { Text(stringResource(R.string.diag_raw)) }
            }
            if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.INFO)
            output?.let { result ->
                if (known.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.diag_known), style = MaterialTheme.typography.titleSmall)
                            known.forEach { (sig, solution) ->
                                Text("• $sig: $solution", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                val fixes = result.optJSONArray("fix_actions") ?: JSONArray()
                if (fixes.length() > 0) {
                    Text(stringResource(R.string.diag_fixes), style = MaterialTheme.typography.titleSmall)
                    for (i in 0 until fixes.length()) {
                        val action = fixes.optJSONObject(i) ?: continue
                        Button(onClick = { confirmAction = action }, modifier = Modifier.fillMaxWidth()) {
                            Text(action.optString("description_ar"))
                        }
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        result.optString("user_report_markdown"),
                        Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { sendReports() }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.diag_send))
                    }
                }
                if (showRaw) {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            result.optJSONObject("raw_input")?.toString(2) ?: "—",
                            Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }

    confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(confirmTitle) },
            text = { Text(confirmBody + "\n" + action.optString("description_ar")) },
            confirmButton = {
                Button(onClick = { applyFix(action); confirmAction = null }) { Text(confirmOk) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmAction = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
