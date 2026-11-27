// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.security.BiometricAuth
import core.security.MainExecutor
import data.local.LocalStore

@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val store = LocalStore.current
    var unlocked by remember { mutableStateOf(false) }
    LaunchedEffect(store.lockEpoch) { unlocked = false }
    if (!store.lockEnabled || unlocked) {
        content()
    } else {
        AppLockUnlockScreen(onUnlocked = { unlocked = true })
    }
}

@Composable
private fun AppLockUnlockScreen(onUnlocked: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val executor = remember { MainExecutor(context) }
    var secret by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var askBiometric by remember { mutableStateOf(false) }
    val biometricTitle = stringResource(R.string.applock_biometric_button)
    val biometricSubtitle = stringResource(R.string.applock_biometric_subtitle)
    val negative = stringResource(R.string.applock_biometric_negative)

    fun verify() {
        if (store.verifyAppLock(secret)) {
            secret = ""
            status = ""
            if (store.lockSetupPending) askBiometric = true else onUnlocked()
        } else {
            status = context.getString(R.string.applock_unlock_failed)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.applock_unlock_title))
        if (store.lockType == "pattern") {
            PatternPad(onDone = { secret = it })
        } else {
            SecretField(secret, { secret = it }, store.lockType, stringResource(R.string.applock_secret))
        }
        Button(onClick = { verify() }, enabled = secret.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.applock_unlock_button))
        }
        if (store.biometricEnabled && BiometricAuth.available(context)) {
            OutlinedButton(
                onClick = {
                    BiometricAuth.prompt(
                        context, biometricTitle, biometricSubtitle, negative, executor,
                        onSuccess = onUnlocked,
                        onFail = { status = context.getString(R.string.applock_biometric_failed) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(biometricTitle) }
        }
        if (status.isNotBlank()) Text(status)
    }

    if (askBiometric) {
        AlertDialog(
            onDismissRequest = {
                askBiometric = false
                store.skipBiometric()
                onUnlocked()
            },
            title = { Text(stringResource(R.string.applock_biometric_ask)) },
            text = { Text(stringResource(R.string.applock_biometric_subtitle)) },
            confirmButton = {
                TextButton(onClick = {
                    askBiometric = false
                    if (BiometricAuth.available(context)) {
                        BiometricAuth.prompt(
                            context, biometricTitle, biometricSubtitle, negative, executor,
                            onSuccess = { store.enableBiometric(); onUnlocked() },
                            onFail = { store.skipBiometric(); onUnlocked() }
                        )
                    } else {
                        store.skipBiometric()
                        status = context.getString(R.string.applock_biometric_unavailable)
                        onUnlocked()
                    }
                }) { Text(stringResource(R.string.applock_biometric_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { askBiometric = false; store.skipBiometric(); onUnlocked() }) {
                    Text(stringResource(R.string.applock_biometric_no))
                }
            }
        )
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
