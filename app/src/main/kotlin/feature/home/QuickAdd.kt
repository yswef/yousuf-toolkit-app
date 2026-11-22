package feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.drawBookSymbol
import data.local.Store
import domain.model.Priority
import domain.model.workoutKeys

private enum class QuickType(val label: Int, val symbol: BookSymbol) {
    TASK(R.string.quick_task, BookSymbol.STAR),
    MONEY(R.string.quick_money, BookSymbol.DIAMOND),
    WATER(R.string.quick_water, BookSymbol.DROP),
    WORKOUT(R.string.quick_workout, BookSymbol.FLAME),
    JOURNAL(R.string.quick_journal, BookSymbol.RUNE),
    CAMPAIGN(R.string.quick_campaign, BookSymbol.ORB)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(store: Store, onDismiss: () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(QuickType.TASK) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var income by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var workout by remember { mutableStateOf(workoutKeys.first()) }
    var minutes by remember { mutableStateOf("20") }
    var target by remember { mutableStateOf(21) }
    var cups by remember { mutableStateOf(1) }

    val defaultMoneyTitle = stringResource(R.string.quick_money_default)
    val canSave = when (type) {
        QuickType.TASK, QuickType.JOURNAL -> title.isNotBlank() || note.isNotBlank()
        QuickType.MONEY -> (amount.toDoubleOrNull() ?: 0.0) > 0.0
        QuickType.WORKOUT -> (minutes.toIntOrNull() ?: 0) > 0
        QuickType.WATER -> true
        QuickType.CAMPAIGN -> title.isNotBlank() && target > 0
    }

    fun save() {
        when (type) {
            QuickType.TASK -> store.addTask(title, note, priority)
            QuickType.MONEY -> {
                val value = amount.toDoubleOrNull() ?: return
                store.addLedger(title.ifBlank { defaultMoneyTitle }, value, income, "other")
            }
            QuickType.WATER -> repeat(cups) { store.addWater(1) }
            QuickType.WORKOUT -> store.addWorkout(workout, minutes.toIntOrNull() ?: 20)
            QuickType.JOURNAL -> store.addEntry(title, note)
            QuickType.CAMPAIGN -> store.addCampaign(title, target)
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.quick_add),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.quick_add_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SmartLine(store, onDismiss)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(QuickType.TASK, QuickType.MONEY, QuickType.WATER).forEach { item ->
                    TypeCard(item, type == item, Modifier.weight(1f)) { type = item }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(QuickType.WORKOUT, QuickType.JOURNAL, QuickType.CAMPAIGN).forEach { item ->
                    TypeCard(item, type == item, Modifier.weight(1f)) { type = item }
                }
            }

            when (type) {
                QuickType.TASK -> {
                    Field(stringResource(R.string.quick_title), title) { title = it }
                    Field(stringResource(R.string.quick_note), note) { note = it }
                    Label(stringResource(R.string.quick_priority))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Priority.entries.forEach { value ->
                            FilterChip(
                                selected = priority == value,
                                onClick = { priority = value },
                                label = { Text(priorityLabel(value)) }
                            )
                        }
                    }
                }
                QuickType.MONEY -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !income, onClick = { income = false }, label = { Text(stringResource(R.string.quick_expense)) })
                        FilterChip(selected = income, onClick = { income = true }, label = { Text(stringResource(R.string.quick_income)) })
                    }
                    Field(stringResource(R.string.quick_title), title) { title = it }
                    Field(stringResource(R.string.quick_amount), amount, number = true) { amount = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("10", "50", "100", "500").forEach { value ->
                            FilterChip(
                                selected = amount == value,
                                onClick = { amount = value },
                                label = { Text("+$value") }
                            )
                        }
                    }
                }
                QuickType.WATER -> {
                    Text(
                        stringResource(R.string.stats_water) + ": " + store.waterToday(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3).forEach { value ->
                            FilterChip(
                                selected = cups == value,
                                onClick = { cups = value },
                                label = { Text("$value") }
                            )
                        }
                    }
                }
                QuickType.WORKOUT -> {
                    Label(stringResource(R.string.quick_kind))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        workoutKeys.forEach { value ->
                            FilterChip(
                                selected = workout == value,
                                onClick = { workout = value },
                                label = { Text(workoutLabel(value)) }
                            )
                        }
                    }
                    Field(stringResource(R.string.quick_minutes), minutes, number = true) { minutes = it }
                }
                QuickType.JOURNAL -> {
                    Field(stringResource(R.string.quick_title), title) { title = it }
                    Field(stringResource(R.string.quick_body), note) { note = it }
                }
                QuickType.CAMPAIGN -> {
                    Field(stringResource(R.string.quick_title), title) { title = it }
                    Label(stringResource(R.string.quick_days))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 21, 30, 60).forEach { value ->
                            FilterChip(
                                selected = target == value,
                                onClick = { target = value },
                                label = { Text("$value") }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { save() },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(stringResource(R.string.quick_save))
            }
        }
    }
}

