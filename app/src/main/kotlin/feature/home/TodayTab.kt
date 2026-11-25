// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.StatTile
import core.design.V3CheckRow
import core.design.V3Group
import core.design.V3HabitTile
import core.design.V3Hero
import core.design.V3Ring
import core.design.V3Row
import core.design.V3SectionHeader
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
    val scheme = MaterialTheme.colorScheme
    val open = store.tasks.filterNot { it.done }
    val todayTasks = open.sortedWith(
        compareBy<Task> { if (it.dueAt > 0L) 0 else 1 }.thenBy { it.dueAt }.thenByDescending { it.priority.ordinal }
    ).take(3)
    val done = store.tasks.count { it.done }
    val total = store.tasks.size
    val faith = store.prayersToday().size
    val water = store.waterToday()
    val sleep = store.sleepToday()
    val waterGoal = store.config.waterGoal.coerceAtLeast(1)
    val sleepGoal = store.config.sleepGoal.coerceAtLeast(1)
    val prayerGoal = prayerKeys.size.coerceAtLeast(1)

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            V3Hero(onClick = onRank) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    V3Ring(
                        progress = if (total == 0) 0f else done.toFloat() / total,
                        size = 84.dp,
                        stroke = 9.dp,
                        color = scheme.primary
                    ) {
                        Text(
                            "$done/$total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.v3_today_progress, done, total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer
                        )
                        Text(
                            stringResource(R.string.v3_today_level, store.level, rankLabel(store.rankKey)),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimaryContainer
                        )
                        LinearProgressIndicator(
                            progress = { store.rankProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = scheme.primary,
                            trackColor = scheme.surfaceVariant
                        )
                        Text(
                            store.nextRank?.let { stringResource(R.string.rank_next, rankLabel(it.first), it.second) }
                                ?: stringResource(R.string.rank_max),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            V3SectionHeader(
                title = stringResource(R.string.today_tasks),
                action = stringResource(R.string.today_view_all),
                onAction = onTasks
            )
        }
        item {
            if (todayTasks.isEmpty()) {
                V3Group {
                    V3Row(title = stringResource(R.string.today_no_tasks), icon = Icons.Filled.List)
                }
            } else {
                V3Group {
                    todayTasks.forEachIndexed { index, task ->
                        V3CheckRow(
                            title = task.title,
                            subtitle = priorityLabel(task.priority),
                            checked = task.done,
                            dotColor = priorityColor(task.priority),
                            divider = index > 0,
                            onToggle = { if (!store.toggleTask(task.id)) denied = true }
                        )
                    }
                }
            }
        }

        item { V3SectionHeader(title = stringResource(R.string.today_habits)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                V3HabitTile(
                    name = stringResource(R.string.feature_water),
                    value = "$water / $waterGoal",
                    addText = stringResource(R.string.v3_add_one),
                    progress = water.toFloat() / waterGoal,
                    symbol = BookSymbol.DROP,
                    color = scheme.secondary,
                    onAdd = { store.addWater(1) },
                    onOpen = onWater,
                    modifier = Modifier.weight(1f)
                )
                V3HabitTile(
                    name = stringResource(R.string.feature_faith),
                    value = stringResource(R.string.faith_progress, faith, prayerGoal),
                    addText = stringResource(R.string.v3_add_one),
                    progress = faith.toFloat() / prayerGoal,
                    symbol = BookSymbol.STAR,
                    color = scheme.tertiary,
                    onAdd = if (faith < prayerGoal) {
                        {
                            val key = prayerKeys.firstOrNull { it !in store.prayersToday() } ?: prayerKeys.last()
                            store.togglePrayer(key)
                        }
                    } else null,
                    onOpen = onFaith,
                    modifier = Modifier.weight(1f)
                )
                V3HabitTile(
                    name = stringResource(R.string.feature_sleep),
                    value = stringResource(R.string.sleep_hours_value, formatHours(sleep)),
                    addText = stringResource(R.string.v3_add_one),
                    progress = (sleep / sleepGoal).toFloat().coerceIn(0f, 1f),
                    symbol = BookSymbol.MOON,
                    color = scheme.primary,
                    onAdd = null,
                    onOpen = onSleep,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            V3Group {
                V3Row(
                    title = stringResource(R.string.tab_stats),
                    subtitle = stringResource(R.string.today_ratio),
                    icon = Icons.Filled.List,
                    onClick = onStats
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(open.size.toString(), stringResource(R.string.today_open), Modifier.weight(1f), onStats)
                StatTile(done.toString(), stringResource(R.string.today_done), Modifier.weight(1f), onStats)
                StatTile(
                    "${if (total == 0) 0 else done * 100 / total}%",
                    stringResource(R.string.today_ratio),
                    Modifier.weight(1f),
                    onStats
                )
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
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
