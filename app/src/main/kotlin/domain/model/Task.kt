package domain.model

enum class Priority { LOW, NORMAL, HIGH }

data class Task(
    val id: Long,
    val title: String,
    val note: String,
    val priority: Priority,
    val done: Boolean,
    val createdAt: Long
)
