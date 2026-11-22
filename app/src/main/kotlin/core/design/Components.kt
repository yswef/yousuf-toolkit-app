package core.design

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.annotation.DrawableRes
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import com.yousuf.toolkit.R
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * صف قابل للسحب كتطبيقات أندرويد الأصلية.
 * السحب من البداية ينفّذ [onSwipeStart] (إنجاز) ويعيد الصف لمكانه،
 * والسحب من النهاية ينفّذ [onSwipeEnd] (حذف) ويُخرج الصف.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeActionRow(
    modifier: Modifier = Modifier,
    onSwipeStart: (() -> Unit)? = null,
    onSwipeEnd: (() -> Unit)? = null,
    startIcon: ImageVector = Icons.Filled.Check,
    endIcon: ImageVector = Icons.Filled.Delete,
    content: @Composable () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeStart?.invoke()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeEnd?.invoke()
                    onSwipeEnd != null
                }
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = onSwipeStart != null,
        enableDismissFromEndToStart = onSwipeEnd != null,
        backgroundContent = {
            val direction = state.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icon(startIcon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    SwipeToDismissBoxValue.EndToStart -> Icon(endIcon, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    else -> {}
                }
            }
        }
    ) {
        content()
    }
}

/** رقم يعدّ تصاعدياً بحركة قصيرة عند تغيّر القيمة. */
@Composable
fun CountUpText(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = Color.Unspecified,
    suffix: String = ""
) {
    val shown by animateIntAsState(targetValue = value, animationSpec = tween(700), label = "count-up")
    Text("$shown$suffix", modifier = modifier, style = style, color = color)
}

val LocalSnackbar = compositionLocalOf<SnackbarHostState?> { null }

/** حذف قابل للتراجع: يشغّل الصوت ويعرض Snackbar بزر تراجع. */
class UndoHost(
    private val state: SnackbarHostState?,
    private val scope: CoroutineScope,
    private val message: String,
    private val undo: String
) {
    fun deleted(restore: () -> Unit) {
        SoundFx.play(SoundFx.Fx.DELETE)
        val host = state ?: return
        scope.launch {
            host.currentSnackbarData?.dismiss()
            val r = host.showSnackbar(message, actionLabel = undo, duration = SnackbarDuration.Short)
            if (r == SnackbarResult.ActionPerformed) restore()
        }
    }
}

@Composable
fun rememberUndoHost(): UndoHost {
    val state = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.item_deleted)
    val undo = stringResource(R.string.undo)
    return remember(state, scope, message, undo) { UndoHost(state, scope, message, undo) }
}

/** حالة فارغة برسمة وجملة، بدل نص مجرد. */
@Composable
fun EmptyIllustration(@DrawableRes image: Int, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp))
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
