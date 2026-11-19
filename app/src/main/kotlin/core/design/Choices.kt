package core.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yousuf.toolkit.R

val teamKeys = listOf(
    "black_bull",
    "golden_dawn",
    "silver_eagle",
    "crimson_lion",
    "blue_rose",
    "green_mantis",
    "coral_peacock",
    "purple_orca",
    "aqua_deer",
    "royal_knights"
)

val goalKeys = listOf("time", "health", "money", "learning", "faith")

@Composable
fun teamLabel(key: String): String = when (key) {
    "black_bull" -> stringResource(R.string.team_black_bull)
    "golden_dawn" -> stringResource(R.string.team_golden_dawn)
    "silver_eagle" -> stringResource(R.string.team_silver_eagle)
    "crimson_lion" -> stringResource(R.string.team_crimson_lion)
    "blue_rose" -> stringResource(R.string.team_blue_rose)
    "green_mantis" -> stringResource(R.string.team_green_mantis)
    "coral_peacock" -> stringResource(R.string.team_coral_peacock)
    "purple_orca" -> stringResource(R.string.team_purple_orca)
    "aqua_deer" -> stringResource(R.string.team_aqua_deer)
    else -> stringResource(R.string.team_royal_knights)
}

@Composable
fun goalLabel(key: String): String = when (key) {
    "time" -> stringResource(R.string.goal_time)
    "health" -> stringResource(R.string.goal_health)
    "money" -> stringResource(R.string.goal_money)
    "learning" -> stringResource(R.string.goal_learning)
    else -> stringResource(R.string.goal_faith)
}
