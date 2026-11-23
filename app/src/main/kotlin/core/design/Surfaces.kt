package core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class CardTone { NEUTRAL, ACCENT, WARM }

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tone: CardTone = CardTone.NEUTRAL,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val container = when (tone) {
        CardTone.NEUTRAL -> scheme.surfaceContainerLow
        CardTone.ACCENT -> scheme.primaryContainer
        CardTone.WARM -> scheme.tertiaryContainer
    }
    val colors = CardDefaults.cardColors(containerColor = container)
    val border = BorderStroke(1.dp, scheme.outlineVariant)
    val body: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colors, border = border, content = body)
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colors, border = border, content = body)
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow)
    val border = BorderStroke(1.dp, scheme.outlineVariant)
    val body: @Composable ColumnScope.() -> Unit = {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colors, border = border, content = body)
    } else {
        Card(modifier = modifier, colors = colors, border = border, content = body)
    }
}
