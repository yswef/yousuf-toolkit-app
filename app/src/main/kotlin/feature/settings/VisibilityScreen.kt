// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.apps.moodHiddenRoutes
import core.apps.moodLabels
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.V3Chip
import data.local.LocalStore
import feature.home.bookSections

@Composable
fun VisibilityScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val moodHidden = moodHiddenRoutes(store.mood)
    val all = bookSections.flatMap { it.books }.map { it.route }
    val ordered = (store.routeOrder.filter { it in all } + all.filter { it !in store.routeOrder }).distinct()
    LaunchedEffect(ordered) {
        if (store.routeOrder != ordered) store.setRouteOrder(ordered)
    }

    ScreenScaffold(title = stringResource(R.string.visibility_title), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { InfoCard(text = stringResource(R.string.apps_sub), tone = InfoTone.INFO) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    moodLabels.forEach { (key, label) ->
                        V3Chip(label, store.mood == key, { store.setMood(key) }, Modifier.weight(1f))
                    }
                }
            }
            item {
                OutlinedButton(onClick = {
                    store.setMood("all")
                    store.hiddenRoutes.forEach { store.toggleHiddenRoute(it) }
                }) { Text(stringResource(R.string.visibility_reset)) }
            }
            item { Text(stringResource(R.string.visibility_books), style = androidx.compose.material3.MaterialTheme.typography.titleMedium) }
            items(ordered, key = { it }) { route ->
                val book = bookSections.flatMap { it.books }.firstOrNull { it.route == route }
                if (book == null) return@items
                val hidden = route in store.hiddenRoutes || route in moodHidden
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(book.title), Modifier.weight(1f))
                        OutlinedButton(onClick = { store.toggleHiddenRoute(route) }) {
                            Text(stringResource(if (hidden) R.string.apps_show else R.string.apps_hide))
                        }
                    }
                    Row(Modifier.padding(horizontal = 12.dp)) {
                        OutlinedButton(onClick = { store.moveRoute(route, true) }) { Text(stringResource(R.string.visibility_up)) }
                        OutlinedButton(onClick = { store.moveRoute(route, false) }) { Text(stringResource(R.string.visibility_down)) }
                    }
                }
            }
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
