@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.life

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.ExtraRecord
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

@Composable
fun LifeScreen(initialTab: Int, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    var adding by remember { mutableStateOf<String?>(null) }
    var adhkar by remember { mutableStateOf<ExtraRecord?>(null) }
    val store = LocalStore.current
    val tabs = listOf(
        R.string.life_health,
        R.string.life_learn,
        R.string.life_faith,
        R.string.life_books,
        R.string.life_mind
    )
    ScreenScaffold(title = stringResource(R.string.feature_life), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
                }
            }
            when (tab) {
                0 -> HealthTab(onAdd = { adding = it })
                1 -> LearnTab(onAdd = { adding = it })
                2 -> FaithTab(onAdd = { adding = it }, onDue = { adhkar = it })
                3 -> BooksTab(onAdd = { adding = it })
                else -> MindTab(onAdd = { adding = it })
            }
        }
    }
    adding?.let { mode ->
        LifeAddDialog(mode = mode, onDismiss = { adding = null })
    }
    if (adhkar != null) {
        val item = adhkar!!
        AlertDialog(
            onDismissRequest = { adhkar = null },
            title = { Text(stringResource(R.string.life_adhkar_due)) },
            text = { Text(item.detail) },
            confirmButton = {
                TextButton(onClick = {
                    store.updateExtra(item.id, "done_" + LocalDate.now().toEpochDay())
                    adhkar = null
                }) { Text(stringResource(R.string.life_adhkar_due)) }
            },
            dismissButton = { TextButton(onClick = { adhkar = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun HealthTab(onAdd: (String) -> Unit) {
    val store = LocalStore.current
    val today = LocalDate.now().toEpochDay()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.life_skincare), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAdd("skin") }) { Text(stringResource(R.string.add)) }
                    }
                    val routines = store.extras.filter { it.kind == "skin" }
                    if (routines.isEmpty()) Text(stringResource(R.string.moneypro_no_data), style = MaterialTheme.typography.bodySmall)
                    routines.forEach { routine ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(routine.title, modifier = Modifier.weight(1f))
                            if (routine.status == "done_$today") {
                                Text(stringResource(R.string.life_skincare_done_today), style = MaterialTheme.typography.labelSmall)
                            } else {
                                TextButton(onClick = { store.updateExtra(routine.id, "done_$today") }) {
                                    Text(stringResource(R.string.life_skincare_done))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.life_cycle), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAdd("cycle") }) { Text(stringResource(R.string.add)) }
                    }
                    val cycles = store.extras.filter { it.kind == "cycle" }.sortedByDescending { it.createdAt }
                    if (cycles.size < 2) {
                        Text(stringResource(R.string.life_cycle_none), style = MaterialTheme.typography.bodySmall)
                    } else {
                        val avg = cycles.zipWithNext().mapNotNull { (a, b) ->
                            val first = a.title.toLongOrNull()
                            val second = b.title.toLongOrNull()
                            if (first != null && second != null) (first - second).toInt() else null
                        }.average().toInt()
                        Text(stringResource(R.string.life_cycle_avg, avg))
                    }
                    cycles.take(6).forEach { cycle ->
                        Text(LocalDate.ofEpochDay(cycle.title.toLongOrNull() ?: today).toString() + " · " + cycle.detail, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { MemoryCard(onAdd = { onAdd("memory") }) }
    }
}

@Composable
private fun MemoryCard(onAdd: () -> Unit) {
    val store = LocalStore.current
    val cards = store.extras.filter { it.kind == "memory" }
    var index by remember { mutableStateOf(0) }
    var reveal by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.life_memory), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onAdd) { Text(stringResource(R.string.add)) }
            }
            if (cards.isEmpty()) {
                Text(stringResource(R.string.moneypro_no_data), style = MaterialTheme.typography.bodySmall)
            } else {
                val card = cards[index.coerceIn(0, cards.lastIndex)]
                Text(card.title, style = MaterialTheme.typography.titleLarge)
                if (reveal) Text(card.detail, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { reveal = !reveal }) {
                        Text(stringResource(if (reveal) R.string.life_memory_hide else R.string.life_memory_show))
                    }
                    TextButton(onClick = {
                        index = (index + 1) % cards.size
                        reveal = false
                    }) { Text(stringResource(R.string.life_memory_next)) }
                }
            }
        }
    }
}

