package feature.watch

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import core.design.SoundFx
import core.design.SwipeActionRow
import core.design.rememberUndoHost
import kotlinx.coroutines.launch

private val statuses = listOf("watching", "plan", "completed", "dropped")

@Composable
private fun statusLabel(s: String) = stringResource(
    when (s) {
        "watching" -> R.string.watch_s_watching
        "plan" -> R.string.watch_s_plan
        "completed" -> R.string.watch_s_completed
        else -> R.string.watch_s_dropped
    }
)

@Composable
private fun airingLabel(s: String) = stringResource(
    when (s) {
        "airing" -> R.string.watch_airing
        "finished" -> R.string.watch_finished
        else -> R.string.watch_upcoming
    }
)

private suspend fun addHit(context: android.content.Context, hit: WatchItem, status: String): WatchItem {
    val full = WatchApi.enrich(hit)
    val done = status == "completed"
    val item = full.copy(
        status = status,
        watched = if (done) (if (full.total > 0) full.total else full.aired) else 0,
        notified = full.aired,
        addedAt = System.currentTimeMillis()
    )
    WatchRepo.upsert(item)
    WatchScheduler.schedule(context)
    return item
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    WatchRepo.init(context)
    var tab by rememberSaveable { mutableStateOf(0) }
    var kind by rememberSaveable { mutableStateOf("all") }
    var status by rememberSaveable { mutableStateOf("all") }
    var searching by remember { mutableStateOf(false) }
    var detailKey by remember { mutableStateOf<String?>(null) }
    var hit by remember { mutableStateOf<WatchItem?>(null) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        WatchChecker.runIfStale(context, minGapMs = 30 * 60 * 1000L)
    }

    ScreenScaffold(
        title = stringResource(R.string.feature_watch),
        onBack = onBack,
        actions = {
            IconButton(onClick = { searching = true }) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_hint))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.watch_tab_list)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.watch_tab_news)) })
            }
            if (tab == 0) {
                MyList(
                    kind = kind, status = status,
                    onKind = { kind = it }, onStatus = { status = it },
                    onOpen = { detailKey = it },
                    onSearch = { searching = true }
                )
            } else {
                NewsTab(onHit = { hit = it })
            }
        }
    }

    if (searching) {
        SearchSheet(onDismiss = { searching = false }, onHit = { hit = it })
    }
    hit?.let { h -> HitSheet(h) { hit = null } }
    detailKey?.let { k ->
        val item = WatchRepo.find(k)
        if (item != null) {
            DetailSheet(item) { detailKey = null }
        }
    }
}

@Composable
private fun MyList(
    kind: String,
    status: String,
    onKind: (String) -> Unit,
    onStatus: (String) -> Unit,
    onOpen: (String) -> Unit,
    onSearch: () -> Unit
) {
    val undo = rememberUndoHost()
    val all = WatchRepo.items
    val shown = all.filter { (kind == "all" || it.kind == kind) && (status == "all" || it.status == status) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { FilterChip(selected = kind == "all", onClick = { onKind("all") }, label = { Text(stringResource(R.string.watch_all)) }) }
                    item { FilterChip(selected = kind == "anime", onClick = { onKind("anime") }, label = { Text(stringResource(R.string.watch_anime)) }) }
                    item { FilterChip(selected = kind == "series", onClick = { onKind("series") }, label = { Text(stringResource(R.string.watch_series)) }) }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { FilterChip(selected = status == "all", onClick = { onStatus("all") }, label = { Text(stringResource(R.string.watch_all)) }) }
                    items(statuses) { s -> FilterChip(selected = status == s, onClick = { onStatus(s) }, label = { Text(statusLabel(s)) }) }
                }
            }
        }
        if (shown.isEmpty()) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.watch_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onSearch) { Text(stringResource(R.string.watch_search_btn)) }
                }
            }
        }
        items(shown, key = { it.key }) { item ->
            val remove = { WatchRepo.remove(item.key); undo.deleted { WatchRepo.upsert(item) } }
            SwipeActionRow(Modifier.animateItem(), onSwipeEnd = remove) {
                WatchRow(item, onOpen = { onOpen(item.key) })
            }
        }
    }
}

