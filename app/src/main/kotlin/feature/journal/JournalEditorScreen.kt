// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.journal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.Html
import android.text.TextUtils
import android.util.Base64
import android.util.JsonReader
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yousuf.toolkit.R
import core.design.ScreenScaffold
import data.local.LocalStore
import domain.model.JournalEntry
import org.json.JSONObject
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun JournalEditorScreen(onBack: () -> Unit) {
    val store = LocalStore.current
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf("") }
    var emojiPackId by remember { mutableStateOf(JournalAssets.emojiPacks.first().id) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf<LocalTime?>(null) }
    var backgroundId by remember { mutableStateOf(JournalAssets.backgrounds.first().id) }
    var stickerPackId by remember { mutableStateOf(JournalAssets.stickerPacks.first().id) }
    var font by remember { mutableStateOf(JournalAssets.fonts.first()) }
    var customText by remember { mutableStateOf("") }
    var customEmoji by remember { mutableStateOf("⭐") }
    var drawMode by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var web by remember { mutableStateOf<WebView?>(null) }
    val background = JournalAssets.backgrounds.first { it.id == backgroundId }
    val initialHtml = remember { JournalEditorHtml.build(JournalAssets.backgrounds.first().color, "") }
    val emojiNames = stringArrayResource(R.array.journal_emoji_pack_names)
    val stickerNames = stringArrayResource(R.array.journal_sticker_pack_names)
    val backgroundNames = stringArrayResource(R.array.journal_background_names)
    val fontNames = stringArrayResource(R.array.journal_font_names)
    val colorNames = stringArrayResource(R.array.journal_color_names)
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/png" }
        val bytes = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
        if (bytes == null || bytes.isEmpty() || bytes.size > 4 * 1024 * 1024) {
            status = context.getString(R.string.journal_image_too_large)
            return@rememberLauncherForActivityResult
        }
        val dataUrl = "data:$mime;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        web?.evaluateJavascript("window.insertImage(" + JSONObject.quote(dataUrl) + ")", null)
    }

    fun push(command: String) {
        web?.evaluateJavascript(command, null)
    }

    fun save() {
        web?.evaluateJavascript("window.getContent()", ValueCallback<String> { value ->
            val html = runCatching {
                JsonReader(StringReader(value.orEmpty())).use { it.nextString() }
            }.getOrDefault(value.orEmpty())
            val plain = Html.fromHtml(html, Html.FROM_HTML_MODE_LEAN).toString().trim()
            if (title.isBlank() && plain.isBlank()) {
                status = context.getString(R.string.journal_editor_empty)
                return@ValueCallback
            }
            val at = date.atTime(time ?: LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            store.addRichEntry(
                JournalEntry(
                    id = 0L,
                    title = title.trim(),
                    body = plain,
                    at = at,
                    mood = mood,
                    background = backgroundId,
                    richBody = html,
                    font = font,
                    colors = "",
                    drawings = "",
                    stickers = stickerPackId,
                    images = "",
                    emojiPack = emojiPackId,
                    stickerPack = stickerPackId,
                    voicePath = "",
                    hasTime = time != null,
                    syncedAt = 0L
                )
            )
            onBack()
        })
    }

    ScreenScaffold(
        title = stringResource(if (step == 0) R.string.journal_mood_title else R.string.journal_editor_title),
        onBack = onBack,
        actions = {
            if (step == 1) {
                TextButton(onClick = { save() }) { Text(stringResource(R.string.save)) }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.error)
            if (step == 0) {
                Text(stringResource(R.string.journal_mood_choose), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(JournalAssets.emojiPacks) { index, pack ->
                        val owned = pack.cost == 0 || pack.id in store.ownedEmojiPacks
                        FilterChip(
                            selected = emojiPackId == pack.id,
                            onClick = {
                                if (owned) {
                                    emojiPackId = pack.id
                                } else if (store.buyJournalAsset("emoji", pack.id, pack.cost)) {
                                    emojiPackId = pack.id
                                    status = ""
                                } else {
                                    status = context.getString(R.string.journal_points_missing)
                                }
                            },
                            label = {
                                Text(
                                    emojiNames.getOrNull(index).orEmpty() +
                                        if (owned) "" else " · " +
                                        stringResource(R.string.journal_points_cost, pack.cost)
                                )
                            }
                        )
                    }
                }
                val pack = JournalAssets.emojiPacks.first { it.id == emojiPackId }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(pack.emojis) { _, emoji ->
                        FilterChip(selected = mood == emoji, onClick = { mood = emoji }, label = { Text(emoji) })
                    }
                }
                Button(onClick = { step = 1 }, enabled = mood.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.journal_editor_start))
                }
            } else {
                Text(stringResource(R.string.journal_date_time), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                            date.year, date.monthValue - 1, date.dayOfMonth
                        ).show()
                    }, modifier = Modifier.weight(1f)) { Text(date.toString()) }
                    OutlinedButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> time = LocalTime.of(h, m) },
                            time?.hour ?: 12, time?.minute ?: 0, true
                        ).show()
                    }, modifier = Modifier.weight(1f)) { Text(time?.toString() ?: stringResource(R.string.journal_time_optional)) }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.journal_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.journal_backgrounds), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(JournalAssets.backgrounds) { index, item ->
                        val owned = item.cost == 0 || item.id in store.ownedBackgrounds
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(42.dp).background(Color(item.color.toInt()), RoundedCornerShape(10.dp))
                            )
                            TextButton(onClick = {
                                if (owned) {
                                    backgroundId = item.id
                                } else if (store.buyJournalAsset("background", item.id, item.cost)) {
                                    backgroundId = item.id
                                    status = ""
                                } else {
                                    status = context.getString(R.string.journal_points_missing)
                                }
                            }) {
                                Text(
                                    backgroundNames.getOrNull(index).orEmpty() +
                                        if (owned) "" else " · " +
                                        stringResource(R.string.journal_points_cost, item.cost)
                                )
                            }
                        }
                    }
                }
                Text(stringResource(R.string.journal_editor_tools), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { OutlinedButton(onClick = { push("window.command('bold')") }) { Text(stringResource(R.string.journal_bold)) } }
                    item { OutlinedButton(onClick = { push("window.command('italic')") }) { Text(stringResource(R.string.journal_italic)) } }
                    item { OutlinedButton(onClick = { push("window.command('underline')") }) { Text(stringResource(R.string.journal_underline)) } }
                    item { OutlinedButton(onClick = { push("window.command('insertUnorderedList')") }) { Text(stringResource(R.string.journal_bullet_list)) } }
                    item { OutlinedButton(onClick = { push("window.command('insertOrderedList')") }) { Text(stringResource(R.string.journal_number_list)) } }
                    item { OutlinedButton(onClick = { push("window.alphaList()") }) { Text(stringResource(R.string.journal_alpha_list)) } }
                    item { OutlinedButton(onClick = { push("window.nestedList()") }) { Text(stringResource(R.string.journal_nested_list)) } }
                    item { OutlinedButton(onClick = { push("window.command('outdent')") }) { Text(stringResource(R.string.journal_outdent)) } }
                    item { OutlinedButton(onClick = { drawMode = !drawMode; push("window.setDrawMode(" + drawMode + ")") }) { Text(stringResource(if (drawMode) R.string.journal_draw_stop else R.string.journal_draw)) } }
                    item { OutlinedButton(onClick = { imagePicker.launch("image/*") }) { Text(stringResource(R.string.journal_add_image)) } }
                }
                Text(stringResource(R.string.journal_fonts), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(JournalAssets.fonts) { index, name ->
                        FilterChip(selected = font == name, onClick = { font = name }, label = { Text(fontNames.getOrNull(index).orEmpty()) })
                    }
                }
                Text(stringResource(R.string.journal_text_colors), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(JournalAssets.colors) { index, color ->
                        OutlinedButton(onClick = { push("window.command('foreColor', " + JSONObject.quote(color) + ")") }) {
                            Text(colorNames.getOrNull(index).orEmpty())
                        }
                    }
                }
                Text(stringResource(R.string.journal_sticker_packs), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(JournalAssets.stickerPacks) { index, pack ->
                        val owned = pack.cost == 0 || pack.id in store.ownedStickerPacks
                        FilterChip(selected = stickerPackId == pack.id, onClick = {
                            if (owned) stickerPackId = pack.id
                            else if (store.buyJournalAsset("sticker", pack.id, pack.cost)) { stickerPackId = pack.id; status = "" }
                            else status = context.getString(R.string.journal_points_missing)
                        }, label = {
                            Text(stickerNames.getOrNull(index).orEmpty() + if (owned) "" else " · " + stringResource(R.string.journal_points_cost, pack.cost))
                        })
                    }
                }
                val pack = JournalAssets.stickerPacks.first { it.id == stickerPackId }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(pack.emojis) { _, sticker ->
                        OutlinedButton(onClick = { push("window.insertSticker(" + JSONObject.quote(sticker) + ")") }) { Text(sticker) }
                    }
                }
                Text(stringResource(R.string.journal_custom_sticker), style = MaterialTheme.typography.titleMedium)
                if (!store.premium) {
                    Text(stringResource(R.string.journal_premium_needed))
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = customEmoji, onValueChange = { customEmoji = it.take(4) }, label = { Text(stringResource(R.string.journal_custom_emoji)) }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = customText, onValueChange = { customText = it.take(40) }, label = { Text(stringResource(R.string.journal_custom_text)) }, modifier = Modifier.weight(2f))
                    }
                    OutlinedButton(onClick = {
                        val sticker = TextUtils.htmlEncode(customEmoji + " " + customText)
                        push("window.insertSticker(" + JSONObject.quote(sticker) + ")")
                    }) { Text(stringResource(R.string.journal_custom_insert)) }
                }
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(430.dp),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            isVerticalScrollBarEnabled = false
                            loadDataWithBaseURL(null, initialHtml, "text/html", "UTF-8", null)
                            web = this
                        }
                    }
                )
                Button(onClick = { save() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
            }
        }
    }

    LaunchedEffect(web, background, font) {
        web?.evaluateJavascript("window.setBackground(" + JSONObject.quote(JournalEditorHtml.cssColor(background.color)) + ")", null)
        web?.evaluateJavascript("window.setFont(" + JSONObject.quote(font) + ")", null)
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
