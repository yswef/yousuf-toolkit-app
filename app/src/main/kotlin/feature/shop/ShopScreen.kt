package feature.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.InfoTone
import data.local.LocalStore
import core.design.ScreenScaffold
import core.net.Api
import core.net.ApiException
import core.net.ShopItem
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<ShopItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(store.authToken, tick) {
        if (store.authToken.isBlank()) return@LaunchedEffect
        loading = true
        runCatching {
            Api.shop(Api.normalizeBase(store.serverUrl), store.authToken)
        }.onSuccess { items = it }.onFailure { error = it.message ?: "" }
        loading = false
    }

    ScreenScaffold(
        title = stringResource(R.string.feature_shop),
        onBack = onBack,
        actions = {
            if (store.authToken.isNotBlank()) {
                TextButton(onClick = { tick++ }) { Text(stringResource(R.string.usage_refresh)) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (store.authToken.isBlank()) {
                InfoCard(text = stringResource(R.string.shop_login_needed), tone = InfoTone.WARN)
                return@Column
            }
            InfoCard(text = stringResource(R.string.shop_balance, store.points))
            if (loading) CircularProgressIndicator(Modifier.padding(16.dp))
            if (error.isNotBlank()) InfoCard(text = error, tone = InfoTone.ERROR)
            if (status.isNotBlank()) InfoCard(text = status, tone = InfoTone.INFO)
            if (items.isEmpty() && !loading) {
                Text(
                    stringResource(R.string.shop_empty),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${item.price}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    status = ""
                                    scope.launch {
                                        runCatching {
                                            Api.buy(Api.normalizeBase(store.serverUrl), store.authToken, item.id)
                                        }.onSuccess { result ->
                                            status = context.getString(R.string.shop_done, result.title)
                                        }.onFailure { failure ->
                                            status = if ((failure as? ApiException)?.code == 402) {
                                                context.getString(R.string.shop_insufficient)
                                            } else {
                                                failure.message ?: context.getString(R.string.sync_failed)
                                            }
                                        }
                                    }
                                }
                            ) { Text(stringResource(R.string.shop_buy)) }
                        }
                    }
                }
            }
        }
    }
}
