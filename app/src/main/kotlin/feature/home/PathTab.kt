package feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.drawBookSymbol
import core.navigation.Routes
import data.local.Store
import domain.model.Campaign
import java.time.Instant
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PathTab(store: Store, onFeature: (String) -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val ranks = store.config.ranks.ifEmpty { listOf("novice" to 1) }
    val currentIndex = ranks.indexOfLast { store.level >= it.second }.coerceAtLeast(0)
    val next = ranks.firstOrNull { store.level < it.second }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            RankBanner(store, ranks, currentIndex, next)
        }
        item {
            Text(
                stringResource(R.string.path_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            QuestMap(store, ranks, currentIndex, Modifier.fillMaxWidth().height(400.dp))
        }
        item {
            Text(
                stringResource(R.string.path_map_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ranks) { rank ->
                    val index = ranks.indexOf(rank)
                    RankChip(
                        name = rankLabel(rank.first),
                        level = rank.second,
                        done = index < currentIndex,
                        current = index == currentIndex
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.path_campaigns),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onFeature(Routes.GAME) }) {
                    Text(stringResource(R.string.path_all_campaigns))
                }
            }
        }
        if (store.campaigns.isEmpty()) {
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.path_no_campaigns),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = onAdd) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.campaign_new))
                        }
                    }
                }
            }
        } else {
            items(store.campaigns.take(3), key = { it.id }) { campaign ->
                CampaignRow(store, campaign, onFeature)
            }
        }
    }
}

@Composable
private fun RankBanner(
    store: Store,
    ranks: List<Pair<String, Int>>,
    currentIndex: Int,
    next: Pair<String, Int>?
) {
    val current = ranks.getOrElse(currentIndex) { ranks.first() }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), Color(0xFF3A0B7A))
                        )
                    )
                    .border(2.dp, Color(0xFFC09843), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    current.second.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.path_rank),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    rankLabel(current.first),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.game_level_points, store.level, store.points),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        LinearProgressIndicator(
            progress = { store.rankProgress },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            if (next == null) {
                stringResource(R.string.rank_max)
            } else {
                stringResource(R.string.rank_next, rankLabel(next.first), next.second)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestMap(store: Store, ranks: List<Pair<String, Int>>, currentIndex: Int, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val gold = Color(0xFFC09843)
    val purple = Color(0xFF755DCF)
    val transition = rememberInfiniteTransition(label = "map")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val symbol = BookSymbol.entries.getOrElse(store.avatar) { BookSymbol.ORB }

    Box(
        modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17102E), Color(0xFF0B1A2A))))
            .border(1.dp, purple.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            for (i in 0 until 18) {
                val x = ((i * 71 + 11) % 100) / 100f * size.width
                val y = ((i * 37 + 5) % 100) / 100f * size.height
                drawCircle(Color.White.copy(alpha = 0.16f), if (i % 5 == 0) 2f else 1.1f, Offset(x, y))
            }
            val nodes = List(ranks.size) { index ->
                val t = if (ranks.size <= 1) 0f else index.toFloat() / (ranks.size - 1)
                Offset(
                    x = size.width * (0.5f + 0.31f * sin(t * PI.toFloat() * 2.4f + 0.35f)),
                    y = size.height * (0.88f - 0.74f * t)
                )
            }
            if (nodes.size > 1) {
                val path = Path().apply {
                    moveTo(nodes.first().x, nodes.first().y)
                    for (i in 1 until nodes.size) {
                        val previous = nodes[i - 1]
                        val node = nodes[i]
                        val mid = (previous.y + node.y) / 2f
                        cubicTo(previous.x, mid, node.x, mid, node.x, node.y)
                    }
                }
                drawPath(path, Color.Black.copy(alpha = 0.35f), style = Stroke(width = 26f, cap = StrokeCap.Round))
                drawPath(path, purple.copy(alpha = 0.30f), style = Stroke(width = 18f, cap = StrokeCap.Round))
                drawPath(path, gold.copy(alpha = 0.85f), style = Stroke(width = 5f, cap = StrokeCap.Round))
            }
            nodes.forEachIndexed { index, node ->
                val done = index < currentIndex
                val current = index == currentIndex
                val topColor = when {
                    done -> gold
                    current -> colors.primary
                    else -> Color(0xFF4B3D63)
                }
                val glow = if (current) 0.35f + pulse * 0.35f else 0.0f
                if (current) {
                    drawCircle(topColor.copy(alpha = glow), 42f + pulse * 8f, node)
                }
                drawOval(
                    Color.Black.copy(alpha = 0.35f),
                    topLeft = Offset(node.x - 26f, node.y + 8f),
                    size = Size(52f, 16f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(topColor.copy(alpha = 0.95f), topColor.copy(alpha = 0.35f))),
                    topLeft = Offset(node.x - 22f, node.y - 8f),
                    size = Size(44f, 28f),
                    cornerRadius = CornerRadius(10f)
                )
                drawOval(
                    color = topColor,
                    topLeft = Offset(node.x - 22f, node.y - 18f),
                    size = Size(44f, 22f)
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(node.x - 14f, node.y - 15f),
                    size = Size(15f, 7f)
                )
                if (done) {
                    drawCircle(Color(0xFF121025), 7f, node)
                    drawCircle(gold, 3f, node)
                }
                if (current) {
                    val playerCenter = Offset(node.x, node.y - 34f - bob * 9f)
                    drawPlayer(playerCenter, symbol, topColor)
                }
            }
        }
    }
}

