// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import data.local.LocalStore

@Composable
fun AccountSwitcher(
    onSwitch: () -> Unit,
    onLogout: () -> Unit,
    onAdd: () -> Unit = {}
) {
    val store = LocalStore.current
    val accounts = store.savedAccounts
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoCard(
            text = stringResource(R.string.accounts_max, accounts.size, store.maxAccounts),
            tone = InfoTone.INFO
        )
        if (accounts.isEmpty()) {
            InfoCard(text = stringResource(R.string.accounts_none), tone = InfoTone.INFO)
        }
        accounts.forEach { account ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(account.name.ifBlank { account.email }, style = MaterialTheme.typography.bodyLarge)
                            Text(account.email, style = MaterialTheme.typography.labelSmall)
                            Text(
                                stringResource(
                                    if (account.key == store.activeAccountKey) R.string.accounts_active else R.string.accounts_saved
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            enabled = account.key != store.activeAccountKey,
                            onClick = { if (store.switchAccount(account.key)) onSwitch() }
                        ) { Text(stringResource(R.string.accounts_switch)) }
                        OutlinedButton(onClick = {
                            val wasActive = account.key == store.activeAccountKey
                            store.removeSavedAccount(account.key)
                            if (wasActive) onLogout()
                        }) { Text(stringResource(R.string.accounts_remove)) }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.accounts_add_another))
            }
            OutlinedButton(onClick = { store.logout(); onLogout() }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.accounts_logout))
            }
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
