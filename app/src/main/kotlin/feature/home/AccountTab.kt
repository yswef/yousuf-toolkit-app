// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.Accent
import core.design.BookSymbol
import core.design.V3Group
import core.design.V3Hero
import core.design.V3Row
import core.design.FormDialog
import core.design.UiMode
import core.design.UiState
import core.design.drawBookSymbol
import core.net.Api
import core.net.ApiException
import core.net.authError
import core.net.healthStatus
import core.navigation.Routes
import data.local.Store
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun AccountTab(
    store: Store,
    ui: UiState,
    onFeature: (String) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = BookSymbol.entries.getOrElse(store.avatar) { BookSymbol.ORB }
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val base = store.serverUrl.ifBlank { store.config.apiBase }
    var serverInput by remember { mutableStateOf(base) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var authStatus by remember { mutableStateOf("") }
    var showRegister by remember { mutableStateOf(false) }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    val joined = runCatching {
        Instant.ofEpochMilli(store.joinedAt).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }.getOrDefault("")

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            V3Hero {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF755DCF), Color(0xFF121025))))
                            .border(2.dp, Color(0xFFC09843), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.size(38.dp)) {
                            drawBookSymbol(symbol, Color(0xFFE8C06A), Offset(size.width / 2, size.height / 2), size.minDimension * 0.44f)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            store.profileName.ifBlank { stringResource(R.string.account_guest) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            rankName(store.rankKey) + " · " + stringResource(R.string.game_level_points, store.level, store.points),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (joined.isNotBlank()) {
                            Text(
                                stringResource(R.string.account_joined, joined),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniPill(stringResource(R.string.stats_energy), "${store.energy}", Modifier.weight(1f))
                    MiniPill(stringResource(R.string.stats_points), "${store.points}", Modifier.weight(1f))
                    MiniPill(stringResource(R.string.path_campaigns), "${store.campaigns.size}", Modifier.weight(1f))
                }
                Button(onClick = { onFeature(Routes.PROFILE) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.account_edit))
                }
            }
        }
        item {
            V3Group {
                V3Row(
                    title = stringResource(R.string.feature_settings),
                    subtitle = stringResource(R.string.v3_appearance),
                    icon = Icons.Filled.Settings,
                    onClick = onSettings
                )
            }
        }
        item {
            GlassCard {
                Text(
                    stringResource(R.string.account_cloud),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.auth_cloud_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = serverInput,
                    onValueChange = { serverInput = it },
                    label = { Text(stringResource(R.string.auth_server_field)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            store.saveServerUrl(serverInput)
                            authStatus = context.getString(R.string.sync_saved)
                        }
                    ) { Text(stringResource(R.string.common_save)) }
                    TextButton(
                        onClick = {
                            busy = true
                            authStatus = ""
                            scope.launch {
                                authStatus = healthStatus(context, Api.health(Api.normalizeBase(serverInput)))
                                busy = false
                            }
                        }
                    ) { Text(stringResource(R.string.auth_server_check)) }
                }
                if (store.hasLocalAccount) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.auth_local_exists, store.localAccountEmail),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            store.deleteLocalAccount()
                            authStatus = context.getString(R.string.auth_local_removed)
                        }) { Text(stringResource(R.string.auth_local_remove)) }
                    }
                }
                if (store.authToken.isBlank()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.account_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.account_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                            onClick = {
                                busy = true
                                authStatus = ""
                                scope.launch {
                                    if (store.verifyLocalAccount(email.trim(), password)) {
                                        password = ""
                                        authStatus = context.getString(R.string.auth_local_login)
                                    } else {
                                        runCatching { Api.login(Api.normalizeBase(base), email.trim(), password) }
                                            .onSuccess { result ->
                                                store.saveAccount(result.token, result.user.id, result.user.name, result.user.email)
                                                password = ""
                                                authStatus = context.getString(R.string.auth_logged_in)
                                            }
                                            .onFailure { authStatus = authError(context, it) }
                                    }
                                    busy = false
                                }
                            }
                        ) { Text(stringResource(R.string.sync_login_btn)) }
                        OutlinedButton(onClick = { showRegister = true }) {
                            Text(stringResource(R.string.account_register))
                        }
                    }
                } else {
                    Text(
                        stringResource(R.string.sync_logged_as) + " " +
                            store.accountName.ifBlank { store.accountEmail },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val token = store.authToken
                                store.logout()
                                runCatching { Api.logout(Api.normalizeBase(base), token) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.sync_logout)) }
                }
                if (busy) CircularProgressIndicator(Modifier.size(22.dp))
                if (authStatus.isNotBlank()) {
                    Text(authStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        item {
            GlassCard {
                Text(
                    stringResource(R.string.account_appearance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Accent.entries.forEach { accent ->
                        val selected = store.accent == accent.key
                        Box(
                            Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accent.dark)
                                .border(if (selected) 3.dp else 1.dp, if (selected) Color.White else Color.Transparent, CircleShape)
                                .clickable { store.saveSkin(accent.key) }
                        )
                    }
                }
                Text(
                    stringResource(R.string.skin_avatar),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BookSymbol.entries.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { item ->
                            val index = BookSymbol.entries.indexOf(item)
                            val selected = store.avatar == index
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
                                    .clickable { store.saveAvatar(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(Modifier.size(24.dp)) {
                                    drawBookSymbol(item, if (selected) scheme.primary else scheme.onSurfaceVariant, Offset(size.width / 2, size.height / 2), size.minDimension * 0.44f)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Text(
                    stringResource(R.string.account_app),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.account_dark), modifier = Modifier.weight(1f))
                    Switch(checked = ui.dark, onCheckedChange = { ui.dark = it }, enabled = !ui.followsSystem)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.account_follow_system), modifier = Modifier.weight(1f))
                    Switch(checked = ui.followsSystem, onCheckedChange = { if (it) ui.followSystem() else ui.dark = ui.dark })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.account_mode), modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = ui.mode == UiMode.PROFESSIONAL,
                        onClick = { ui.mode = UiMode.PROFESSIONAL },
                        label = { Text(stringResource(R.string.ui_professional)) }
                    )
                    FilterChip(
                        selected = ui.mode == UiMode.SIMPLE,
                        onClick = { ui.mode = UiMode.SIMPLE },
                        label = { Text(stringResource(R.string.ui_simple)) }
                    )
                }
                OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.account_full_settings))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onFeature(Routes.LICENSE) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.account_license))
                    }
                    OutlinedButton(onClick = { onFeature(Routes.TESTS) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.account_tests))
                    }
                }
            }
        }
    }
    if (showRegister) {
        fun createLocal() {
            val ok = store.createLocalAccount(regName.trim(), regEmail.trim(), regPassword)
            authStatus = context.getString(if (ok) R.string.auth_local_created else R.string.auth_email_invalid)
            if (ok) {
                regPassword = ""
                password = ""
                showRegister = false
            }
        }
        FormDialog(
            title = stringResource(R.string.account_register),
            confirm = stringResource(R.string.account_confirm),
            onDismiss = { showRegister = false },
            onConfirm = {
                busy = true
                authStatus = ""
                scope.launch {
                    runCatching { Api.register(Api.normalizeBase(base), regName.trim(), regEmail.trim(), regPassword) }
                        .onSuccess { result ->
                            store.saveAccount(result.token, result.user.id, result.user.name, result.user.email)
                            regPassword = ""
                            showRegister = false
                            authStatus = context.getString(R.string.auth_registered)
                        }
                        .onFailure { e ->
                            if (e is ApiException && (e.message == Api.SERVER_HTML ||
                                    e.message == Api.NETWORK_ERROR || e.message == Api.BAD_JSON ||
                                    e.message == Api.INVALID_BASE)) {
                                createLocal()
                            } else {
                                authStatus = authError(context, e)
                            }
                        }
                    busy = false
                }
            },
            valid = regName.isNotBlank() && regEmail.isNotBlank() && regPassword.length >= 8
        ) {
            Text(stringResource(R.string.account_hint), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text(stringResource(R.string.account_name)) }, singleLine = true)
            OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text(stringResource(R.string.account_email)) }, singleLine = true)
            OutlinedTextField(value = regPassword, onValueChange = { regPassword = it }, label = { Text(stringResource(R.string.account_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            TextButton(onClick = { createLocal() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.auth_local_create))
            }
        }
    }

}

@Composable
private fun MiniPill(label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun rankName(key: String): String = when (key) {
    "novice" -> stringResource(R.string.rank_novice)
    "apprentice" -> stringResource(R.string.rank_apprentice)
    "knight" -> stringResource(R.string.rank_knight)
    "lieutenant" -> stringResource(R.string.rank_lieutenant)
    "captain" -> stringResource(R.string.rank_captain)
    "mage" -> stringResource(R.string.rank_mage)
    "sage" -> stringResource(R.string.rank_sage)
    "emperor" -> stringResource(R.string.rank_emperor)
    else -> key
}

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
