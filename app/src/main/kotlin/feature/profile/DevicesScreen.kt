// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.net.Api
import core.net.DeviceInfo
import data.local.LocalStore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DevicesScreen(onBack: () -> Unit, onLoggedOut: () -> Unit = {}) {
    val store = LocalStore.current
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var error by remember { mutableStateOf("") }

    fun load() {
        scope.launch {
            runCatching { Api.devices(Api.normalizeBase(store.serverUrl), store.authToken) }
                .onSuccess { devices = it; error = "" }
                .onFailure { error = it.message ?: "" }
        }
    }
    LaunchedEffect(Unit) { load() }

    ScreenScaffold(title = stringResource(R.string.devices_title), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (error.isNotBlank()) item { InfoCard(text = error, tone = InfoTone.WARN) }
            if (devices.isEmpty() && error.isBlank()) {
                item { InfoCard(text = stringResource(R.string.devices_empty), tone = InfoTone.INFO) }
            }
            items(devices, key = { it.id }) { device ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(device.label.ifBlank { "device-${device.id}" }, style = MaterialTheme.typography.titleMedium)
                        Text(device.model, style = MaterialTheme.typography.bodySmall)
                        Text(
                            stringResource(R.string.devices_last_seen, date(device.lastSeen)),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = {
                                scope.launch {
                                    runCatching { Api.revokeDevice(Api.normalizeBase(store.serverUrl), store.authToken, device.id) }
                                    load()
                                }
                            }) { Text(stringResource(R.string.devices_revoke)) }
                            OutlinedButton(onClick = {
                                store.logout()
                                onLoggedOut()
                            }) { Text(stringResource(R.string.sync_logout)) }
                        }
                    }
                }
            }
        }
    }
}

private fun date(ms: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(ms))
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
