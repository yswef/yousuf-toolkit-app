package domain.model

data class SleepLog(
    val id: Long,
    val hours: Double,
    val at: Long
)

data class LearnItem(
    val id: Long,
    val title: String,
    val minutes: Int,
    val target: Int,
    val rewarded: Boolean
)

data class Book(
    val id: Long,
    val title: String,
    val page: Int,
    val pages: Int,
    val rewarded: Boolean
)

val prayerKeys = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
