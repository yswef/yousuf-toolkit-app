package feature.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yousuf.toolkit.R
import feature.device.DeviceScreen

@Composable
fun VaultScreen(onBack: () -> Unit) {
    DeviceScreen(onBack = onBack, initialTab = 4, titleOverride = stringResource(R.string.feature_vault))
}
