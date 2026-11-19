package domain.model

data class Campaign(
    val id: Long,
    val title: String,
    val target: Int,
    val checkins: Int,
    val startedAt: Long,
    val lastCheckin: Long
) {
    val done: Boolean
        get() = target > 0 && checkins >= target
}
