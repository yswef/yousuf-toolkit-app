package feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yousuf.toolkit.R
import core.design.drawBookSymbol
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private val parchment = Color(0xFFFFF8E4)
private val parchmentDeep = Color(0xFFE8D2A2)
private val runeInk = Color(0xFF6B4A12)
private val goldLight = Color(0xFFFFE9B0)
private val goldDeep = Color(0xFFB9862B)

@Composable
fun MagicBookCard(book: MagicBook, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .graphicsLayer {
                rotationX = 5f
                rotationY = -6f
                cameraDistance = 16f * density
            }
            .shadow(10.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(book.start, book.end)))
            .border(1.dp, Color.White.copy(alpha = 0.32f), shape)
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.95f, size.height * 0.08f)
            )
            drawLine(
                color = book.glow.copy(alpha = 0.35f),
                start = Offset(size.width * 0.1f, size.height * 0.72f),
                end = Offset(size.width * 0.72f, size.height * 0.1f),
                strokeWidth = 1.4f
            )
            for (i in 0 until 7) {
                drawCircle(
                    color = book.glow.copy(alpha = 0.55f),
                    radius = if (i % 3 == 0) 2.4f else 1.3f,
                    center = Offset(
                        size.width * (0.18f + (i * 13 % 70) / 100f),
                        size.height * (0.12f + (i * 29 % 60) / 100f)
                    )
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(7.dp)
                .background(Brush.verticalGradient(listOf(goldLight, goldDeep)))
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 17.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.book_kind),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
                letterSpacing = 2.sp
            )
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(58.dp)) {
                    drawCircle(book.glow.copy(alpha = 0.18f), size.minDimension * 0.5f, center)
                    drawBookSymbol(book.symbol, book.glow, center, size.minDimension * 0.34f)
                }
            }
            Text(
                stringResource(book.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun MagicBookOpen(book: MagicBook, onCancel: () -> Unit, onOpened: () -> Unit) {
    val density = LocalDensity.current.density
    val zoom = remember { Animatable(0.35f) }
    val turn = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(book.route) {
        core.design.SoundFx.play(core.design.SoundFx.Fx.OPEN)
        zoom.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        glow.animateTo(1f, tween(160))
        turn.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
        delay(120)
        finished = true
        onOpened()
    }

    val loop = rememberInfiniteTransition(label = "book")
    val spin by loop.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing)),
        label = "bookSpin"
    )

    val width = minOf(LocalConfiguration.current.screenWidthDp.dp * 0.78f, 340.dp)
    val shape = RoundedCornerShape(22.dp)

    Dialog(
        onDismissRequest = { if (!finished) onCancel() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            Modifier.fillMaxSize().background(Color(0xF5070312)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val ring = size.minDimension * 0.38f
                rotate(spin, center) {
                    drawCircle(
                        color = book.glow.copy(alpha = 0.22f),
                        radius = ring,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                    drawArc(
                        color = goldLight.copy(alpha = 0.7f),
                        startAngle = 10f,
                        sweepAngle = 64f,
                        useCenter = false,
                        topLeft = Offset(center.x - ring, center.y - ring),
                        size = Size(ring * 2, ring * 2),
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
                val dist = size.minDimension * (0.24f + 0.46f * glow.value)
                for (i in 0 until 20) {
                    val angle = Math.toRadians((i * 18).toDouble())
                    drawCircle(
                        color = Color(0xFFFFE08A).copy(alpha = glow.value * 0.85f),
                        radius = if (i % 3 == 0) 3.4f else 1.6f,
                        center = Offset(
                            center.x + cos(angle).toFloat() * dist,
                            center.y + sin(angle).toFloat() * dist
                        )
                    )
                }
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(book.title),
                    color = Color(0xFFFFE4A6),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(22.dp))
                Box(
                    Modifier
                        .width(width)
                        .aspectRatio(0.72f)
                        .graphicsLayer {
                            scaleX = zoom.value
                            scaleY = zoom.value
                        }
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(shape)
                            .background(Brush.verticalGradient(listOf(parchment, parchmentDeep)))
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            for (i in 0 until 8) {
                                val y = size.height * (0.12f + i * 0.075f)
                                drawLine(
                                    color = runeInk.copy(alpha = 0.22f),
                                    start = Offset(size.width * 0.16f, y),
                                    end = Offset(size.width * 0.84f, y),
                                    strokeWidth = 1.4f
                                )
                            }
                            drawCircle(
                                color = book.glow.copy(alpha = 0.30f * glow.value),
                                radius = size.width * 0.5f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                        }
                        Canvas(Modifier.align(Alignment.Center).size(92.dp)) {
                            drawBookSymbol(book.symbol, runeInk.copy(alpha = 0.85f), center, size.minDimension * 0.34f)
                        }
                        Text(
                            stringResource(R.string.app_name),
                            color = runeInk.copy(alpha = 0.7f),
                            letterSpacing = 3.sp,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.TopStart).padding(20.dp)
                        )
                    }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationY = -112f * turn.value
                                cameraDistance = 26f * density
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                shadowElevation = 18f
                                alpha = if (turn.value > 0.86f) {
                                    (1f - (turn.value - 0.86f) / 0.14f).coerceIn(0f, 1f)
                                } else 1f
                            }
                            .clip(shape)
                            .background(Brush.linearGradient(listOf(book.start, book.end)))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.09f),
                                radius = size.width * 0.6f,
                                center = Offset(size.width * 0.85f, size.height * 0.12f)
                            )
                            drawLine(
                                color = book.glow.copy(alpha = 0.4f),
                                start = Offset(size.width * 0.14f, size.height * 0.78f),
                                end = Offset(size.width * 0.8f, size.height * 0.14f),
                                strokeWidth = 1.6f
                            )
                        }
                        Box(
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(8.dp)
                                .background(Brush.verticalGradient(listOf(goldLight, goldDeep)))
                        )
                        Column(
                            Modifier.fillMaxSize().padding(22.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.book_kind),
                                color = Color.White.copy(alpha = 0.75f),
                                letterSpacing = 3.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Canvas(Modifier.align(Alignment.CenterHorizontally).size(96.dp)) {
                                drawCircle(book.glow.copy(alpha = 0.2f), size.minDimension * 0.5f, center)
                                drawBookSymbol(book.symbol, book.glow, center, size.minDimension * 0.34f)
                            }
                            Text(
                                stringResource(book.title),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.book_opening),
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
