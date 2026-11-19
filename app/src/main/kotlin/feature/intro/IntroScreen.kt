package feature.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yousuf.toolkit.R
import kotlin.math.cos
import kotlin.math.sin

private val voidTop = Color(0xFF06040F)
private val voidBottom = Color(0xFF191036)
private val aura = Color(0xFF7C5CFF)
private val auraSoft = Color(0xFFB9A6FF)
private val gold = Color(0xFFF2D48A)

@Composable
fun IntroScreen(onFinish: () -> Unit) {
    var done by remember { mutableStateOf(false) }
    val finish = {
        if (!done) {
            done = true
            onFinish()
        }
    }

    val reveal = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(durationMillis = 1400, delayMillis = 250))
    }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 4600, easing = LinearEasing))
        finish()
    }

    val exit = ((progress.value - 0.76f) / 0.24f).coerceIn(0f, 1f)

    val loop = rememberInfiniteTransition(label = "intro")
    val spin by loop.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "spin"
    )
    val counter by loop.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing)),
        label = "counter"
    )
    val breathe by loop.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val drift by loop.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "drift"
    )

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier.fillMaxSize().graphicsLayer {
                scaleX = 1f + exit * 1.5f
                scaleY = 1f + exit * 1.5f
                alpha = 1f - exit * 0.45f
            }
        ) {
            drawRect(Brush.verticalGradient(listOf(voidTop, voidBottom, voidTop)))

            for (i in 0 until 64) {
                val x = ((i * 73 + 11) % 100) / 100f * size.width
                val base = ((i * 37 + 5) % 100) / 100f
                val y = ((base + drift * 0.15f) % 1f) * size.height
                val twinkle = 0.25f + 0.55f * ((i * 29) % 10) / 10f
                drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = if (i % 9 == 0) 2.2f else 1.1f,
                    center = Offset(x, y)
                )
            }

            val center = Offset(size.width / 2f, size.height * 0.40f)
            val radius = size.minDimension * 0.30f * breathe

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(aura.copy(alpha = 0.34f), Color.Transparent),
                    center = center,
                    radius = radius * 2.1f
                ),
                radius = radius * 2.1f,
                center = center
            )

            rotate(spin, center) {
                drawCircle(
                    color = auraSoft.copy(alpha = 0.55f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawArc(
                    color = gold.copy(alpha = 0.8f),
                    startAngle = 12f,
                    sweepAngle = 58f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = aura.copy(alpha = 0.75f),
                    startAngle = 190f,
                    sweepAngle = 34f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }

            rotate(counter, center) {
                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30).toDouble())
                    val point = Offset(
                        center.x + cos(angle).toFloat() * radius * 0.84f,
                        center.y + sin(angle).toFloat() * radius * 0.84f
                    )
                    drawCircle(
                        color = if (i % 3 == 0) gold else auraSoft.copy(alpha = 0.7f),
                        radius = if (i % 3 == 0) 4f else 2.5f,
                        center = point
                    )
                }
            }

            drawCircle(
                color = Color.Transparent,
                radius = radius * 0.62f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(gold.copy(alpha = 0.20f), aura.copy(alpha = 0.10f), Color.Transparent),
                    center = center,
                    radius = radius * 0.62f
                ),
                radius = radius * 0.62f,
                center = center
            )

            rotate(spin * 0.5f, center) {
                drawLine(
                    color = auraSoft.copy(alpha = 0.35f),
                    start = Offset(center.x - radius * 1.25f, center.y),
                    end = Offset(center.x + radius * 1.25f, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = auraSoft.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y - radius * 1.25f),
                    end = Offset(center.x, center.y + radius * 1.25f),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            Modifier.fillMaxSize().padding(32.dp).graphicsLayer {
                scaleX = 1f + exit * 1.5f
                scaleY = 1f + exit * 1.5f
                alpha = 1f - exit * 0.45f
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))
            Text(
                stringResource(R.string.intro_title),
                color = gold,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 7.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = reveal.value
                    scaleX = 0.94f + reveal.value * 0.06f
                    scaleY = 0.94f + reveal.value * 0.06f
                }
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.intro_tagline),
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = reveal.value }
            )
            Spacer(Modifier.height(26.dp))
            Box(
                Modifier.width(64.dp).height(2.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, gold, Color.Transparent)
                        ),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2f
                    )
                }
            }
        }

        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 40.dp).graphicsLayer {
                alpha = 1f - exit
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxWidth(),
                color = gold,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = finish) {
                Text(stringResource(R.string.intro_skip), color = Color.White.copy(alpha = 0.8f))
            }
        }

        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(gold.copy(alpha = 0.9f), Color.Transparent)
                    )
                )
                .graphicsLayer { alpha = exit }
        )

        Column(
            Modifier.fillMaxSize().padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                stringResource(R.string.intro_portal),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.graphicsLayer { alpha = exit }
            )
        }
    }
}
