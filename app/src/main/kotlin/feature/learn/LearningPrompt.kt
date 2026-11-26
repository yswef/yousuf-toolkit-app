// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.learn

object LearningPrompt {
    fun build(
        template: String,
        subject: String,
        level: String,
        goals: List<String>,
        mode: String,
        minutes: String
    ): String {
        val cleanGoals = goals.map { it.trim() }.filter { it.isNotBlank() }.joinToString("\n") { "- $it" }
        return template
            .replace("{{SUBJECT}}", subject.trim())
            .replace("{{LEVEL}}", level.trim())
            .replace("{{GOALS}}", cleanGoals)
            .replace("{{MODE}}", mode)
            .replace("{{MINUTES}}", minutes.trim())
    }
}
