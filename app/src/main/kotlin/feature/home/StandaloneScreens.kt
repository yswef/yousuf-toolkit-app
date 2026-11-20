package feature.home

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import core.design.UiState
import core.navigation.Routes
import data.local.LocalStore

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    ScreenScaffold(title = stringResource(R.string.stats_title), onBack = onBack) { padding ->
        StatsTab(store, Modifier.fillMaxSize().padding(padding))
    }
}

@Composable
fun PathScreen(onBack: () -> Unit, onFeature: (String) -> Unit) {
    val store = LocalStore.current
    ScreenScaffold(title = stringResource(R.string.path_title), onBack = onBack) { padding ->
        PathTab(
            store = store,
            onFeature = onFeature,
            onAdd = { onFeature(Routes.GAME) },
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

@Composable
fun AccountScreen(onBack: () -> Unit, onFeature: (String) -> Unit, onSettings: () -> Unit, ui: UiState) {
    val store = LocalStore.current
    ScreenScaffold(title = stringResource(R.string.account_title), onBack = onBack) { padding ->
        AccountTab(
            store = store,
            ui = ui,
            onFeature = onFeature,
            onSettings = onSettings,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