@Composable
private fun LearnTab(onAdd: (String) -> Unit) {
    val store = LocalStore.current
    val clipboard = LocalClipboardManager.current
    var subject by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("30") }
    var prompt by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var planStatus by remember { mutableStateOf("") }
    var priorQuestion by remember { mutableStateOf(0 to 0) }
    var priorAnswer by remember { mutableStateOf("") }
    var priorResult by remember { mutableStateOf("") }
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.life_prompt), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.life_prompt_subject)) }, singleLine = true)
                    OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text(stringResource(R.string.life_prompt_level)) }, singleLine = true)
                    OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text(stringResource(R.string.life_prompt_goal)) }, singleLine = true)
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text(stringResource(R.string.life_prompt_minutes)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = {
                            prompt = "أريد خطة تعلم لموضوع " + subject + " بمستوى " + level + " وهدف " + goal +
                                ". خصص " + minutes + " دقيقة يومياً، وقسّم الخطة إلى أسابيع، وأضف تمريناً عملياً واختباراً قصيراً لكل أسبوع."
                        }) { Text(stringResource(R.string.life_prompt_build)) }
                        if (prompt.isNotBlank()) {
                            TextButton(onClick = { clipboard.setText(AnnotatedString(prompt)) }) {
                                Text(stringResource(R.string.life_prompt_copy))
                            }
                        }
                    }
                    if (prompt.isNotBlank()) Text(prompt, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.life_plan_import), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = plan, onValueChange = { plan = it }, label = { Text(stringResource(R.string.life_plan_hint)) }, minLines = 3, maxLines = 8)
                    OutlinedButton(onClick = {
                        val rows = plan.lines().mapNotNull { line ->
                            val parts = line.split("|").map { it.trim() }
                            if (parts.size >= 2) {
                                val target = parts.getOrNull(2)?.toIntOrNull() ?: parts[1].toIntOrNull() ?: 0
                                val mins = parts[1].toIntOrNull() ?: 0
                                if (parts[0].isNotBlank() && target > 0) Triple(parts[0], mins, target) else null
                            } else null
                        }
                        rows.forEach { store.addLearn(it.first, it.third) }
                        planStatus = context.getString(if (rows.isEmpty()) R.string.life_plan_empty else R.string.life_plan_done, rows.size)
                        if (rows.isNotEmpty()) plan = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.life_plan_import_btn)) }
                    if (planStatus.isNotBlank()) Text(planStatus, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.life_note_add), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAdd("note") }) { Text(stringResource(R.string.add)) }
            }
        }
        items(store.extras.filter { it.kind == "note" }) { note ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(note.title, fontWeight = FontWeight.Bold)
                    if (note.detail.isNotBlank()) Text(note.detail)
                }
            }
        }
        item { VoiceCard(onSaved = { onAdd("audio") }) }
        item {
            Text(stringResource(R.string.life_balance), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.life_balance_hint), style = MaterialTheme.typography.bodySmall)
            store.learn.forEach { item ->
                val ratio = if (item.target <= 0) 0f else (item.minutes.toFloat() / item.target).coerceIn(0f, 1f)
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(item.title + "  " + item.minutes + " / " + item.target)
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.life_tracks), style = MaterialTheme.typography.titleMedium)
                    listOf("English", "Programming", "Design", "Business").forEach { track ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(track, modifier = Modifier.weight(1f))
                            TextButton(onClick = { store.addLearn(track, 60) }) { Text(stringResource(R.string.life_track_add)) }
                        }
                    }
                    Text(stringResource(R.string.life_level_test), style = MaterialTheme.typography.titleMedium)
                    var chosen by remember { mutableStateOf(0) }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf(R.string.life_level_beginner, R.string.life_level_mid, R.string.life_level_advanced)) { label ->
                            FilterChip(selected = chosen == label, onClick = { chosen = label }, label = { Text(stringResource(label)) })
                        }
                    }
                    if (chosen != 0) Text(stringResource(R.string.life_level_result, stringResource(chosen)))
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.life_prior_skills), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.life_prior_hint), style = MaterialTheme.typography.bodySmall)
                    if (priorQuestion.first == 0) {
                        OutlinedButton(onClick = {
                            val a = (2..9).random()
                            val b = (2..9).random()
                            priorQuestion = a to b
                        }) { Text(stringResource(R.string.life_prior_start)) }
                    } else {
                        Text(stringResource(R.string.life_prior_question, priorQuestion.first, priorQuestion.second))
                        OutlinedTextField(value = priorAnswer, onValueChange = { priorAnswer = it }, label = { Text(stringResource(R.string.life_prior_answer)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                        TextButton(onClick = {
                            priorResult = context.getString(if (priorAnswer.toIntOrNull() == priorQuestion.first * priorQuestion.second) R.string.life_prior_pass else R.string.life_prior_fail)
                        }) { Text(stringResource(R.string.life_prior_answer)) }
                        if (priorResult.isNotBlank()) Text(priorResult)
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.life_suggest), style = MaterialTheme.typography.titleMedium)
            store.learn.take(6).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        val query = Uri.encode(item.title + " course podcast")
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")))
                    }) { Text(stringResource(R.string.life_suggest_open)) }
                }
            }
        }
    }
}

