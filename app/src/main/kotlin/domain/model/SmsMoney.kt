package domain.model

data class SmsMoney(
    val key: String,
    val sender: String,
    val body: String,
    val amount: Double,
    val balance: Double?,
    val income: Boolean,
    val at: Long
)
