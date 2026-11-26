// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.update

import android.content.Context
import core.net.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class ContentResult(val version: String, val changed: List<String>, val failed: List<String>)

object ContentUpdate {

    private const val PREF = "content_update"
    private const val KEY_VERSION = "version"

    fun localVersion(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_VERSION, "0") ?: "0"

    suspend fun sync(context: Context, base: String): ContentResult = withContext(Dispatchers.IO) {
        if (!Api.validBase(base)) return@withContext ContentResult("0", emptyList(), listOf("invalid base"))
        return@withContext try {
            val manifest = Api.contentManifest(Api.normalizeBase(base))
            val dir = File(context.filesDir, "content").apply { mkdirs() }
            val changed = ArrayList<String>()
            val failed = ArrayList<String>()
            for (file in manifest.files) {
                val target = File(dir, file.name)
                val current = if (target.isFile) sha256(target.readBytes()) else ""
                if (current == file.sha256) continue
                val url = if (file.url.startsWith("http")) file.url
                else Api.normalizeBase(base) + "/api/" + file.url.trimStart('/')
                try {
                    val bytes = Api.download(url)
                    if (sha256(bytes) != file.sha256) {
                        failed.add(file.name)
                        continue
                    }
                    JSONObject(String(bytes, Charsets.UTF_8))
                    val temp = File(dir, file.name + ".tmp")
                    temp.writeBytes(bytes)
                    if (!temp.renameTo(target)) temp.copyTo(target, overwrite = true)
                    temp.delete()
                    changed.add(file.name)
                } catch (t: Throwable) {
                    failed.add(file.name)
                }
            }
            context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY_VERSION, manifest.version).apply()
            ContentResult(manifest.version, changed, failed)
        } catch (t: Throwable) {
            ContentResult("0", emptyList(), listOf(t.message ?: "sync failed"))
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
