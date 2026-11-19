package feature.water

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import data.local.LocalStore
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WaterScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val cups = store.waterToday()
    val goal = store.config.waterGoal
    val ratio = if (goal > 0) (cups.toFloat() / goal).coerceIn(0f, 1f) else 0f

    ScreenScaffold(title = stringResource(R.string.feature_water), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(stringResource(R.string.water_today), style = MaterialTheme.typography.titleMedium)
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 14.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$cups / $goal",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Text(
                                    stringResource(R.string.water_cups),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.water_goal, goal),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { store.addWater(-1) }) {
                                Text(stringResource(R.string.water_remove))
                            }
                            Button(onClick = { store.addWater(1) }) {
                                Text(stringResource(R.string.water_add))
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.water_week), style = MaterialTheme.typography.titleSmall)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            repeat(7) { index ->
                                val date = LocalDate.now().minusDays((6 - index).toLong())
                                val value = store.water[date.toString()] ?: 0
                                val fill = if (goal > 0) (value.toFloat() / goal).coerceIn(0f, 1f) else 0f
                                Column(
                                    Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        value.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        Modifier.height(64.dp).width(18.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height((58f * fill).dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                    Text(
                                        date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.forLanguageTag("ar")),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
