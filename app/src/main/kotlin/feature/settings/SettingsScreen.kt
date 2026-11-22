package feature.settings

import core.design.ToggleRow
import core.design.SoundFx
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import core.design.UiMode
import core.design.UiState
import feature.home.DataManagement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    ui: UiState,
    onBack: () -> Unit,
    onReplayIntro: () -> Unit,
    onProfile: () -> Unit,
    onSkin: () -> Unit = {},
    onLicense: () -> Unit = {},
    onTests: () -> Unit = {}
) {
    ScreenScaffold(title = stringResource(R.string.feature_settings), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DataManagement()
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(stringResource(R.string.ui_mode))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = ui.mode == UiMode.PROFESSIONAL, onClick = { ui.mode = UiMode.PROFESSIONAL })
                Text(stringResource(R.string.ui_professional))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = ui.mode == UiMode.SIMPLE, onClick = { ui.mode = UiMode.SIMPLE })
                Text(stringResource(R.string.ui_simple))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.theme_system), modifier = Modifier.weight(1f))
                Switch(checked = ui.followsSystem, onCheckedChange = {
                    if (it) ui.followSystem() else ui.dark = ui.dark
                })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dark_theme), modifier = Modifier.weight(1f))
                Switch(checked = ui.dark, onCheckedChange = { ui.dark = it }, enabled = !ui.followsSystem)
            }
            val context = LocalContext.current
            var sound by remember { mutableStateOf(SoundFx.enabled) }
            ToggleRow(
                title = stringResource(R.string.sound_title),
                subtitle = stringResource(R.string.sound_desc),
                checked = sound,
                onChange = { sound = it; SoundFx.setEnabled(context, it) }
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text(
                stringResource(R.string.settings_visuals),
                style = MaterialTheme.typography.titleSmall
            )
            OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_profile))
            }
            OutlinedButton(onClick = onSkin, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_skin))
            }
            OutlinedButton(onClick = onLicense, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_license))
            }
            OutlinedButton(onClick = onTests, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_tests))
            }
            OutlinedButton(onClick = onReplayIntro, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_replay_intro))
            }
        }
    }
}
