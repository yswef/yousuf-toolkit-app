package domain.model

data class Ledger(
    val id: Long,
    val title: String,
    val amount: Double,
    val income: Boolean,
    val category: String,
    val at: Long
)

val categoryKeys = listOf("salary", "food", "transport", "bills", "shopping", "other")
