// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.dev

import android.content.Context
import data.local.Store
import org.json.JSONObject
import java.io.File

data class DevTest(
    val id: String,
    val route: String,
    val title: String,
    val section: String,
    val dataKey: String,
    val action: String,
    val enabled: Boolean
)

data class ReleaseInfo(
    val version: String,
    val code: Int,
    val date: String,
    val features: List<String>,
    val fixes: List<String>,
    val notes: String
)

data class ReleaseDiff(
    val current: String,
    val previous: String,
    val added: List<String>,
    val removed: List<String>,
    val fixesAdded: List<String>,
    val currentNotes: String,
    val previousNotes: String
)

object DevTests {

    private fun text(context: Context, name: String): String {
        val updated = File(context.filesDir, "content/$name")
        if (updated.isFile && updated.length() > 0) return updated.bufferedReader().use { it.readText() }
        return context.assets.open(name).bufferedReader().use { it.readText() }
    }

    fun tests(context: Context): List<DevTest> = runCatching {
        val root = JSONObject(text(context, "devmode_tests.json"))
        val arr = root.optJSONArray("tests")
        val out = ArrayList<DevTest>()
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                out.add(
                    DevTest(
                        id = o.optString("id"),
                        route = o.optString("route"),
                        title = o.optString("title"),
                        section = o.optString("section"),
                        dataKey = o.optString("data_key"),
                        action = o.optString("action"),
                        enabled = o.optBoolean("enabled", true)
                    )
                )
            }
        }
        out
    }.getOrDefault(emptyList())

    fun releases(context: Context): List<ReleaseInfo> = runCatching {
        val root = JSONObject(text(context, "releases.json"))
        val arr = root.optJSONArray("versions")
        val out = ArrayList<ReleaseInfo>()
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                out.add(
                    ReleaseInfo(
                        version = o.optString("version"),
                        code = o.optInt("code"),
                        date = o.optString("date"),
                        features = o.optJSONArray("features").stringList(),
                        fixes = o.optJSONArray("fixes").stringList(),
                        notes = o.optString("notes")
                    )
                )
            }
        }
        out
    }.getOrDefault(emptyList())

    fun diff(context: Context, currentVersion: String): ReleaseDiff {
        val all = releases(context)
        val index = all.indexOfFirst { it.version == currentVersion }.let { if (it >= 0) it else all.lastIndex }
        val current = all.getOrNull(index)
        val previous = all.getOrNull(index - 1)
        val currentFeatures = current?.features ?: emptyList()
        val previousFeatures = previous?.features ?: emptyList()
        return ReleaseDiff(
            current = current?.version ?: currentVersion,
            previous = previous?.version ?: "—",
            added = currentFeatures.filterNot { it in previousFeatures },
            removed = previousFeatures.filterNot { it in currentFeatures },
            fixesAdded = (current?.fixes ?: emptyList()).filterNot { it in (previous?.fixes ?: emptyList()) },
            currentNotes = current?.notes ?: "",
            previousNotes = previous?.notes ?: ""
        )
    }

    fun dataValue(store: Store, key: String, version: String): String = when (key) {
        "tasks" -> "مهام: ${store.tasks.size} · منجز: ${store.tasks.count { it.done }}"
        "ledger" -> "عمليات: ${store.ledger.size} · محافظ: ${store.wallets.size}"
        "workouts" -> "تمارين: ${store.workouts.size}"
        "journal" -> "يوميات: ${store.journal.size}"
        "water" -> "أيام ماء: ${store.water.size}"
        "sleep" -> "سجلات نوم: ${store.sleep.size}"
        "faithDays" -> "أيام إيمان: ${store.faithDaysCount}"
        "learn" -> "دروس: ${store.learn.size}"
        "books" -> "كتب: ${store.books.size}"
        "points" -> "نقاط: ${store.points} · XP: ${store.xp}"
        "campaigns" -> "حملات: ${store.campaigns.size}"
        "profileName" -> "الاسم: ${store.profileName.ifBlank { "—" }}"
        "accent" -> "اللون: ${store.accent} · الأفاتار: ${store.avatar}"
        "serverUrl" -> "الخادم: ${store.serverUrl}"
        "accounts" -> "حسابات محلية: ${store.accounts.size}"
        "licenseCode" -> "ترخيص: ${store.licenseCode.ifBlank { "—" }}"
        "version" -> "الإصدار: $version"
        "authToken" -> "الجلسة: ${if (store.authToken.isBlank()) "غير مسجّل" else "نشطة"}"
        "accountDevices" -> "أجهزة مسجّلة: ${store.accountDevices.size}"
        "hiddenApps" -> "تطبيقات مخفية: ${store.hiddenApps.size}"
        "hiddenRoutes" -> "كتب/أدوات مخفية: ${store.hiddenRoutes.size}"
        "screenshots" -> "وسائط محفوظة: ${store.screenshots.size}"
        "gitRepo" -> "مستودع: ${store.gitOwner}/${store.gitRepo}"
        "vault" -> "خزنة: ${store.vault.size}"
        "wallets" -> "محافظ: ${store.wallets.size}"
        "notifications" -> "إشعارات: ${store.notifications.size}"
        "blockedApps" -> "محجوب: ${store.blockedApps.size}"
        "xp" -> "المستوى: ${store.level} · XP: ${store.xp}"
        "level" -> "المستوى: ${store.level} · تقدّم: ${(store.levelProgress * 100).toInt()}%"
        "sound" -> "الصوتيات: ${if (core.design.SoundFx.enabled) "مفعّلة" else "مكتومة"}"
        "onboarded" -> "التهيئة: ${if (store.onboarded) "مكتملة" else "لم تتم"}"
        "routes" -> "مسارات: ${core.navigation.Routes::class.java.fields.size}"
        "prayers" -> "أيام صلاة: ${store.prayers.size}"
        "extras" -> "سجلات إضافية: ${store.extras.size}"
        "accountRelations" -> "علاقات الحساب: ${store.accountRelations.size}"
        "developerRole" -> "صلاحية المطور: ${store.developerRole.ifBlank { "—" }}"
        else -> runCatching {
            val root = JSONObject(store.devSnapshot())
            if (root.has(key)) {
                when (val value = root.opt(key)) {
                    is org.json.JSONArray -> "$key: ${value.length()} عنصراً"
                    is JSONObject -> "$key: ${value.length()} حقلاً"
                    else -> "$key: $value"
                }
            } else {
                "بيانات التطبيق: نقاط ${store.points} · مهام ${store.tasks.size} · جلسة ${if (store.authToken.isBlank()) "غير مسجّل" else "نشطة"}"
            }
        }.getOrDefault("—")
    }

    private fun org.json.JSONArray?.stringList(): List<String> {
        if (this == null) return emptyList()
        val out = ArrayList<String>(length())
        for (i in 0 until length()) out.add(optString(i))
        return out
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
