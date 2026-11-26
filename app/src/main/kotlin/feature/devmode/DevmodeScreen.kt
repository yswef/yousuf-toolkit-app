// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.devmode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.SoundFx
import core.design.V3Chip
import core.dev.DevTest
import core.dev.DevTests
import core.diagnostics.DiagnosticEngine
import core.diagnostics.DiagnosticsCollector
import core.navigation.Routes
import core.net.Api
import core.update.ContentUpdate
import data.local.LocalStore
import domain.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TestLog(val title: String, val before: String, val after: String, val result: String)

@Composable
fun DevmodeScreen(onBack: () -> Unit, onFeature: (String) -> Unit = {}) {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull().orEmpty()
    }
    val catalog = remember { DevTests.tests(context) }
    val knownRoutes = remember {
        Routes::class.java.fields.mapNotNull { runCatching { it.get(null) as? String }.getOrNull() }.toSet()
    }
    var section by remember { mutableStateOf(0) }
    var developer by remember { mutableStateOf(store.developerRole.isNotBlank()) }
    var gateMessage by remember { mutableStateOf("") }
    var disabled by remember { mutableStateOf<Set<String>>(emptySet()) }
    var snapshots by remember { mutableStateOf<List<String>>(emptyList()) }
    var baseline by remember { mutableStateOf(store.devSnapshot()) }
    var logs by remember { mutableStateOf<List<TestLog>>(emptyList()) }
    var result by remember { mutableStateOf("") }
    val diff = remember { DevTests.diff(context, version) }
    val lockedText = stringResource(R.string.devmode_locked)
    val restoredText = stringResource(R.string.devmode_restored)
    val restoredAllText = stringResource(R.string.devmode_restored_all)
    val copiedText = stringResource(R.string.devmode_copied)

    LaunchedEffect(Unit) {
        runCatching { Api.devmodeStatus(Api.normalizeBase(store.serverUrl), store.authToken) }
            .onSuccess { status ->
                store.setDeveloper(status.role)
                developer = status.developer
                gateMessage = if (status.developer) "الصلاحية: ${status.role}" else ""
                if (status.tests.isNotEmpty()) {
                    disabled = catalog.map { it.id }.toSet() - status.tests.toSet()
                }
            }
            .onFailure {
                gateMessage = if (store.developerRole.isNotBlank()) {
                    "تعذّر التحقق من الخادم؛ تُستخدم صلاحية محفوظة من آخر تحقق."
                } else {
                    lockedText
                }
            }
    }

    suspend fun perform(test: DevTest): String {
        when (test.action) {
            "add_task" -> { store.addTask("اختبار مطور", "من وضع المطور", Priority.NORMAL); "أُضيفت مهمة اختبار" }
            "add_ledger" -> { store.addLedger("اختبار مال", 1.0, true, "اختبار"); "أُضيفت عملية مالية" }
            "add_workout" -> { store.addWorkout("مشى", 10); "أُضيف تمرين" }
            "add_journal" -> { store.addEntry("اختبار يومية", "محتوى تجريبي"); "أُضيفت يومية" }
            "add_water" -> { store.addWater(250); "أُضيف 250ml ماء" }
            "add_sleep" -> { store.addSleep(1.0); "أُضيفت ساعة نوم" }
            "add_learn" -> { store.addLearn("اختبار تعلّم", 10); "أُضيف درس" }
            "add_book" -> { store.addBook("كتاب اختبار", 10); "أُضيف كتاب" }
            "add_progress" -> { store.devGrantProgress(25); "أُضيف 25 نقطة/XP" }
            "add_campaign" -> { store.addCampaign("حملة اختبار", 3); "أُضيفت حملة" }
            "cycle_accent" -> { store.saveAvatar((store.avatar + 1) % 8); "تغيّر الأفاتار" }
            "add_alarm" -> { store.addAlarm("اختبار", "تنبيه تجريبي", System.currentTimeMillis() + 60_000); "أُضيف تنبيه" }
            "add_vault" -> { store.addVault("اختبار", "محتوى تجريبي"); "أُضيفت ملاحظة للخزنة" }
            "add_wallet" -> { store.addWallet("محفظة اختبار"); "أُضيفت محفظة" }
            "add_notification" -> { store.addNotification(context.packageName, "اختبار", "إشعار من وضع المطور"); "أُضيف إشعار" }
            "play_sound" -> { SoundFx.play(SoundFx.Fx.LEVEL_UP); "شُغّل مؤثر الرتبة" }
            "level_up" -> { store.devGrantProgress(100); SoundFx.play(SoundFx.Fx.LEVEL_UP); "ارتفع المستوى مع الصوت" }
            "replay_intro" -> { onFeature(Routes.INTRO); "عُرضت المقدمة" }
            "check_session" -> {
                val status = Api.devmodeStatus(Api.normalizeBase(store.serverUrl), store.authToken)
                "developer=${status.developer} role=${status.role}"
            }
            "check_devices" -> {
                val list = Api.devices(Api.normalizeBase(store.serverUrl), store.authToken)
                "الأجهزة المسجّلة: ${list.size}"
            }
            "check_git" -> "المستودع: ${store.gitOwner}/${store.gitRepo} · التوكن: ${if (store.gitToken.isBlank()) "غير مضبوط" else "مضبوط"}"
            "run_diagnostics" -> withContext(Dispatchers.IO) {
                val input = DiagnosticsCollector.collect(context, emptyList(), emptyMap())
                val output = DiagnosticEngine.analyze(input)
                "مشاكل مكتشفة: ${output.optJSONArray("issues")?.length() ?: 0}"
            }
            "check_sync" -> "الخادم: " + Api.health(Api.normalizeBase(store.serverUrl))
            "check_updates" -> {
                val r = ContentUpdate.sync(context, store.serverUrl)
                "نسخة المحتوى ${r.version} · تغيّر ${r.changed.size} ملف"
            }
            "check_backup" -> "حجم نسخة الحالة: ${store.devSnapshot().length} حرف"
            "run_selftest" -> "فحص ذاتي: ${catalog.size} ميزة، ${knownRoutes.size} مسار"
            "open" -> {
                if (test.route in knownRoutes) {
                    onFeature(test.route)
                    "فُتحت شاشة ${test.title}"
                } else "لا شاشة مستقلة؛ اكتفى بقراءة البيانات"
            }
            else -> "لا إجراء معرّف لهذا الاختبار"
        }
    }

    fun runTest(test: DevTest) {
        val before = store.devSnapshot()
        val beforeValue = DevTests.dataValue(store, test.dataKey, version)
        snapshots = snapshots + before
        scope.launch {
            val text = runCatching { perform(test) }.getOrElse { "خطأ: ${it.message}" }
            val afterValue = DevTests.dataValue(store, test.dataKey, version)
            logs = listOf(TestLog(test.title, beforeValue, afterValue, text)) + logs
            result = text
        }
    }

    ScreenScaffold(title = stringResource(R.string.feature_devmode), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(text = stringResource(R.string.devmode_desc), tone = InfoTone.INFO)
            if (gateMessage.isNotBlank()) {
                InfoCard(text = gateMessage, tone = if (developer) InfoTone.INFO else InfoTone.WARN)
            }
            if (!developer) {
                InfoCard(text = stringResource(R.string.devmode_locked), tone = InfoTone.WARN)
                Text(
                    stringResource(R.string.devmode_locked_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                V3Chip(stringResource(R.string.devmode_section_tests), section == 0, { section = 0 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.devmode_section_rollback), section == 1, { section = 1 }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                V3Chip(stringResource(R.string.devmode_section_data), section == 2, { section = 2 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.devmode_section_diff), section == 3, { section = 3 }, Modifier.weight(1f))
            }
            if (result.isNotBlank()) InfoCard(text = result, tone = InfoTone.INFO)

            when (section) {
                0 -> {
                    val enabled = catalog.filter { it.enabled && it.id !in disabled }
                    Text(
                        stringResource(R.string.devmode_tests_count, enabled.size, catalog.size),
                        style = MaterialTheme.typography.titleSmall
                    )
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val grouped = enabled.groupBy { it.section }
                        grouped.forEach { (name, items) ->
                            item { Text(name, style = MaterialTheme.typography.titleMedium) }
                            items(items, key = { it.id }) { test ->
                                TestRow(
                                    test = test,
                                    data = DevTests.dataValue(store, test.dataKey, version),
                                    onTest = { runTest(test) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Text(stringResource(R.string.devmode_section_rollback), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            enabled = snapshots.isNotEmpty(),
                            onClick = {
                                val last = snapshots.last()
                                snapshots = snapshots.dropLast(1)
                                if (store.devRestore(last)) result = restoredText
                            }
                        ) { Text(stringResource(R.string.devmode_restore_last)) }
                        OutlinedButton(onClick = {
                            if (store.devRestore(baseline)) {
                                snapshots = emptyList()
                                result = restoredAllText
                            }
                        }) { Text(stringResource(R.string.devmode_restore_session)) }
                    }
                    Text(
                        stringResource(R.string.devmode_snapshots, snapshots.size),
                        style = MaterialTheme.typography.labelSmall
                    )
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(logs, key = { "${it.title}-${it.after}-${logs.indexOf(it)}" }) { log ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(log.title, style = MaterialTheme.typography.bodyLarge)
                                    Text("قبل: ${log.before}", style = MaterialTheme.typography.labelSmall)
                                    Text("بعد: ${log.after}", style = MaterialTheme.typography.labelSmall)
                                    Text(log.result, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Text(stringResource(R.string.devmode_section_data), style = MaterialTheme.typography.titleMedium)
                    val snapshot = buildString {
                        appendLine("version=$version")
                        appendLine("server=${store.serverUrl}")
                        appendLine("account=${store.accountEmail}")
                        appendLine("role=${store.developerRole}")
                        appendLine("points=${store.points} xp=${store.xp} level=${store.level}")
                        appendLine("tasks=${store.tasks.size} ledger=${store.ledger.size} journal=${store.journal.size}")
                        appendLine("campaigns=${store.campaigns.size} wallets=${store.wallets.size}")
                        appendLine("books=${store.books.size} habits=${store.habits.size} screenshots=${store.screenshots.size}")
                        appendLine("premium=${store.premium} accent=${store.accent} mood=${store.mood}")
                        appendLine("hiddenApps=${store.hiddenApps.size} hiddenRoutes=${store.hiddenRoutes.size}")
                        appendLine("savedAccounts=${store.savedAccounts.size}/${store.maxAccounts}")
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Text(snapshot, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = {
                        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        manager.setPrimaryClip(ClipData.newPlainText("devmode", store.devSnapshot()))
                        result = copiedText
                    }) { Text(stringResource(R.string.devmode_copy)) }
                    Text(
                        stringResource(R.string.devmode_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(stringResource(R.string.devmode_section_diff), style = MaterialTheme.typography.titleMedium)
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("الحالي: ${diff.current} · السابق: ${diff.previous}", style = MaterialTheme.typography.titleSmall)
                            Text("ملاحظة الحالي: ${diff.currentNotes}", style = MaterialTheme.typography.bodySmall)
                            Text("ملاحظة السابق: ${diff.previousNotes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { DiffGroup(stringResource(R.string.devmode_diff_added), diff.added) }
                        item { DiffGroup(stringResource(R.string.devmode_diff_removed), diff.removed) }
                        item { DiffGroup(stringResource(R.string.devmode_diff_fixes), diff.fixesAdded) }
                        item {
                            InfoCard(
                                text = stringResource(R.string.devmode_diff_source),
                                tone = InfoTone.INFO
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestRow(test: DevTest, data: String, onTest: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(test.title, style = MaterialTheme.typography.bodyLarge)
                Text(data, style = MaterialTheme.typography.labelSmall)
                Text("${test.id} · ${test.action}", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(onClick = onTest) { Text(stringResource(R.string.devmode_test_button)) }
        }
    }
}

@Composable
private fun DiffGroup(title: String, items: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (items.isEmpty()) Text(stringResource(R.string.devmode_diff_none), style = MaterialTheme.typography.bodySmall)
            else items.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
