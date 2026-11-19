package feature.blocker

import androidx.compose.runtime.Composable
import feature.device.DeviceScreen

@Composable
fun BlockerScreen(onBack: () -> Unit) {
    DeviceScreen(onBack = onBack, initialTab = 1)
}
