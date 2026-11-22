package feature.notiflog

import core.design.EmptyIllustration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import data.local.LocalStore
import core.design.RowItem
import core.design.ScreenScaffold
import java.text.DateFormat
import java.util.Date

@Composable
fun NotiflogScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    ScreenScaffold(
        title = stringResource(R.string.feature_notiflog),
        onBack = onBack,
        actions = {
            TextButton(
                onClick = { store.clearNotifications() },
                enabled = store.notifications.isNotEmpty()
            ) { Text(stringResource(R.string.notif_clear)) }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            InfoCard(text = stringResource(R.string.notif_hint))
            if (store.notifications.isEmpty()) {
                EmptyIllustration(R.drawable.empty_notifications, stringResource(R.string.notif_empty))
            }
            store.notifications.forEach { entry ->
                RowItem(
                    title = entry.title.ifBlank { entry.pkg },
                    subtitle = entry.text.ifBlank { timeFormat.format(Date(entry.at)) }
                )
            }
        }
    }
}
