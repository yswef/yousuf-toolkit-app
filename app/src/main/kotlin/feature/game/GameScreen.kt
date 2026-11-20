package feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.InfoCard
import data.local.LocalStore
import core.design.ScreenScaffold
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun GameScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("21") }
    val weeklyTitle = stringResource(R.string.game_weekly)
    val monthlyTitle = stringResource(R.string.game_monthly)

    ScreenScaffold(
        title = stringResource(R.string.feature_game),
        onBack = onBack,
        actions = {
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.campaign_new))
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.rank_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        rankLabel(store.rankKey),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.game_level_points, store.level, store.points),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    LinearProgressIndicator(
                        progress = { store.rankProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val next = store.nextRank
                    Text(
                        if (next == null) {
                            stringResource(R.string.rank_max)
                        } else {
                            stringResource(R.string.rank_next, rankLabel(next.first), next.second)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.game_magic), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.game_shields, store.shields), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.game_shield_hint), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { store.buyShield() }) {
                        Text(stringResource(R.string.game_shield_buy, store.config.shieldCost))
                    }
                    if (store.frozenDay == store.today) {
                        Text(stringResource(R.string.game_freeze_done))
                    } else {
                        OutlinedButton(onClick = { store.freezeToday() }) {
                            Text(stringResource(R.string.game_freeze) + " · " + stringResource(R.string.game_freeze_cost, store.config.freezeCost))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { store.addCampaign(weeklyTitle, store.config.weeklyRaidTarget) }) {
                            Text(stringResource(R.string.game_weekly))
                        }
                        OutlinedButton(onClick = { store.addCampaign(monthlyTitle, store.config.monthlyChallengeTarget) }) {
                            Text(stringResource(R.string.game_monthly))
                        }
                    }
                    Text(stringResource(R.string.game_medals), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.game_medal_rank, rankLabel(store.rankKey)))
                    Text(stringResource(R.string.game_medal_campaign) + ": " + if (store.campaigns.any { it.done }) stringResource(R.string.game_unlocked) else stringResource(R.string.game_no_medal))
                    Text(stringResource(R.string.game_medal_streak) + ": " + if (store.level >= 5) stringResource(R.string.game_unlocked) else stringResource(R.string.game_no_medal))
                }
            }

            InfoCard(text = stringResource(R.string.campaigns_desc))

            if (store.campaigns.isEmpty()) {
                Text(
                    stringResource(R.string.campaign_empty),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            store.campaigns.forEach { campaign ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                campaign.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${campaign.checkins}/${campaign.target}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                if (campaign.target <= 0) 0f
                                else (campaign.checkins.toFloat() / campaign.target).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!campaign.done) {
                                Button(
                                    onClick = { store.checkinCampaign(campaign.id) },
                                    enabled = !checkedToday(campaign.lastCheckin)
                                ) {
                                    Text(
                                        if (checkedToday(campaign.lastCheckin)) {
                                            stringResource(R.string.campaign_checked)
                                        } else {
                                            stringResource(R.string.campaign_checkin)
                                        }
                                    )
                                }
                            } else {
                                Text(
                                    stringResource(R.string.campaign_done),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            OutlinedButton(onClick = { store.deleteCampaign(campaign.id) }) {
                                Text(stringResource(R.string.common_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        FormDialog(
            title = stringResource(R.string.campaign_new),
            confirm = stringResource(R.string.common_save),
            onDismiss = { adding = false },
            onConfirm = {
                if (store.addCampaign(title, target.toIntOrNull() ?: 0)) {
                    title = ""
                    target = "21"
                    adding = false
                }
            },
            valid = title.isNotBlank() && (target.toIntOrNull() ?: 0) in 1..365
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.campaign_title)) },
                singleLine = true
            )
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text(stringResource(R.string.campaign_target)) },
                singleLine = true
            )
        }
    }
}

private fun checkedToday(at: Long): Boolean =
    at > 0L && Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()

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
