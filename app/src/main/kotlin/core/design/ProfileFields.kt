package core.design

import android.app.DatePickerDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

val genderKeys = listOf("male", "female", "unspecified")
val activityKeys = listOf("low", "medium", "high")

fun ageFromBirthDate(value: String): Int? =
    if (value.isBlank()) null else runCatching {
        Period.between(LocalDate.parse(value), LocalDate.now()).years
    }.getOrNull()

private val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")

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
    onTeam: (String) -> Unit,
    birthDate: String = "",
    onBirthDate: (String) -> Unit = {},
    height: String = "",
    onHeight: (String) -> Unit = {},
    weight: String = "",
    onWeight: (String) -> Unit = {},
    gender: String = "",
    onGender: (String) -> Unit = {},
    activity: String = "",
    onActivity: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val computedAge = ageFromBirthDate(birthDate)
    val initial = runCatching { LocalDate.parse(birthDate) }.getOrElse { LocalDate.of(2000, 1, 1) }

    fun openPicker() {
        DatePickerDialog(
            context,
            { _, year, month, day -> onBirthDate(LocalDate.of(year, month + 1, day).toString()) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    OutlinedTextField(
        value = name,
        onValueChange = onName,
        label = { Text(stringResource(R.string.register_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedButton(onClick = { openPicker() }, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (birthDate.isBlank()) stringResource(R.string.register_birth_hint)
            else stringResource(R.string.register_birth_date) + ": " + initial.format(dateFormat)
        )
    }
    if (computedAge != null) {
        Text(
            stringResource(R.string.register_age_computed, computedAge),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        OutlinedTextField(
            value = age,
            onValueChange = onAge,
            label = { Text(stringResource(R.string.register_age_legacy)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = height,
            onValueChange = onHeight,
            label = { Text(stringResource(R.string.register_height)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = weight,
            onValueChange = onWeight,
            label = { Text(stringResource(R.string.register_weight)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
    }
    Text(stringResource(R.string.register_gender), style = MaterialTheme.typography.labelLarge)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(genderKeys, key = { it }) { key ->
            FilterChip(
                selected = (gender.ifBlank { "unspecified" }) == key,
                onClick = { onGender(key) },
                label = { Text(genderLabel(key)) }
            )
        }
    }
    Text(stringResource(R.string.register_activity), style = MaterialTheme.typography.labelLarge)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(activityKeys, key = { it }) { key ->
            FilterChip(
                selected = (activity.ifBlank { "medium" }) == key,
                onClick = { onActivity(key) },
                label = { Text(activityLabel(key)) }
            )
        }
    }
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

@Composable
fun genderLabel(key: String): String = when (key) {
    "male" -> stringResource(R.string.gender_male)
    "female" -> stringResource(R.string.gender_female)
    else -> stringResource(R.string.gender_unspecified)
}

@Composable
fun activityLabel(key: String): String = when (key) {
    "low" -> stringResource(R.string.activity_low)
    "high" -> stringResource(R.string.activity_high)
    else -> stringResource(R.string.activity_medium)
}
