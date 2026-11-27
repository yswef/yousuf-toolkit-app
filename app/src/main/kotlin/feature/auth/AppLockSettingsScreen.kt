// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import data.local.LocalStore

@Composable
fun AppLockSettingsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val names = stringArrayResource(R.array.applock_type_names)
    val types = listOf("pattern", "pin", "password")
    var type by remember { mutableStateOf(store.lockType) }
    var secret by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    ScreenScaffold(title = stringResource(R.string.applock_settings), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.applock_type), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEachIndexed { index, value ->
                    FilterChip(selected = type == value, onClick = { type = value; secret = ""; confirm = "" }, label = { Text(names[index]) })
                }
            }
            Text(stringResource(if (store.lockEnabled) R.string.applock_status_on else R.string.applock_status_off))
            Text(stringResource(if (store.biometricEnabled) R.string.applock_biometric_on else R.string.applock_biometric_off))
            if (type == "pattern") {
                Text(stringResource(R.string.applock_secret))
                PatternPad(onDone = { secret = it })
                Text(stringResource(R.string.applock_confirm))
                PatternPad(onDone = { confirm = it })
            } else {
                SecretField(secret, { secret = it }, type, stringResource(R.string.applock_secret))
                SecretField(confirm, { confirm = it }, type, stringResource(R.string.applock_confirm))
            }
            Button(
                onClick = {
                    val min = if (type == "password") 6 else 4
                    if (secret.length >= min && secret == confirm && store.setAppLock(type, secret)) {
                        status = context.getString(R.string.applock_saved)
                        secret = ""
                        confirm = ""
                        onBack()
                    } else {
                        status = context.getString(R.string.applock_invalid)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.applock_save)) }
            if (store.lockEnabled) {
                OutlinedButton(onClick = { store.disableAppLock(); status = context.getString(R.string.applock_disabled) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.applock_disable))
                }
            }
            if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.bodySmall)
        }
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
