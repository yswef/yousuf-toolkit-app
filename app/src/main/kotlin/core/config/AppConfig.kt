package core.config

import android.content.Context
import android.util.Log
import org.json.JSONObject

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
    val latestReleaseUrl: String
) {
    companion object {
        fun load(context: Context): AppConfig = try {
            val text = context.assets.open("app.json").bufferedReader().use { it.readText() }
            val root = JSONObject(text)
            val energy = root.getJSONObject("energy")
            val points = root.getJSONObject("points")
            val levels = root.getJSONObject("levels")
            val daily = root.optJSONObject("daily") ?: JSONObject()
            val updater = root.optJSONObject("updater") ?: JSONObject()
            val env = runCatching {
                JSONObject(context.assets.open("environments.json").bufferedReader().use { it.readText() })
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
                latestReleaseUrl = updater.optString("latestReleaseUrl", "")
            )
        } catch (t: Throwable) {
            Log.e("AppConfig", "Failed to load app.json", t)
            AppConfig(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, "", "")
        }
    }
}
