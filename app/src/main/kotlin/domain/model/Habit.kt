package domain.model

data class Habit(
    val id: Long,
    val title: String,
    val streak: Int,
    val best: Int,
    val lastDay: String
)
