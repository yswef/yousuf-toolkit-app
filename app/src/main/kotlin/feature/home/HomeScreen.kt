package feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.navigation.Routes
import data.local.LocalStore
import data.local.Store
import java.util.Locale

private data class Section(val title: Int, val items: List<Pair<String, Int>>)

private val sections = listOf(
    Section(
        R.string.section_core,
        listOf(
            Routes.TASKS to R.string.feature_tasks,
            Routes.MONEY to R.string.feature_money,
            Routes.WORKOUTS to R.string.feature_workouts,
            Routes.JOURNAL to R.string.feature_journal
        )
    ),
    Section(
        R.string.section_daily,
        listOf(
            Routes.WATER to R.string.feature_water,
            Routes.SLEEP to R.string.feature_sleep,
            Routes.FAITH to R.string.feature_faith,
            Routes.LEARN to R.string.feature_learn,
            Routes.LIBRARY to R.string.feature_library
        )
    ),
    Section(
        R.string.section_game,
        listOf(
            Routes.GAME to R.string.feature_game,
            Routes.SHOP to R.string.feature_shop,
            Routes.RAIDS to R.string.feature_raids,
            Routes.PROFILE to R.string.feature_profile,
            Routes.SKIN to R.string.feature_skin
        )
    ),
    Section(
        R.string.section_device,
        listOf(
            Routes.BLOCKER to R.string.feature_blocker,
            Routes.CLEANER to R.string.feature_cleaner,
            Routes.CONTACTS to R.string.feature_contacts,
            Routes.DEVICE to R.string.feature_device,
            Routes.FIREWALL to R.string.feature_firewall,
            Routes.NOTIFLOG to R.string.feature_notiflog,
            Routes.UPDATER to R.string.feature_updater,
            Routes.VAULT to R.string.feature_vault,
            Routes.WALLETS to R.string.feature_wallets
        )
    ),
    Section(
        R.string.section_more,
        listOf(
            Routes.FORUM to R.string.feature_forum,
            Routes.LICENSE to R.string.feature_license,
            Routes.DEVMODE to R.string.feature_devmode,
            Routes.TESTS to R.string.feature_tests
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onFeature: (String) -> Unit, onSettings: () -> Unit) {
    val store = LocalStore.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_home)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.feature_settings))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") { HeroCard(store) }
            item(key = "today") { TodayCard(store, onFeature) }
            sections.forEach { section ->
                item(key = "section-${section.title}") {
                    Text(
                        stringResource(section.title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(items = section.items, key = { it.first }) { (route, title) ->
                    ListItem(
                        headlineContent = { Text(stringResource(title)) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFeature(route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(store: Store) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_level, store.level),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.home_points, store.points),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    stringResource(R.string.home_xp, (store.levelProgress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { store.levelProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_energy, store.energy, store.config.energyMax),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { store.rest() }) {
                    Text(stringResource(R.string.home_rest, store.config.restGain))
                }
            }
        }
    }
}

@Composable
private fun TodayCard(store: Store, onFeature: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.home_today), style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Stat(
                    label = stringResource(R.string.home_tasks_left),
                    value = store.openTasks.toString(),
                    modifier = Modifier.weight(1f)
                ) { onFeature(Routes.TASKS) }
                Stat(
                    label = stringResource(R.string.home_water),
                    value = stringResource(R.string.home_water_value, store.waterToday(), store.config.waterGoal),
                    modifier = Modifier.weight(1f)
                ) { onFeature(Routes.WATER) }
                Stat(
                    label = stringResource(R.string.home_prayers),
                    value = stringResource(R.string.home_prayers_value, store.prayersToday().size),
                    modifier = Modifier.weight(1f)
                ) { onFeature(Routes.FAITH) }
                Stat(
                    label = stringResource(R.string.home_sleep),
                    value = String.format(Locale.US, "%.1f", store.sleepToday()),
                    modifier = Modifier.weight(1f)
                ) { onFeature(Routes.SLEEP) }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
