// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package core.net

import domain.model.LearningLesson
import domain.model.LearningPlan
import org.json.JSONArray
import org.json.JSONObject

object LearningPlanJson {
    fun parse(
        planJson: String,
        siteHtml: String,
        courseMd: String,
        roadmapXml: String,
        serverId: Long,
        siteUrl: String
    ): LearningPlan? {
        val root = runCatching { JSONObject(planJson) }.getOrNull() ?: return null
        val array = root.optJSONArray("lessons")
        val lessons = (0 until (array?.length() ?: 0)).mapNotNull { index ->
            val item = array?.optJSONObject(index) ?: return@mapNotNull null
            LearningLesson(
                id = index.toLong() + 1L,
                title = item.optString("title", ""),
                minutes = item.optInt("minutes", 0),
                test = item.optString("test", ""),
                motivation = item.optString("motivation", ""),
                done = false
            )
        }.filter { it.title.isNotBlank() }
        if (lessons.isEmpty()) return null
        return LearningPlan(
            id = 0L,
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
            planJson = planJson,
            status = "ready",
            serverId = serverId,
            siteUrl = siteUrl,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun JSONArray?.strings(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { value -> value.isNotBlank() } }
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
