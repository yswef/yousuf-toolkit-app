package core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * خلفية هادئة قياسية: لون سطح المادة مع لمسة علوية خفيفة جداً من لون الهوية.
 * (كانت تدرجات ونجوماً؛ صارت أقرب لتطبيقات أندرويد الأصلية.)
 */
@Composable
fun MagicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    Box(
        modifier
            .fillMaxSize()
            .background(bg)
            .background(Brush.verticalGradient(listOf(tint, Color.Transparent), endY = 700f))
    ) {
        content()
    }
}
