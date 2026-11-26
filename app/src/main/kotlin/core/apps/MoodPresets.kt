// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.apps

import core.navigation.Routes

data class AppEntry(val name: String, val packageName: String, val hidden: Boolean)

val moodLabels = listOf("all" to "الكل", "focus" to "تركيز", "calm" to "هدوء", "energy" to "نشاط", "night" to "ليل")

private val focusHidden = setOf(
    Routes.WATCH, Routes.GAME, Routes.SHOP, Routes.RAIDS, Routes.SKIN, Routes.FORUM
)
private val calmHidden = setOf(
    Routes.WORKOUTS, Routes.RAIDS, Routes.ALARM, Routes.DEVICE, Routes.CLEANER, Routes.TASKSPRO
)
private val energyHidden = setOf(
    Routes.SLEEP, Routes.ADHKAR, Routes.JOURNAL, Routes.WATCH
)
private val nightHidden = setOf(
    Routes.WORKOUTS, Routes.CLEANER, Routes.DEVICE, Routes.DEVMODE, Routes.TESTS
)

fun moodHiddenRoutes(mood: String): Set<String> = when (mood) {
    "focus" -> focusHidden
    "calm" -> calmHidden
    "energy" -> energyHidden
    "night" -> nightHidden
    else -> emptySet()
}

private val socialPackages = listOf(
    "instagram", "tiktok", "snapchat", "facebook", "twitter", "x.com", "whatsapp",
    "telegram", "youtube", "reddit", "linkedin", "pinterest", "discord"
)

fun isSocial(packageName: String): Boolean =
    socialPackages.any { packageName.lowercase().contains(it) }

fun moodHiddenPackages(mood: String, packages: List<String>): Set<String> = when (mood) {
    "focus", "night" -> packages.filter { isSocial(it) }.toSet()
    else -> emptySet()
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
