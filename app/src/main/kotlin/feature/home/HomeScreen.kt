package feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yousuf.toolkit.R
import core.design.MagicBackground
import core.navigation.Routes
import data.local.LocalStore
import feature.money.MoneyScreen
import feature.tasks.TasksScreen

private data class Tab(val label: Int, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFeature: (String) -> Unit,
    onSettings: () -> Unit,
    onAccount: () -> Unit
) {
    val store = LocalStore.current
    val starterTasks = stringArrayResource(R.array.starter_tasks).toList()
    LaunchedEffect(Unit) { store.seedStarterTasks(starterTasks) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    val tabs = listOf(
        Tab(R.string.tab_today, Icons.Filled.Home),
        Tab(R.string.tab_tasks, Icons.Filled.CheckCircle),
        Tab(R.string.tab_money, Icons.Filled.ShoppingCart),
        Tab(R.string.tab_tools, Icons.Filled.List)
    )
    val title = when (tab) {
        0 -> R.string.tab_today
        1 -> R.string.tab_tasks
        2 -> R.string.tab_money
        else -> R.string.tab_tools
    }

    MagicBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopAppBar(
                    title = {
                        if (tab == 0) {
                            Column {
                                Text(
                                    if (store.profileName.isNotBlank()) {
                                        stringResource(R.string.home_greeting, store.profileName)
                                    } else {
                                        stringResource(R.string.home_greeting_guest)
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(title),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                stringResource(title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onAccount) {
                            Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.account_title))
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.feature_settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)) {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = { tab = index },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label), fontSize = 12.sp) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (tab != 3) {
                    FloatingActionButton(onClick = { adding = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.quick_add))
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> TodayTab(
                        store = store,
                        onTasks = { tab = 1 },
                        onRank = { onFeature(Routes.PATH) },
                        onStats = { onFeature(Routes.STATS) },
                        onWater = { onFeature(Routes.WATER) },
                        onFaith = { onFeature(Routes.FAITH) },
                        onSleep = { onFeature(Routes.SLEEP) },
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> TasksScreen(
                        embedded = true,
                        onMore = { onFeature(Routes.TASKSPRO) }
                    )
                    2 -> MoneyScreen(
                        embedded = true,
                        onWallets = { onFeature(Routes.WALLETS) },
                        onMore = { onFeature(Routes.MONEYPRO) }
                    )
                    else -> ToolsTab(onFeature, Modifier.fillMaxSize())
                }
            }
        }

        if (adding) {
            QuickAddSheet(store, onDismiss = { adding = false })
        }
    }
}
