package feature.cleaner

import android.content.Context
import android.os.Environment
import androidx.annotation.StringRes
import com.yousuf.toolkit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

enum class CleanCat(@StringRes val title: Int, @StringRes val desc: Int, val defaultOn: Boolean) {
    APP_CACHE(R.string.clean_cat_cache, R.string.clean_cat_cache_d, true),
    TEMP(R.string.clean_cat_temp, R.string.clean_cat_temp_d, true),
    EMPTY(R.string.clean_cat_empty, R.string.clean_cat_empty_d, true),
    APK(R.string.clean_cat_apk, R.string.clean_cat_apk_d, true),
    DUPES(R.string.clean_cat_dupes, R.string.clean_cat_dupes_d, false),
    LARGE(R.string.clean_cat_large, R.string.clean_cat_large_d, false)
}

data class Junk(val path: String, val size: Long, val isDir: Boolean = false)

private const val KB = 1024L
private const val MB = 1024L * 1024L
private val TEMP_EXT = setOf("tmp", "temp", "log", "bak", "old", "chk", "dmp", "cache")

/** مجلدات المستوى الأول القياسية لا تُحذف حتى لو كانت فارغة، ومجلد Android لا يُمس أبداً. */
private val PROTECTED_TOP = setOf(
    "Android", "DCIM", "Pictures", "Music", "Movies", "Download", "Documents",
    "Alarms", "Notifications", "Ringtones", "Podcasts", "Audiobooks", "Recordings"
)

fun cleanRoots(context: Context, allFiles: Boolean): List<File> {
    val roots = ArrayList<File>()
    listOfNotNull(context.cacheDir, context.codeCacheDir, context.externalCacheDir).forEach { roots.add(it) }
    if (allFiles) roots.add(Environment.getExternalStorageDirectory())
    return roots.distinct()
}

fun fmtSize(bytes: Long): String {
    val b = bytes.toDouble()
    return when {
        bytes >= 1024L * MB -> String.format(java.util.Locale.getDefault(), "%.2f GB", b / (1024.0 * MB))
        bytes >= MB -> String.format(java.util.Locale.getDefault(), "%.1f MB", b / MB)
        bytes >= KB -> String.format(java.util.Locale.getDefault(), "%.0f KB", b / KB)
        else -> "$bytes B"
    }
}

suspend fun scanJunk(
    context: Context,
    allFiles: Boolean,
    onStage: (Int) -> Unit
): Map<CleanCat, List<Junk>> = withContext(Dispatchers.IO) {
    val out = LinkedHashMap<CleanCat, MutableList<Junk>>()
    CleanCat.entries.forEach { out[it] = ArrayList() }

    onStage(R.string.clean_stage_cache)
    listOfNotNull(context.cacheDir, context.codeCacheDir, context.externalCacheDir).distinct().forEach { root ->
        var n = 0
        for (f in root.walkTopDown()) {
            if (++n % 400 == 0) ensureActive()
            if (f.isFile) out.getValue(CleanCat.APP_CACHE).add(Junk(f.absolutePath, f.length()))
        }
    }

    if (allFiles) {
        val root = Environment.getExternalStorageDirectory()
        onStage(R.string.clean_stage_files)
        val dirs = ArrayList<File>()
        val candidates = ArrayList<File>()
        val large = ArrayList<Junk>()
        val flagged = HashSet<String>()
        var count = 0
        val walk = root.walkTopDown().onEnter { !(it.parentFile == root && it.name == "Android") }
        for (f in walk) {
            if (++count % 400 == 0) ensureActive()
            if (count > 600_000) break
            if (f.isDirectory) {
                if (f != root) dirs.add(f)
                continue
            }
            val len = f.length()
            val ext = f.extension.lowercase()
            val inThumbs = f.parentFile?.name.equals(".thumbnails", ignoreCase = true) || f.name.startsWith(".thumbdata")
            if (inThumbs || ext in TEMP_EXT) {
                out.getValue(CleanCat.TEMP).add(Junk(f.absolutePath, len)); flagged.add(f.absolutePath)
            } else if (ext == "apk") {
                out.getValue(CleanCat.APK).add(Junk(f.absolutePath, len)); flagged.add(f.absolutePath)
            }
            if (len >= 100 * MB) large.add(Junk(f.absolutePath, len))
            if (len in (50 * KB)..(300 * MB) && f.absolutePath !in flagged) candidates.add(f)
        }
        out.getValue(CleanCat.LARGE).addAll(large.sortedByDescending { it.size }.take(25))

        onStage(R.string.clean_stage_empty)
        val emptySet = HashSet<String>()
        val emptyList = ArrayList<Junk>()
        for (dir in dirs.sortedByDescending { it.absolutePath.length }) {
            ensureActive()
            if (dir.parentFile == root && dir.name in PROTECTED_TOP) continue
            val kids = dir.list() ?: continue
            if (kids.all { File(dir, it).absolutePath in emptySet }) {
                emptySet.add(dir.absolutePath)
                emptyList.add(Junk(dir.absolutePath, 0L, isDir = true))
            }
        }
        out.getValue(CleanCat.EMPTY).addAll(emptyList)

        onStage(R.string.clean_stage_dupes)
        var hashed = 0L
        val bySize = candidates.groupBy { it.length() }.filter { it.value.size > 1 }
        outer@ for ((_, group) in bySize) {
            val byHash = HashMap<String, MutableList<File>>()
            for (f in group) {
                ensureActive()
                if (hashed > 8L * 1024L * MB) break@outer
                hashed += f.length()
                val h = sha256(f) ?: continue
                byHash.getOrPut(h) { ArrayList() }.add(f)
            }
            for ((_, same) in byHash) {
                if (same.size < 2) continue
                same.sortedBy { it.lastModified() }.drop(1).forEach {
                    out.getValue(CleanCat.DUPES).add(Junk(it.absolutePath, it.length()))
                }
            }
        }
    }
    out
}

private fun sha256(f: File): String? = try {
    val md = MessageDigest.getInstance("SHA-256")
    FileInputStream(f).use { input ->
        val buf = ByteArray(64 * 1024)
        while (true) {
            val r = input.read(buf)
            if (r <= 0) break
            md.update(buf, 0, r)
        }
    }
    md.digest().joinToString("") { "%02x".format(it) }
} catch (_: Exception) {
    null
}

/** حذف العناصر المحددة فقط، داخل الجذور المسموحة. المجلدات تُحذف إن كانت فارغة فقط. */
suspend fun deleteJunk(items: List<Junk>, roots: List<File>): Pair<Int, Long> = withContext(Dispatchers.IO) {
    val rootPaths = roots.mapNotNull { runCatching { it.canonicalPath }.getOrNull() }
    var n = 0
    var freed = 0L
    for (j in items) {
        val f = File(j.path)
        val canon = runCatching { f.canonicalPath }.getOrNull() ?: continue
        val inside = rootPaths.any { canon.startsWith(it + File.separator) }
        if (!inside || !f.exists()) continue
        if (f.delete()) {
            n++
            freed += j.size
        }
    }
    n to freed
}
