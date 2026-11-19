package feature.license

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import data.local.LocalStore
import core.design.ScreenScaffold
import core.net.Api
import kotlinx.coroutines.launch

@Composable
fun LicenseScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    ScreenScaffold(title = stringResource(R.string.feature_license), onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoCard(text = stringResource(R.string.license_desc))
            if (store.premium) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.license_active),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(store.licenseCode, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedButton(
                    onClick = { store.saveLicense("") },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { Text(stringResource(R.string.license_remove)) }
            } else {
                if (store.authToken.isBlank()) {
                    InfoCard(text = stringResource(R.string.license_login), tone = InfoTone.WARN)
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text(stringResource(R.string.license_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Button(
                    onClick = {
                        busy = true
                        status = ""
                        error = false
                        scope.launch {
                            runCatching {
                                Api.activateLicense(
                                    Api.normalizeBase(store.serverUrl),
                                    store.authToken,
                                    code.trim()
                                )
                            }.onSuccess { activated ->
                                store.saveLicense(activated)
                                status = context.getString(R.string.license_ok)
                                code = ""
                            }.onFailure {
                                error = true
                                status = it.message ?: context.getString(R.string.license_failed)
                            }
                            busy = false
                        }
                    },
                    enabled = !busy && code.isNotBlank() && store.authToken.isNotBlank(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { Text(stringResource(R.string.license_activate)) }
            }
            if (busy) CircularProgressIndicator(Modifier.padding(16.dp))
            if (status.isNotBlank()) {
                InfoCard(
                    text = status,
                    tone = if (error) InfoTone.ERROR else InfoTone.INFO
                )
            }
            Text(
                stringResource(R.string.license_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