@Composable
private fun VoiceCard(onSaved: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var file by remember { mutableStateOf<File?>(null) }
    var status by remember { mutableStateOf("") }
    var playing by remember { mutableStateOf<MediaPlayer?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) status = context.getString(R.string.life_note_mic_denied)
    }
    fun start() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val target = File(context.filesDir, "voice-" + System.currentTimeMillis() + ".m4a")
        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(target.absolutePath)
            prepare()
            start()
        }
        file = target
    }
    fun stop() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        val target = file
        if (target != null && target.exists() && target.length() > 0) {
            store.addExtra("audio", target.name, target.absolutePath, 0.0, 0L)
            status = context.getString(R.string.life_note_saved)
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.life_note_audio), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { if (recorder == null) start() else stop() }) {
                    Text(stringResource(if (recorder == null) R.string.life_note_record else R.string.life_note_stop))
                }
            }
            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
            store.extras.filter { it.kind == "audio" }.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        runCatching {
                            playing?.release()
                            playing = MediaPlayer().apply {
                                setDataSource(item.detail)
                                prepare()
                                start()
                            }
                        }
                    }) { Text(stringResource(R.string.life_note_playing)) }
                    TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun FaithTab(onAdd: (String) -> Unit, onDue: (ExtraRecord) -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val today = LocalDate.now().toEpochDay()
    var lat by remember { mutableStateOf("21.4225") }
    var lng by remember { mutableStateOf("39.8262") }
    var qibla by remember { mutableStateOf<Int?>(null) }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val manager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            val location = runCatching { manager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) }.getOrNull()
                ?: runCatching { manager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER) }.getOrNull()
            if (location != null) {
                lat = String.format(Locale.US, "%.6f", location.latitude)
                lng = String.format(Locale.US, "%.6f", location.longitude)
            }
        }
    }
    val due = store.extras.filter { it.kind == "adhkar" && it.status != "done_$today" && it.title <= LocalTime.now().toString() }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.life_adhkar), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAdd("adhkar") }) { Text(stringResource(R.string.add)) }
                    }
                    if (due.isEmpty()) Text(stringResource(R.string.life_adhkar_none), style = MaterialTheme.typography.bodySmall)
                    due.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title + " · " + item.detail, modifier = Modifier.weight(1f), maxLines = 2)
                            TextButton(onClick = { onDue(item) }) { Text(stringResource(R.string.life_adhkar_due)) }
                        }
                    }
                    store.extras.filter { it.kind == "adhkar" }.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title + " · " + item.detail, modifier = Modifier.weight(1f), maxLines = 2)
                            TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.life_qibla), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text(stringResource(R.string.life_qibla_lat)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = lng, onValueChange = { lng = it }, label = { Text(stringResource(R.string.life_qibla_lng)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                            Text(stringResource(R.string.life_qibla_location))
                        }
                        OutlinedButton(onClick = {
                            val phi1 = Math.toRadians(lat.toDoubleOrNull() ?: 21.4225)
                            val phi2 = Math.toRadians(21.4225)
                            val delta = Math.toRadians((39.8262 - (lng.toDoubleOrNull() ?: 39.8262)))
                            val y = sin(delta)
                            val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(delta)
                            qibla = ((Math.toDegrees(atan2(y, x)) + 360) % 360).let { Math.round(it).toInt() }
                        }) { Text(stringResource(R.string.life_qibla)) }
                    }
                    qibla?.let { Text(stringResource(R.string.life_qibla_bearing, it), style = MaterialTheme.typography.titleMedium) }
                }
            }
        }
    }
}

