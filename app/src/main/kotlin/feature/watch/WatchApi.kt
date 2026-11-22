package feature.watch

import android.text.Html
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.OffsetDateTime

data class NewsItem(val title: String, val excerpt: String, val url: String, val date: String, val source: String, val image: String)

private fun JSONObject.str(name: String): String = if (isNull(name)) "" else optString(name, "")

private fun parseIso(s: String): Long = try {
    if (s.isBlank()) 0L else OffsetDateTime.parse(s).toInstant().toEpochMilli()
} catch (_: Exception) {
    0L
}

private fun https(u: String) = if (u.startsWith("http://")) "https://" + u.removePrefix("http://") else u

private fun stripHtml(s: String): String =
    if (s.isBlank()) "" else Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString().trim()

/** "Last, First" → "First Last" */
private fun flipName(n: String): String {
    val parts = n.split(", ")
    return if (parts.size == 2) parts[1] + " " + parts[0] else n
}

/**
 * مصادر البيانات (مجانية بلا مفتاح):
 * - الأنمي: Jikan (واجهة MyAnimeList غير الرسمية)
 * - المسلسلات: TVMaze
 * العناوين المتوفرة: الإنجليزي والياباني (أنمي)، والأصلي وأحياناً العربي (مسلسلات).
 */
object WatchApi {
    private const val JIKAN = "https://api.jikan.moe/v4"
    private const val TVMAZE = "https://api.tvmaze.com"

