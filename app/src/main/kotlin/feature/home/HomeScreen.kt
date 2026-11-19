package feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yousuf.toolkit.R
import core.design.MagicBackground
import core.navigation.Routes
import data.local.LocalStore
import data.local.Store
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onFeature: (String) -> Unit, onSettings: () -> Unit) {
    val store = LocalStore.current
    var opening by remember { mutableStateOf<MagicBook?>(null) }

    MagicBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                if (store.profileName.isNotBlank()) {
                                    stringResource(R.string.home_greeting, store.profileName)
                                } else {
                                    stringResource(R.string.home_greeting_guest)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.feature_settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "hero") {
                    HeroBanner(store)
                }
                item(span = { GridItemSpan(maxLineSpan) }, key = "today") {
                    TodayRow(store, onFeature)
                }
                item(span = { GridItemSpan(maxLineSpan) }, key = "shelf") {
                    Column(Modifier.padding(top = 6.dp)) {
                        Text(
                            stringResource(R.string.home_books),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.home_books_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                bookSections.forEach { section ->
                    item(span = { GridItemSpan(maxLineSpan) }, key = "section-${section.title}") {
                        Text(
                            stringResource(section.title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(items = section.books, key = { it.route }) { book ->
                        MagicBookCard(book = book, onClick = { opening = book })
                    }
                }
            }
        }

        opening?.let { book ->
            MagicBookOpen(
                book = book,
                onCancel = { opening = null },
                onOpened = {
                    opening = null
                    onFeature(book.route)
                }
            )
        }
    }
}

@Composable
private fun HeroBanner(store: Store) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF7B2FF7), Color(0xFF43118F), Color(0xFF0F6E75))
                )
            )
            .border(1.dp, Color(0xFFFFD166).copy(alpha = 0.45f), shape)
            .padding(18.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.9f, size.height * 0.05f)
            )
            drawCircle(
                color = Color(0xFFFFD166).copy(alpha = 0.14f),
                radius = size.width * 0.3f,
                center = Offset(size.width * 0.05f, size.height * 0.95f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_level, store.level),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                    Text(
                        stringResource(R.string.home_points, store.points),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD166)
                    )
                }
                Text(
                    stringResource(R.string.home_xp, (store.levelProgress * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
            LinearProgressIndicator(
                progress = { store.levelProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFD166),
                trackColor = Color.White.copy(alpha = 0.18f)
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_energy, store.energy, store.config.energyMax),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                TextButton(onClick = { store.rest() }) {
                    Text(
                        stringResource(R.string.home_rest, store.config.restGain),
                        color = Color(0xFFFFD166)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayRow(store: Store, onFeature: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniStat(
            value = store.openTasks.toString(),
            label = stringResource(R.string.home_tasks_left),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = { onFeature(Routes.TASKS) }
        )
        MiniStat(
            value = stringResource(R.string.home_water_value, store.waterToday(), store.config.waterGoal),
            label = stringResource(R.string.home_water),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
            onClick = { onFeature(Routes.WATER) }
        )
        MiniStat(
            value = stringResource(R.string.home_prayers_value, store.prayersToday().size),
            label = stringResource(R.string.home_prayers),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
            onClick = { onFeature(Routes.FAITH) }
        )
        MiniStat(
            value = String.format(Locale.US, "%.1f", store.sleepToday()),
            label = stringResource(R.string.home_sleep),
            color = Color(0xFF6A5CFF),
            modifier = Modifier.weight(1f),
            onClick = { onFeature(Routes.SLEEP) }
        )
    }
}

@Composable
private fun MiniStat(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
