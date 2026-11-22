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
    primary = Color(0xFF6C4CF1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9E3FF),
    onPrimaryContainer = Color(0xFF241A52),
    inversePrimary = Color(0xFFB8A6FF),
    secondary = Color(0xFF2F8F83),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F3EF),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFF9A741F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE9B8),
    onTertiaryContainer = Color(0xFF2A1D00),
    background = Color(0xFFFBF9FF),
    onBackground = Color(0xFF1B1A24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1A24),
    surfaceVariant = Color(0xFFF0ECF8),
    onSurfaceVariant = Color(0xFF5E5A6E),
    outline = Color(0xFFD8D3E6),
    outlineVariant = Color(0xFFE9E4F2),
    error = Color(0xFFC03943),
    onError = Color.White,
    errorContainer = Color(0xFFFBE0E2),
    onErrorContainer = Color(0xFF3F0008),
    surfaceDim = Color(0xFFDDD8E6),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F5FD),
    surfaceContainer = Color(0xFFF3EFFA),
    surfaceContainerHigh = Color(0xFFEEE9F7),
    surfaceContainerHighest = Color(0xFFE8E3F2)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFB8A6FF),
    onPrimary = Color(0xFF241A52),
    primaryContainer = Color(0xFF3A2C80),
    onPrimaryContainer = Color(0xFFE9E3FF),
    inversePrimary = Color(0xFF6C4CF1),
    secondary = Color(0xFF6FE3D5),
    onSecondary = Color(0xFF00201D),
    secondaryContainer = Color(0xFF00504A),
    onSecondaryContainer = Color(0xFFB4F2EC),
    tertiary = Color(0xFFF2C35B),
    onTertiary = Color(0xFF2A1D00),
    tertiaryContainer = Color(0xFF6B4300),
    onTertiaryContainer = Color(0xFFFFE9B8),
    background = Color(0xFF121118),
    onBackground = Color(0xFFECE9F5),
    surface = Color(0xFF1B1A24),
    onSurface = Color(0xFFECE9F5),
    surfaceVariant = Color(0xFF25232F),
    onSurfaceVariant = Color(0xFFB3AEC4),
    outline = Color(0xFF3A3748),
    outlineVariant = Color(0xFF2C2938),
    error = Color(0xFFFF8A92),
    onError = Color(0xFF3A0006),
    errorContainer = Color(0xFF5C1219),
    onErrorContainer = Color(0xFFFFDAD9),
    surfaceDim = Color(0xFF121118),
    surfaceBright = Color(0xFF33313F),
    surfaceContainerLowest = Color(0xFF0E0D13),
    surfaceContainerLow = Color(0xFF17161E),
    surfaceContainer = Color(0xFF1B1A24),
    surfaceContainerHigh = Color(0xFF25232F),
    surfaceContainerHighest = Color(0xFF2F2D3B)
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
