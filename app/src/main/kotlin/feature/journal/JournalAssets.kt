// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.journal

data class JournalBackground(val id: String, val color: Long, val cost: Int)
data class JournalPack(val id: String, val cost: Int, val emojis: List<String>)

object JournalAssets {
    val backgrounds = listOf(
        JournalBackground("cream", 0xFFFFF7E6L, 0),
        JournalBackground("sky", 0xFFE9F4FFL, 0),
        JournalBackground("rose", 0xFFFFECF1L, 35),
        JournalBackground("mint", 0xFFE8FAF1L, 35),
        JournalBackground("lavender", 0xFFF0EAFFL, 40),
        JournalBackground("sand", 0xFFFFF0D9L, 40),
        JournalBackground("night", 0xFF24243AL, 45),
        JournalBackground("grid", 0xFFF4F7FBL, 45),
        JournalBackground("dots", 0xFFFFF9EEL, 50),
        JournalBackground("ocean", 0xFFE5F6FFL, 50)
    )
    val emojiPacks = listOf(
        JournalPack("free-1", 0, listOf("😊", "😢", "😡", "😴", "🤩", "😌", "🥺", "🤔")),
        JournalPack("free-2", 0, listOf("🌞", "🌧️", "🌱", "🔥", "⭐", "🌙", "🍀", "💧")),
        JournalPack("paid-1", 25, listOf("🦊", "🐼", "🐸", "🦋", "🐢", "🦉", "🐝", "🐳")),
        JournalPack("paid-2", 30, listOf("🎈", "🎨", "🎵", "📚", "🧩", "🪁", "🎯", "🏆")),
        JournalPack("paid-3", 35, listOf("💪", "🧠", "❤️", "✨", "🌈", "☕", "🕊️", "🛠️"))
    )
    val stickerPacks = listOf(
        JournalPack("free-1", 0, listOf("⭐", "❤️", "✅", "💡", "🌸", "☀️")),
        JournalPack("free-2", 0, listOf("📝", "📌", "🎀", "🍎", "🚀", "🏅")),
        JournalPack("paid-1", 30, listOf("🐱", "🐶", "🦄", "🐙", "🦕", "🐝")),
        JournalPack("paid-2", 35, listOf("🎂", "🎁", "🎪", "🎮", "🎧", "🎬")),
        JournalPack("paid-3", 40, listOf("🕌", "🕋", "🌙", "🪔", "📿", "🌴")),
        JournalPack("paid-4", 45, listOf("🧪", "🔬", "🧭", "🛰️", "⚙️", "🧬"))
    )
    val fonts = listOf("Cairo", "serif", "monospace", "sans-serif")
    val colors = listOf("#222222", "#6C4CF1", "#E5484D", "#16855B", "#B45309", "#0B6BCB")
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
