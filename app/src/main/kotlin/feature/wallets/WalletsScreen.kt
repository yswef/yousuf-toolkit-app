package feature.wallets

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.InfoCard
import data.local.LocalStore
import core.design.ScreenScaffold
import domain.model.Wallet
import java.util.Locale

@Composable
fun WalletsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var showNew by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }
    var target by remember { mutableStateOf<Wallet?>(null) }
    var withdraw by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }

    ScreenScaffold(
        title = stringResource(R.string.feature_wallets),
        onBack = onBack,
        actions = {
            IconButton(onClick = { showNew = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.common_add))
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            InfoCard(text = stringResource(R.string.wallets_desc))
            if (store.wallets.isEmpty()) {
                Text(
                    stringResource(R.string.wallet_empty),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            store.wallets.forEach { wallet ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                wallet.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { store.deleteWallet(wallet.id) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.common_delete)
                                )
                            }
                        }
                        Text(
                            String.format(Locale.getDefault(), "%.2f", wallet.balance),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                target = wallet
                                withdraw = false
                                amount = ""
                            }) { Text(stringResource(R.string.wallet_deposit)) }
                            OutlinedButton(onClick = {
                                target = wallet
                                withdraw = true
                                amount = ""
                            }) { Text(stringResource(R.string.wallet_withdraw)) }
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.wallets_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showNew) {
        FormDialog(
            title = stringResource(R.string.wallet_new),
            confirm = stringResource(R.string.common_save),
            onDismiss = { showNew = false },
            onConfirm = {
                store.addWallet(name, initial.toDoubleOrNull() ?: 0.0)
                name = ""
                initial = ""
                showNew = false
            },
            valid = name.isNotBlank() && (initial.isBlank() || initial.toDoubleOrNull() != null)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.wallet_name)) },
                singleLine = true
            )
            OutlinedTextField(
                value = initial,
                onValueChange = { initial = it },
                label = { Text(stringResource(R.string.wallet_initial)) },
                singleLine = true
            )
        }
    }

    target?.let { wallet ->
        FormDialog(
            title = if (withdraw) stringResource(R.string.wallet_withdraw) else stringResource(R.string.wallet_deposit),
            confirm = stringResource(R.string.common_save),
            onDismiss = { target = null },
            onConfirm = {
                val value = amount.toDoubleOrNull()
                if (value != null && value > 0.0) {
                    if (withdraw) {
                        if (!store.withdraw(wallet.id, value)) {
                            Toast.makeText(context, R.string.wallet_insufficient, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        store.deposit(wallet.id, value)
                    }
                    amount = ""
                }
                target = null
            },
            valid = (amount.toDoubleOrNull() ?: 0.0) > 0.0
        ) {
            Text(wallet.name, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.wallet_amount)) },
                singleLine = true
            )
        }
    }
}
