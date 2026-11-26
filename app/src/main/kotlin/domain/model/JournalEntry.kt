// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package domain.model

data class JournalEntry(
    val id: Long,
    val title: String,
    val body: String,
    val at: Long,
    val mood: String = "",
    val background: String = "cream",
    val richBody: String = "",
    val font: String = "Cairo",
    val colors: String = "",
    val drawings: String = "",
    val stickers: String = "",
    val images: String = "",
    val emojiPack: String = "free-1",
    val stickerPack: String = "free-1",
    val voicePath: String = "",
    val hasTime: Boolean = false,
    val syncedAt: Long = 0L
)
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
