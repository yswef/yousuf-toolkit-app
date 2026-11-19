package feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import data.local.Store
import domain.model.prayerKeys
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatsTab(store: Store, modifier: Modifier = Modifier) {
    val progress = dailyProgress(store)
    val animated by animateFloatAsState(progress, tween(900), label = "today")
    val week = weekActivity(store)
    val maxWeek = week.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    val income = store.ledger.filter { it.income }.sumOf { it.amount }
    val expense = store.ledger.filterNot { it.income }.sumOf { it.amount }
    val streak = streak(store)
    val doneTasks = store.tasks.count { it.done }
    val waterTotal = store.water.values.sum()
    val labels = stringArrayResource(R.array.week_days_short)
    val track = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                stringResource(R.string.stats_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(118.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val width = 13.dp.toPx()
                            drawArc(
                                color = track,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(Color(0xFFC09843), Color(0xFF755DCF), Color(0xFF5CC59A), Color(0xFFC09843))
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animated,
                                useCenter = false,
                                style = Stroke(width, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(progress * 100).roundToInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.stats_completion),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RingRow(
                            stringResource(R.string.stats_water),
                            stringResource(R.string.home_water_value, store.waterToday(), store.config.waterGoal),
                            if (store.config.waterGoal > 0) store.waterToday() / store.config.waterGoal.toFloat() else 0f,
                            Color(0xFF22D3EE)
                        )
                        RingRow(
                            stringResource(R.string.stats_prayers),
                            "${store.prayersToday().size}/${prayerKeys.size}",
                            store.prayersToday().size / prayerKeys.size.toFloat(),
                            Color(0xFF5CC59A)
                        )
                        RingRow(
                            stringResource(R.string.stats_sleep),
                            String.format(Locale.US, "%.1f", store.sleepToday()),
                            if (store.config.sleepGoal > 0) (store.sleepToday() / store.config.sleepGoal).toFloat() else 0f,
                            Color(0xFF8B7CFF)
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    symbol = BookSymbol.STAR,
                    value = store.openTasks.toString(),
                    label = stringResource(R.string.stats_open_tasks),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    symbol = BookSymbol.DIAMOND,
                    value = money(store.balance),
                    label = stringResource(R.string.stats_balance),
                    color = Color(0xFFC09843),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    symbol = BookSymbol.FLAME,
                    value = store.workouts.size.toString(),
                    label = stringResource(R.string.stats_workouts),
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    symbol = BookSymbol.RUNE,
                    value = store.journal.size.toString(),
                    label = stringResource(R.string.stats_journal),
                    color = Color(0xFF6FE3D5),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            GlassCard {
                Text(
                    stringResource(R.string.stats_week),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (week.all { it.second <= 0f }) {
                    Text(
                        stringResource(R.string.stats_week_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Canvas(Modifier.fillMaxWidth().height(132.dp)) {
                        val gap = 8.dp.toPx()
                        val barWidth = (size.width - gap * 6) / 7f
                        week.forEachIndexed { index, item ->
                            val height = (size.height - 14.dp.toPx()) * (item.second / maxWeek)
                            val left = index * (barWidth + gap)
                            drawRoundRect(
                                color = track.copy(alpha = 0.7f),
                                topLeft = Offset(left, 0f),
                                size = Size(barWidth, size.height - 14.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
                            )
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(Color(0xFFC09843), Color(0xFF755DCF))),
                                topLeft = Offset(left, size.height - 14.dp.toPx() - height),
                                size = Size(barWidth, height.coerceAtLeast(4f)),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        week.forEachIndexed { index, item ->
                            Text(
                                labels[(item.first.dayOfWeek.value % 7)],
                                style = MaterialTheme.typography.labelSmall,
                                color = if (index == week.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Text(
                    stringResource(R.string.stats_money),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val total = (income + expense).coerceAtLeast(1.0)
                            val incomeSweep = (income / total * 360.0).toFloat()
                            drawArc(
                                color = Color(0xFFC09843),
                                startAngle = -90f,
                                sweepAngle = incomeSweep,
                                useCenter = false,
                                style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFFFF6B8A),
                                startAngle = -90f + incomeSweep,
                                sweepAngle = 360f - incomeSweep,
                                useCenter = false,
                                style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text("${(income / (income + expense).coerceAtLeast(1.0) * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
                    }
                    Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoneyRow(stringResource(R.string.stats_income), money(income), Color(0xFFC09843))
                        MoneyRow(stringResource(R.string.stats_expense), money(expense), Color(0xFFFF6B8A))
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    symbol = BookSymbol.FLAME,
                    value = stringResource(R.string.stats_streak_days, streak),
                    label = stringResource(R.string.stats_streak),
                    color = Color(0xFFFFB454),
                    modifier = Modifier.weight(1.3f)
                )
                StatCard(
                    symbol = BookSymbol.ORB,
                    value = stringResource(R.string.stats_best_points, store.points),
                    label = stringResource(R.string.stats_best),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1.7f)
                )
            }
        }
        item {
            GlassCard {
                Text(
                    stringResource(R.string.stats_today),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.stats_best_tasks, doneTasks) + " · " +
                        stringResource(R.string.stats_best_water, waterTotal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RingRow(label: String, value: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.labelSmall, color = color)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StatCard(symbol: BookSymbol, value: String, label: String, color: Color, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(18.dp)) {
                    core.design.drawBookSymbol(symbol, color, Offset(size.width / 2, size.height / 2), size.minDimension * 0.42f)
                }
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MoneyRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
    }
}

private fun dailyProgress(store: Store): Float {
    val water = if (store.config.waterGoal > 0) (store.waterToday().toFloat() / store.config.waterGoal).coerceAtMost(1f) else 0f
    val prayers = store.prayersToday().size / prayerKeys.size.toFloat()
    val sleep = if (store.config.sleepGoal > 0) (store.sleepToday() / store.config.sleepGoal).toFloat().coerceAtMost(1f) else 0f
    val tasks = store.tasks.let { list ->
        if (list.isEmpty()) 0f else list.count { it.done } / list.size.toFloat()
    }
    return (water + prayers + sleep + tasks) / 4f
}

private fun weekActivity(store: Store): List<Pair<LocalDate, Float>> {
    val today = LocalDate.now()
    return (6 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val key = day.toString()
        val water = (store.water[key] ?: 0) * 0.35f
        val prayers = (store.prayers[key]?.size ?: 0) * 0.9f
        val workouts = store.workouts.count { dayOf(it.at) == key } * 1.4f
        val journal = store.journal.count { dayOf(it.at) == key } * 0.8f
        val tasks = store.tasks.count { it.done && dayOf(it.createdAt) == key } * 0.7f
        val sleep = if (store.sleep.any { dayOf(it.at) == key }) 0.5f else 0f
        day to (water + prayers + workouts + journal + tasks + sleep)
    }
}

private fun streak(store: Store): Int {
    var count = 0
    var day = LocalDate.now()
    repeat(365) {
        val key = day.toString()
        val active = (store.water[key] ?: 0) > 0 ||
            (store.prayers[key]?.isNotEmpty() == true) ||
            store.workouts.any { dayOf(it.at) == key } ||
            store.journal.any { dayOf(it.at) == key } ||
            store.sleep.any { dayOf(it.at) == key }
        if (!active) return count
        count++
        day = day.minusDays(1)
    }
    return count
}

private fun money(value: Double): String = String.format(Locale.US, "%,.0f", value)

private fun dayOf(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
