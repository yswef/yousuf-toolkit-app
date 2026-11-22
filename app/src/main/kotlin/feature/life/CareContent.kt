package feature.life

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.SoundFx
import data.local.LocalStore
import data.local.Store
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ---------------- العناية بالبشرة ----------------

private data class SkinType(val key: String, val label: String, val serum: String, val moisturizer: String, val exfoliate: String)

private val skinTypes = listOf(
    SkinType("normal", "عادية", "سيروم مرطب خفيف", "مرطب متوازن", "تقشير لطيف مرة أسبوعياً"),
    SkinType("oily", "دهنية", "سيروم نياسيناميد لتنظيم الدهون", "مرطب جل خفيف خالٍ من الزيوت", "تقشير BHA مرة إلى مرتين أسبوعياً"),
    SkinType("dry", "جافة", "سيروم هيالورونيك على بشرة رطبة", "كريم غني بالسيراميد", "تقشير لطيف جداً كل 10 أيام"),
    SkinType("combo", "مختلطة", "سيروم نياسيناميد", "مرطب جل كريمي", "تقشير لطيف مرة أسبوعياً لمنطقة T"),
    SkinType("sensitive", "حساسة", "سيروم بانثينول أو سنتيلا مهدئ", "مرطب خالٍ من العطور", "بلا تقشير كيميائي قوي؛ اكتفِ بالغسول اللطيف")
)

private data class Step(val key: String, val title: String, val note: String)

private fun morningSteps(t: SkinType) = listOf(
    Step("am1", "غسول لطيف", "بماء فاتر لمدة 30 ثانية ثم جفف بلا فرك."),
    Step("am2", t.serum, "ضع قطرات قليلة على بشرة نظيفة."),
    Step("am3", t.moisturizer, "طبقة رقيقة تكفي."),
    Step("am4", "واقي شمس SPF 30 أو أعلى", "حتى في الأيام الغائمة، وأعد وضعه كل 3 ساعات خارج المنزل.")
)

private fun eveningSteps(t: SkinType) = listOf(
    Step("pm1", "إزالة الواقي والمكياج", "ماء ميسيلار أو زيت منظف إن كنت تضع واقياً أو مكياجاً."),
    Step("pm2", "غسول لطيف", "غسلة ثانية تزيل الأوساخ والزيوت."),
    Step("pm3", t.serum, "قبل المرطب بدقيقة."),
    Step("pm4", t.moisturizer, "ليرمم الجلد ليلاً.")
)

private fun epochDay() = LocalDate.now().toEpochDay()

private fun stepDone(store: Store, key: String, today: Long) =
    store.extras.any { it.kind == "skinstep" && it.title == key && it.status == "done_$today" }

private fun toggleStep(store: Store, key: String, today: Long) {
    var e = store.extras.firstOrNull { it.kind == "skinstep" && it.title == key }
    if (e == null) {
        store.addExtra("skinstep", key, "", 0.0, 0L)
        e = store.extras.firstOrNull { it.kind == "skinstep" && it.title == key }
    }
    if (e != null) store.updateExtra(e.id, if (e.status == "done_$today") "open" else "done_$today")
}