@Composable
private fun TypeCard(type: QuickType, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(Modifier.size(24.dp)) {
            drawBookSymbol(type.symbol, color, Offset(size.width / 2, size.height / 2), size.minDimension * 0.42f)
        }
        Text(stringResource(type.label), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun Field(label: String, value: String, number: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.LOW -> stringResource(R.string.priority_low)
    Priority.NORMAL -> stringResource(R.string.priority_normal)
    Priority.HIGH -> stringResource(R.string.priority_high)
}

@Composable
private fun workoutLabel(key: String): String = when (key) {
    "strength" -> stringResource(R.string.workout_strength)
    "cardio" -> stringResource(R.string.workout_cardio)
    "stretch" -> stringResource(R.string.workout_stretch)
    else -> stringResource(R.string.workout_walk)
}

private sealed class Smart {
    data class TaskItem(val title: String) : Smart()
    data class Money(val title: String, val amount: Double, val income: Boolean) : Smart()
    data class Water(val cups: Int) : Smart()
}

private fun normalizeDigits(t: String): String = buildString {
    for (c in t) append(if (c in '\u0660'..'\u0669') ('0' + (c - '\u0660')) else if (c in '\u06F0'..'\u06F9') ('0' + (c - '\u06F0')) else c)
}

private val incomeWords = listOf("راتب", "دخل", "ربح", "استلمت", "مكافأة", "salary", "income")

private fun parseSmart(raw: String): Smart? {
    val text = normalizeDigits(raw.trim())
    if (text.isBlank()) return null
    val tokens = text.split(Regex("\\s+"))
    val first = tokens.first().lowercase()
    val number = tokens.firstNotNullOfOrNull { it.trimStart('+', '-').toDoubleOrNull() }
    if (first == "ماء" || first == "water") {
        return Smart.Water((number?.toInt() ?: 1).coerceIn(1, 20))
    }
    if (first == "مهمة" || first == "task") {
        val title = tokens.drop(1).joinToString(" ")
        return if (title.isBlank()) null else Smart.TaskItem(title)
    }
    if (number != null && number > 0.0) {
        val numToken = tokens.first { it.trimStart('+', '-').toDoubleOrNull() != null }
        val title = tokens.filter { it != numToken }.joinToString(" ")
        val income = numToken.startsWith("+") || incomeWords.any { text.contains(it, ignoreCase = true) }
        return Smart.Money(title, number, income)
    }
    return Smart.TaskItem(text)
}

@Composable
private fun SmartLine(store: Store, onDone: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val parsed = remember(text) { parseSmart(text) }
    val defaultTitle = stringResource(R.string.quick_money_default)

    fun apply() {
        when (val p = parsed) {
            is Smart.TaskItem -> store.addTask(p.title, "", Priority.NORMAL)
            is Smart.Money -> store.addLedger(p.title.ifBlank { defaultTitle }, p.amount, p.income, "other")
            is Smart.Water -> repeat(p.cups) { store.addWater(1) }
            null -> return
        }
        core.design.SoundFx.play(core.design.SoundFx.Fx.SUCCESS)
        onDone()
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.smart_hint)) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { apply() })
        )
        val label = when (val p = parsed) {
            is Smart.TaskItem -> stringResource(R.string.smart_task, p.title)
            is Smart.Money -> stringResource(if (p.income) R.string.smart_income else R.string.smart_expense, p.title, amountPlain(p.amount))
            is Smart.Water -> stringResource(R.string.smart_water, p.cups)
            null -> null
        }
        if (label != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                androidx.compose.material3.Button(onClick = { apply() }) { Text(stringResource(R.string.add)) }
            }
        }
    }
}

private fun amountPlain(v: Double): String = if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