@Composable
private fun WatchRow(item: WatchItem, onOpen: () -> Unit) {
    val fresh = item.aired - item.watched
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RemoteImage(item.poster, Modifier.width(72.dp).height(104.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.titleAlt.isNotBlank()) {
                    Text(item.titleAlt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    stringResource(R.string.watch_progress, item.watched, if (item.total > 0) item.total.toString() else "؟") +
                        " • " + statusLabel(item.status),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(airingLabel(item.airing), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (item.status == "watching" && fresh > 0) {
                    Text(stringResource(R.string.watch_behind, fresh), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            IconButton(onClick = {
                val max = if (item.total > 0) item.total else Int.MAX_VALUE
                val n = (item.watched + 1).coerceAtMost(max)
                val finished = item.total > 0 && n >= item.total && item.airing == "finished"
                WatchRepo.upsert(item.copy(watched = n, status = if (finished) "completed" else if (item.status == "plan") "watching" else item.status))
                SoundFx.play(if (finished) SoundFx.Fx.LEVEL_UP else SoundFx.Fx.TAP)
            }) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.watch_plus_one)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailSheet(item: WatchItem, onDismiss: () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uri = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Header(item)
            Text(stringResource(R.string.watch_status), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(statuses) { s ->
                    FilterChip(selected = item.status == s, onClick = {
                        val full = s == "completed"
                        WatchRepo.upsert(item.copy(status = s, watched = if (full && item.total > 0) item.total else item.watched))
                    }, label = { Text(statusLabel(s)) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.watch_episode), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { WatchRepo.upsert(item.copy(watched = (item.watched - 1).coerceAtLeast(0))) }) { Text("−") }
                Text("${item.watched} / ${if (item.total > 0) item.total else "؟"}", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = {
                    val max = if (item.total > 0) item.total else Int.MAX_VALUE
                    WatchRepo.upsert(item.copy(watched = (item.watched + 1).coerceAtMost(max)))
                    SoundFx.play(SoundFx.Fx.TAP)
                }) { Text("+") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    refreshing = true
                    scope.launch {
                        val fresh = WatchApi.refresh(item)
                        if (fresh != null) WatchRepo.upsert(fresh)
                        refreshing = false
                    }
                }, enabled = !refreshing) { Text(stringResource(R.string.watch_refresh)) }
                if (item.url.isNotBlank()) {
                    OutlinedButton(onClick = { runCatching { uri.openUri(item.url) } }) { Text(stringResource(R.string.watch_open_page)) }
                }
                TextButton(onClick = { WatchRepo.remove(item.key); onDismiss() }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text(stringResource(R.string.delete))
                }
            }
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Text(stringResource(R.string.watch_notif_off), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun Header(item: WatchItem) {
    var more by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RemoteImage(item.poster, Modifier.width(110.dp).height(160.dp).clip(RoundedCornerShape(14.dp)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (item.titleAlt.isNotBlank()) Text(item.titleAlt, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (item.titleOrig.isNotBlank() && item.titleOrig != item.title) {
                Text(item.titleOrig, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                listOfNotNull(
                    if (item.kind == "anime") stringResource(R.string.watch_anime) else stringResource(R.string.watch_series),
                    item.year.takeIf { it > 0 }?.toString(),
                    if (item.total > 0) stringResource(R.string.watch_eps, item.total) else null
                ).joinToString(" • "),
                style = MaterialTheme.typography.labelMedium
            )
            Text(airingLabel(item.airing), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            if (item.nextAt > 0L) {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(item.nextAt))
                Text(stringResource(R.string.watch_next, date), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    if (item.synopsis.isNotBlank()) {
        Text(stringResource(R.string.watch_story), style = MaterialTheme.typography.labelLarge)
        Text(
            item.synopsis,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (more) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { more = !more }
        )
    }
    if (item.characters.isNotEmpty()) {
        Text(stringResource(R.string.watch_characters), style = MaterialTheme.typography.labelLarge)
        Text(item.characters.joinToString(" • "), style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HitSheet(hit: WatchItem, onDismiss: () -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    val saved = WatchRepo.find(hit.key)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Header(saved ?: hit)
            if (saved != null) {
                Text(stringResource(R.string.watch_added, statusLabel(saved.status)), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            } else {
                Text(stringResource(R.string.watch_add_as), style = MaterialTheme.typography.labelLarge)
                if (busy) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(Modifier.height(24.dp))
                        Text(stringResource(R.string.watch_fetching))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("watching", "plan", "completed").forEach { s ->
                            Button(onClick = {
                                busy = true
                                scope.launch {
                                    addHit(context, hit, s)
                                    SoundFx.play(SoundFx.Fx.SUCCESS)
                                    busy = false
                                }
                            }, modifier = Modifier.weight(1f)) { Text(statusLabel(s), maxLines = 1) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchSheet(onDismiss: () -> Unit, onHit: (WatchItem) -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var searchKind by remember { mutableStateOf("anime") }
    var results by remember { mutableStateOf<List<WatchItem>?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun go() {
        if (query.isBlank() || loading) return
        loading = true
        scope.launch {
            results = if (searchKind == "anime") WatchApi.searchAnime(query) else WatchApi.searchSeries(query)
            loading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet) {
        Column(Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = searchKind == "anime", onClick = { searchKind = "anime"; results = null }, label = { Text(stringResource(R.string.watch_anime)) })
                FilterChip(selected = searchKind == "series", onClick = { searchKind = "series"; results = null }, label = { Text(stringResource(R.string.watch_series)) })
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.watch_search_hint)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { go() })
            )
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            val list = results
            if (list != null && list.isEmpty() && !loading) {
                Text(stringResource(R.string.watch_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(list.orEmpty(), key = { it.key }) { h ->
                    HitRow(h) { onHit(h) }
                }
            }
        }
    }
}

@Composable
private fun HitRow(h: WatchItem, onClick: () -> Unit) {
    val saved = WatchRepo.find(h.key) != null
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RemoteImage(h.poster, Modifier.width(56.dp).height(80.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(h.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (h.titleAlt.isNotBlank()) Text(h.titleAlt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(
                    listOfNotNull(h.year.takeIf { it > 0 }?.toString(), if (h.total > 0) stringResource(R.string.watch_eps, h.total) else null, airingLabel(h.airing)).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall
                )
                if (saved) Text(stringResource(R.string.watch_saved_mark), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun NewsTab(onHit: (WatchItem) -> Unit) {
    val uri = LocalUriHandler.current
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var airing by remember { mutableStateOf<List<WatchItem>>(emptyList()) }
    var today by remember { mutableStateOf<List<WatchItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(reload) {
        loading = true
        val tracked = WatchRepo.items.filter { it.kind == "anime" && it.status != "dropped" }.take(5)
        val collected = ArrayList<NewsItem>()
        for (t in tracked) {
            collected.addAll(WatchApi.animeNews(t))
            kotlinx.coroutines.delay(400)
        }
        news = collected.sortedByDescending { it.date }
        airing = WatchApi.airingNow()
        today = WatchApi.seriesToday()
        loading = false
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.watch_news_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                TextButton(onClick = { reload++ }, enabled = !loading) { Text(stringResource(R.string.watch_refresh)) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        if (news.isNotEmpty()) {
            item { Text(stringResource(R.string.watch_news_mine), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(news) { n ->
                Card(Modifier.fillMaxWidth().clickable { runCatching { uri.openUri(n.url) } }) {
                    Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (n.image.isNotBlank()) RemoteImage(n.image, Modifier.width(64.dp).height(64.dp).clip(RoundedCornerShape(10.dp)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(n.excerpt, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(n.source + " • " + n.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        } else if (!loading && WatchRepo.items.none { it.kind == "anime" }) {
            item { Text(stringResource(R.string.watch_news_none), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (airing.isNotEmpty()) {
            item { Text(stringResource(R.string.watch_airing_now), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { PosterRow(airing, onHit) }
        }
        if (today.isNotEmpty()) {
            item { Text(stringResource(R.string.watch_series_today), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { PosterRow(today, onHit) }
        }
    }
}

@Composable
private fun PosterRow(list: List<WatchItem>, onHit: (WatchItem) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(list, key = { it.key }) { h ->
            Column(Modifier.width(110.dp).clickable { onHit(h) }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RemoteImage(h.poster, Modifier.width(110.dp).height(160.dp).clip(RoundedCornerShape(12.dp)), ContentScale.Crop)
                Text(h.title, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
