package feature.money

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.Ledger
import domain.model.categoryKeys
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    var adding by remember { mutableStateOf(false) }

    val entries = store.ledger.sortedByDescending { it.at }
    val income = store.ledger.filter { it.income }.sumOf { it.amount }
    val expense = store.ledger.filterNot { it.income }.sumOf { it.amount }

    ScreenScaffold(
        title = stringResource(R.string.feature_money),
        onBack = onBack,
        actions = {
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
            if (entries.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.money_none),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            items(entries, key = { it.id }) { entry ->
                LedgerRow(entry = entry, onDelete = { store.deleteLedger(entry.id) })
            }
        }
    }

    if (adding) {
        LedgerDialog(
            onDismiss = { adding = false },
            onAdd = { title, amount, isIncome, category ->
                store.addLedger(title, amount, isIncome, category)
            }
        )
    }
}

@Composable
private fun LedgerRow(entry: Ledger, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.money_row_meta, dateText(entry.at), categoryLabel(entry.category)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    onDismiss: () -> Unit,
    onAdd: (String, Double, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var income by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(categoryKeys.last()) }
    val parsed = amount.toDoubleOrNull()

    FormDialog(
        title = stringResource(R.string.money_new),
        confirm = stringResource(R.string.add),
        onDismiss = onDismiss,
        onConfirm = {
            parsed?.let { onAdd(title, it, income, category) }
            onDismiss()
        },
        valid = title.isNotBlank() && parsed != null && parsed > 0.0
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
