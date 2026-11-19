package core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Platform {
    data class Release(
        val tag: String,
        val page: String,
        val apk: String,
        val notes: String,
        val publishedAt: String
    )

    suspend fun latestRelease(apiUrl: String): Release? = withContext(Dispatchers.IO) {
        if (!apiUrl.startsWith("https://")) return@withContext null
        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 12000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(text)
            val assets = root.optJSONArray("assets")
            var apk = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name").endsWith(".apk")) {
                        apk = asset.optString("browser_download_url")
                        break
                    }
                }
            }
            Release(
                tag = root.optString("tag_name"),
                page = root.optString("html_url"),
                apk = apk,
                notes = root.optString("body"),
                publishedAt = root.optString("published_at")
            )
        } catch (t: Throwable) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
