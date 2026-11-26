// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class GitException(message: String) : Exception(message)

data class GitBranch(val name: String, val sha: String, val protected: Boolean)
data class GitCommit(val sha: String, val message: String, val date: String, val author: String)
data class GitRelease(val tag: String, val name: String, val publishedAt: String, val body: String, val apkUrl: String)
data class GitDiff(val aheadBy: Int, val behindBy: Int, val totalCommits: Int, val files: Int)
data class LocalRepo(val name: String, val path: String, val branch: String, val headSha: String, val lastModified: Long)

object GitHub {

    private const val API = "https://api.github.com"

    suspend fun repo(owner: String, name: String, token: String): JSONObject = withContext(Dispatchers.IO) {
        json(request("/repos/$owner/$name", token), asObject = true) as JSONObject
    }

    suspend fun branches(owner: String, name: String, token: String): List<GitBranch> = withContext(Dispatchers.IO) {
        val arr = json(request("/repos/$owner/$name/branches?per_page=100", token), asObject = false) as JSONArray
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            GitBranch(
                name = o.optString("name"),
                sha = o.optJSONObject("commit")?.optString("sha").orEmpty(),
                protected = o.optBoolean("protected")
            )
        }
    }

    suspend fun commits(owner: String, name: String, ref: String, token: String): List<GitCommit> = withContext(Dispatchers.IO) {
        val arr = json(request("/repos/$owner/$name/commits?sha=$ref&per_page=10", token), asObject = false) as JSONArray
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val c = o.optJSONObject("commit") ?: JSONObject()
            GitCommit(
                sha = o.optString("sha"),
                message = c.optString("message").lineSequence().firstOrNull().orEmpty(),
                date = c.optJSONObject("committer")?.optString("date").orEmpty(),
                author = c.optJSONObject("author")?.optString("name").orEmpty()
            )
        }
    }

    suspend fun compare(owner: String, name: String, base: String, head: String, token: String): GitDiff = withContext(Dispatchers.IO) {
        val o = json(request("/repos/$owner/$name/compare/$base...$head", token), asObject = true) as JSONObject
        GitDiff(
            aheadBy = o.optInt("ahead_by"),
            behindBy = o.optInt("behind_by"),
            totalCommits = o.optInt("total_commits"),
            files = o.optJSONArray("files")?.length() ?: 0
        )
    }

    suspend fun releases(owner: String, name: String, token: String): List<GitRelease> = withContext(Dispatchers.IO) {
        val arr = json(request("/repos/$owner/$name/releases?per_page=20", token), asObject = false) as JSONArray
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            var apk = ""
            val assets = o.optJSONArray("assets")
            if (assets != null) {
                for (a in 0 until assets.length()) {
                    val asset = assets.optJSONObject(a) ?: continue
                    if (asset.optString("name").endsWith(".apk")) {
                        apk = asset.optString("browser_download_url")
                        break
                    }
                }
            }
            GitRelease(
                tag = o.optString("tag_name"),
                name = o.optString("name"),
                publishedAt = o.optString("published_at"),
                body = o.optString("body"),
                apkUrl = apk
            )
        }
    }

    suspend fun merge(owner: String, name: String, base: String, head: String, token: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("base", base).put("head", head)
            .put("commit_message", "Merge $head into $base via Y-Dimensions")
        val o = json(request("/repos/$owner/$name/merges", token, "POST", body.toString()), asObject = true) as JSONObject
        o.optString("sha").ifBlank { "merged" }
    }

    suspend fun pull(owner: String, name: String, ref: String, token: String, target: File): Int = withContext(Dispatchers.IO) {
        val bytes = requestBytes("/repos/$owner/$name/zipball/$ref", token)
        target.mkdirs()
        var count = 0
        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val parts = entry.name.split("/", limit = 2)
                val relative = if (parts.size > 1) parts[1] else parts[0]
                if (relative.isNotBlank() && !relative.contains("..")) {
                    val out = File(target, relative)
                    if (entry.isDirectory) out.mkdirs()
                    else {
                        out.parentFile?.mkdirs()
                        FileOutputStream(out).use { f -> zip.copyTo(f) }
                        count++
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        count
    }

    suspend fun push(
        owner: String, name: String, branch: String, path: String, content: String, token: String
    ): String = withContext(Dispatchers.IO) {
        val existing = runCatching {
            json(request("/repos/$owner/$name/contents/$path?ref=$branch", token), asObject = true) as JSONObject
        }.getOrNull()
        val body = JSONObject()
            .put("message", "Update $path from Y-Dimensions")
            .put("content", android.util.Base64.encodeToString(content.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP))
            .put("branch", branch)
        if (existing != null && existing.optString("sha").isNotBlank()) body.put("sha", existing.optString("sha"))
        val o = json(request("/repos/$owner/$name/contents/$path", token, "PUT", body.toString()), asObject = true) as JSONObject
        o.optJSONObject("commit")?.optString("sha").orEmpty().ifBlank { "pushed" }
    }

    private fun json(text: String, asObject: Boolean): Any =
        if (asObject) JSONObject(text) else JSONArray(text)

    private fun request(path: String, token: String, method: String = "GET", body: String? = null): String {
        val connection = URL(API + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "Y-Dimensions")
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            if (token.isNotBlank()) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(text).optString("message") }.getOrNull().orEmpty()
                throw GitException("HTTP $code ${msg.ifBlank { "GitHub error" }}")
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun requestBytes(path: String, token: String): ByteArray {
        val connection = URL(API + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 60000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/octet-stream")
            connection.setRequestProperty("User-Agent", "Y-Dimensions")
            if (token.isNotBlank()) connection.setRequestProperty("Authorization", "Bearer $token")
            if (connection.responseCode !in 200..299) throw GitException("HTTP " + connection.responseCode)
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
