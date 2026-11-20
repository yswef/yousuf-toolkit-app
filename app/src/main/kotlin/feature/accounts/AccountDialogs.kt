@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.security.PasswordTools
import data.local.LocalStore
import domain.model.AccountDraft
import domain.model.AccountItem
import domain.model.accountCategories
import domain.model.accountLayers
import domain.model.relationKinds

@Composable
fun AccountDialog(initial: AccountItem?, onDismiss: () -> Unit) {
    val store = LocalStore.current
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var username by remember(initial?.id) { mutableStateOf(initial?.username.orEmpty()) }
    var email by remember(initial?.id) { mutableStateOf(initial?.email.orEmpty()) }
    var password by remember(initial?.id) { mutableStateOf(initial?.let { store.accountPassword(it) }.orEmpty()) }
    var secret by remember(initial?.id) { mutableStateOf(initial?.let { store.accountTotp(it) }.orEmpty()) }
    var layer by remember(initial?.id) { mutableStateOf(initial?.layer ?: "normal") }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: "other") }
    var recoveryEmail by remember(initial?.id) { mutableStateOf(initial?.recoveryEmail.orEmpty()) }
    var recoveryPhone by remember(initial?.id) { mutableStateOf(initial?.recoveryPhone.orEmpty()) }
    var codes by remember(initial?.id) { mutableStateOf(initial?.let { store.accountCodes(it) }.orEmpty()) }
    var notes by remember(initial?.id) { mutableStateOf(initial?.let { store.accountNotes(it) }.orEmpty()) }
    var reveal by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.accounts_add else R.string.accounts_edit)) },
        text = {
            LazyColumn(
                Modifier.heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.accounts_title_field)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.accounts_username)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.accounts_email)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.accounts_password)) },
                        singleLine = true,
                        visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { reveal = !reveal }) {
                            Text(stringResource(if (reveal) R.string.accounts_hide else R.string.accounts_show))
                        }
                        OutlinedButton(onClick = { password = PasswordTools.generate(18, true) }) {
                            Text(stringResource(R.string.accounts_generate))
                        }
                    }
                }
                item { OutlinedTextField(value = secret, onValueChange = { secret = it }, label = { Text(stringResource(R.string.accounts_totp)) }, placeholder = { Text(stringResource(R.string.accounts_totp_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { Text(stringResource(R.string.accounts_layer), style = MaterialTheme.typography.labelLarge) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accountLayers) { value ->
                            FilterChip(selected = layer == value, onClick = { layer = value }, label = { Text(layerLabel(value)) })
                        }
                    }
                }
                item { Text(stringResource(R.string.accounts_category), style = MaterialTheme.typography.labelLarge) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accountCategories) { value ->
                            FilterChip(selected = category == value, onClick = { category = value }, label = { Text(categoryLabel(value)) })
                        }
                    }
                }
                item { OutlinedTextField(value = recoveryEmail, onValueChange = { recoveryEmail = it }, label = { Text(stringResource(R.string.accounts_recovery_email)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = recoveryPhone, onValueChange = { recoveryPhone = it }, label = { Text(stringResource(R.string.accounts_recovery_phone)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = codes, onValueChange = { codes = it }, label = { Text(stringResource(R.string.accounts_codes)) }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.accounts_notes)) }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() || email.isNotBlank() || username.isNotBlank(),
                onClick = {
                    store.saveAccount(
                        initial?.id ?: 0L,
                        AccountDraft(title, username, email, password, secret, layer, category, recoveryEmail, recoveryPhone, codes, notes)
                    )
                    onDismiss()
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun RelationDialog(onDismiss: () -> Unit) {
    val store = LocalStore.current
    var fromId by remember { mutableStateOf(store.accounts.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(store.accounts.getOrNull(1)?.id ?: 0L) }
    var kind by remember { mutableStateOf("recovery") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.accounts_relation_add)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 440.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Text(stringResource(R.string.accounts_relation_from)) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(store.accounts, key = { it.id }) { account ->
                            FilterChip(selected = fromId == account.id, onClick = { fromId = account.id }, label = { Text(account.title) })
                        }
                    }
                }
                item { Text(stringResource(R.string.accounts_relation_to)) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(store.accounts, key = { it.id }) { account ->
                            FilterChip(selected = toId == account.id, onClick = { toId = account.id }, label = { Text(account.title) })
                        }
                    }
                }
                item { Text(stringResource(R.string.accounts_relation_kind)) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(relationKinds) { value ->
                            FilterChip(selected = kind == value, onClick = { kind = value }, label = { Text(relationLabel(value)) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = fromId > 0L && toId > 0L && fromId != toId, onClick = {
                store.addAccountRelation(fromId, toId, kind)
                onDismiss()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun layerLabel(value: String): String = stringResource(
    when (value) {
        "primary" -> R.string.accounts_layer_primary
        "backup" -> R.string.accounts_layer_backup
        "decoy" -> R.string.accounts_layer_decoy
        else -> R.string.accounts_layer_normal
    }
)

@Composable
fun categoryLabel(value: String): String = stringResource(
    when (value) {
        "email" -> R.string.accounts_category_email
        "social" -> R.string.accounts_category_social
        "game" -> R.string.accounts_category_game
        "bank" -> R.string.accounts_category_bank
        "work" -> R.string.accounts_category_work
        else -> R.string.accounts_category_other
    }
)

@Composable
fun relationLabel(value: String): String = stringResource(
    when (value) {
        "recovery" -> R.string.accounts_relation_recovery
        "alias" -> R.string.accounts_relation_alias
        "linked" -> R.string.accounts_relation_linked
        else -> R.string.accounts_relation_same
    }
)

fun daysSince(at: Long): Int {
    if (at <= 0L) return 0
    val days = (System.currentTimeMillis() - at) / 86_400_000L
    return days.coerceAtLeast(0L).toInt()
}

fun escape(value: String): String = value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
