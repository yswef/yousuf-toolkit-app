package feature.watch

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** عنصر في سجل المتابعة (أنمي أو مسلسل). */
data class WatchItem(
    val key: String,            // anime:123 أو tv:456
    val kind: String,           // anime | series
    val sid: Long,
    val title: String,          // العنوان الرئيسي (الإنجليزي)
    val titleAlt: String,       // العنوان بلغة ثانية (الياباني أو العربي إن وجد)
    val titleOrig: String,      // العنوان الأصلي/اللاتيني
    val poster: String,
    val synopsis: String,
    val characters: List<String>,
    val year: Int,
    val total: Int,             // عدد الحلقات المعروفة (0 = غير معروف)
    val airing: String,         // airing | finished | upcoming
    val aired: Int,             // الحلقات التي عُرضت فعلاً
    val notified: Int,          // آخر حلقة أُشعر بها
    val nextAt: Long,           // موعد الحلقة القادمة (ms) أو 0
    val status: String,         // watching | plan | completed | dropped
    val watched: Int,
    val addedAt: Long,
    val url: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("key", key); put("kind", kind); put("sid", sid)
        put("title", title); put("titleAlt", titleAlt); put("titleOrig", titleOrig)
        put("poster", poster); put("synopsis", synopsis)
        put("characters", JSONArray(characters))
        put("year", year); put("total", total); put("airing", airing)
        put("aired", aired); put("notified", notified); put("nextAt", nextAt)
        put("status", status); put("watched", watched); put("addedAt", addedAt); put("url", url)
    }

    companion object {
        fun fromJson(o: JSONObject): WatchItem {
            val chars = ArrayList<String>()
            o.optJSONArray("characters")?.let { a -> for (i in 0 until a.length()) chars.add(a.optString(i)) }
            return WatchItem(
                key = o.optString("key"), kind = o.optString("kind"), sid = o.optLong("sid"),
                title = o.optString("title"), titleAlt = o.optString("titleAlt"), titleOrig = o.optString("titleOrig"),
                poster = o.optString("poster"), synopsis = o.optString("synopsis"), characters = chars,
                year = o.optInt("year"), total = o.optInt("total"), airing = o.optString("airing", "upcoming"),
                aired = o.optInt("aired"), notified = o.optInt("notified"), nextAt = o.optLong("nextAt"),
                status = o.optString("status", "plan"), watched = o.optInt("watched"),
                addedAt = o.optLong("addedAt"), url = o.optString("url")
            )
        }
    }
}

/** تخزين قائمة المتابعة في ملف مستقل (لا يتعارض مع Store). */
object WatchRepo {
    val items = mutableStateListOf<WatchItem>()
    private var loaded = false
    private var file: File? = null

    @Synchronized
    fun init(context: Context) {
        if (loaded) return
        val f = File(context.applicationContext.filesDir, "watchlist.json")
        file = f
        loaded = true
        runCatching {
            if (f.exists()) {
                val arr = JSONArray(f.readText())
                for (i in 0 until arr.length()) items.add(WatchItem.fromJson(arr.getJSONObject(i)))
            }
        }
    }

    fun find(key: String): WatchItem? = items.firstOrNull { it.key == key }

    @Synchronized
    fun upsert(item: WatchItem) {
        val index = items.indexOfFirst { it.key == item.key }
        if (index >= 0) items[index] = item else items.add(0, item)
        save()
    }

    @Synchronized
    fun remove(key: String) {
        items.removeAll { it.key == key }
        save()
    }

    private fun save() {
        val f = file ?: return
        runCatching {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            f.writeText(arr.toString())
        }
    }
}
