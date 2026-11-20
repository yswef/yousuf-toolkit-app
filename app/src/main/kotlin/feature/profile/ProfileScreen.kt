package feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.ProfileFields
import core.design.activityLabel
import core.design.ageFromBirthDate
import core.design.genderLabel
import core.design.ScreenScaffold
import core.design.goalKeys
import core.design.goalLabel
import core.design.teamKeys
import core.design.teamLabel
import data.local.LocalStore
import data.local.Store
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    var editing by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = stringResource(R.string.feature_profile),
        onBack = onBack,
        actions = {
            TextButton(onClick = { editing = true }) {
                Text(stringResource(R.string.profile_edit))
            }
        }
    ) { padding ->
        if (!store.hasProfile) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.profile_none),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HeaderCard(store)
                DetailCard(store)
            }
        }
    }

    if (editing) {
        EditDialog(store, onClose = { editing = false })
    }
}

@Composable
private fun HeaderCard(store: Store) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF7B2FF7), Color(0xFF3A0B7A), Color(0xFF0F6E75))))
            .border(1.dp, Color(0xFFFFD166).copy(alpha = 0.45f), shape)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.mentor),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(86.dp).clip(CircleShape).border(2.dp, Color(0xFFFFD166), CircleShape)
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                store.profileName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                stringResource(R.string.profile_hero),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFD166)
            )
            Text(
                stringResource(R.string.home_level, store.level),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                stringResource(R.string.home_points, store.points),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun DetailCard(store: Store) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            InfoRow(stringResource(R.string.profile_age_label), stringResource(R.string.profile_age_value, store.computedAge))
            if (store.profileBirthDate.isNotBlank()) {
                HorizontalDivider()
                InfoRow(stringResource(R.string.profile_birth_date), formatBirthDate(store.profileBirthDate))
            }
            if (store.profileHeightCm > 0) {
                HorizontalDivider()
                InfoRow(stringResource(R.string.profile_height), stringResource(R.string.profile_value_cm, store.profileHeightCm))
            }
            if (store.profileWeightKg > 0.0) {
                HorizontalDivider()
                InfoRow(stringResource(R.string.profile_weight), stringResource(R.string.profile_value_kg, formatNumber(store.profileWeightKg)))
            }
            if (store.bmi > 0.0) {
                HorizontalDivider()
                InfoRow(stringResource(R.string.profile_bmi), stringResource(R.string.profile_value_bmi, formatNumber(store.bmi)))
            }
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_gender), genderLabel(store.profileGender.ifBlank { "unspecified" }))
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_activity), activityLabel(store.profileActivity.ifBlank { "medium" }))
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_goal_label), goalLabel(store.profileGoal.ifBlank { goalKeys.first() }))
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_team_label), teamLabel(store.profileTeam.ifBlank { teamKeys.first() }))
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_joined), joinedText(store.joinedAt))
            HorizontalDivider()
            InfoRow(stringResource(R.string.profile_energy_label), stringResource(R.string.home_energy, store.energy, store.config.energyMax))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EditDialog(store: Store, onClose: () -> Unit) {
    var name by remember { mutableStateOf(store.profileName) }
    var age by remember { mutableStateOf(if (store.profileAge > 0) store.profileAge.toString() else "") }
    var birthDate by remember { mutableStateOf(store.profileBirthDate) }
    var height by remember { mutableStateOf(if (store.profileHeightCm > 0) store.profileHeightCm.toString() else "") }
    var weight by remember { mutableStateOf(if (store.profileWeightKg > 0.0) store.profileWeightKg.toString() else "") }
    var gender by remember { mutableStateOf(store.profileGender.ifBlank { "unspecified" }) }
    var activity by remember { mutableStateOf(store.profileActivity.ifBlank { "medium" }) }
    var goal by remember { mutableStateOf(store.profileGoal.ifBlank { goalKeys.first() }) }
    var team by remember { mutableStateOf(store.profileTeam.ifBlank { teamKeys.first() }) }
    val ageValue = ageFromBirthDate(birthDate) ?: age.toIntOrNull()
    val valid = name.isNotBlank() && ageValue != null && ageValue in 10..99

    FormDialog(
        title = stringResource(R.string.profile_edit),
        confirm = stringResource(R.string.save),
        onDismiss = onClose,
        onConfirm = {
            ageValue?.let {
                store.saveProfile(
                    name = name,
                    age = it,
                    goal = goal,
                    team = team,
                    birthDate = birthDate,
                    heightCm = height.toIntOrNull() ?: 0,
                    weightKg = weight.toDoubleOrNull() ?: 0.0,
                    gender = gender,
                    activity = activity
                )
            }
            onClose()
        },
        valid = valid
    ) {
        ProfileFields(
            name = name,
            onName = { name = it },
            age = age,
            onAge = { age = it },
            goal = goal,
            onGoal = { goal = it },
            team = team,
            onTeam = { team = it },
            birthDate = birthDate,
            onBirthDate = { birthDate = it },
            height = height,
            onHeight = { height = it },
            weight = weight,
            onWeight = { weight = it },
            gender = gender,
            onGender = { gender = it },
            activity = activity,
            onActivity = { activity = it }
        )
    }
}

private fun formatBirthDate(value: String): String =
    runCatching {
        java.time.LocalDate.parse(value).format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }.getOrDefault(value)

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(java.util.Locale.US, "%.1f", value)

private fun joinedText(at: Long): String =
    if (at <= 0L) "-" else Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()
