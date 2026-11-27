// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package domain.model

data class SmsMoney(
    val key: String,
    val sender: String,
    val body: String,
    val amount: Double,
    val balance: Double?,
    val income: Boolean,
    val at: Long,
    val title: String = "",
    val counterparty: String = "",
    val note: String = "",
    val category: String = "other",
    val source: String = "sms"
)
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
