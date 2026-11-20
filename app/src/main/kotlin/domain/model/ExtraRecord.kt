package domain.model

data class ExtraRecord(
    val id: Long,
    val kind: String,
    val title: String,
    val detail: String,
    val amount: Double,
    val status: String,
    val dueAt: Long,
    val createdAt: Long
)
