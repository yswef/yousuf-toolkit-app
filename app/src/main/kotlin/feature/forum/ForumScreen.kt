package feature.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.FormDialog
import core.design.InfoCard
import core.design.InfoTone
import core.design.LocalStore
import core.design.ScreenScaffold
import core.net.Api
import core.net.BoardPost
import core.net.BoardThread
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun ForumScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val scope = rememberCoroutineScope()
    var threads by remember { mutableStateOf<List<BoardThread>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var tick by remember { mutableIntStateOf(0) }
    var openThread by remember { mutableStateOf<BoardThread?>(null) }
    var showNew by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newBody by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<BoardPost>>(emptyList()) }
    var reply by remember { mutableStateOf("") }
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    fun loadThreads() {
        if (store.authToken.isBlank()) return
        loading = true
        error = ""
        scope.launch {
            runCatching {
                Api.threads(Api.normalizeBase(store.serverUrl), store.authToken)
            }.onSuccess { threads = it }.onFailure { error = it.message ?: "" }
            loading = false
        }
    }

    LaunchedEffect(store.authToken, tick) { loadThreads() }
    LaunchedEffect(openThread) {
        val thread = openThread ?: return@LaunchedEffect
        runCatching {
            Api.posts(Api.normalizeBase(store.serverUrl), store.authToken, thread.id)
        }.onSuccess { posts = it }.onFailure { error = it.message ?: "" }
    }

    ScreenScaffold(
        title = openThread?.title ?: stringResource(R.string.feature_forum),
        onBack = { if (openThread != null) openThread = null else onBack() },
        actions = {
            if (store.authToken.isNotBlank() && openThread == null) {
                IconButton(onClick = { showNew = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.forum_new))
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (store.authToken.isBlank()) {
                InfoCard(text = stringResource(R.string.forum_login_needed), tone = InfoTone.WARN)
                return@Column
            }
            if (loading) CircularProgressIndicator(Modifier.padding(16.dp))
            if (error.isNotBlank()) {
                InfoCard(text = error, tone = InfoTone.ERROR)
            }
            if (openThread == null) {
                TextButton(onClick = { tick++ }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(stringResource(R.string.usage_refresh))
                }
                if (threads.isEmpty() && !loading) {
                    Text(
                        stringResource(R.string.forum_empty),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                threads.forEach { thread ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(thread.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${thread.author} · ${formatter.format(Date(thread.createdAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { openThread = thread }) {
                            Text("${thread.postCount}")
                        }
                    }
                }
            } else {
                posts.forEach { post ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${post.author} · ${formatter.format(Date(post.createdAt))}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(post.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = reply,
                        onValueChange = { reply = it },
                        label = { Text(stringResource(R.string.forum_body_hint)) },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val thread = openThread
                            val body = reply.trim()
                            if (thread != null && body.isNotBlank()) {
                                scope.launch {
                                    runCatching {
                                        Api.newPost(Api.normalizeBase(store.serverUrl), store.authToken, thread.id, body)
                                    }.onSuccess {
                                        reply = ""
                                        posts = Api.posts(Api.normalizeBase(store.serverUrl), store.authToken, thread.id)
                                    }.onFailure { error = it.message ?: "" }
                                }
                            }
                        },
                        enabled = reply.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text(stringResource(R.string.forum_send)) }
                }
            }
        }
    }

    if (showNew) {
        FormDialog(
            title = stringResource(R.string.forum_new),
            confirm = stringResource(R.string.common_save),
            onDismiss = { showNew = false },
            onConfirm = {
                scope.launch {
                    runCatching {
                        Api.newThread(
                            Api.normalizeBase(store.serverUrl),
                            store.authToken,
                            newTitle.trim(),
                            newBody.trim()
                        )
                    }.onSuccess {
                        newTitle = ""
                        newBody = ""
                        showNew = false
                        tick++
                    }.onFailure { error = it.message ?: "" }
                }
            },
            valid = newTitle.isNotBlank() && newBody.isNotBlank()
        ) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text(stringResource(R.string.forum_title_hint)) },
                singleLine = true
            )
            OutlinedTextField(
                value = newBody,
                onValueChange = { newBody = it },
                label = { Text(stringResource(R.string.forum_body_hint)) },
                minLines = 3
            )
        }
    }
}
