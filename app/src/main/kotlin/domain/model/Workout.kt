package domain.model

data class Workout(
    val id: Long,
    val kind: String,
    val minutes: Int,
    val at: Long
)

val workoutKeys = listOf("strength", "cardio", "stretch", "walk")
