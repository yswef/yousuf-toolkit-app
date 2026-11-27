// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.learn

import android.content.Context
import android.net.Uri
import domain.model.LearningLessonDraft
import domain.model.LearningPlanDraft
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipInputStream

object LearningZipImporter {
    fun parse(context: Context, uri: Uri): LearningPlanDraft {
        val files = mutableMapOf<String, String>()
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("unable to open learning zip")
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val bytes = zip.readBytes()
                    if (bytes.size > 8 * 1024 * 1024) throw IllegalArgumentException("learning zip file is too large")
                    files[entry.name.substringAfterLast('/')] = String(bytes, Charsets.UTF_8)
                }
                zip.closeEntry()
            }
        }
        val planJson = files["plan.json"] ?: throw IllegalArgumentException("plan.json is missing")
        val siteHtml = files["site.html"] ?: throw IllegalArgumentException("site.html is missing")
        val courseMd = files["course.md"].orEmpty()
        val roadmapXml = files["roadmap.xml"].orEmpty()
        val root = JSONObject(planJson)
        val lessonArray = root.optJSONArray("lessons")
        val lessons = (0 until (lessonArray?.length() ?: 0)).mapNotNull { index ->
            val item = lessonArray?.optJSONObject(index) ?: return@mapNotNull null
            LearningLessonDraft(
                title = item.optString("title", ""),
                minutes = item.optInt("minutes", 0),
                test = item.optString("test", ""),
                motivation = item.optString("motivation", "")
            )
        }.filter { it.title.isNotBlank() }
        if (lessons.isEmpty()) throw IllegalArgumentException("learning lessons are missing")
        return LearningPlanDraft(
            title = root.optString("title", ""),
            subject = root.optString("subject", ""),
            category = root.optString("category", ""),
            level = root.optString("level", ""),
            goals = root.optJSONArray("goals").strings(),
            mode = if (root.optString("mode", "") == "time") "time" else "daily",
            minutes = root.optInt("minutes", 0),
            lessons = lessons,
            motivations = root.optJSONArray("motivations").strings(),
            tasks = root.optJSONArray("tasks").strings(),
            habits = root.optJSONArray("habits").strings(),
            tests = root.optJSONArray("tests").strings(),
            courseMd = courseMd,
            roadmapXml = roadmapXml,
            siteHtml = siteHtml,
            planJson = planJson
        )
    }

    private fun JSONArray?.strings(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { value -> value.isNotBlank() } }
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
