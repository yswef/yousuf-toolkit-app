package core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFields(
    name: String,
    onName: (String) -> Unit,
    age: String,
    onAge: (String) -> Unit,
    goal: String,
    onGoal: (String) -> Unit,
    team: String,
    onTeam: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onName,
        label = { Text(stringResource(R.string.register_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = age,
        onValueChange = onAge,
        label = { Text(stringResource(R.string.register_age)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        stringResource(R.string.register_goal),
        style = MaterialTheme.typography.labelLarge
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(goalKeys, key = { it }) { key ->
            FilterChip(
                selected = goal == key,
                onClick = { onGoal(key) },
                label = { Text(goalLabel(key)) }
            )
        }
    }
    Text(
        stringResource(R.string.register_team),
        style = MaterialTheme.typography.labelLarge
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        teamKeys.chunked(2).forEach { pair ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { key ->
                    FilterChip(
                        selected = team == key,
                        onClick = { onTeam(key) },
                        label = { Text(teamLabel(key), maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