    private suspend fun get(url: String): String? = withContext(Dispatchers.IO) {
        for (attempt in 0 until 2) {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 10_000
                c.readTimeout = 15_000
                c.setRequestProperty("User-Agent", "YDimension/0.12 (Android)")
                c.setRequestProperty("Accept", "application/json")
                val code = c.responseCode
                if (code == 429) {
                    c.disconnect()
                    delay(1300)
                    continue
                }
                if (code !in 200..299) {
                    c.disconnect()
                    return@withContext null
                }
                val text = c.inputStream.bufferedReader().use { it.readText() }
                c.disconnect()
                return@withContext text
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return@withContext null
            }
        }
        null
    }

    private fun enc(q: String) = URLEncoder.encode(q.trim(), "UTF-8")

    // ---------- أنمي ----------
    private fun parseAnime(o: JSONObject): WatchItem {
        val id = o.optLong("mal_id")
        val english = o.str("title_english")
        val romaji = o.str("title")
        val jpg = o.optJSONObject("images")?.optJSONObject("jpg")
        val poster = https(jpg?.str("large_image_url").orEmpty().ifBlank { jpg?.str("image_url").orEmpty() })
        val st = o.str("status")
        val airing = when {
            st.startsWith("Finished") -> "finished"
            st.startsWith("Currently") -> "airing"
            else -> "upcoming"
        }
        val year = o.optInt("year", 0).takeIf { it > 0 }
            ?: o.str("aired").let { 0 }
        val aired = o.optJSONObject("aired")?.str("from").orEmpty()
        val y = if (year > 0) year else aired.take(4).toIntOrNull() ?: 0
        return WatchItem(
            key = "anime:$id", kind = "anime", sid = id,
            title = english.ifBlank { romaji }, titleAlt = o.str("title_japanese"), titleOrig = romaji,
            poster = poster, synopsis = o.str("synopsis"), characters = emptyList(),
            year = y, total = if (o.isNull("episodes")) 0 else o.optInt("episodes"),
            airing = airing, aired = if (airing == "finished") (if (o.isNull("episodes")) 0 else o.optInt("episodes")) else 0,
            notified = 0, nextAt = 0L, status = "plan", watched = 0, addedAt = 0L, url = o.str("url")
        )
    }

    private fun parseAnimeList(text: String?): List<WatchItem> {
        if (text == null) return emptyList()
        return runCatching {
            val arr = JSONObject(text).optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { parseAnime(arr.getJSONObject(it)) }.distinctBy { it.key }
        }.getOrDefault(emptyList())
    }

    suspend fun searchAnime(q: String): List<WatchItem> =
        parseAnimeList(get("$JIKAN/anime?q=${enc(q)}&limit=15&order_by=popularity"))

    suspend fun airingNow(): List<WatchItem> =
        parseAnimeList(get("$JIKAN/seasons/now?limit=12&filter=tv"))

    private suspend fun animeEpisodeState(id: Long): Pair<Int, Long>? {
        val first = get("$JIKAN/anime/$id/episodes?page=1") ?: return null
        val j = runCatching { JSONObject(first) }.getOrNull() ?: return null
        val last = j.optJSONObject("pagination")?.optInt("last_visible_page", 1) ?: 1
        val page = if (last > 1) {
            delay(400)
            runCatching { JSONObject(get("$JIKAN/anime/$id/episodes?page=$last") ?: return null) }.getOrNull() ?: return null
        } else j
        val arr = page.optJSONArray("data") ?: JSONArray()
        val now = System.currentTimeMillis()
        var aired = 0
        var next = 0L
        for (i in 0 until arr.length()) {
            val at = parseIso(arr.getJSONObject(i).str("aired"))
            if (at == 0L || at <= now) aired++ else if (next == 0L) next = at
        }
        return ((last - 1) * 100 + aired) to next
    }

    private suspend fun animeCharacters(id: Long): List<String> {
        val text = get("$JIKAN/anime/$id/characters") ?: return emptyList()
        return runCatching {
            val arr = JSONObject(text).optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { arr.getJSONObject(it) }
                .filter { it.str("role") == "Main" }
                .mapNotNull { it.optJSONObject("character")?.str("name")?.takeIf { n -> n.isNotBlank() } }
                .map { flipName(it) }
                .take(8)
        }.getOrDefault(emptyList())
    }

    suspend fun animeNews(item: WatchItem): List<NewsItem> {
        val text = get("$JIKAN/anime/${item.sid}/news") ?: return emptyList()
        return runCatching {
            val arr = JSONObject(text).optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).map { arr.getJSONObject(it) }.take(3).map {
                NewsItem(
                    title = it.str("title"), excerpt = it.str("excerpt"), url = it.str("url"),
                    date = it.str("date").take(10), source = item.title,
                    image = https(it.optJSONObject("images")?.optJSONObject("jpg")?.str("image_url").orEmpty())
                )
            }
        }.getOrDefault(emptyList())
    }

    // ---------- مسلسلات ----------
    private fun parseShow(o: JSONObject): WatchItem {
        val id = o.optLong("id")
        val img = o.optJSONObject("image")
        val st = o.str("status")
        val airing = when (st) {
            "Running" -> "airing"
            "Ended" -> "finished"
            else -> "upcoming"
        }
        val name = o.str("name")
        return WatchItem(
            key = "tv:$id", kind = "series", sid = id,
            title = name, titleAlt = "", titleOrig = name,
            poster = https(img?.str("original").orEmpty().ifBlank { img?.str("medium").orEmpty() }),
            synopsis = stripHtml(o.str("summary")), characters = emptyList(),
            year = o.str("premiered").take(4).toIntOrNull() ?: 0, total = 0,
            airing = airing, aired = 0, notified = 0, nextAt = 0L,
            status = "plan", watched = 0, addedAt = 0L, url = o.str("url")
        )
    }

    suspend fun searchSeries(q: String): List<WatchItem> {
        val text = get("$TVMAZE/search/shows?q=${enc(q)}") ?: return emptyList()
        return runCatching {
            val arr = JSONArray(text)
            (0 until minOf(arr.length(), 15)).mapNotNull { arr.getJSONObject(it).optJSONObject("show") }.map { parseShow(it) }
        }.getOrDefault(emptyList())
    }

    suspend fun seriesToday(): List<WatchItem> {
        val text = get("$TVMAZE/schedule?country=US") ?: return emptyList()
        return runCatching {
            val arr = JSONArray(text)
            (0 until arr.length()).mapNotNull { arr.getJSONObject(it).optJSONObject("show") }
                .map { parseShow(it) }.distinctBy { it.key }.take(14)
        }.getOrDefault(emptyList())
    }

    private suspend fun seriesDetails(id: Long): Triple<List<String>, Pair<Int, Int>, Long>? {
        val text = get("$TVMAZE/shows/$id?embed%5B%5D=episodes&embed%5B%5D=cast") ?: return null
        return runCatching {
            val o = JSONObject(text)
            val emb = o.optJSONObject("_embedded")
            val cast = emb?.optJSONArray("cast") ?: JSONArray()
            val chars = (0 until cast.length()).mapNotNull { cast.getJSONObject(it).optJSONObject("character")?.str("name") }
                .filter { it.isNotBlank() }.distinct().take(8)
            val eps = emb?.optJSONArray("episodes") ?: JSONArray()
            val now = System.currentTimeMillis()
            var total = 0
            var aired = 0
            var next = 0L
            for (i in 0 until eps.length()) {
                val e = eps.getJSONObject(i)
                val type = e.str("type")
                if (type.isNotBlank() && type != "regular") continue
                total++
                val at = parseIso(e.str("airstamp"))
                if (at in 1..now) aired++ else if (at > now && next == 0L) next = at
            }
            Triple(chars, total to aired, next)
        }.getOrNull()
    }

    private suspend fun seriesArabicTitle(id: Long): String {
        val text = get("$TVMAZE/shows/$id/akas") ?: return ""
        return runCatching {
            val arr = JSONArray(text)
            (0 until arr.length()).map { arr.getJSONObject(it).str("name") }
                .firstOrNull { n -> n.any { it in '؀'..'ۿ' } }.orEmpty()
        }.getOrDefault("")
    }

    // ---------- إغناء وتحديث ----------
    /** يكمل الشخصيات وعدد الحلقات المعروضة (عند الإضافة). */
    suspend fun enrich(item: WatchItem): WatchItem {
        if (item.kind == "anime") {
            val chars = animeCharacters(item.sid)
            delay(400)
            val state = animeEpisodeState(item.sid)
            return item.copy(
                characters = chars.ifEmpty { item.characters },
                aired = state?.first ?: item.aired,
                nextAt = state?.second ?: item.nextAt
            )
        }
        val d = seriesDetails(item.sid)
        val ar = seriesArabicTitle(item.sid)
        return item.copy(
            characters = d?.first?.ifEmpty { null } ?: item.characters,
            total = d?.second?.first ?: item.total,
            aired = d?.second?.second ?: item.aired,
            nextAt = d?.third ?: item.nextAt,
            titleAlt = ar.ifBlank { item.titleAlt }
        )
    }

    /** تحديث دوري لعنصر محفوظ مع الحفاظ على حقول المستخدم. */
    suspend fun refresh(item: WatchItem): WatchItem? {
        if (item.kind == "anime") {
            val text = get("$JIKAN/anime/${item.sid}") ?: return null
            val o = runCatching { JSONObject(text).optJSONObject("data") }.getOrNull() ?: return null
            val base = parseAnime(o)
            delay(400)
            val state = animeEpisodeState(item.sid)
            return item.copy(
                total = base.total.takeIf { it > 0 } ?: item.total,
                airing = base.airing,
                aired = maxOf(state?.first ?: 0, if (base.airing == "finished") base.total else 0, item.aired),
                nextAt = state?.second ?: 0L
            )
        }
        val text = get("$TVMAZE/shows/${item.sid}") ?: return null
        val base = runCatching { parseShow(JSONObject(text)) }.getOrNull() ?: return null
        val d = seriesDetails(item.sid)
        return item.copy(
            airing = base.airing,
            total = d?.second?.first ?: item.total,
            aired = maxOf(d?.second?.second ?: 0, item.aired),
            nextAt = d?.third ?: 0L
        )
    }
}
