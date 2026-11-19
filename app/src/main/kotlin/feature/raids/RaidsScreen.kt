package feature.raids

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.LocalStore
import core.design.ScreenScaffold
import core.net.Api
import core.net.RaidInfo
import kotlinx.coroutines.launch

@Composable
fun RaidsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf<RaidInfo?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var nextSeconds by remember { mutableIntStateOf(0) }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(store.authToken, tick) {
        if (store.authToken.isBlank()) return@LaunchedEffect
        loading = true
        runCatching {
            Api.raid(Api.normalizeBase(store.serverUrl), store.authToken)
        }.onSuccess { info = it }.onFailure { error = it.message ?: "" }
        loading = false
    }

    ScreenScaffold(
        title = stringResource(R.string.feature_raids),
        onBack = onBack,
        actions = {
            if (store.authToken.isNotBlank()) {
                TextButton(onClick = { tick++ }) { Text(stringResource(R.string.raid_refresh)) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (store.authToken.isBlank()) {
                InfoCard(text = stringResource(R.string.raid_login_needed), tone = InfoTone.WARN)
                return@Column
            }
            if (loading) CircularProgressIndicator(Modifier.padding(16.dp))
            if (error.isNotBlank()) InfoCard(text = error, tone = InfoTone.ERROR)
            if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.INFO)
            info?.let { raid ->
                val fraction = if (raid.goal <= 0) 0f else (raid.progress.toFloat() / raid.goal).coerceIn(0f, 1f)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                        Row {
                            Text(
                                stringResource(R.string.raid_progress),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${raid.progress} / ${raid.goal}")
                        }
                        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                        Row {
                            Text(
                                stringResource(R.string.raid_members) + ": ${raid.members}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                stringResource(R.string.raid_rank) + ": ${raid.rank}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            stringResource(R.string.raid_tag, raid.season),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (nextSeconds > 0) {
                    Text(
                        stringResource(R.string.raid_next, stringResource(R.string.raid_minutes, nextSeconds / 60)),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {
                        status = ""
                        scope.launch {
                            runCatching {
                                Api.raidCheckin(Api.normalizeBase(store.serverUrl), store.authToken)
                            }.onSuccess { result ->
                                nextSeconds = result.nextInSeconds
                                status = if (result.connected) {
                                    context.getString(R.string.raid_joined)
                                } else {
                                    context.getString(R.string.raid_failed)
                                }
                                tick++
                            }.onFailure { status = it.message ?: context.getString(R.string.raid_failed) }
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) { Text(stringResource(R.string.raid_join)) }
            }
        }
    }
}
