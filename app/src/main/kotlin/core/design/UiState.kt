// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.design

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UiState(context: Context, initialSystemDark: Boolean) {
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    private var overrideDark by mutableStateOf(
        if (prefs.contains("dark")) prefs.getBoolean("dark", false) else null
    )
    var systemDark by mutableStateOf(initialSystemDark)

    private var modeState by mutableStateOf(
        runCatching { UiMode.valueOf(prefs.getString("mode", UiMode.PROFESSIONAL.name) ?: UiMode.PROFESSIONAL.name) }
            .getOrDefault(UiMode.PROFESSIONAL)
    )
    var mode: UiMode
        get() = modeState
        set(value) {
            modeState = value
            prefs.edit().putString("mode", value.name).apply()
        }

    private var fontState by mutableStateOf(prefs.getFloat("font_scale", 1f).coerceIn(1f, 1.3f))
    val fontScale: Float get() = fontState

    fun setFontScale(value: Float) {
        fontState = value.coerceIn(1f, 1.3f)
        prefs.edit().putFloat("font_scale", fontState).apply()
    }

    private var motionState by mutableStateOf(
        runCatching { MotionLevel.valueOf(prefs.getString("motion", MotionLevel.FULL.name) ?: MotionLevel.FULL.name) }
            .getOrDefault(MotionLevel.FULL)
    )
    var motion: MotionLevel
        get() = motionState
        set(value) {
            motionState = value
            prefs.edit().putString("motion", value.name).apply()
        }

    var systemReducedMotion by mutableStateOf(reducedMotion(appContext))
        private set

    val effectiveMotion: MotionLevel
        get() = if (systemReducedMotion) MotionLevel.NONE else motion

    fun refreshSystemMotion() {
        systemReducedMotion = reducedMotion(appContext)
    }

    val followsSystem: Boolean get() = overrideDark == null
    var dark: Boolean
        get() = overrideDark ?: systemDark
        set(value) {
            overrideDark = value
            prefs.edit().putBoolean("dark", value).apply()
        }

    fun followSystem() {
        overrideDark = null
        prefs.edit().remove("dark").apply()
    }

    private fun reducedMotion(ctx: Context): Boolean = try {
        Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) <= 0f
    } catch (t: Throwable) {
        false
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
