package feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.navigation.Routes

private val features = listOf(
    Routes.TASKS to R.string.feature_tasks,
    Routes.MONEY to R.string.feature_money,
    Routes.WORKOUTS to R.string.feature_workouts,
    Routes.JOURNAL to R.string.feature_journal,
    Routes.GAME to R.string.feature_game,
    Routes.WATER to R.string.feature_water,
    Routes.SLEEP to R.string.feature_sleep,
    Routes.FAITH to R.string.feature_faith,
    Routes.LEARN to R.string.feature_learn,
    Routes.LIBRARY to R.string.feature_library,
    Routes.SHOP to R.string.feature_shop,
    Routes.RAIDS to R.string.feature_raids,
    Routes.PROFILE to R.string.feature_profile,
    Routes.SKIN to R.string.feature_skin,
    Routes.BLOCKER to R.string.feature_blocker,
    Routes.CLEANER to R.string.feature_cleaner,
    Routes.CONTACTS to R.string.feature_contacts,
    Routes.DEVICE to R.string.feature_device,
    Routes.DEVMODE to R.string.feature_devmode,
    Routes.FIREWALL to R.string.feature_firewall,
    Routes.FORUM to R.string.feature_forum,
    Routes.LICENSE to R.string.feature_license,
    Routes.NOTIFLOG to R.string.feature_notiflog,
    Routes.TESTS to R.string.feature_tests,
    Routes.UPDATER to R.string.feature_updater,
    Routes.VAULT to R.string.feature_vault,
    Routes.WALLETS to R.string.feature_wallets
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onFeature: (String) -> Unit, onSettings: () -> Unit) {
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
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Text(
                    stringResource(R.string.features),
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(features) { (route, title) ->
                ListItem(
                    headlineContent = { Text(stringResource(title)) },
                    modifier = Modifier.clickable { onFeature(route) }
                )
            }
        }
    }
}
