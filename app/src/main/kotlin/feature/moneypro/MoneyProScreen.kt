@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package feature.moneypro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.ExtraRecord
import domain.model.categoryKeys
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Composable
fun MoneyProScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        R.string.moneypro_budget,
        R.string.moneypro_wishlist,
        R.string.moneypro_recurring,
        R.string.moneypro_savings,
        R.string.moneypro_currency,
        R.string.moneypro_transfer
    )
    ScreenScaffold(title = stringResource(R.string.feature_money_more), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(title)) })
                }
            }
            when (tab) {
                0 -> BudgetTab()
                1 -> WishlistTab()
                2 -> RecurringTab()
                3 -> SavingsTab()
                4 -> CurrencyTab()
                else -> TransferTab()
            }
        }
    }
}

@Composable
private fun BudgetTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    val now = LocalDate.now()
    fun spent(category: String): Double = store.ledger.filter {
        !it.income && it.category == category && monthOf(it.at) == now.monthValue && yearOf(it.at) == now.year
    }.sumOf { it.amount }
    val budgets = store.extras.filter { it.kind == "budget" }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.moneypro_budget), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.add)) }
            }
        }
        if (budgets.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(budgets, key = { it.id }) { budget ->
            val used = spent(budget.detail)
            val ratio = if (budget.amount <= 0.0) 0f else (used / budget.amount).toFloat().coerceIn(0f, 1f)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(categoryName(budget.detail), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.moneypro_spent, money(used)))
                    Text(stringResource(R.string.moneypro_limit_value, money(budget.amount)))
                    Text(stringResource(R.string.moneypro_budget_left, money((budget.amount - used).coerceAtLeast(0.0))))
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = { store.deleteExtra(budget.id) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
    if (adding) AddItemDialog(kind = "budget", onDismiss = { adding = false })
}

@Composable
private fun WishlistTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    val items = store.extras.filter { it.kind == "wish" }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.moneypro_wishlist), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.add)) }
            }
        }
        if (items.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text(money(item.amount))
                    if (item.detail.isNotBlank()) Text(item.detail, style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(if (item.status == "bought") R.string.moneypro_wish_bought else R.string.moneypro_wish_open))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.status != "bought") {
                            TextButton(onClick = {
                                store.updateExtra(item.id, "bought")
                                store.addLedger(item.title, item.amount, false, "shopping")
                            }) { Text(stringResource(R.string.moneypro_wish_bought)) }
                        }
                        TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
    if (adding) AddItemDialog(kind = "wish", onDismiss = { adding = false })
}

@Composable
private fun RecurringTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    val items = store.extras.filter { it.kind == "recurring" }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.moneypro_recurring), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.add)) }
            }
        }
        if (items.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text(money(item.amount))
                    Text(item.detail + " " + daysText(), style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (item.status != "paid") {
                            TextButton(onClick = {
                                store.updateExtra(item.id, "paid")
                                store.addLedger(item.title, item.amount, false, "bills")
                            }) { Text(stringResource(R.string.moneypro_pay_now)) }
                        } else {
                            Text(stringResource(R.string.moneypro_paid))
                            TextButton(onClick = { store.updateExtra(item.id, "open") }) { Text(stringResource(R.string.accounts_edit)) }
                        }
                        TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
    if (adding) AddItemDialog(kind = "recurring", onDismiss = { adding = false })
}

@Composable
private fun SavingsTab() {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf<ExtraRecord?>(null) }
    val items = store.extras.filter { it.kind == "saving" }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.moneypro_savings), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { adding = true }) { Text(stringResource(R.string.add)) }
            }
        }
        if (items.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(items, key = { it.id }) { item ->
            val target = item.detail.toDoubleOrNull() ?: 0.0
            val ratio = if (target <= 0.0) 0f else (item.amount / target).toFloat().coerceIn(0f, 1f)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.moneypro_saved, money(item.amount)))
                    Text(stringResource(R.string.moneypro_target) + ": " + money(target))
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { saving = item }) { Text(stringResource(R.string.moneypro_add_amount)) }
                        TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                    }
                }
            }
        }
    }
    if (adding) AddItemDialog(kind = "saving", onDismiss = { adding = false })
    saving?.let { target ->
        AmountDialog(
            title = stringResource(R.string.moneypro_add_amount),
            onDismiss = { saving = null },
            onSave = { amount -> store.addExtraAmount(target.id, amount); saving = null }
        )
    }
}

