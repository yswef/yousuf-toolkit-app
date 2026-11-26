// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.Accent
import core.design.InfoCard
import core.design.InfoTone
import core.design.MotionLevel
import core.design.ScreenScaffold
import core.design.SoundFx
import core.design.UiMode
import core.design.UiState
import core.design.V3Chip
import core.design.V3ColorSwatch
import core.design.V3Group
import core.design.V3Row
import core.design.V3SectionHeader
import core.design.V3SwitchRow
import data.local.LocalStore
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
    onTests: () -> Unit = {},
    onFeature: (String) -> Unit = {}
) {
    val store = LocalStore.current
    val context = LocalContext.current
    var sound by remember { mutableStateOf(SoundFx.enabled) }

    ScreenScaffold(title = stringResource(R.string.feature_settings), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            V3SectionHeader(title = stringResource(R.string.v3_appearance))
            V3Group {
                V3SwitchRow(
                    title = stringResource(R.string.theme_system),
                    subtitle = stringResource(R.string.v3_theme_system_hint),
                    checked = ui.followsSystem,
                    onChange = { if (it) ui.followSystem() else ui.dark = ui.dark }
                )
                V3SwitchRow(
                    title = stringResource(R.string.dark_theme),
                    subtitle = "",
                    checked = ui.dark,
                    enabled = !ui.followsSystem,
                    divider = true,
                    onChange = { ui.dark = it }
                )
                V3SwitchRow(
                    title = stringResource(R.string.sound_title),
                    subtitle = stringResource(R.string.sound_desc),
                    checked = sound,
                    divider = true,
                    onChange = { sound = it; SoundFx.setEnabled(context, it) }
                )
            }

            Text(stringResource(R.string.v3_font_size), style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Chip(stringResource(R.string.v3_font_normal), ui.fontScale == 1f, { ui.setFontScale(1f) }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.v3_font_large), ui.fontScale == 1.15f, { ui.setFontScale(1.15f) }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.v3_font_xlarge), ui.fontScale == 1.3f, { ui.setFontScale(1.3f) }, Modifier.weight(1f))
            }

            Text(stringResource(R.string.v3_motion), style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Chip(stringResource(R.string.v3_motion_full), ui.motion == MotionLevel.FULL, { ui.motion = MotionLevel.FULL }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.v3_motion_light), ui.motion == MotionLevel.LIGHT, { ui.motion = MotionLevel.LIGHT }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.v3_motion_none), ui.motion == MotionLevel.NONE, { ui.motion = MotionLevel.NONE }, Modifier.weight(1f))
            }
            if (ui.systemReducedMotion) {
                InfoCard(text = stringResource(R.string.v3_motion_system), tone = InfoTone.WARN)
            }

            Text(stringResource(R.string.v3_accent), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Accent.entries.forEach { accent ->
                    V3ColorSwatch(
                        color = accent.light,
                        selected = store.accent == accent.key,
                        onClick = { store.saveSkin(accent.key) }
                    )
                }
            }

            Text(stringResource(R.string.ui_mode), style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V3Chip(
                    stringResource(R.string.ui_professional),
                    ui.mode == UiMode.PROFESSIONAL,
                    { ui.mode = UiMode.PROFESSIONAL },
                    Modifier.weight(1f)
                )
                V3Chip(
                    stringResource(R.string.ui_simple),
                    ui.mode == UiMode.SIMPLE,
                    { ui.mode = UiMode.SIMPLE },
                    Modifier.weight(1f)
                )
            }

            V3SectionHeader(title = stringResource(R.string.feature_settings))
            V3Group {
                V3Row(
                    title = stringResource(R.string.settings_profile),
                    subtitle = stringResource(R.string.settings_visuals),
                    icon = Icons.Filled.Person,
                    onClick = onProfile
                )
                V3Row(
                    title = stringResource(R.string.settings_skin),
                    icon = Icons.Filled.Settings,
                    divider = true,
                    onClick = onSkin
                )
                V3Row(
                    title = stringResource(R.string.settings_license),
                    icon = Icons.Filled.CheckCircle,
                    divider = true,
                    onClick = onLicense
                )
                V3Row(
                    title = stringResource(R.string.settings_tests),
                    icon = Icons.Filled.List,
                    divider = true,
                    onClick = onTests
                )
                V3Row(
                    title = stringResource(R.string.settings_replay_intro),
                    icon = Icons.Filled.PlayArrow,
                    divider = true,
                    onClick = onReplayIntro
                )
            }

            V3SectionHeader(title = stringResource(R.string.git_title))
            V3Group {
                V3Row(
                    title = stringResource(R.string.devices_title),
                    icon = Icons.Filled.Person,
                    onClick = { onFeature(core.navigation.Routes.DEVICES) }
                )
                V3Row(
                    title = stringResource(R.string.apps_title),
                    icon = Icons.Filled.Home,
                    divider = true,
                    onClick = { onFeature(core.navigation.Routes.APPS) }
                )
                V3Row(
                    title = stringResource(R.string.visibility_title),
                    icon = Icons.Filled.List,
                    divider = true,
                    onClick = { onFeature(core.navigation.Routes.VISIBILITY) }
                )
                V3Row(
                    title = stringResource(R.string.media_title),
                    icon = Icons.Filled.PlayArrow,
                    divider = true,
                    onClick = { onFeature(core.navigation.Routes.MEDIA) }
                )
                V3Row(
                    title = stringResource(R.string.files_title),
                    icon = Icons.Filled.List,
                    divider = true,
                    onClick = { onFeature(core.navigation.Routes.FILES) }
                )
                V3Row(
                    title = stringResource(R.string.git_title),
                    icon = Icons.Filled.Settings,
                    divider = true,
                    onClick = { onFeature(core.navigation.Routes.GIT) }
                )
            }

            PermissionsSection()

            DataManagement()

            V3SectionHeader(title = stringResource(R.string.v3_about))
            InfoCard(
                text = stringResource(
                    R.string.v3_developer_info,
                    store.config.developerName,
                    store.config.developerEmail
                ),
                tone = InfoTone.INFO
            )
            Text(
                stringResource(R.string.account_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
