package feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ProfileFields
import core.design.ScreenScaffold
import core.design.goalKeys
import core.design.teamKeys
import data.local.LocalStore

@Composable
fun RegisterScreen(onBack: () -> Unit, onDone: () -> Unit) {
    val store = LocalStore.current
    var name by remember { mutableStateOf(store.profileName) }
    var age by remember { mutableStateOf(if (store.profileAge > 0) store.profileAge.toString() else "") }
    var goal by remember { mutableStateOf(store.profileGoal.ifBlank { goalKeys.first() }) }
    var team by remember { mutableStateOf(store.profileTeam.ifBlank { teamKeys.first() }) }

    val ageValue = age.toIntOrNull()
    val valid = name.isNotBlank() && ageValue != null && ageValue in 10..99

    ScreenScaffold(
        title = stringResource(R.string.register_title),
        onBack = onBack
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.mentor),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(78.dp).clip(CircleShape)
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.register_mentor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.register_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileFields(
                        name = name,
                        onName = { name = it },
                        age = age,
                        onAge = { age = it },
                        goal = goal,
                        onGoal = { goal = it },
                        team = team,
                        onTeam = { team = it }
                    )
                }
            }

            Text(
                stringResource(R.string.register_local_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    ageValue?.let {
                        store.saveProfile(name, it, goal, team)
                        onDone()
                    }
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.register_save))
            }

            if (!valid && (name.isNotBlank() || age.isNotBlank())) {
                Text(
                    stringResource(R.string.register_invalid),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
