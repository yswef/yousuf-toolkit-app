package feature.cleaner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.InfoCard
import core.design.ScreenScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@Composable
fun CleanerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var files by remember { mutableIntStateOf(0) }
    var bytes by remember { mutableLongStateOf(0L) }
    var scanned by remember { mutableStateOf(false) }

    fun roots(): List<File> = listOfNotNull(
        context.cacheDir,
        context.codeCacheDir,
        context.externalCacheDir
    ).distinct()

    ScreenScaffold(title = stringResource(R.string.feature_cleaner), onBack = onBack) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(text = stringResource(R.string.cleaner_desc))
            if (busy) {
                CircularProgressIndicator(Modifier.padding(24.dp))
                Text(
                    stringResource(R.string.cleaner_scanning),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    String.format(Locale.getDefault(), "%.1f", bytes / 1048576.0),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    stringResource(R.string.cleaner_freed, String.format(Locale.getDefault(), "%.1f", bytes / 1048576.0)),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (scanned) {
                    Text(
                        stringResource(R.string.cleaner_found, files),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = {
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            var count = 0
                            var size = 0L
                            roots().forEach { root ->
                                root.walkTopDown().forEach { file ->
                                    if (file.isFile) {
                                        count++
                                        size += file.length()
                                    }
                                }
                            }
                            count to size
                        }
                        files = result.first
                        bytes = result.second
                        busy = false
                        scanned = true
                    }
                },
                enabled = !busy,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text(stringResource(R.string.cleaner_scan)) }
            OutlinedButton(
                onClick = {
                    busy = true
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            roots().forEach { root ->
                                root.listFiles()?.forEach { it.deleteRecursively() }
                            }
                        }
                        files = 0
                        bytes = 0L
                        busy = false
                        scanned = true
                    }
                },
                enabled = !busy && bytes > 0L,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text(stringResource(R.string.cleaner_clean)) }
            Text(
                stringResource(R.string.cleaner_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
