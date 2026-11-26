// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.config

import android.content.Context
import android.util.Log
import java.io.File
import org.json.JSONObject

data class SisterApp(val name: String, val url: String, val icon: String = "")

data class AppConfig(
    val energyMax: Int,
    val energyCost: Int,
    val restGain: Int,
    val pointsTask: Int,
    val pointsWorkout: Int,
    val pointsWater: Int,
    val pointsFaith: Int,
    val pointsLearn: Int,
    val pointsReading: Int,
    val pointsSleep: Int,
    val xpPerLevel: Int,
    val waterGoal: Int,
    val sleepGoal: Int,
    val apiBase: String,
    val latestReleaseUrl: String,
    val shieldCost: Int = 50,
    val freezeCost: Int = 30,
    val weeklyRaidTarget: Int = 7,
    val monthlyChallengeTarget: Int = 30,
    val ranks: List<Pair<String, Int>> = emptyList(),
    val developerName: String = "",
    val developerEmail: String = "",
    val sisterApps: List<SisterApp> = emptyList()
) {
    companion object {
        private fun assetText(context: Context, name: String): String {
            val updated = File(context.filesDir, "content/$name")
            if (updated.isFile && updated.length() > 0) return updated.bufferedReader().use { it.readText() }
            return context.assets.open(name).bufferedReader().use { it.readText() }
        }

        fun load(context: Context): AppConfig = try {
            val text = assetText(context, "app.json")
            val root = JSONObject(text)
            val energy = root.getJSONObject("energy")
            val points = root.getJSONObject("points")
            val levels = root.getJSONObject("levels")
            val daily = root.optJSONObject("daily") ?: JSONObject()
            val updater = root.optJSONObject("updater") ?: JSONObject()
            val game = root.optJSONObject("game") ?: JSONObject()
            val ranks = ArrayList<Pair<String, Int>>()
            val rankArray = game.optJSONArray("ranks")
            if (rankArray != null) {
                for (i in 0 until rankArray.length()) {
                    val item = rankArray.optJSONObject(i) ?: continue
                    val key = item.optString("key")
                    if (key.isNotBlank()) ranks.add(key to item.optInt("level", 1))
                }
            }
            val developer = root.optJSONObject("developer")
            val sisterArray = root.optJSONArray("sister_apps")
            val sisters = ArrayList<SisterApp>()
            if (sisterArray != null) {
                for (i in 0 until sisterArray.length()) {
                    val item = sisterArray.optJSONObject(i) ?: continue
                    val name = item.optString("name")
                    if (name.isNotBlank()) sisters.add(SisterApp(name, item.optString("url"), item.optString("icon", "")))
                }
            }
            val env = runCatching {
                JSONObject(assetText(context, "environments.json"))
            }.getOrNull() ?: JSONObject()
            AppConfig(
                energyMax = energy.getInt("max"),
                energyCost = energy.getInt("costPerTask"),
                restGain = energy.getInt("restGain"),
                pointsTask = points.getInt("task"),
                pointsWorkout = points.getInt("workout"),
                pointsWater = points.getInt("water"),
                pointsFaith = points.optInt("faith", 0),
                pointsLearn = points.optInt("learn", 0),
                pointsReading = points.optInt("reading", 0),
                pointsSleep = points.optInt("sleep", 0),
                xpPerLevel = levels.getInt("xpPerLevel"),
                waterGoal = daily.optInt("waterGoal", 0),
                sleepGoal = daily.optInt("sleepGoalHours", 0),
                apiBase = env.optString("apiBaseUrl", ""),
                latestReleaseUrl = updater.optString("latestReleaseUrl", ""),
                shieldCost = game.optInt("shieldCost", 50),
                freezeCost = game.optInt("freezeCost", 30),
                weeklyRaidTarget = game.optInt("weeklyRaidTarget", 7),
                monthlyChallengeTarget = game.optInt("monthlyChallengeTarget", 30),
                ranks = ranks.sortedBy { it.second },
                developerName = developer?.optString("name", "") ?: "",
                developerEmail = developer?.optString("email", "") ?: "",
                sisterApps = sisters
            )
        } catch (t: Throwable) {
            Log.e("AppConfig", "Failed to load app.json", t)
            AppConfig(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, "", "")
        }
    }
}

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
