// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package domain.model

data class LearningLesson(
    val id: Long,
    val title: String,
    val minutes: Int,
    val test: String,
    val motivation: String,
    val done: Boolean = false
)

data class LearningLessonDraft(
    val title: String,
    val minutes: Int,
    val test: String,
    val motivation: String
)

data class LearningPlanDraft(
    val title: String,
    val subject: String,
    val category: String,
    val level: String,
    val goals: List<String>,
    val mode: String,
    val minutes: Int,
    val lessons: List<LearningLessonDraft>,
    val motivations: List<String>,
    val tasks: List<String>,
    val habits: List<String>,
    val tests: List<String>,
    val courseMd: String,
    val roadmapXml: String,
    val siteHtml: String,
    val planJson: String
)

data class LearningPlan(
    val id: Long,
    val title: String,
    val subject: String,
    val category: String,
    val level: String,
    val goals: List<String>,
    val mode: String,
    val minutes: Int,
    val lessons: List<LearningLesson>,
    val motivations: List<String>,
    val tasks: List<String>,
    val habits: List<String>,
    val tests: List<String>,
    val courseMd: String,
    val roadmapXml: String,
    val siteHtml: String,
    val planJson: String,
    val status: String = "local",
    val serverId: Long = 0L,
    val siteUrl: String = "",
    val createdAt: Long = 0L
)
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
