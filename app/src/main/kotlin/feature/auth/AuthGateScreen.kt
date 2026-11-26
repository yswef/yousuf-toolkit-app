// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.auth

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import core.design.V3Chip
import core.net.Api
import core.net.ApiException
import core.net.authError
import data.local.LocalStore
import kotlinx.coroutines.launch

@Composable
fun AuthGateScreen() {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(store.accountEmail) }
    var password by remember { mutableStateOf("") }
    var server by remember { mutableStateOf(store.serverUrl) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val label = remember { "${Build.MANUFACTURER} ${Build.MODEL}".trim() }
    val fullText = stringResource(R.string.accounts_full)
    val switchedText = stringResource(R.string.accounts_switched)
    val savedText = stringResource(R.string.accounts_saved_ok)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.auth_gate_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.auth_gate_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V3Chip(stringResource(R.string.auth_gate_login), !mode, { mode = false }, Modifier.weight(1f))
            V3Chip(stringResource(R.string.auth_gate_register), mode, { mode = true }, Modifier.weight(1f))
        }
        OutlinedTextField(
            value = server, onValueChange = { server = it },
            label = { Text(stringResource(R.string.auth_gate_server)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = {
                busy = true
                scope.launch {
                    status = when (val r = Api.health(Api.normalizeBase(server))) {
                        "ok" -> context.getString(R.string.auth_server_ok)
                        else -> context.getString(R.string.auth_server_fail, r.take(120))
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.auth_gate_check)) }
        if (mode) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.auth_gate_name)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_gate_email)) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_gate_password)) },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                busy = true
                status = ""
                scope.launch {
                    val base = Api.normalizeBase(server)
                    runCatching {
                        if (mode) Api.register(base, name.trim(), email.trim(), password, label, label)
                        else Api.login(base, email.trim(), password, label, label)
                    }.onSuccess { result ->
                        store.saveServerUrl(server)
                        val ok = store.attachAccount(
                            result.token, result.user.id, result.user.name, result.user.email
                        )
                        status = if (ok) savedText else fullText
                        password = ""
                    }.onFailure { e ->
                        status = if (e is ApiException) authError(context, e)
                        else context.getString(R.string.auth_gate_error)
                    }
                    busy = false
                }
            },
            enabled = !busy && email.isNotBlank() && password.length >= 8 && (!mode || name.isNotBlank()),
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(if (mode) R.string.auth_gate_register else R.string.auth_gate_login)) }
        if (busy) CircularProgressIndicator()
        if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.WARN)
        Text(
            stringResource(R.string.auth_gate_device) + ": " + label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AccountSwitcher(
            onSwitch = { status = switchedText },
            onLogout = { status = "" },
            onAdd = {
                email = ""
                password = ""
                name = ""
                mode = false
                status = ""
            }
        )
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
