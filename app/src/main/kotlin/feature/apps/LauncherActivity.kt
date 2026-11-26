// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.apps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import core.design.AppTheme
import data.local.LocalStore
import data.local.Store
import androidx.compose.foundation.isSystemInDarkTheme
import core.design.UiState

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val store = remember { Store.of(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            val ui = remember { UiState(applicationContext, systemDark) }
            CompositionLocalProvider(LocalStore provides store) {
                AppTheme(mode = ui.mode, dark = ui.dark, accent = store.accent, fontScale = ui.fontScale, motion = ui.effectiveMotion) {
                    LauncherHome(onExit = { finish() })
                }
            }
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
