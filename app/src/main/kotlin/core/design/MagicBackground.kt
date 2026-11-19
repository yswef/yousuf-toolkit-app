package core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun MagicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = if (dark) {
        listOf(Color(0xFF07030F), Color(0xFF1B0A3A), Color(0xFF062028))
    } else {
        listOf(Color(0xFFF9F3FF), Color(0xFFEDE0FF), Color(0xFFDDF7F3))
    }
    val aura = MaterialTheme.colorScheme.primary
    val aura2 = MaterialTheme.colorScheme.secondary

    Box(
        modifier.fillMaxSize().background(Brush.verticalGradient(colors))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(aura.copy(alpha = if (dark) 0.28f else 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.06f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.88f, size.height * 0.06f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(aura2.copy(alpha = if (dark) 0.22f else 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.08f, size.height * 0.92f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.08f, size.height * 0.92f)
            )
            for (i in 0 until 26) {
                val x = ((i * 67 + 13) % 100) / 100f * size.width
                val y = ((i * 41 + 7) % 100) / 100f * size.height
                val alpha = if (dark) 0.35f else 0.22f
                drawCircle(
                    color = Color.White.copy(alpha = alpha * (0.4f + (i % 5) / 5f)),
                    radius = if (i % 7 == 0) 2f else 1.1f,
                    center = Offset(x, y)
                )
            }
        }
        content()
    }
}