@Composable
fun SkinRoutineCard() {
    val store = LocalStore.current
    val today = epochDay()
    val saved = store.extras.firstOrNull { it.kind == "skintype" }?.title
    val type = skinTypes.firstOrNull { it.key == saved } ?: skinTypes[0]
    var tips by remember { mutableStateOf(false) }

    fun pickType(key: String) {
        val e = store.extras.firstOrNull { it.kind == "skintype" }
        if (e == null) store.addExtra("skintype", key, "", 0.0, 0L) else {
            store.deleteExtra(e.id)
            store.addExtra("skintype", key, "", 0.0, 0L)
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.care_skin_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.care_skin_type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(skinTypes.size) { i ->
                    val t = skinTypes[i]
                    FilterChip(selected = t.key == type.key, onClick = { pickType(t.key) }, label = { Text(t.label) })
                }
            }
            Text(stringResource(R.string.care_morning), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            morningSteps(type).forEach { StepRow(store, it, today) }
            Text(stringResource(R.string.care_evening), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            eveningSteps(type).forEach { StepRow(store, it, today) }
            Text(stringResource(R.string.care_weekly), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(type.exfoliate, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { tips = !tips }) { Text(stringResource(if (tips) R.string.care_hide_tips else R.string.care_show_tips)) }
            if (tips) {
                listOf(
                    "اختبر أي منتج جديد على باطن الذراع 24 ساعة قبل استعماله على الوجه.",
                    "أدخل منتجاً واحداً جديداً كل أسبوعين لتعرف ما يناسبك.",
                    "لا تفرك الوجه بقوة ولا تستعمل ماءً ساخناً جداً.",
                    "اشرب الماء وانم 7 ساعات على الأقل؛ فالبشرة تتجدد ليلاً.",
                    "غيّر غطاء الوسادة أسبوعياً ونظّف هاتفك قبل ملامسته لوجهك.",
                    "إن ظهر التهاب شديد أو حبوب مؤلمة أو بقع متغيرة فاستشر طبيب جلدية."
                ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun StepRow(store: Store, step: Step, today: Long) {
    val done = stepDone(store, step.key, today)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = done, onCheckedChange = {
            toggleStep(store, step.key, today)
            SoundFx.play(if (!done) SoundFx.Fx.SUCCESS else SoundFx.Fx.TAP)
        })
        Column(Modifier.weight(1f)) {
            Text(step.title, style = MaterialTheme.typography.bodyMedium)
            Text(step.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------- العناية بالأكل ----------------

private data class Goal(val key: String, val label: String, val target: Int, val hint: String)

private val foodGoals = listOf(
    Goal("veg", "خضار", 3, "حصة = كوب خضار نيئة أو نصف كوب مطبوخة"),
    Goal("fruit", "فواكه", 2, "حبة متوسطة أو كوب مقطّع"),
    Goal("protein", "بروتين", 3, "بيض، دجاج، سمك، لحم، عدس، فول، لبن"),
    Goal("grain", "حبوب كاملة", 2, "خبز أسمر، شوفان، أرز بني"),
    Goal("water", "ماء (أكواب)", 8, "ثمانية أكواب تقريباً، وأكثر مع الحر والرياضة")
)

private val mealTypes = listOf("فطور", "غداء", "عشاء", "وجبة خفيفة")

private val mealIdeas = listOf(
    "فطور" to listOf("فول بزيت الزيتون مع خبز أسمر وخضار", "شوفان بالحليب والموز والقرفة", "بيض مسلوق مع طماطم وخيار"),
    "غداء" to listOf("دجاج مشوي مع أرز بني وسلطة", "سمك مشوي مع خضار مطبوخة", "عدس بالخضار مع خبز أسمر", "شوربة لحم قليلة الدهن مع سلطة"),
    "عشاء" to listOf("زبادي مع خيار وخبز محمص", "شوربة خضار دافئة", "تونة مع سلطة وزيت زيتون"),
    "وجبة خفيفة" to listOf("حفنة مكسرات غير مملحة", "تفاحة أو موزة", "جزر وحمص", "كوب لبن بلا سكر")
)

private fun goalCount(store: Store, key: String, today: Long): Int {
    val e = store.extras.firstOrNull { it.kind == "food" && it.title == key }
    return if (e != null && e.status == "d$today") e.amount.toInt() else 0
}

private fun changeGoal(store: Store, key: String, today: Long, delta: Int) {
    var e = store.extras.firstOrNull { it.kind == "food" && it.title == key }
    if (e == null) {
        store.addExtra("food", key, "", 0.0, 0L)
        e = store.extras.firstOrNull { it.kind == "food" && it.title == key }
    }
    if (e != null) {
        val base = if (e.status == "d$today") e.amount.toInt() else 0
        store.updateExtra(e.id, "d$today", (base + delta).coerceAtLeast(0).toDouble())
    }
}

private fun isToday(at: Long): Boolean =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()

@Composable
fun NutritionCard() {
    val store = LocalStore.current
    val today = epochDay()
    var adding by remember { mutableStateOf(false) }
    var ideas by remember { mutableStateOf(false) }
    val meals = store.extras.filter { it.kind == "meal" && isToday(it.createdAt) }.sortedBy { it.createdAt }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.care_food_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.care_food_goals), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            foodGoals.forEach { g ->
                val n = goalCount(store, g.key, today)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${g.label}: $n / ${g.target}", style = MaterialTheme.typography.bodyMedium, fontWeight = if (n >= g.target) FontWeight.Bold else FontWeight.Normal)
                        Text(g.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { changeGoal(store, g.key, today, -1) }, enabled = n > 0) { Text("−") }
                    TextButton(onClick = {
                        changeGoal(store, g.key, today, 1)
                        SoundFx.play(if (n + 1 == g.target) SoundFx.Fx.SUCCESS else SoundFx.Fx.TAP)
                    }) { Text("+") }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val kcal = meals.sumOf { it.amount }.toInt()
                Text(
                    stringResource(R.string.care_meals_today, meals.size, kcal),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { adding = true }) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add)) }
            }
            if (meals.isEmpty()) Text(stringResource(R.string.care_meals_none), style = MaterialTheme.typography.bodySmall)
            meals.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(m.title, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            m.detail + if (m.amount > 0) " • ${m.amount.toInt()} kcal" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { store.deleteExtra(m.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
            TextButton(onClick = { ideas = !ideas }) { Text(stringResource(if (ideas) R.string.care_hide_tips else R.string.care_food_ideas)) }
            if (ideas) {
                mealIdeas.forEach { (type, list) ->
                    Text(type, style = MaterialTheme.typography.titleSmall)
                    list.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
                Text(stringResource(R.string.care_disclaimer), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (adding) {
        var name by remember { mutableStateOf("") }
        var kcal by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(mealTypes[0]) }
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.care_meal_add)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(stringResource(R.string.care_meal_name)) })
                    OutlinedTextField(
                        value = kcal,
                        onValueChange = { kcal = it.filter { c -> c.isDigit() }.take(5) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(R.string.care_meal_kcal)) }
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(mealTypes.size) { i ->
                            FilterChip(selected = mealTypes[i] == type, onClick = { type = mealTypes[i] }, label = { Text(mealTypes[i]) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (store.addExtra("meal", name, type, kcal.toDoubleOrNull() ?: 0.0, 0L)) {
                        SoundFx.play(SoundFx.Fx.TAP)
                        adding = false
                    }
                }, enabled = name.isNotBlank()) { Text(stringResource(R.string.add)) }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
