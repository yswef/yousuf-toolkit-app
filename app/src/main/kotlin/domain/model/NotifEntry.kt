package domain.model

data class NotifEntry(
    val id: Long,
    val pkg: String,
    val title: String,
    val text: String,
    val at: Long
)
