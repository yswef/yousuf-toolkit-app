package feature.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousefalhmzy.ydimension.R

private val teams = listOf(
    R.string.team_black_bull,
    R.string.team_golden_dawn,
    R.string.team_silver_eagle,
    R.string.team_crimson_lion,
    R.string.team_blue_rose,
    R.string.team_green_mantis,
    R.string.team_coral_peacock,
    R.string.team_purple_orca,
    R.string.team_aqua_deer,
    R.string.team_royal_knights
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_game)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Text(
                    stringResource(R.string.teams_title),
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(teams) { team ->
                ListItem(headlineContent = { Text(stringResource(team)) })
            }
        }
    }
}
