package core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class UiMode {
    PROFESSIONAL,
    SIMPLE
}

@Composable
fun AppTheme(mode: UiMode, dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) darkColorScheme() else lightColorScheme()
    val type = if (mode == UiMode.SIMPLE) simpleType() else professionalType()
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}
