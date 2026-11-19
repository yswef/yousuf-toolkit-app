package core.design

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

enum class BookSymbol { STAR, MOON, DROP, FLAME, LEAF, ORB, DIAMOND, RUNE }

fun DrawScope.drawBookSymbol(symbol: BookSymbol, color: Color, center: Offset, radius: Float) {
    when (symbol) {
        BookSymbol.STAR -> {
            val path = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) radius else radius * 0.45f
                val angle = Math.toRadians((-90 + i * 36).toDouble())
                val x = center.x + cos(angle).toFloat() * r
                val y = center.y + sin(angle).toFloat() * r
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
        BookSymbol.MOON -> drawArc(
            color = color,
            startAngle = 50f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = radius * 0.28f, cap = StrokeCap.Round)
        )
        BookSymbol.DROP -> {
            val path = Path()
            path.moveTo(center.x, center.y - radius)
            path.cubicTo(
                center.x + radius * 0.95f, center.y - radius * 0.1f,
                center.x + radius * 0.6f, center.y + radius,
                center.x, center.y + radius
            )
            path.cubicTo(
                center.x - radius * 0.6f, center.y + radius,
                center.x - radius * 0.95f, center.y - radius * 0.1f,
                center.x, center.y - radius
            )
            path.close()
            drawPath(path, color)
        }
        BookSymbol.FLAME -> {
            val path = Path()
            path.moveTo(center.x, center.y - radius)
            path.cubicTo(
                center.x + radius * 0.95f, center.y - radius * 0.2f,
                center.x + radius * 0.45f, center.y + radius * 0.9f,
                center.x, center.y + radius
            )
            path.cubicTo(
                center.x - radius * 0.65f, center.y + radius * 0.55f,
                center.x - radius * 0.4f, center.y,
                center.x - radius * 0.1f, center.y - radius * 0.25f
            )
            path.close()
            drawPath(path, color)
        }
        BookSymbol.LEAF -> {
            rotate(45f, center) {
                drawOval(
                    color = color,
                    topLeft = Offset(center.x - radius * 0.45f, center.y - radius),
                    size = Size(radius * 0.9f, radius * 2),
                    style = Stroke(width = radius * 0.12f)
                )
            }
        }
        BookSymbol.ORB -> {
            drawCircle(color.copy(alpha = 0.25f), radius, center)
            drawCircle(color, radius * 0.55f, center)
            drawCircle(Color.White.copy(alpha = 0.85f), radius * 0.16f, Offset(center.x - radius * 0.2f, center.y - radius * 0.22f))
        }
        BookSymbol.DIAMOND -> {
            val path = Path()
            path.moveTo(center.x, center.y - radius)
            path.lineTo(center.x + radius * 0.75f, center.y)
            path.lineTo(center.x, center.y + radius)
            path.lineTo(center.x - radius * 0.75f, center.y)
            path.close()
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = radius * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        BookSymbol.RUNE -> {
            drawLine(color, Offset(center.x - radius * 0.6f, center.y - radius * 0.8f), Offset(center.x - radius * 0.6f, center.y + radius * 0.8f), strokeWidth = radius * 0.14f, cap = StrokeCap.Round)
            drawLine(color, Offset(center.x - radius * 0.6f, center.y), Offset(center.x + radius * 0.6f, center.y - radius * 0.5f), strokeWidth = radius * 0.14f, cap = StrokeCap.Round)
            drawLine(color, Offset(center.x - radius * 0.6f, center.y + radius * 0.2f), Offset(center.x + radius * 0.7f, center.y + radius * 0.6f), strokeWidth = radius * 0.14f, cap = StrokeCap.Round)
            drawCircle(color, radius * 0.14f, Offset(center.x + radius * 0.7f, center.y - radius * 0.6f))
        }
    }
}
