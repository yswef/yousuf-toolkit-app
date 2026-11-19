package core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiException(val code: Int, message: String) : Exception(message)

data class RemoteUser(val id: Long, val name: String, val email: String)

data class AuthResult(val token: String, val user: RemoteUser)

data class BoardThread(val id: Long, val title: String, val author: String, val createdAt: Long, val postCount: Int)

data class BoardPost(val id: Long, val author: String, val body: String, val createdAt: Long)

data class ShopItem(val id: Long, val title: String, val description: String, val price: Int, val kind: String)

data class ShopBuyResult(val balance: Int, val title: String)

data class RaidInfo(val season: Long, val progress: Int, val goal: Int, val members: Int, val rank: Int)

data class CheckinResult(val connected: Boolean, val nextInSeconds: Int, val day: Int)

object Api {
    fun validBase(raw: String): Boolean {
        val value = raw.trim()
        return value.startsWith("https://") || value.startsWith("http://")
    }

    fun normalizeBase(raw: String): String = raw.trim().trimEnd('/')

    suspend fun register(base: String, name: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val response = call(
                base, "/api/auth.php?action=register", "POST",
                JSONObject().put("name", name).put("email", email).put("password", password)
            )
            AuthResult(response.getString("token"), user(response.getJSONObject("user")))
        }

    suspend fun login(base: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val response = call(
                base, "/api/auth.php?action=login", "POST",
                JSONObject().put("email", email).put("password", password)
            )
            AuthResult(response.getString("token"), user(response.getJSONObject("user")))
        }

    suspend fun forgot(base: String, email: String) {
        withContext(Dispatchers.IO) {
            call(base, "/api/auth.php?action=forgot", "POST", JSONObject().put("email", email))
        }
    }

    suspend fun logout(base: String, token: String) {
        withContext(Dispatchers.IO) { call(base, "/api/auth.php?action=logout", "POST", null, token) }
    }

    suspend fun sync(base: String, token: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) { call(base, "/api/sync.php", "POST", payload, token) }

    suspend fun threads(base: String, token: String): List<BoardThread> = withContext(Dispatchers.IO) {
        val response = call(base, "/api/forum.php?action=threads", "GET", null, token)
        response.optJSONArray("threads").mapObjects {
            BoardThread(
                id = it.optLong("id"),
                title = it.optString("title"),
                author = it.optString("author"),
                createdAt = it.optLong("created_at") * 1000L,
                postCount = it.optInt("post_count")
            )
        }
    }

    suspend fun posts(base: String, token: String, threadId: Long): List<BoardPost> =
        withContext(Dispatchers.IO) {
            val response = call(base, "/api/forum.php?action=posts&thread_id=$threadId", "GET", null, token)
            response.optJSONArray("posts").mapObjects {
                BoardPost(
                    id = it.optLong("id"),
                    author = it.optString("author"),
                    body = it.optString("body"),
                    createdAt = it.optLong("created_at") * 1000L
                )
            }
        }

    suspend fun newThread(base: String, token: String, title: String, body: String) {
        withContext(Dispatchers.IO) {
            call(
                base, "/api/forum.php?action=new_thread", "POST",
                JSONObject().put("title", title).put("body", body), token
            )
        }
    }

    suspend fun newPost(base: String, token: String, threadId: Long, body: String) {
        withContext(Dispatchers.IO) {
            call(
                base, "/api/forum.php?action=new_post", "POST",
                JSONObject().put("thread_id", threadId).put("body", body), token
            )
        }
    }

    suspend fun shop(base: String, token: String): List<ShopItem> = withContext(Dispatchers.IO) {
        val response = call(base, "/api/shop.php?action=list", "GET", null, token)
        response.optJSONArray("items").mapObjects {
            ShopItem(
                id = it.optLong("id"),
                title = it.optString("title"),
                description = it.optString("description"),
                price = it.optInt("price"),
                kind = it.optString("kind")
            )
        }
    }

    suspend fun buy(base: String, token: String, itemId: Long): ShopBuyResult =
        withContext(Dispatchers.IO) {
            val response = call(
                base, "/api/shop.php?action=buy", "POST", JSONObject().put("item_id", itemId), token
            )
            ShopBuyResult(response.optInt("balance"), response.optString("title"))
        }

    suspend fun raid(base: String, token: String): RaidInfo = withContext(Dispatchers.IO) {
        val response = call(base, "/api/raids.php?action=info", "GET", null, token)
        RaidInfo(
            season = response.optLong("season"),
            progress = response.optInt("progress"),
            goal = response.optInt("goal"),
            members = response.optInt("members"),
            rank = response.optInt("rank")
        )
    }

    suspend fun raidCheckin(base: String, token: String): CheckinResult =
        withContext(Dispatchers.IO) {
            val response = call(base, "/api/raids.php?action=checkin", "POST", null, token)
            CheckinResult(
                connected = response.optBoolean("connected"),
                nextInSeconds = response.optInt("next_in_seconds"),
                day = response.optInt("day")
            )
        }

    suspend fun vaultList(base: String, token: String): JSONArray = withContext(Dispatchers.IO) {
        call(base, "/api/vault.php", "GET", null, token).optJSONArray("items") ?: JSONArray()
    }

    suspend fun vaultUpsert(base: String, token: String, clientId: Long, payload: String, updatedAt: Long) {
        withContext(Dispatchers.IO) {
            call(
                base, "/api/vault.php", "POST",
                JSONObject()
                    .put("client_id", clientId)
                    .put("payload", payload)
                    .put("updated_at", updatedAt),
                token
            )
        }
    }

    suspend fun vaultDelete(base: String, token: String, clientId: Long) {
        withContext(Dispatchers.IO) {
            call(base, "/api/vault.php?client_id=$clientId", "DELETE", null, token)
        }
    }

    private fun call(
        base: String,
        path: String,
        method: String,
        body: JSONObject?,
        token: String = ""
    ): JSONObject {
        if (!validBase(base)) throw ApiException(0, "invalid base url")
        val connection = URL(normalizeBase(base) + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 8000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            if (token.isNotBlank()) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val parsed = runCatching { JSONObject(text) }.getOrElse {
                JSONObject().put("error", if (text.isBlank()) "HTTP $code" else text.take(300))
            }
            if (code !in 200..299) {
                throw ApiException(code, parsed.optString("error", "HTTP $code"))
            }
            return parsed
        } finally {
            connection.disconnect()
        }
    }

    private fun user(json: JSONObject) = RemoteUser(
        id = json.optLong("id"),
        name = json.optString("name"),
        email = json.optString("email")
    )
}

private fun <T> JSONArray?.mapObjects(block: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val out = ArrayList<T>(length())
    for (i in 0 until length()) {
        val item = optJSONObject(i) ?: continue
        out.add(block(item))
    }
    return out
}
