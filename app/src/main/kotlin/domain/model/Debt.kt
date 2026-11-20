package domain.model

data class Debt(
    val id: Long,
    val person: String,
    val amount: Double,
    val owedByMe: Boolean,
    val note: String,
    val settled: Boolean,
    val at: Long
)
