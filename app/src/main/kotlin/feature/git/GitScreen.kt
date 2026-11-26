// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package feature.git

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import core.design.InfoTone
import core.design.ScreenScaffold
import core.design.V3Chip
import core.git.GitBranch
import core.git.GitHub
import core.git.LocalGit
import core.git.LocalRepoInfo
import data.local.LocalStore
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

@Composable
fun GitScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = LocalStore.current
    val scope = rememberCoroutineScope()
    var token by remember { mutableStateOf(store.gitToken) }
    var owner by remember { mutableStateOf(store.gitOwner) }
    var repoName by remember { mutableStateOf(store.gitRepo) }
    var tab by remember { mutableStateOf(0) }
    var output by remember { mutableStateOf("") }
    var branches by remember { mutableStateOf<List<GitBranch>>(emptyList()) }
    var base by remember { mutableStateOf("dev") }
    var newest by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    var localRepos by remember { mutableStateOf<List<LocalRepoInfo>>(emptyList()) }
    var releases by remember { mutableStateOf<List<core.git.GitRelease>>(emptyList()) }
    var command by remember { mutableStateOf("git status") }
    var mergeHead by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun log(text: String) { output = text + "\n" + output }
    fun run(block: suspend () -> String) {
        if (token.isBlank()) { log(context.getString(R.string.git_no_token)); return }
        busy = true
        scope.launch {
            val text = runCatching { block() }.getOrElse { it.message ?: "error" }
            log(text)
            busy = false
        }
    }

    ScreenScaffold(title = stringResource(R.string.git_title), onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text(stringResource(R.string.git_owner)) }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = repoName, onValueChange = { repoName = it }, label = { Text(stringResource(R.string.git_repo)) }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text(stringResource(R.string.git_token)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { store.saveGit(token, owner, repoName); log("saved") }) { Text(stringResource(R.string.git_save)) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                V3Chip(stringResource(R.string.git_local), tab == 0, { tab = 0 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.git_remote), tab == 1, { tab = 1 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.git_commands), tab == 2, { tab = 2 }, Modifier.weight(1f))
                V3Chip(stringResource(R.string.git_releases), tab == 3, { tab = 3 }, Modifier.weight(1f))
            }

            when (tab) {
                0 -> {
                    Button(onClick = { localRepos = LocalGit.scan() }) { Text(stringResource(R.string.git_local)) }
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(localRepos, key = { it.path }) { r ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(r.name, style = MaterialTheme.typography.titleSmall)
                                    Text("${r.branch} · ${r.headSha.take(8)}", style = MaterialTheme.typography.labelSmall)
                                    Text(r.path, style = MaterialTheme.typography.labelSmall)
                                    Text(stringResource(R.string.git_last_commit, java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(r.lastUpdate))), style = MaterialTheme.typography.labelSmall)
                                    if (r.description.isNotBlank()) Text(stringResource(R.string.git_description, r.description), style = MaterialTheme.typography.labelSmall)
                                    Text(r.branches.joinToString("، "), style = MaterialTheme.typography.labelSmall)
                                    if (r.dirty) Text("تغييرات غير ملتزمة", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = {
                            run {
                                val r = GitHub.repo(owner, repoName, token)
                                base = r.optString("default_branch", "dev")
                                info = "default_branch=" + base +
                                    "\nfull_name=" + r.optString("full_name") +
                                    "\ndescription=" + r.optString("description") +
                                    "\nstars=" + r.optInt("stargazers_count") +
                                    "\nforks=" + r.optInt("forks_count") +
                                    "\nlanguage=" + r.optString("language") +
                                    "\nopen_issues=" + r.optInt("open_issues_count") +
                                    "\nsize=" + r.optInt("size") +
                                    "\ncreated_at=" + r.optString("created_at") +
                                    "\nupdated_at=" + r.optString("updated_at") +
                                    "\npushed_at=" + r.optString("pushed_at") +
                                    "\nlicense=" + (r.optJSONObject("license")?.optString("spdx_id") ?: "-")
                                val list = GitHub.branches(owner, repoName, token)
                                branches = list
                                var latest = ""
                                var latestDate = ""
                                list.take(12).forEach { b ->
                                    val c = runCatching { GitHub.commits(owner, repoName, b.name, token).firstOrNull() }.getOrNull()
                                    if (c != null && c.date > latestDate) { latestDate = c.date; latest = b.name }
                                }
                                newest = latest.ifBlank { base }
                                "base=$base\nnewest=$newest\nbranches=" + list.joinToString(", ") { it.name } +
                                    (if (latestDate.isNotBlank()) "\nnewest_date=$latestDate" else "")
                            }
                        }) { Text(stringResource(R.string.git_sync)) }
                        Button(onClick = {
                            run {
                                val d = GitHub.compare(owner, repoName, base, newest, token)
                                "ahead_by=${d.aheadBy} behind_by=${d.behindBy} commits=${d.totalCommits} files=${d.files}"
                            }
                        }) { Text("git diff") }
                    }
                    if (info.isNotBlank()) InfoCard(text = info, tone = InfoTone.INFO)
                    Text(stringResource(R.string.git_base) + ": " + base, style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.git_newest) + ": " + newest, style = MaterialTheme.typography.bodySmall)
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(branches, key = { it.name }) { b ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(10.dp)) {
                                    Text(b.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(b.sha.take(10) + if (b.protected) " · محمي" else "", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    OutlinedTextField(value = command, onValueChange = { command = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.git_cmd_hint)) })
                    OutlinedTextField(value = mergeHead, onValueChange = { mergeHead = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.git_from)) })
                    Button(onClick = {
                        val parts = command.trim().split(Regex("\\s+"))
                        when {
                            command.startsWith("git status") -> run {
                                val r = GitHub.repo(owner, repoName, token)
                                "branch=" + r.optString("default_branch") + " pushed_at=" + r.optString("pushed_at")
                            }
                            command.startsWith("git branch") -> run {
                                branches = GitHub.branches(owner, repoName, token)
                                branches.joinToString("\n") { (if (it.protected) "* " else "  ") + it.name + " " + it.sha.take(8) }
                            }
                            command.startsWith("git log") -> run {
                                GitHub.commits(owner, repoName, base, token)
                                    .joinToString("\n") { it.sha.take(8) + " " + it.date + " " + it.author + " " + it.message }
                            }
                            command.startsWith("git diff") -> run {
                                val head = if (parts.size >= 4) parts[3] else newest
                                val d = GitHub.compare(owner, repoName, parts.getOrNull(2) ?: base, head, token)
                                "ahead=${d.aheadBy} behind=${d.behindBy} files=${d.files}"
                            }
                            command.startsWith("git pull") -> run {
                                val target = File(context.filesDir, "git/${repoName}/${base}")
                                val n = GitHub.pull(owner, repoName, base, token, target)
                                "pulled $n files into ${target.absolutePath}"
                            }
                            command.startsWith("git push") -> run {
                                val path = parts.getOrNull(2) ?: "docs/mother-panel/MOTHER_PANEL_HANDOFF.md"
                                val local = File(context.filesDir, "git/${repoName}/${base}/$path")
                                val content = if (local.isFile) local.readText() else "# Y-Dimensions sync\n"
                                "pushed " + GitHub.push(owner, repoName, base, path, content, token)
                            }
                            command.startsWith("git merge") -> run {
                                val head = parts.getOrNull(2) ?: mergeHead
                                "merge " + GitHub.merge(owner, repoName, base, head, token)
                            }
                            command.startsWith("git release") -> run {
                                releases = GitHub.releases(owner, repoName, token)
                                releases.joinToString("\n") { it.tag + " " + it.publishedAt }
                            }
                            else -> log("أوامر مدعومة: status, branch, log, diff, pull, push, merge, release")
                        }
                    }) { Text(stringResource(R.string.git_run)) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = {
                            run { "merge " + GitHub.merge(owner, repoName, base, mergeHead, token) }
                        }, enabled = mergeHead.isNotBlank()) { Text(stringResource(R.string.git_merge)) }
                        Button(onClick = {
                            run {
                                val target = File(context.filesDir, "git/${repoName}/${base}")
                                "pulled " + GitHub.pull(owner, repoName, base, token, target) + " files"
                            }
                        }) { Text(stringResource(R.string.git_pull)) }
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            output,
                            Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                else -> {
                    Button(onClick = { run {
                        releases = GitHub.releases(owner, repoName, token)
                        releases.joinToString("\n") { it.tag + " · " + it.name }
                    } }) { Text(stringResource(R.string.git_releases)) }
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(releases, key = { it.tag }) { r ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(r.tag + " · " + r.name, style = MaterialTheme.typography.titleSmall)
                                    Text(r.publishedAt, style = MaterialTheme.typography.labelSmall)
                                    if (r.body.isNotBlank()) Text(r.body.take(200), style = MaterialTheme.typography.bodySmall)
                                    if (r.apkUrl.isNotBlank()) {
                                        OutlinedButton(onClick = {
                                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.apkUrl))) }
                                        }) { Text(stringResource(R.string.git_apk)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
