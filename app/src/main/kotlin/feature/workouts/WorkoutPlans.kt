package feature.workouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.SoundFx
import data.local.Store

data class Exercise(val name: String, val dose: String, val tip: String)

data class WorkoutPlan(
    val id: String,
    val title: String,
    val summary: String,
    val level: String,
    val kind: String,      // strength | cardio | stretch | walk (من workoutKeys)
    val minutes: Int,
    val exercises: List<Exercise>
)

private fun E(n: String, d: String, t: String) = Exercise(n, d, t)

val workoutPlans: List<WorkoutPlan> = listOf(
    WorkoutPlan(
        "home-full", "كامل الجسم في المنزل (بلا أدوات)", "ثلاث جولات سريعة تشغّل كل العضلات الكبيرة.", "مبتدئ", "strength", 25,
        listOf(
            E("إحماء: مشي في المكان + دوران الكتفين", "3 دقائق", "ابدأ ببطء ثم زد الإيقاع."),
            E("سكوات (قرفصاء)", "3 × 12", "الركبتان باتجاه القدمين، والظهر مستقيم."),
            E("ضغط (على الركبتين للمبتدئ)", "3 × 8", "الجسم خط مستقيم، ونزول بتحكم."),
            E("طعنات (لانج) بالتبادل", "3 × 10 لكل رجل", "الركبة الأمامية لا تتجاوز أصابع القدم."),
            E("بلانك", "3 × 30 ثانية", "شدّ البطن ولا ترفع الحوض."),
            E("جسر الحوض", "3 × 15", "اضغط بالكعبين وشدّ الأرداف أعلى الحركة."),
            E("تمدد ختامي", "4 دقائق", "تنفس بعمق وثبّت كل وضعية 20 ثانية.")
        )
    ),
    WorkoutPlan(
        "abs10", "بطن وجذع في 10 دقائق", "تمارين مركّزة للجذع بلا قفز.", "مبتدئ", "strength", 10,
        listOf(
            E("كرنش", "3 × 15", "ارفع الكتفين فقط ولا تسحب الرقبة."),
            E("رفع الساقين (مستلقياً)", "3 × 12", "الظهر لاصق بالأرض."),
            E("بلانك جانبي", "2 × 25 ثانية لكل جهة", "الجسم في خط واحد."),
            E("تسلّق الجبل (Mountain climbers)", "3 × 20 ثانية", "بإيقاع ثابت وتنفس منتظم."),
            E("تمرين الطائر-الكلب (Bird dog)", "2 × 10 لكل جهة", "ثبّت الحوض ولا تهتز.")
        )
    ),
    WorkoutPlan(
        "hiit20", "كارديو HIIT قصير", "فترات جهد عالٍ وراحة قصيرة لحرق السعرات.", "متوسط", "cardio", 20,
        listOf(
            E("إحماء خفيف", "3 دقائق", "هرولة في المكان وفتح الذراعين."),
            E("قفز الحبل (أو بدونه)", "40 ثانية جهد / 20 راحة × 3", "اهبط على مقدمة القدم."),
            E("ركبة عالية", "40 ثانية / 20 راحة × 3", "ارفع الركبة لمستوى الحوض."),
            E("بيربي مبسّط (بدون قفز)", "40 ثانية / 20 راحة × 3", "حافظ على استقامة الظهر."),
            E("سكوات قفز", "40 ثانية / 20 راحة × 3", "هبوط ناعم بركبتين مثنيتين."),
            E("تهدئة وتمدد", "4 دقائق", "ابطئ التنفس تدريجياً.")
        )
    ),
    WorkoutPlan(
        "stretch-am", "تمدد الصباح", "لفك تيبس النوم وتنشيط الدورة الدموية.", "للجميع", "stretch", 8,
        listOf(
            E("دوران الرقبة والكتفين", "1 دقيقة", "حركات بطيئة بلا ألم."),
            E("تمدد القط والجمل", "10 تكرارات", "مع الزفير اقوّس الظهر."),
            E("تمدد أمامي للأرجل (واقفاً)", "30 ثانية", "الركبتان مرنتان ولا تقفل."),
            E("تمدد الصدر عند الجدار", "30 ثانية لكل جهة", "لا تدفع للألم."),
            E("وضعية الطفل", "45 ثانية", "تنفس في أسفل الظهر.")
        )
    ),
    WorkoutPlan(
        "desk", "لمن يجلس طويلاً (ظهر ورقبة)", "تمارين قصيرة تفعلها بين ساعات العمل.", "للجميع", "stretch", 7,
        listOf(
            E("تمدد الرقبة الجانبي", "20 ثانية لكل جهة", "ارخِ الكتف المقابل."),
            E("دوران الكتفين للخلف", "15 تكراراً", "اجمع لوحي الكتف."),
            E("التفاف الجذع جالساً", "20 ثانية لكل جهة", "استقم قبل الالتفاف."),
            E("تمدد أسفل الظهر (ركبة للصدر)", "20 ثانية لكل رجل", "تنفس بهدوء."),
            E("الوقوف والمشي", "دقيقتان", "اشرب كوب ماء أثناء ذلك.")
        )
    ),
    WorkoutPlan(
        "walk30", "مشي سريع 30 دقيقة", "أبسط روتين لصحة القلب، ويناسب الجميع.", "مبتدئ", "walk", 30,
        listOf(
            E("مشي بطيء للإحماء", "5 دقائق", "تنفس بعمق."),
            E("مشي سريع (تتكلم بصعوبة قليلة)", "20 دقيقة", "ذراعان تتحركان وكتفان مرتخيان."),
            E("مشي بطيء للتهدئة", "5 دقائق", "ثم تمدد الساقين.")
        )
    ),
    WorkoutPlan(
        "upper-db", "الجزء العلوي بالدمبل", "لمن يملك دمبل أو زجاجات ماء ثقيلة.", "متوسط", "strength", 30,
        listOf(
            E("ضغط الكتف", "3 × 10", "لا تقوّس أسفل الظهر."),
            E("تجديف بذراع واحدة", "3 × 12 لكل جهة", "اسحب المرفق للخلف قرب الجسم."),
            E("رفرفة جانبية", "3 × 12", "ارفع حتى مستوى الكتف فقط."),
            E("بايسبس كيرل", "3 × 12", "ثبّت المرفقين بجانب الجسم."),
            E("ترايسبس خلف الرأس", "3 × 12", "المرفقان للأعلى ثابتان."),
            E("تمدد الكتفين والصدر", "3 دقائق", "تنفس ببطء.")
        )
    )
)

