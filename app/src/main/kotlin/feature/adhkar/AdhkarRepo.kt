package feature.adhkar

import android.content.Context
import org.json.JSONArray

data class Dhikr(
    val id: Int,
    val text: String,
    val count: Int
)

data class DhikrCategory(
    val id: Int,
    val title: String,
    val items: List<Dhikr>
)

object AdhkarRepo {
    fun load(context: Context): List<DhikrCategory> = runCatching {
        val text = context.assets.open("adhkar/ar.json").bufferedReader().use { it.readText() }
        val root = JSONArray(text)
        val out = ArrayList<DhikrCategory>(root.length())
        for (i in 0 until root.length()) {
            val category = root.optJSONObject(i) ?: continue
            val items = ArrayList<Dhikr>()
            val array = category.optJSONArray("array") ?: JSONArray()
            for (j in 0 until array.length()) {
                val item = array.optJSONObject(j) ?: continue
                val body = item.optString("text")
                if (body.isNotBlank()) {
                    items.add(Dhikr(item.optInt("id", j + 1), body, item.optInt("count", 1).coerceAtLeast(1)))
                }
            }
            if (items.isNotEmpty()) {
                out.add(DhikrCategory(category.optInt("id", i + 1), category.optString("category"), items))
            }
        }
        out
    }.getOrDefault(emptyList())
}
