// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.money

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CardDefaults
import core.design.rememberUndoHost
import core.design.SwipeActionRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.design.FormDialog
import core.design.V3Hero
import core.design.V3QuickAction
import core.design.V3SectionHeader
import core.design.InfoCard
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Ledger
import domain.model.Debt
import domain.model.categoryKeys
import core.device.Access
import core.device.SmsReader
import core.device.WalletRules
import core.device.WalletTxParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.provider.Settings
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyScreen(onBack: () -> Unit = {}, onWallets: () -> Unit, onMore: () -> Unit = {}, embedded: Boolean = false) {
    val store = LocalStore.current
    val undo = rememberUndoHost()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var adding by remember { mutableStateOf(false) }
    var addIncome by remember { mutableStateOf(false) }
    var addingDebt by remember { mutableStateOf(false) }
    var deletingDebt by remember { mutableStateOf<Debt?>(null) }
    var status by remember { mutableStateOf("") }

    fun importMessages() {
        scope.launch {
            val messages = withContext(Dispatchers.IO) { SmsReader.read(context) }
            val added = store.importSms(messages)
            status = if (added > 0) {
                context.getString(R.string.money_sms_added, added)
            } else {
                context.getString(R.string.money_sms_none)
            }
        }
    }

    fun importNotifications() {
        if (!Access.notificationListener(context)) {
            status = context.getString(R.string.money_notif_permission)
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            return
        }
        scope.launch {
            val rules = withContext(Dispatchers.IO) { WalletRules.load(context) }
            val items = store.notifications.flatMap { entry ->
                WalletTxParser.parseAll(rules, "notification", entry.pkg, entry.title, entry.text, entry.at)
            }
            val added = store.importSms(items)
            status = if (added > 0) {
                context.getString(R.string.money_notif_added, added)
            } else {
                context.getString(R.string.money_notif_none)
            }
        }
    }

    val smsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) importMessages() else status = context.getString(R.string.money_sms_denied)
    }

    val entries = store.ledger.sortedByDescending { it.at }
    val income = store.ledger.filter { it.income }.sumOf { it.amount }
    val expense = store.ledger.filterNot { it.income }.sumOf { it.amount }

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (embedded) 96.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (embedded) {
                item {
                    V3Hero(onClick = onWallets) {
                        val scheme = MaterialTheme.colorScheme
                        Text(
                            stringResource(R.string.money_balance),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onPrimaryContainer
                        )
                        Text(
                            amountText(store.balance),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MoneyPill(stringResource(R.string.money_income_value, amountText(income)), Modifier.weight(1f))
                            MoneyPill(stringResource(R.string.money_expense_value, amountText(expense)), Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            V3QuickAction(
                                stringResource(R.string.money_income), BookSymbol.DIAMOND,
                                { addIncome = true; adding = true }, Modifier.weight(1f),
                                container = MaterialTheme.colorScheme.surface
                            )
                            V3QuickAction(
                                stringResource(R.string.money_expense), BookSymbol.DIAMOND,
                                { addIncome = false; adding = true }, Modifier.weight(1f),
                                container = MaterialTheme.colorScheme.surface
                            )
                            V3QuickAction(
                                stringResource(R.string.feature_wallets), BookSymbol.ORB,
                                onWallets, Modifier.weight(1f),
                                container = MaterialTheme.colorScheme.surface
                            )
                            V3QuickAction(
                                stringResource(R.string.v3_more), BookSymbol.STAR,
                                onMore, Modifier.weight(1f),
                                container = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { importNotifications() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.money_notif_import))
                        }
                        TextButton(
                            onClick = {
                                if (SmsReader.hasPermission(context)) importMessages() else smsPermission.launch(Manifest.permission.READ_SMS)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.money_sms_import))
                        }
                    }
                }
            }
            if (status.isNotBlank()) {
                item { InfoCard(text = status) }
            }
            item {
                val account = store.wallets.firstOrNull { store.isDefaultWallet(it) }
                if (account != null) {
                    Card(
                        onClick = onWallets,
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(account.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    amountText(account.balance),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(stringResource(R.string.default_account_hint), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            if (!embedded) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.money_balance),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                amountText(store.balance),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Text(
                                    stringResource(R.string.money_income_value, amountText(income)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.money_expense_value, amountText(expense)),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onWallets, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.feature_wallets))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.debt_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { addingDebt = true }) { Text(stringResource(R.string.debt_add)) }
                }
            }
            if (store.debts.isEmpty()) {
                item { Text(stringResource(R.string.debt_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(store.debts.sortedWith(compareBy<Debt> { it.settled }.thenByDescending { it.at }), key = { "debt-${it.id}" }) { debt ->
                DebtRow(debt, onSettled = { store.setDebtSettled(debt.id, it) }, onDelete = { deletingDebt = debt })
            }
            item { V3SectionHeader(title = stringResource(R.string.v3_ledger_header)) }
            if (entries.isEmpty()) {
                item {
                    Image(
                        painter = painterResource(R.drawable.empty_money),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(24.dp))
                    )
                    Text(
                        stringResource(R.string.money_none),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            items(entries, key = { it.id }) { entry ->
                val remove = { store.deleteLedger(entry.id); undo.deleted { store.restoreLedger(entry) } }
                SwipeActionRow(Modifier.animateItem(), onSwipeEnd = remove) { LedgerRow(entry = entry, onDelete = remove) }
            }
        }
    }
    if (embedded) {
        content(PaddingValues(0.dp))
    } else {
        ScreenScaffold(
            title = stringResource(R.string.feature_money),
            onBack = onBack,
            actions = {
                TextButton(onClick = onMore) {
                    Text(stringResource(R.string.moneypro_budget))
                }
                TextButton(onClick = { importNotifications() }) {
                    Text(stringResource(R.string.money_notif_import))
                }
                TextButton(onClick = {
                    if (SmsReader.hasPermission(context)) {
                        importMessages()
                    } else {
                        smsPermission.launch(Manifest.permission.READ_SMS)
                    }
                }) {
                    Text(stringResource(R.string.money_sms_import))
                }
                IconButton(onClick = { adding = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            },
            content = content
        )
    }

    if (addingDebt) {
        DebtDialog(onDismiss = { addingDebt = false }, onAdd = { person, amount, owedByMe, note ->
            store.addDebt(person, amount, owedByMe, note)
        })
    }
    deletingDebt?.let { debt ->
        AlertDialog(
            onDismissRequest = { deletingDebt = null },
            title = { Text(stringResource(R.string.debt_delete_title)) },
            text = { Text(stringResource(R.string.debt_delete_message, debt.person)) },
            confirmButton = { TextButton(onClick = { store.deleteDebt(debt.id); deletingDebt = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { deletingDebt = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (adding) {
        LedgerDialog(
            initialIncome = addIncome,
            onDismiss = { adding = false },
            onAdd = { title, amount, isIncome, category, at ->
                store.addLedger(title, amount, isIncome, category, at)
            }
        )
    }
}

@Composable
private fun DebtRow(debt: Debt, onSettled: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(debt.person, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(if (debt.owedByMe) R.string.debt_i_owe else R.string.debt_owed_to_me, amountText(debt.amount)))
                if (debt.note.isNotBlank()) Text(debt.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(if (debt.settled) R.string.debt_settled else R.string.debt_unsettled), style = MaterialTheme.typography.bodySmall)
            }
            Checkbox(checked = debt.settled, onCheckedChange = onSettled)
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete)) }
        }
    }
}

@Composable
private fun DebtDialog(onDismiss: () -> Unit, onAdd: (String, Double, Boolean, String) -> Unit) {
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var owedByMe by remember { mutableStateOf(true) }
    val value = amount.toDoubleOrNull()
    FormDialog(
        title = stringResource(R.string.debt_add),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        valid = person.isNotBlank() && value != null && value.isFinite() && value > 0,
        onConfirm = { value?.let { onAdd(person, it, owedByMe, note) }; onDismiss() }
    ) {
        OutlinedTextField(value = person, onValueChange = { person = it }, label = { Text(stringResource(R.string.debt_person)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.money_amount)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = owedByMe, onClick = { owedByMe = true })
            Text(stringResource(R.string.debt_direction_mine))
            RadioButton(selected = !owedByMe, onClick = { owedByMe = false })
            Text(stringResource(R.string.debt_direction_theirs))
        }
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.task_note)) }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.debt_separate_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MoneyPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun LedgerRow(entry: Ledger, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(categoryColor(entry.category).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(categoryColor(entry.category)))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(entry.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.money_row_meta, dateText(entry.at), categoryLabel(entry.category)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.note.isNotBlank()) {
                    Text(
                        entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Text(
                (if (entry.income) "+" else "-") + amountText(entry.amount),
                color = if (entry.income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerDialog(
    initialIncome: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Boolean, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var income by remember { mutableStateOf(initialIncome) }
    var category by remember { mutableStateOf(categoryKeys.last()) }
    val context = LocalContext.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    val parsed = amount.toDoubleOrNull()

    FormDialog(
        title = stringResource(R.string.money_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            parsed?.let { onAdd(title, it, income, category, date.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
            onDismiss()
        },
        valid = title.isNotBlank() && parsed != null && parsed.isFinite() && parsed > 0.0
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.money_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.money_amount)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.money_date), style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(0L, 1L, 2L)) { days ->
                val day = LocalDate.now().minusDays(days)
                FilterChip(
                    selected = date == day,
                    onClick = { date = day },
                    label = { Text(stringResource(when (days) {
                        0L -> R.string.money_date_today
                        1L -> R.string.money_date_yesterday
                        else -> R.string.money_date_before_yesterday
                    })) }
                )
            }
            item {
                FilterChip(
                    selected = date !in listOf(LocalDate.now(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(2)),
                    onClick = {
                        DatePickerDialog(context, { _, year, month, day ->
                            date = LocalDate.of(year, month + 1, day)
                        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
                    },
                    label = { Text(stringResource(R.string.money_date_choose, date.toString())) }
                )
            }
        }
        Text(stringResource(R.string.money_kind), style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !income, onClick = { income = false })
            Text(stringResource(R.string.money_expense))
            RadioButton(selected = income, onClick = { income = true })
            Text(stringResource(R.string.money_income))
        }
        Text(stringResource(R.string.money_category), style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoryKeys, key = { it }) { key ->
                FilterChip(
                    selected = category == key,
                    onClick = { category = key },
                    label = { Text(categoryLabel(key)) }
                )
            }
        }
    }
}

@Composable
private fun categoryColor(key: String): Color = when (key) {
    "salary" -> Color(0xFF5CC59A)
    "food" -> Color(0xFFFFA94D)
    "transport" -> Color(0xFF4DABF7)
    "bills" -> Color(0xFFFF6B8A)
    "shopping" -> Color(0xFFB197FC)
    else -> Color(0xFFC09843)
}

@Composable
private fun categoryLabel(key: String): String = when (key) {
    "salary" -> stringResource(R.string.cat_salary)
    "food" -> stringResource(R.string.cat_food)
    "transport" -> stringResource(R.string.cat_transport)
    "bills" -> stringResource(R.string.cat_bills)
    "shopping" -> stringResource(R.string.cat_shopping)
    else -> stringResource(R.string.cat_other)
}

private fun dateText(at: Long): String =
    Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()

private fun amountText(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value)

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
