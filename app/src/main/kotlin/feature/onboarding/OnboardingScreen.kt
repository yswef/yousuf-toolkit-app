package feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.drawBookSymbol
import kotlinx.coroutines.delay

private val night = Color(0xFF07030F)
private val gold = Color(0xFFFFD166)

@Composable
fun OnboardingScreen(onRegister: () -> Unit) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
    }

    val lines = listOf(
        R.string.mentor_line_1,
        R.string.mentor_line_2,
        R.string.mentor_line_3,
        R.string.mentor_line_4
    )
    var index by remember { mutableStateOf(0) }
    var shown by remember(index) { mutableStateOf(0) }
    val full = stringResource(lines[index])
    val complete = shown >= full.length

    LaunchedEffect(index) {
        delay(320)
        for (i in 1..full.length) {
            delay(18)
            if (shown < i) shown = i
        }
    }

    val advance = {
        if (!complete) {
            shown = full.length
        } else if (index < lines.lastIndex) {
            index += 1
        } else {
            onRegister()
        }
    }

    Box(Modifier.fillMaxSize().background(night)) {
        Image(
            painter = painterResource(R.drawable.mentor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                alpha = 0.45f + reveal.value * 0.55f
                scaleX = 1.02f
                scaleY = 1.02f
            }
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color(0xB306030F),
                    0.34f to Color(0x2206030F),
                    0.60f to Color(0xAA06030F),
                    1f to Color(0xF2070312)
                )
            )
        )
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0 until 40) {
                val x = ((i * 71 + 17) % 100) / 100f * size.width
                val y = ((i * 43 + 11) % 100) / 100f * size.height
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f + (i % 4) * 0.08f),
                    radius = if (i % 8 == 0) 2f else 1.1f,
                    center = Offset(x, y)
                )
            }
        }

        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.app_name),
                    color = gold,
                    letterSpacing = 3.sp,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRegister) {
                    Text(
                        stringResource(R.string.onboarding_skip),
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().graphicsLayer {
                    translationY = (1f - reveal.value) * 90f
                    alpha = reveal.value
                },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xCC120A24))
                        .border(1.dp, gold.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                        .clickable { advance() }
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Canvas(Modifier.size(20.dp)) {
                            drawBookSymbol(BookSymbol.STAR, gold, center, size.minDimension * 0.42f)
                        }
                        Text(
                            stringResource(R.string.mentor_name),
                            color = gold,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        full.take(shown),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.heightIn(min = 84.dp)
                    )
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(lines.size) { i ->
                            Box(
                                Modifier
                                    .height(6.dp)
                                    .width(if (i == index) 22.dp else 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (i <= index) gold else Color.White.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                    Button(
                        onClick = advance,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = Color(0xFF2A1300)
                        )
                    ) {
                        Text(
                            if (index == lines.lastIndex && complete) {
                                stringResource(R.string.onboarding_finish)
                            } else {
                                stringResource(R.string.onboarding_next)
                            }
                        )
                    }
                }
            }
        }
    }
}
