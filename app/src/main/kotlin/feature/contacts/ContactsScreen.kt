package feature.contacts

import androidx.compose.runtime.Composable
import feature.devicepro.DeviceProScreen

@Composable
fun ContactsScreen(onBack: () -> Unit) {
    DeviceProScreen(onBack = onBack, initialTab = 3)
}