private fun DrawScope.drawPlayer(center: Offset, symbol: BookSymbol, color: Color) {
    drawOval(
        Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(center.x - 18f, center.y + 24f),
        size = Size(36f, 10f)
    )
    val cape = Path().apply {
        moveTo(center.x - 14f, center.y - 4f)
        lineTo(center.x - 22f, center.y + 22f)
        lineTo(center.x + 4f, center.y + 16f)
        lineTo(center.x + 2f, center.y - 5f)
        close()
    }
    drawPath(cape, color.copy(alpha = 0.55f))
    val robe = Path().apply {
        moveTo(center.x - 5f, center.y - 4f)
        lineTo(center.x - 13f, center.y + 22f)
        lineTo(center.x + 13f, center.y + 22f)
        lineTo(center.x + 5f, center.y - 4f)
        close()
    }
    drawPath(robe, Brush.verticalGradient(listOf(color, color.copy(alpha = 0.65f))))
    drawLine(
        Color(0xFFC09843),
        Offset(center.x + 17f, center.y + 24f),
        Offset(center.x + 13f, center.y - 28f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.95f), color.copy(alpha = 0.55f), Color.Transparent),
            center = Offset(center.x + 13f, center.y - 30f),
            radius = 14f
        ),
        radius = 14f,
        center = Offset(center.x + 13f, center.y - 30f)
    )
    drawBookSymbol(symbol, Color.White, Offset(center.x + 13f, center.y - 30f), 5.5f)
    drawCircle(Color(0xFFF1D3A2), 6.5f, Offset(center.x, center.y - 10f))
    val hat = Path().apply {
        moveTo(center.x - 9f, center.y - 14f)
        lineTo(center.x, center.y - 30f)
        lineTo(center.x + 9f, center.y - 14f)
        close()
    }
    drawPath(hat, color.copy(alpha = 0.95f))
    drawCircle(Color.White.copy(alpha = 0.55f), 2f, Offset(center.x - 2f, center.y - 11f))
}

@Composable
private fun RankChip(name: String, level: Int, done: Boolean, current: Boolean) {
    val color = when {
        current -> MaterialTheme.colorScheme.primary
        done -> Color(0xFFC09843)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (current) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$level", style = MaterialTheme.typography.labelSmall, color = color)
        Text(name, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun CampaignRow(store: Store, campaign: Campaign, onFeature: (String) -> Unit) {
    val checked = campaign.lastCheckin > 0L && dayOf(campaign.lastCheckin) == dayOf(System.currentTimeMillis())
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(campaign.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${campaign.checkins}/${campaign.target}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = { store.checkinCampaign(campaign.id) },
                enabled = !checked && !campaign.done
            ) {
                Text(stringResource(if (checked) R.string.path_checked else R.string.path_checkin))
            }
        }
        if (campaign.done) {
            Text(stringResource(R.string.campaign_done), style = MaterialTheme.typography.labelSmall, color = Color(0xFFC09843))
        } else {
            LinearProgressIndicator(
                progress = { campaign.checkins / campaign.target.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFC09843),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
internal fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun rankLabel(key: String): String = when (key) {
    "novice" -> stringResource(R.string.rank_novice)
    "apprentice" -> stringResource(R.string.rank_apprentice)
    "knight" -> stringResource(R.string.rank_knight)
    "captain" -> stringResource(R.string.rank_captain)
    "mage" -> stringResource(R.string.rank_mage)
    "sage" -> stringResource(R.string.rank_sage)
    "emperor" -> stringResource(R.string.rank_emperor)
    else -> key
}

private fun dayOf(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
