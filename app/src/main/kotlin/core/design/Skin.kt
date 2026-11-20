package core.design

import androidx.compose.ui.graphics.Color

enum class Accent(val key: String, val light: Color, val dark: Color) {
    VIOLET("violet", Color(0xFF6C4CF1), Color(0xFFB8A6FF)),
    GOLD("gold", Color(0xFF9A741F), Color(0xFFE8C06A)),
    TEAL("teal", Color(0xFF0E8278), Color(0xFF6FE3D5)),
    ROSE("rose", Color(0xFFAA3E68), Color(0xFFFFA8C5))
}

fun accentOf(key: String): Accent = Accent.entries.firstOrNull { it.key == key } ?: Accent.VIOLET
