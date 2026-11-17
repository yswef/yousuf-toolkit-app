package core.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UiState {
    var mode by mutableStateOf(UiMode.PROFESSIONAL)
    var dark by mutableStateOf(false)
}
