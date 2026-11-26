// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import data.local.LocalStore

@Composable
fun RightsScreen(onAccepted: () -> Unit) {
    val store = LocalStore.current
    var accepted by remember { mutableStateOf(store.rightsAccepted) }
    val items = listOf(
        R.string.rights_item_1,
        R.string.rights_item_2,
        R.string.rights_item_3,
        R.string.rights_item_4,
        R.string.rights_item_5,
        R.string.rights_item_6
    )
    ScreenScaffold(title = stringResource(R.string.rights_title), onBack = {}) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.rights_intro), style = MaterialTheme.typography.bodyLarge)
            items.forEach { id -> Text("• " + stringResource(id)) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text(stringResource(R.string.rights_accept), modifier = Modifier.weight(1f))
            }
            Button(
                onClick = { store.acceptRights(); onAccepted() },
                enabled = accepted,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.rights_button)) }
        }
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
