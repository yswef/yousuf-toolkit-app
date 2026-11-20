package feature.device

import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.InfoCard
import core.design.InfoTone
import data.local.LocalStore
import core.design.MagicTabs
import core.design.RowItem
import core.design.SectionTitle
import core.design.ScreenScaffold
import core.design.ToggleRow
import core.device.Access
import core.device.AppUsage
import core.net.Api
import data.local.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceScreen(onBack: () -> Unit, initialTab: Int = 0, titleOverride: String? = null, onMore: () -> Unit = {}) {
    val store = LocalStore.current
    var tab by remember { mutableStateOf(initialTab.coerceIn(0, 4)) }
    val labels = listOf(
        stringResource(R.string.device_tab_usage),
        stringResource(R.string.device_tab_blocker),
        stringResource(R.string.device_tab_domains),
        stringResource(R.string.device_tab_notifications),
        stringResource(R.string.device_tab_vault)
    )
    ScreenScaffold(
        title = titleOverride ?: stringResource(R.string.feature_device),
        onBack = onBack,
        actions = { TextButton(onClick = onMore) { Text(stringResource(R.string.device_more)) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MagicTabs(labels = labels, selected = tab, onSelect = { tab = it })
            when (tab) {
                0 -> UsageTab()
                1 -> BlockerTab(store)
                2 -> DomainsTab(store)
                3 -> NotificationsTab(store)
                else -> VaultTab(store)
            }
        }
    }
}

@Composable
private fun UsageTab() {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(AppUsage.hasAccess(context)) }
    var entries by remember { mutableStateOf<List<core.device.UsageEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(hasAccess, tick) {
        if (!hasAccess) {
            entries = emptyList()
            return@LaunchedEffect
        }
        loading = true
        entries = withContext(Dispatchers.IO) { AppUsage.today(context) }
        loading = false
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (!hasAccess) {
            InfoCard(
                text = stringResource(R.string.usage_access_desc),
                tone = InfoTone.WARN,
                action = {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }) { Text(stringResource(R.string.usage_access_open)) }
                }
            )
            TextButton(onClick = { hasAccess = AppUsage.hasAccess(context) }) {
                Text(stringResource(R.string.usage_refresh))
            }
            return@Column
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.usage_today),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { tick++ }) { Text(stringResource(R.string.usage_refresh)) }
        }
        if (loading) {
            CircularProgressIndicator(Modifier.padding(24.dp))
            return@Column
        }
        if (entries.isEmpty()) {
            Text(
                stringResource(R.string.usage_empty),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        entries.forEach { entry ->
            RowItem(
                title = entry.label,
                subtitle = String.format(
                    Locale.getDefault(),
                    stringResource(R.string.usage_minutes),
                    entry.minutes
                )
            )
        }
        Text(
            stringResource(R.string.usage_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BlockerTab(store: Store) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var accessibility by remember { mutableStateOf(Access.accessibility(context)) }
    var showApps by remember { mutableStateOf(false) }
    var clock by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { Access.installedApps(context) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            clock++
        }
    }
    val blocking = remember(
        clock,
        store.blockerOn,
        store.blockedApps,
        store.blockStart,
        store.blockEnd
    ) { store.isBlockingNow() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ToggleRow(
            title = stringResource(R.string.blocker_title),
            subtitle = stringResource(R.string.blocker_desc),
            checked = store.blockerOn,
            onChange = { store.setBlocker(it) }
        )
        InfoCard(
            text = if (blocking) {
                stringResource(R.string.blocker_now_active)
            } else {
                stringResource(R.string.blocker_now_idle)
            },
            tone = if (blocking) InfoTone.INFO else InfoTone.WARN
        )
        SectionTitle(stringResource(R.string.blocker_window), Modifier.padding(horizontal = 16.dp))
        Text(
            stringResource(R.string.blocker_window_desc),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> store.setBlockWindow(hour * 60 + minute, store.blockEnd) },
                        store.blockStart / 60,
                        store.blockStart % 60,
                        true
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.blocker_start) + " " +
                        String.format(Locale.getDefault(), "%02d:%02d", store.blockStart / 60, store.blockStart % 60)
                )
            }
            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> store.setBlockWindow(store.blockStart, hour * 60 + minute) },
                        store.blockEnd / 60,
                        store.blockEnd % 60,
                        true
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.blocker_end) + " " +
                        String.format(Locale.getDefault(), "%02d:%02d", store.blockEnd / 60, store.blockEnd % 60)
                )
            }
        }
        ToggleRow(
            title = stringResource(R.string.blocker_accessibility),
            subtitle = stringResource(R.string.blocker_accessibility_desc),
            checked = accessibility,
            onChange = { on ->
                if (on) {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } else {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            }
        )
        TextButton(
            onClick = { accessibility = Access.accessibility(context) },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(stringResource(R.string.usage_refresh)) }
        SectionTitle(stringResource(R.string.blocker_apps), Modifier.padding(horizontal = 16.dp))
        TextButton(onClick = { showApps = !showApps }, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                if (showApps) {
                    stringResource(R.string.blocker_hide_apps)
                } else {
                    stringResource(R.string.blocker_show_apps)
                }
            )
        }
        if (apps.isEmpty()) {
            Text(
                stringResource(R.string.blocker_loading),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showApps) {
            apps.forEach { (pkg, label) ->
                ToggleRow(
                    title = label,
                    subtitle = pkg,
                    checked = pkg in store.blockedApps,
                    onChange = { store.toggleBlockedApp(pkg) }
                )
            }
        }
        Text(
            stringResource(R.string.blocker_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DomainsTab(store: Store) {
    var value by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        InfoCard(text = stringResource(R.string.domains_desc), tone = InfoTone.INFO)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.domain_hint)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    store.addDomain(value)
                    value = ""
                },
                enabled = value.isNotBlank()
            ) { Text(stringResource(R.string.common_add)) }
        }
        if (store.blockedDomains.isEmpty()) {
            Text(
                stringResource(R.string.domain_empty),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        store.blockedDomains.forEach { domain ->
            RowItem(
                title = domain,
                subtitle = "",
                trailing = {
                    IconButton(onClick = { store.deleteDomain(domain) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
                    }
                }
            )
        }
        Text(
            stringResource(R.string.domains_limited),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotificationsTab(store: Store) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(Access.notificationListener(context)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (!granted) {
            InfoCard(
                text = stringResource(R.string.notif_permission_desc),
                tone = InfoTone.WARN,
                action = {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }) { Text(stringResource(R.string.notif_permission_open)) }
                }
            )
            TextButton(onClick = { granted = Access.notificationListener(context) }) {
                Text(stringResource(R.string.usage_refresh))
            }
            return@Column
        }
        ToggleRow(
            title = stringResource(R.string.notif_listener),
            subtitle = stringResource(R.string.notif_listener_desc),
            checked = granted,
            onChange = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.notif_list),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { store.clearNotifications() },
                enabled = store.notifications.isNotEmpty()
            ) { Text(stringResource(R.string.notif_clear)) }
        }
        if (store.notifications.isEmpty()) {
            Text(
                stringResource(R.string.notif_empty),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        store.notifications.forEach { entry ->
            RowItem(
                title = entry.title.ifBlank { entry.pkg },
                subtitle = entry.text.ifBlank { timeFormat.format(Date(entry.at)) }
            )
        }
        Text(
            stringResource(R.string.notif_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VaultTab(store: Store) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var viewed by remember { mutableStateOf<Pair<String, String>?>(null) }
    var failed by remember { mutableStateOf(false) }
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        InfoCard(text = stringResource(R.string.vault_desc), tone = InfoTone.INFO)
        SectionTitle(stringResource(R.string.vault_add), Modifier.padding(horizontal = 16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.vault_title_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text(stringResource(R.string.vault_body_hint)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Button(
            onClick = {
                if (store.addVault(title, body)) {
                    title = ""
                    body = ""
                    failed = false
                    Toast.makeText(context, R.string.vault_saved, Toast.LENGTH_SHORT).show()
                } else {
                    failed = true
                }
            },
            enabled = title.isNotBlank() || body.isNotBlank(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) { Text(stringResource(R.string.common_save)) }
        if (failed) {
            InfoCard(text = stringResource(R.string.vault_failed), tone = InfoTone.ERROR)
        }
        if (store.vault.isEmpty()) {
            Text(
                stringResource(R.string.vault_empty),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        store.vault.forEach { note ->
            val text = store.vaultText(note)
            val noteTitle = when {
                text == null -> stringResource(R.string.vault_locked)
                text.first.isBlank() -> stringResource(R.string.vault_untitled)
                else -> text.first
            }
            RowItem(
                title = noteTitle,
                subtitle = formatter.format(Date(note.at)),
                onClick = {
                    viewed = text
                    if (text == null) failed = true
                },
                trailing = {
                    IconButton(onClick = { store.deleteVault(note.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
                    }
                }
            )
        }
        Text(
            stringResource(R.string.vault_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    viewed?.let { content ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewed = null },
            title = { Text(content.first.ifBlank { stringResource(R.string.vault_untitled) }) },
            text = { Text(content.second) },
            confirmButton = {
                TextButton(onClick = { viewed = null }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }
}

@Composable
private fun SyncTab(store: Store) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf(store.serverUrl) }
    var email by remember { mutableStateOf(store.accountEmail) }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var showRegister by remember { mutableStateOf(false) }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        InfoCard(
            text = stringResource(R.string.sync_desc),
            tone = if (store.authToken.isBlank()) InfoTone.WARN else InfoTone.INFO
        )
        OutlinedTextField(
            value = server,
            onValueChange = { server = it },
            label = { Text(stringResource(R.string.sync_server_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )
        TextButton(
            onClick = {
                store.saveServerUrl(server)
                status = context.getString(R.string.sync_saved)
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) { Text(stringResource(R.string.common_save)) }

        if (store.authToken.isBlank()) {
            SectionTitle(stringResource(R.string.sync_login), Modifier.padding(horizontal = 16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.sync_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.sync_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Button(
                onClick = {
                    if (!Api.validBase(server)) {
                        status = context.getString(R.string.sync_no_server)
                    } else {
                        busy = true
                        status = ""
                        scope.launch {
                            runCatching {
                                Api.login(Api.normalizeBase(server), email.trim(), password)
                            }.onSuccess { result ->
                                store.saveServerUrl(server)
                                store.saveAccount(result.token, result.user.id, result.user.name, result.user.email)
                                password = ""
                                status = context.getString(R.string.sync_ok)
                            }.onFailure {
                                status = it.message ?: context.getString(R.string.sync_failed)
                            }
                            busy = false
                        }
                    }
                },
                enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text(stringResource(R.string.sync_login_btn)) }
            TextButton(
                onClick = { showRegister = true },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) { Text(stringResource(R.string.account_register)) }
        } else {
            InfoCard(
                text = stringResource(R.string.sync_logged_as) + " " +
                    store.accountName.ifBlank { store.accountEmail },
                tone = InfoTone.INFO,
                action = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                busy = true
                                status = ""
                                scope.launch {
                                    runCatching {
                                        Api.sync(Api.normalizeBase(server), store.authToken, payload(store))
                                    }.onSuccess {
                                        status = context.getString(R.string.sync_ok)
                                    }.onFailure {
                                        status = it.message ?: context.getString(R.string.sync_failed)
                                    }
                                    runCatching {
                                        store.vault.forEach { note ->
                                            Api.vaultUpsert(
                                                Api.normalizeBase(server),
                                                store.authToken,
                                                note.id,
                                                note.payload,
                                                note.at
                                            )
                                        }
                                    }
                                    busy = false
                                }
                            },
                            enabled = !busy
                        ) { Text(stringResource(R.string.sync_now)) }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val token = store.authToken
                                    store.logout()
                                    runCatching { Api.logout(Api.normalizeBase(server), token) }
                                }
                            }
                        ) { Text(stringResource(R.string.sync_logout)) }
                    }
                }
            )
        }
        if (busy) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }
        if (status.isNotBlank()) {
            Text(
                status,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            stringResource(R.string.sync_hint),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showRegister) {
        FormDialog(
            title = stringResource(R.string.account_register),
            confirm = stringResource(R.string.account_confirm),
            onDismiss = { showRegister = false },
            onConfirm = {
                if (!Api.validBase(server)) {
                    status = context.getString(R.string.sync_no_server)
                } else {
                    busy = true
                    status = ""
                    scope.launch {
                        runCatching {
                            Api.register(Api.normalizeBase(server), regName.trim(), regEmail.trim(), regPassword)
                        }.onSuccess { result ->
                            store.saveServerUrl(server)
                            store.saveAccount(result.token, result.user.id, result.user.name, result.user.email)
                            regPassword = ""
                            showRegister = false
                            status = context.getString(R.string.account_created)
                        }.onFailure {
                            status = it.message ?: context.getString(R.string.sync_failed)
                        }
                        busy = false
                    }
                }
            },
            valid = regName.isNotBlank() && regEmail.isNotBlank() && regPassword.length >= 8
        ) {
            Text(stringResource(R.string.account_hint), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = regName,
                onValueChange = { regName = it },
                label = { Text(stringResource(R.string.account_name)) },
                singleLine = true
            )
            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text(stringResource(R.string.account_email)) },
                singleLine = true
            )
            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text(stringResource(R.string.account_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}

private fun payload(store: Store): JSONObject {
    val root = JSONObject()
    root.put("points", store.points)
    root.put("xp", store.xp)
    root.put("level", store.level)
    root.put("profile", JSONObject().apply {
        put("name", store.profileName)
        put("age", store.profileAge)
        put("goal", store.profileGoal)
        put("team", store.profileTeam)
    })
    root.put("journal", JSONArray().apply {
        store.journal.forEach { entry ->
            put(JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("body", entry.body)
                put("at", entry.at)
            })
        }
    })
    root.put("ledger", JSONArray().apply {
        store.ledger.forEach { entry ->
            put(JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("amount", entry.amount)
                put("income", entry.income)
                put("category", entry.category)
                put("at", entry.at)
            })
        }
    })
    root.put("books", JSONArray().apply {
        store.books.forEach { book ->
            put(JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("page", book.page)
                put("pages", book.pages)
            })
        }
    })
    root.put("wallets", JSONArray().apply {
        store.wallets.forEach { wallet ->
            put(JSONObject().apply {
                put("id", wallet.id)
                put("name", wallet.name)
                put("balance", wallet.balance)
            })
        }
    })
    return root
}