fun LazyListScope.workoutPlansSection(onOpen: (WorkoutPlan) -> Unit) {
    item(key = "plans-head") {
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.plans_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.plans_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    workoutPlans.forEach { plan ->
        item(key = "plan-" + plan.id) {
            Card(onClick = { onOpen(plan) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(plan.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.plan_meta, plan.level, plan.exercises.size, plan.minutes),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanSheet(plan: WorkoutPlan, store: Store, onDismiss: () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val done = remember(plan.id) { plan.exercises.map { false }.toMutableStateList() }
    val count = done.count { it }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(plan.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(plan.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = { if (plan.exercises.isNotEmpty()) count.toFloat() / plan.exercises.size else 0f },
                modifier = Modifier.fillMaxWidth()
            )
            plan.exercises.forEachIndexed { i, ex ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        done[i] = !done[i]
                        SoundFx.play(if (done[i]) SoundFx.Fx.TAP else SoundFx.Fx.SWIPE)
                    }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(checked = done[i], onCheckedChange = { done[i] = it })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            ex.name,
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = if (done[i]) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(ex.dose, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(ex.tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text(stringResource(R.string.plan_safety), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = {
                    store.addWorkout(plan.kind, plan.minutes)
                    SoundFx.play(SoundFx.Fx.LEVEL_UP)
                    onDismiss()
                },
                enabled = count == plan.exercises.size,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.plan_finish, plan.minutes)) }
        }
    }
}