@Composable
private fun CurrencyTab() {
    val store = LocalStore.current
    val rates = store.extras.filter { it.kind == "currency" }
    var code by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("100") }
    var from by remember { mutableStateOf("SAR") }
    var to by remember { mutableStateOf("USD") }
    var result by remember { mutableStateOf("") }
    fun rateOf(value: String): Double =
        if (value == "SAR") 1.0 else rates.firstOrNull { it.title == value }?.amount ?: 0.0
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.moneypro_currency), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.moneypro_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    Text(stringResource(R.string.moneypro_from))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("SAR") + rates.map { it.title }) { currency ->
                            FilterChip(selected = from == currency, onClick = { from = currency }, label = { Text(currency) })
                        }
                    }
                    Text(stringResource(R.string.moneypro_to))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("SAR") + rates.map { it.title }) { currency ->
                            FilterChip(selected = to == currency, onClick = { to = currency }, label = { Text(currency) })
                        }
                    }
                    val source = rateOf(from)
                    val target = rateOf(to)
                    if (source > 0.0 && target > 0.0) {
                        Text(stringResource(R.string.moneypro_result, money((amount.toDoubleOrNull() ?: 0.0) * target / source)))
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.moneypro_add_currency), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text(stringResource(R.string.moneypro_code)) }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text(stringResource(R.string.moneypro_rate)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedButton(onClick = {
                val value = rate.toDoubleOrNull() ?: 0.0
                if (code.isNotBlank() && value > 0.0) store.addExtra("currency", code, "", value, 0L)
                code = ""; rate = ""
            }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
        }
        if (rates.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(rates, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title + " = " + money(item.amount) + " SAR", modifier = Modifier.weight(1f))
                    TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun TransferTab() {
    val store = LocalStore.current
    var fromId by remember { mutableStateOf(store.wallets.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(store.wallets.getOrNull(1)?.id ?: 0L) }
    var amount by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val doneText = stringResource(R.string.moneypro_transfer_done)
    val failText = stringResource(R.string.moneypro_insufficient)
    val history = store.extras.filter { it.kind == "transfer" }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.moneypro_transfer), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.moneypro_from))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(store.wallets, key = { it.id }) { wallet ->
                            FilterChip(selected = fromId == wallet.id, onClick = { fromId = wallet.id }, label = { Text(wallet.name) })
                        }
                    }
                    Text(stringResource(R.string.moneypro_to))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(store.wallets, key = { it.id }) { wallet ->
                            FilterChip(selected = toId == wallet.id, onClick = { toId = wallet.id }, label = { Text(wallet.name) })
                        }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.moneypro_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedButton(onClick = {
                        val ok = store.transferBetweenWallets(fromId, toId, amount.toDoubleOrNull() ?: 0.0)
                        status = if (ok) doneText else failText
                        if (ok) amount = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.moneypro_convert)) }
                    if (status.isNotBlank()) Text(status)
                }
            }
        }
        if (history.isEmpty()) item { InfoCard(text = stringResource(R.string.moneypro_no_data)) }
        items(history, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title + " · " + money(item.amount), modifier = Modifier.weight(1f))
                    TextButton(onClick = { store.deleteExtra(item.id) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun AddItemDialog(kind: String, onDismiss: () -> Unit) {
    val store = LocalStore.current
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("food") }
    val titleLabel = when (kind) {
        "wish" -> R.string.moneypro_title
        "recurring" -> R.string.moneypro_title
        "saving" -> R.string.moneypro_title
        else -> R.string.moneypro_title
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(titleLabel)) }, singleLine = true)
                if (kind == "budget") {
                    Text(stringResource(R.string.moneypro_category))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categoryKeys) { key ->
                            FilterChip(selected = category == key, onClick = { category = key }, label = { Text(categoryName(key)) })
                        }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.moneypro_limit)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                } else {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(if (kind == "saving") R.string.moneypro_target else R.string.moneypro_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text(stringResource(if (kind == "recurring") R.string.moneypro_period else R.string.moneypro_detail)) }, keyboardOptions = KeyboardOptions(keyboardType = if (kind == "recurring") KeyboardType.Number else KeyboardType.Text), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = {
                val value = amount.toDoubleOrNull() ?: 0.0
                when (kind) {
                    "budget" -> store.addExtra("budget", title, category, value, 0L)
                    "wish" -> store.addExtra("wish", title, detail, value, 0L)
                    "recurring" -> store.addExtra("recurring", title, detail, value, 0L)
                    "saving" -> store.addExtra("saving", title, amount, 0.0, 0L)
                }
                onDismiss()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun AmountDialog(title: String, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text(stringResource(R.string.moneypro_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) },
        confirmButton = {
            TextButton(enabled = (value.toDoubleOrNull() ?: 0.0) > 0.0, onClick = { onSave(value.toDoubleOrNull() ?: 0.0) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun categoryName(key: String): String = stringResource(
    when (key) {
        "salary" -> R.string.cat_salary
        "food" -> R.string.cat_food
        "transport" -> R.string.cat_transport
        "bills" -> R.string.cat_bills
        "shopping" -> R.string.cat_shopping
        else -> R.string.cat_other
    }
)

private fun money(value: Double): String = String.format(Locale.US, "%.2f", value)

private fun monthOf(at: Long): Int = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).monthValue

private fun yearOf(at: Long): Int = Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).year

@Composable
private fun daysText(): String = stringResource(R.string.moneypro_period)