@Composable
private fun BooksTab(onAdd: (String) -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.life_paid_books), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAdd("paidbook") }) { Text(stringResource(R.string.add)) }
            }
        }
        items(store.extras.filter { it.kind == "paidbook" }) { book ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(book.title, fontWeight = FontWeight.Bold)
                    Text(money(book.amount))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(book.detail))) }
                        }) { Text(stringResource(R.string.life_paid_open)) }
                        TextButton(onClick = { store.deleteExtra(book.id) }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.life_reader_notes), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAdd("readnote") }) { Text(stringResource(R.string.add)) }
            }
        }
        items(store.extras.filter { it.kind == "readnote" }) { note ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(note.title, fontWeight = FontWeight.Bold)
                    Text(note.detail)
                }
            }
        }
        item {
            Text(stringResource(R.string.life_book_task), style = MaterialTheme.typography.titleMedium)
            store.books.forEach { book ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(book.title + " (" + book.page + "/" + book.pages + ")", modifier = Modifier.weight(1f))
                    TextButton(onClick = { store.addTask(book.title, "", domain.model.Priority.NORMAL) }) {
                        Text(stringResource(R.string.life_book_task))
                    }
                }
            }
        }
    }
}

@Composable
private fun MindTab(onAdd: (String) -> Unit) {
    val store = LocalStore.current
    val moods = store.extras.filter { it.kind == "mood" }.sortedByDescending { it.createdAt }.take(14)
    val average = if (moods.isEmpty()) 0.0 else moods.sumOf { it.amount } / moods.size
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.life_mood), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAdd("mood") }) { Text(stringResource(R.string.add)) }
                    }
                    Text(stringResource(R.string.life_mood_avg, String.format(Locale.US, "%.1f", average)))
                    Text(
                        stringResource(
                            when {
                                average < 2.5 -> R.string.life_mood_low
                                average < 3.8 -> R.string.life_mood_mid
                                else -> R.string.life_mood_high
                            }
                        )
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.life_smart), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.life_smart_hint), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.life_strong_alarm), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.life_strong_hint), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(moods) { mood ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(mood.amount.toInt().toString() + "/5", modifier = Modifier.padding(end = 8.dp), fontWeight = FontWeight.Bold)
                    Text(mood.detail, modifier = Modifier.weight(1f))
                    TextButton(onClick = { store.deleteExtra(mood.id) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun LifeAddDialog(mode: String, onDismiss: () -> Unit) {
    val store = LocalStore.current
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0).toString()) }
    var severity by remember { mutableStateOf("3") }
    val titleLabel = when (mode) {
        "skin" -> R.string.life_skincare_add
        "note" -> R.string.life_note_title
        "memory" -> R.string.life_memory_question
        "adhkar" -> R.string.life_adhkar
        "paidbook" -> R.string.life_paid_add
        "readnote" -> R.string.life_note_title
        "mood" -> R.string.life_mood_add
        "cycle" -> R.string.life_cycle
        else -> R.string.life_note_audio
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleLabel)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (mode != "cycle" && mode != "mood") {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(titleLabel)) }, singleLine = true)
                }
                when (mode) {
                    "note", "readnote" -> OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_note_body)) }, minLines = 3, maxLines = 6)
                    "memory" -> OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_memory_answer)) }, minLines = 2, maxLines = 5)
                    "adhkar" -> {
                        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text(stringResource(R.string.life_adhkar_due)) }, singleLine = true)
                        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_adhkar_text)) }, minLines = 2, maxLines = 5)
                    }
                    "paidbook" -> {
                        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_paid_url)) }, singleLine = true)
                        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.life_paid_price)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    }
                    "cycle" -> {
                        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_cycle_symptoms)) }, singleLine = true)
                        OutlinedTextField(value = severity, onValueChange = { severity = it }, label = { Text(stringResource(R.string.life_cycle_flow)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    }
                    "mood" -> {
                        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.life_mood)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                        OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(R.string.life_mood_note)) }, minLines = 2, maxLines = 4)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (mode) {
                    "skin" -> store.addExtra("skin", title, "", 0.0, 0L)
                    "note" -> store.addExtra("note", title, detail, 0.0, 0L)
                    "memory" -> store.addExtra("memory", title, detail, 0.0, 0L)
                    "adhkar" -> store.addExtra("adhkar", time, detail, 0.0, 0L)
                    "paidbook" -> store.addExtra("paidbook", title, detail, amount.toDoubleOrNull() ?: 0.0, 0L)
                    "readnote" -> store.addExtra("readnote", title, detail, 0.0, 0L)
                    "cycle" -> store.addExtra("cycle", LocalDate.now().toEpochDay().toString(), detail, severity.toDoubleOrNull() ?: 0.0, 0L)
                    "mood" -> store.addExtra("mood", "", detail, (amount.toDoubleOrNull() ?: 3.0).coerceIn(1.0, 5.0), 0L)
                    "audio" -> store.addExtra("audio", title.ifBlank { "voice" }, detail, 0.0, 0L)
                }
                onDismiss()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun money(value: Double): String = String.format(Locale.US, "%.2f", value)
