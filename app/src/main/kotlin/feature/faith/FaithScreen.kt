package feature.faith

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.prayerKeys

@Composable
fun FaithScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val done = store.prayersToday()
    val ratio = done.size.toFloat() / prayerKeys.size

    ScreenScaffold(title = stringResource(R.string.feature_faith), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.faith_today), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.faith_progress, done.size, prayerKeys.size),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            stringResource(R.string.faith_full_days, store.faithFullDays()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        prayerKeys.forEachIndexed { index, key ->
                            if (index > 0) HorizontalDivider()
                            Row(
                                Modifier.fillMaxWidth().clickable { store.togglePrayer(key) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = key in done,
                                    onCheckedChange = { store.togglePrayer(key) }
                                )
                                Text(prayerLabel(key), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun prayerLabel(key: String): String = when (key) {
    "fajr" -> stringResource(R.string.faith_fajr)
    "dhuhr" -> stringResource(R.string.faith_dhuhr)
    "asr" -> stringResource(R.string.faith_asr)
    "maghrib" -> stringResource(R.string.faith_maghrib)
    else -> stringResource(R.string.faith_isha)
}
