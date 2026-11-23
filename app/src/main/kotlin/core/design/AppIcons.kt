package core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private fun vec(name: String, path: String, mirror: Boolean = false): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirror)
        .addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

object AppIcons {
    val Apps: ImageVector by lazy {
        vec("apps", "M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4v4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z")
    }
    val Drop: ImageVector by lazy {
        vec("drop", "M12 2.5c-3.5 4.2-6 7.2-6 10.5a6 6 0 0 0 12 0c0-3.3-2.5-6.3-6-10.5z")
    }
    val Moon: ImageVector by lazy {
        vec("moon", "M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z")
    }
    val Mosque: ImageVector by lazy {
        vec("mosque", "M12 2c-.9 1.6-2 2.8-3.5 4C7.2 7 7 8 7 9h10c0-1-.2-2-1.5-3C14 4.8 12.9 3.6 12 2zM5 11v9h14v-9H5zm5 9v-4c0-1.1.9-2 2-2s2 .9 2 2v4h-4z")
    }
    val ChevronEnd: ImageVector by lazy {
        vec("chevron_end", "M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z", mirror = true)
    }
}
