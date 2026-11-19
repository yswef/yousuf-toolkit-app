package feature.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.LocalStore
import core.design.ScreenScaffold
import core.device.Access
import core.device.AppUsage
import core.device.SmsReader
import core.net.Api

@Composable
fun TestsScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    val checks = listOf(
            stringResource(R.string.test_config) to (tick >= 0 && store.config.energyMax > 0),
            stringResource(R.string.test_storage) to context.filesDir.canWrite(),
            stringResource(R.string.test_profile) to store.hasProfile,
            stringResource(R.string.test_server) to Api.validBase(store.serverUrl),
            stringResource(R.string.test_account) to store.authToken.isNotBlank(),
            stringResource(R.string.test_notifications) to Access.notificationListener(context),
            stringResource(R.string.test_usage) to AppUsage.hasAccess(context),
            stringResource(R.string.test_accessibility) to Access.accessibility(context),
            stringResource(R.string.test_sms) to SmsReader.hasPermission(context),
        stringResource(R.string.test_premium) to store.premium
    )
    val passed = checks.count { it.second }

    ScreenScaffold(
        title = stringResource(R.string.feature_tests),
        onBack = onBack,
        actions = {
            TextButton(onClick = { tick++ }) { Text(stringResource(R.string.test_refresh)) }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard(text = stringResource(R.string.test_summary, passed, checks.size))
            checks.forEach { (label, ok) ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (ok) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(Modifier.padding(14.dp)) {
                        Text(label, modifier = Modifier.weight(1f))
                        Text(
                            if (ok) stringResource(R.string.test_ok) else stringResource(R.string.test_missing),
                            color = if (ok) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.test_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
