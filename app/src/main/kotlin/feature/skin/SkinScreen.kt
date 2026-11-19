package feature.skin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.Accent
import core.design.BookSymbol
import core.design.InfoCard
import data.local.LocalStore
import core.design.ScreenScaffold
import core.design.drawBookSymbol

@Composable
fun SkinScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    ScreenScaffold(title = stringResource(R.string.feature_skin), onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(text = stringResource(R.string.skin_desc))
            Text(
                stringResource(R.string.skin_accent),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Accent.entries.forEach { accent ->
                    val selected = store.accent == accent.key
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(accent.light)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .clickable { store.saveSkin(accent.key) }
                    )
                }
            }
            Text(
                accentName(store.accent),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                stringResource(R.string.skin_avatar),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BookSymbol.entries.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { symbol ->
                            val absolute = BookSymbol.entries.indexOf(symbol)
                            val selected = store.avatar == absolute
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { store.saveAvatar(absolute) },
                                contentAlignment = Alignment.Center
                            ) {
                                val accent = MaterialTheme.colorScheme.primary
                                Canvas(Modifier.size(32.dp)) {
                                    drawBookSymbol(
                                        symbol,
                                        accent,
                                        Offset(size.width / 2, size.height / 2),
                                        size.minDimension * 0.45f
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.skin_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun accentName(key: String): String = when (key) {
    Accent.GOLD.key -> stringResource(R.string.accent_gold)
    Accent.TEAL.key -> stringResource(R.string.accent_teal)
    Accent.ROSE.key -> stringResource(R.string.accent_rose)
    else -> stringResource(R.string.accent_violet)
}
