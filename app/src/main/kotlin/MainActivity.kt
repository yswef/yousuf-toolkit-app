import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import core.design.AppTheme
import core.design.UiState
import core.navigation.AppNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ui = remember { UiState() }
            AppTheme(mode = ui.mode, dark = ui.dark) {
                AppNav(ui = ui)
            }
        }
    }
}
