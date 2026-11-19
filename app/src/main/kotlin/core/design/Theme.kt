package core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class UiMode {
    PROFESSIONAL,
    SIMPLE
}

private val LightScheme = lightColorScheme(
    primary = Color(0xFF6C22D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF23005C),
    inversePrimary = Color(0xFFCFB8FF),
    secondary = Color(0xFF008F86),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB4F2EC),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFFB86A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDDB0),
    onTertiaryContainer = Color(0xFF3A1F00),
    background = Color(0xFFF7F1FF),
    onBackground = Color(0xFF1D1231),
    surface = Color(0xFFFDF8FF),
    onSurface = Color(0xFF1D1231),
    surfaceVariant = Color(0xFFE9DFF7),
    onSurfaceVariant = Color(0xFF4B3F63),
    outline = Color(0xFF7C6E96),
    outlineVariant = Color(0xFFCFC2E3),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFCFB8FF),
    onPrimary = Color(0xFF3A0B7A),
    primaryContainer = Color(0xFF52209B),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF55E0D5),
    onSecondary = Color(0xFF00201D),
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB4F2EC),
    tertiary = Color(0xFFFFCF7A),
    onTertiary = Color(0xFF3A1F00),
    tertiaryContainer = Color(0xFF6B4300),
    onTertiaryContainer = Color(0xFFFFDDB0),
    background = Color(0xFF08040F),
    onBackground = Color(0xFFEDE5FA),
    surface = Color(0xFF130B26),
    onSurface = Color(0xFFEDE5FA),
    surfaceVariant = Color(0xFF271842),
    onSurfaceVariant = Color(0xFFCDBBE8),
    outline = Color(0xFF9B89B8),
    outlineVariant = Color(0xFF4B3D63),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun AppTheme(
    mode: UiMode,
    dark: Boolean,
    accent: String = Accent.VIOLET.key,
    content: @Composable () -> Unit
) {
    val base = if (dark) DarkScheme else LightScheme
    val skin = accentOf(accent)
    val colors = base.copy(
        primary = if (dark) skin.dark else skin.light,
        inversePrimary = if (dark) skin.light else skin.dark
    )
    val type = if (mode == UiMode.SIMPLE) simpleType() else professionalType()
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}
