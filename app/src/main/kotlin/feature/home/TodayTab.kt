package feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.StatTile
import core.design.drawBookSymbol
import data.local.Store
import domain.model.Priority
import domain.model.Task
import domain.model.prayerKeys

@Composable
fun TodayTab(
    store: Store,
    onTasks: () -> Unit,
    onRank: () -> Unit,
    onStats: () -> Unit,
    onWater: () -> Unit,
    onFaith: () -> Unit,
    onSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var denied by remember { mutableStateOf(false) }
    val open = store.tasks.filterNot { it.done }
    val todayTasks = open.sortedWith(
        compareBy<Task> { if (it.dueAt > 0L) 0 else 1 }.thenBy { it.dueAt }.thenByDescending { it.priority.ordinal }
    ).take(3)
    val done = store.tasks.count { it.done }
    val faith = store.prayersToday().size
    val water = store.waterToday()
    val sleep = store.sleepToday()
    val waterGoal = store.config.waterGoal.coerceAtLeast(1)
    val sleepGoal = store.config.sleepGoal.coerceAtLeast(1)

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onRank),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(MaterialTheme.colorScheme.primary, lerp(MaterialTheme.colorScheme.primary, Color.Black, 0.55f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                store.level.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                rankLabel(store.rankKey),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.game_level_points, store.level, store.points),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${store.points} XP",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { store.rankProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        store.nextRank?.let { stringResource(R.string.rank_next, rankLabel(it.first), it.second) }
                            ?: stringResource(R.string.rank_max),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.today_tasks),
                action = stringResource(R.string.today_view_all),
                onAction = onTasks
            )
        }
        if (todayTasks.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.today_no_tasks),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(todayTasks, key = { "today-${it.id}" }) { task ->
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .padding(start = 10.dp)
                                .size(width = 4.dp, height = 36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(priorityColor(task.priority))
                        )
                        Checkbox(
                            checked = task.done,
                            onCheckedChange = { if (!store.toggleTask(task.id)) denied = true }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (task.done) TextDecoration.LineThrough else null
                            )
                            Text(
                                priorityLabel(task.priority),
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor(task.priority)
                            )
                        }
                        if (task.dueAt <= 0L) {
                            Text(
                                stringResource(R.string.taskspro_no_due),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = stringResource(R.string.today_habits), action = null, onAction = {})
        }
        item {
            HabitRow(
                symbol = BookSymbol.DROP,
                color = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.feature_water),
                subtitle = stringResource(R.string.water_goal, waterGoal),
                value = "$water / $waterGoal",
                progress = water.toFloat() / waterGoal,
                onAdd = { store.addWater(1) },
                onOpen = onWater
            )
        }
        item {
            HabitRow(
                symbol = BookSymbol.STAR,
                color = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.feature_faith),
                subtitle = stringResource(R.string.faith_today),
                value = stringResource(R.string.faith_progress, faith, prayerKeys.size),
                progress = faith.toFloat() / prayerKeys.size,
                onAdd = { store.togglePrayer(prayerKeys.firstOrNull { it !in store.prayersToday() } ?: prayerKeys.last()) },
                onOpen = onFaith
            )
        }
        item {
            HabitRow(
                symbol = BookSymbol.MOON,
                color = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.feature_sleep),
                subtitle = stringResource(R.string.sleep_goal, sleepGoal),
                value = stringResource(R.string.sleep_hours_value, formatHours(sleep)),
                progress = (sleep / sleepGoal).toFloat().coerceIn(0f, 1f),
                onAdd = onSleep,
                onOpen = onSleep
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(open.size.toString(), stringResource(R.string.today_open), Modifier.weight(1f), onStats)
                MiniStat(done.toString(), stringResource(R.string.today_done), Modifier.weight(1f), onStats)
                MiniStat("${(if (store.tasks.isEmpty()) 0 else done * 100 / store.tasks.size)}%", stringResource(R.string.today_ratio), Modifier.weight(1f), onStats)
            }
        }
    }

    if (denied) {
        AlertDialog(
            onDismissRequest = { denied = false },
            title = { Text(stringResource(R.string.energy_title)) },
            text = { Text(stringResource(R.string.not_enough_energy)) },
            confirmButton = { TextButton(onClick = { denied = false }) { Text(stringResource(R.string.ok)) } }
        )
    }
}

@Composable
private fun SectionHeader(title: String, action: String?, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun HabitRow(
    symbol: BookSymbol,
    color: Color,
    title: String,
    subtitle: String,
    value: String,
    progress: Float,
    onAdd: () -> Unit,
    onOpen: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(22.dp)) {
                    drawBookSymbol(symbol, color, Offset(size.width / 2, size.height / 2), size.minDimension * 0.44f)
                }
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    StatTile(value = value, label = label, modifier = modifier, onClick = onClick)
}

@Composable
private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.LOW -> stringResource(R.string.priority_low)
    Priority.NORMAL -> stringResource(R.string.priority_normal)
    Priority.HIGH -> stringResource(R.string.priority_high)
}

@Composable
private fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    Priority.NORMAL -> MaterialTheme.colorScheme.primary
    Priority.HIGH -> MaterialTheme.colorScheme.error
}

@Composable
private fun rankLabel(key: String): String = when (key) {
    "apprentice" -> stringResource(R.string.rank_apprentice)
    "knight" -> stringResource(R.string.rank_knight)
    "lieutenant" -> stringResource(R.string.rank_lieutenant)
    "captain" -> stringResource(R.string.rank_captain)
    "mage" -> stringResource(R.string.rank_mage)
    "sage" -> stringResource(R.string.rank_sage)
    "emperor" -> stringResource(R.string.rank_emperor)
    else -> stringResource(R.string.rank_novice)
}

private fun formatHours(hours: Double): String =
    if (hours % 1.0 == 0.0) hours.toInt().toString() else String.format(java.util.Locale.US, "%.1f", hours)
