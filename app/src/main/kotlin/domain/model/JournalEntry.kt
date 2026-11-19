package domain.model

data class JournalEntry(
    val id: Long,
    val title: String,
    val body: String,
    val at: Long
)
