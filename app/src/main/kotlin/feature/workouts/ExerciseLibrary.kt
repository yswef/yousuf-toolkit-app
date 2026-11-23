package feature.workouts

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * مكتبة التمارين: بيانات مستودع free-exercise-db (ملكية عامة) محزومة في assets/exercises/exercises.json.
 * الصور تُجلب عند الحاجة من المستودع نفسه وتُخزَّن مؤقتاً (ImageLoader).
 */
data class LibExercise(
    val id: String,
    val name: String,
    val category: String,
    val level: String,
    val equipment: String,
    val primary: List<String>,
    val secondary: List<String>,
    val steps: List<String>,
    val images: Int
) {
    fun imageUrl(index: Int): String = EXERCISE_IMAGE_BASE + id + "/" + index + ".jpg"
}

const val EXERCISE_IMAGE_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

object ExerciseLibrary {
    @Volatile private var cache: List<LibExercise>? = null

    suspend fun load(context: Context): List<LibExercise> = withContext(Dispatchers.IO) {
        cache ?: parse(context).also { cache = it }
    }

    private fun parse(context: Context): List<LibExercise> {
        val text = context.assets.open("exercises/exercises.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)
        val out = ArrayList<LibExercise>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                LibExercise(
                    id = o.getString("id"),
                    name = o.getString("n"),
                    category = o.getString("c"),
                    level = o.getString("l"),
                    equipment = o.getString("q"),
                    primary = o.getJSONArray("p").strings(),
                    secondary = o.getJSONArray("s").strings(),
                    steps = o.getJSONArray("i").strings(),
                    images = o.optInt("m", 0)
                )
            )
        }
        return out
    }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
}

val muscleLabels: Map<String, String> = linkedMapOf(
    "chest" to "الصدر",
    "shoulders" to "الأكتاف",
    "biceps" to "البايسبس",
    "triceps" to "الترايسبس",
    "forearms" to "الساعدان",
    "lats" to "الظهر العريض",
    "middle back" to "منتصف الظهر",
    "lower back" to "أسفل الظهر",
    "traps" to "الترابيس",
    "neck" to "الرقبة",
    "abdominals" to "البطن",
    "quadriceps" to "أمام الفخذ",
    "hamstrings" to "خلف الفخذ",
    "glutes" to "الأرداف",
    "calves" to "السمانة",
    "adductors" to "داخل الفخذ",
    "abductors" to "خارج الفخذ"
)

val equipmentLabels: Map<String, String> = linkedMapOf(
    "body only" to "بلا أدوات",
    "dumbbell" to "دمبل",
    "barbell" to "بار",
    "kettlebells" to "كيتل بيل",
    "bands" to "أحزمة مطاطية",
    "cable" to "كابل",
    "machine" to "جهاز",
    "e-z curl bar" to "بار EZ",
    "exercise ball" to "كرة تمارين",
    "medicine ball" to "كرة طبية",
    "foam roll" to "أسطوانة فوم",
    "other" to "أخرى",
    "none" to "غير محدد"
)

val levelLabels: Map<String, String> = linkedMapOf(
    "beginner" to "مبتدئ",
    "intermediate" to "متوسط",
    "expert" to "متقدم"
)

val categoryLabels: Map<String, String> = linkedMapOf(
    "strength" to "قوة",
    "stretching" to "تمدد",
    "cardio" to "كارديو",
    "plyometrics" to "بليومتري",
    "powerlifting" to "رفع القوة",
    "olympic weightlifting" to "رفع أولمبي",
    "strongman" to "سترونج مان"
)
