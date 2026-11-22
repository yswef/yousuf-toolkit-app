package feature.watch

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** محمّل صور بسيط بلا مكتبات: ذاكرة مؤقتة + ملفات في cacheDir. */
object ImageLoader {
    private val memory = LruCache<String, ImageBitmap>(64)

    suspend fun load(context: Context, url: String, reqWidth: Int = 420): ImageBitmap? = withContext(Dispatchers.IO) {
        memory.get(url)?.let { return@withContext it }
        val dir = File(context.cacheDir, "img").apply { mkdirs() }
        val file = File(dir, Integer.toHexString(url.hashCode()) + ".img")
        if (!file.exists() || file.length() == 0L) {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 10_000
                c.readTimeout = 15_000
                c.setRequestProperty("User-Agent", "YDimension/0.12 (Android)")
                if (c.responseCode in 200..299) {
                    c.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
                }
                c.disconnect()
            } catch (_: Exception) {
                file.delete()
                return@withContext null
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= reqWidth) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeFile(file.path, opts) ?: run { file.delete(); return@withContext null }
        val image = bmp.asImageBitmap()
        memory.put(url, image)
        image
    }
}

@Composable
fun RemoteImage(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val context = LocalContext.current
    var image by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        if (url.isNotBlank()) image = ImageLoader.load(context, url)
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        image?.let {
            Image(bitmap = it, contentDescription = null, contentScale = contentScale, modifier = Modifier.fillMaxSize())
        }
    }
}
