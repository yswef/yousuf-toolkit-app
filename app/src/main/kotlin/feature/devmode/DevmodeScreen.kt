package feature.devmode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import data.local.LocalStore
import core.design.RowItem
import core.design.ScreenScaffold

@Composable
fun DevmodeScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    val snapshot = buildString {
        appendLine("version=$version")
        appendLine("server=${store.serverUrl}")
        appendLine("account=${store.accountEmail}")
        appendLine("points=${store.points} xp=${store.xp} level=${store.level}")
        appendLine("tasks=${store.tasks.size} ledger=${store.ledger.size} journal=${store.journal.size}")
        appendLine("campaigns=${store.campaigns.size} wallets=${store.wallets.size}")
        appendLine("premium=${store.premium} accent=${store.accent}")
    }

    ScreenScaffold(title = stringResource(R.string.feature_devmode), onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InfoCard(text = stringResource(R.string.devmode_desc))
            RowItem(title = stringResource(R.string.devmode_version), subtitle = version)
            RowItem(title = stringResource(R.string.devmode_server), subtitle = store.serverUrl.ifBlank { "-" })
            RowItem(
                title = stringResource(R.string.devmode_account),
                subtitle = store.accountEmail.ifBlank { "-" }
            )
            RowItem(
                title = stringResource(R.string.devmode_progress),
                subtitle = "${store.level} · ${store.points}"
            )
            RowItem(
                title = stringResource(R.string.devmode_data),
                subtitle = "${store.tasks.size}/${store.ledger.size}/${store.journal.size}/${store.campaigns.size}"
            )
            RowItem(
                title = stringResource(R.string.devmode_theme),
                subtitle = "${store.accent} · ${store.avatar}"
            )
            OutlinedButton(
                onClick = {
                    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    manager.setPrimaryClip(ClipData.newPlainText("diagnostics", snapshot))
                },
                modifier = Modifier.padding(16.dp)
            ) { Text(stringResource(R.string.devmode_copy)) }
            Text(
                stringResource(R.string.devmode_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
