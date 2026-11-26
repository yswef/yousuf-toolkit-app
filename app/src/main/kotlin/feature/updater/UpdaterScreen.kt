package feature.updater

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.config.AppConfig
import core.design.InfoCard
import core.design.InfoTone
import core.design.ScreenScaffold
import core.net.Platform
import core.update.ApkInstaller
import kotlinx.coroutines.launch

@Composable
fun UpdaterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val config = remember { AppConfig.load(context) }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<Platform.Release?>(null) }
    var error by remember { mutableStateOf(false) }
    var apk by remember { mutableStateOf<File?>(null) }
    var updateStatus by remember { mutableStateOf("") }
    val current = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    ScreenScaffold(title = stringResource(R.string.feature_updater), onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            InfoCard(text = stringResource(R.string.updater_tag, current))
            Button(
                onClick = {
                    checking = true
                    error = false
                    scope.launch {
                        release = Platform.latestRelease(config.latestReleaseUrl)
                        error = release == null
                        checking = false
                    }
                },
                enabled = !checking,
                modifier = Modifier.padding(16.dp)
            ) { Text(stringResource(R.string.updater_check)) }
            if (checking) {
                CircularProgressIndicator(Modifier.padding(16.dp))
                Text(
                    stringResource(R.string.updater_checking),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (error) {
                InfoCard(text = stringResource(R.string.updater_error), tone = InfoTone.ERROR)
            }
            release?.let { info ->
                if (info.tag.isBlank()) {
                    InfoCard(text = stringResource(R.string.updater_none), tone = InfoTone.WARN)
                } else {
                    InfoCard(
                        text = stringResource(R.string.updater_tag, info.tag) + "\n" +
                            if (info.tag == "v$current") {
                                stringResource(R.string.updater_latest)
                            } else {
                                stringResource(R.string.updater_available)
                            },
                        tone = if (info.tag == "v$current") InfoTone.INFO else InfoTone.WARN
                    )
                    if (info.notes.isNotBlank()) {
                        Text(
                            info.notes,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (info.page.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.page)))
                                } catch (t: ActivityNotFoundException) {
                                    error = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) { Text(stringResource(R.string.updater_open)) }
                    }
                    if (info.apk.isNotBlank()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    updateStatus = runCatching {
                                        apk = ApkInstaller.download(context, info.apk)
                                        context.getString(R.string.update_ready)
                                    }.getOrElse { it.message ?: "" }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text(stringResource(R.string.update_download)) }
                    }
                    apk?.let { file ->
                        Button(
                            onClick = {
                                if (!ApkInstaller.canInstall(context)) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                                Uri.parse("package:" + context.packageName)
                                            )
                                        )
                                    }
                                    updateStatus = context.getString(R.string.update_permission)
                                } else {
                                    runCatching { ApkInstaller.install(context, file) }
                                        .onFailure { updateStatus = it.message ?: "" }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text(stringResource(R.string.update_install)) }
                    }
                    if (updateStatus.isNotBlank()) {
                        Text(updateStatus, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
                    }
                    Text(
                        stringResource(R.string.update_signature),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
