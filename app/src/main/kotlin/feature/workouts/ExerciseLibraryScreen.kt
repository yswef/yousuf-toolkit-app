package feature.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import core.design.ScreenScaffold
import feature.watch.RemoteImage

private fun metaLine(e: LibExercise): String =
    listOfNotNull(
        e.primary.firstOrNull()?.let { muscleLabels[it] ?: it },
        equipmentLabels[e.equipment] ?: e.equipment,
        levelLabels[e.level] ?: e.level
    ).joinToString(" · ")

@Composable
fun ExerciseLibraryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var all by remember { mutableStateOf<List<LibExercise>?>(null) }
    LaunchedEffect(Unit) { all = ExerciseLibrary.load(context) }

    var query by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf<String?>(null) }
    var equip by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<LibExercise?>(null) }

    val list = remember(all, query, muscle, equip, level) {
        val q = query.trim().lowercase()
        val m = muscle
        val eq = equip
        val lv = level
        all.orEmpty().filter { e ->
            (m == null || e.primary.contains(m) || e.secondary.contains(m)) &&
                (eq == null || e.equipment == eq) &&
                (lv == null || e.level == lv) &&
                (q.isEmpty() || e.name.lowercase().contains(q))
        }
    }

    ScreenScaffold(title = "مكتبة التمارين", onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("ابحث باسم التمرين (بالإنجليزية)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(key = "f-muscle") { FilterRow("العضلة", muscleLabels, muscle) { muscle = it } }
            item(key = "f-equip") { FilterRow("الأداة", equipmentLabels, equip) { equip = it } }
            item(key = "f-level") { FilterRow("المستوى", levelLabels, level) { level = it } }
            item(key = "count") {
                Text(
                    if (all == null) "جارٍ التحميل…" else "${list.size} تمرين",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(list, key = { it.id }) { ex ->
                Card(onClick = { selected = ex }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))) {
                            if (ex.images > 0) RemoteImage(ex.imageUrl(0), Modifier.fillMaxSize())
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(ex.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
                            Text(
                                metaLine(ex),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    selected?.let { ExerciseDetailSheet(it) { selected = null } }
}

@Composable
private fun FilterRow(title: String, options: Map<String, String>, selected: String?, onSelect: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item(key = "all") {
                FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("الكل") })
            }
            items(options.entries.toList(), key = { it.key }) { opt ->
                FilterChip(
                    selected = selected == opt.key,
                    onClick = { onSelect(if (selected == opt.key) null else opt.key) },
                    label = { Text(opt.value) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetailSheet(ex: LibExercise, onDismiss: () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(ex.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                listOfNotNull(
                    categoryLabels[ex.category] ?: ex.category,
                    equipmentLabels[ex.equipment] ?: ex.equipment,
                    levelLabels[ex.level] ?: ex.level
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (ex.images > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0 until minOf(ex.images, 2)) {
                        RemoteImage(
                            ex.imageUrl(i),
                            Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(10.dp)),
                            ContentScale.Crop
                        )
                    }
                }
            }
            Text(
                "العضلات الأساسية: " + ex.primary.joinToString("، ") { muscleLabels[it] ?: it },
                style = MaterialTheme.typography.bodyMedium
            )
            if (ex.secondary.isNotEmpty()) {
                Text(
                    "عضلات مساعدة: " + ex.secondary.joinToString("، ") { muscleLabels[it] ?: it },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("طريقة الأداء", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ex.steps.forEachIndexed { i, s ->
                Text("${i + 1}. $s", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "المصدر: free-exercise-db (ملكية عامة). الشرح بالإنجليزية. توقف عند أي ألم واستشر مختصاً عند الحاجة.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
