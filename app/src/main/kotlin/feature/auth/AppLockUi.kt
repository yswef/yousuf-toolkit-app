// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private fun dotCenters(width: Float, height: Float): List<Offset> {
    val xs = listOf(width * 0.2f, width * 0.5f, width * 0.8f)
    val ys = listOf(height * 0.2f, height * 0.5f, height * 0.8f)
    return ys.flatMap { y -> xs.map { x -> Offset(x, y) } }
}

@Composable
fun PatternPad(onDone: (String) -> Unit) {
    var selected by remember { mutableStateOf(listOf<Int>()) }
    val radius = with(LocalDensity.current) { 42.dp.toPx() }
    Canvas(
        Modifier.fillMaxWidth().height(260.dp).pointerInput(Unit) {
            fun addDot(position: Offset) {
                val index = dotCenters(size.width.toFloat(), size.height.toFloat())
                    .indexOfFirst { (it - position).getDistance() < radius }
                if (index >= 0 && index !in selected) selected = selected + index
            }
            detectDragGestures(
                onDragStart = { position -> selected = emptyList(); addDot(position) },
                onDrag = { change, _ -> addDot(change.position) },
                onDragEnd = { onDone(selected.joinToString("")) },
                onDragCancel = { selected = emptyList() }
            )
        }
    ) {
        val points = dotCenters(size.width, size.height)
        selected.zipWithNext().forEach { (a, b) ->
            drawLine(
                color = Color(0xFF6C4CF1),
                start = points[a],
                end = points[b],
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEachIndexed { index, point ->
            drawCircle(
                color = if (index in selected) Color(0xFF6C4CF1) else Color(0xFFB9AECF),
                radius = if (index in selected) 18.dp.toPx() else 12.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    type: String,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { androidx.compose.material3.Text(label) },
        singleLine = true,
        visualTransformation = if (type == "password" || type == "pin") PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (type == "pin") KeyboardType.NumberPassword else KeyboardType.Password),
        modifier = modifier.fillMaxWidth()
    )
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
