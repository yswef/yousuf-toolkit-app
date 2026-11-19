package feature.firewall

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yousuf.toolkit.R
import feature.device.DeviceScreen

@Composable
fun FirewallScreen(onBack: () -> Unit) {
    DeviceScreen(onBack = onBack, initialTab = 2, titleOverride = stringResource(R.string.feature_firewall))
}
