package core.design

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UiState(context: Context) {
    private val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    private var overrideDark by mutableStateOf(
        if (prefs.contains("dark")) prefs.getBoolean("dark", false) else null
    )
    var systemDark by mutableStateOf(false)
    var mode by mutableStateOf(UiMode.PROFESSIONAL)
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
}
