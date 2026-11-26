// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.files

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.io.File

enum class SortMode { NAME, DATE, SIZE, TYPE }

data class FileItem(
    val file: File,
    val name: String,
    val isDir: Boolean,
    val size: Long,
    val modified: Long,
    val extension: String
)

object FileManager {

    fun list(dir: File, sort: SortMode, ascending: Boolean, query: String): List<FileItem> {
        val items = dir.listFiles()?.map { f ->
            FileItem(
                file = f,
                name = f.name,
                isDir = f.isDirectory,
                size = if (f.isFile) f.length() else 0L,
                modified = f.lastModified(),
                extension = f.extension.lowercase()
            )
        } ?: emptyList()
        val filtered = if (query.isBlank()) items else items.filter { it.name.contains(query, ignoreCase = true) }
        val comparator: Comparator<FileItem> = when (sort) {
            SortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortMode.DATE -> compareBy { it.modified }
            SortMode.SIZE -> compareBy { it.size }
            SortMode.TYPE -> compareBy { it.extension }
        }
        val sorted = filtered.sortedWith(comparator)
        val dirsFirst = sorted.sortedByDescending { it.isDir }
        return if (ascending) dirsFirst else dirsFirst.reversed()
    }

    fun scanDocuments(root: File, limit: Int = 400): List<FileItem> {
        val out = ArrayList<FileItem>()
        walk(root, 0, out, limit)
        return out.sortedByDescending { it.modified }
    }

    private fun walk(dir: File, depth: Int, out: MutableList<FileItem>, limit: Int) {
        if (depth > 6 || out.size >= limit) return
        val children = dir.listFiles() ?: return
        for (f in children) {
            if (out.size >= limit) return
            if (f.isDirectory) {
                if (!f.name.startsWith(".") && f.name != "Android") walk(f, depth + 1, out, limit)
            } else if (f.extension.lowercase() in setOf("pdf", "md", "markdown", "txt")) {
                out.add(FileItem(f, f.name, false, f.length(), f.lastModified(), f.extension.lowercase()))
            }
        }
    }

    fun copy(src: File, destDir: File, move: Boolean): Boolean = runCatching {
        val target = File(destDir, src.name)
        if (src.isDirectory) {
            target.mkdirs()
            src.listFiles()?.forEach { copy(it, target, false) }
            if (move) src.deleteRecursively()
        } else {
            destDir.mkdirs()
            src.copyTo(target, overwrite = true)
            if (move) src.delete()
        }
        true
    }.getOrDefault(false)

    fun sizeText(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }

    fun markdown(source: String): AnnotatedString = buildAnnotatedString {
        source.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF8B6CF5))) {
                        append(trimmed.removePrefix("# ") + "\n")
                    }
                }
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB9A6FF))) {
                        append(trimmed.removePrefix("## ") + "\n")
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> append("• " + inline(trimmed.substring(2)) + "\n")
                trimmed.startsWith("```") -> append("\n")
                trimmed.startsWith("`") -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, color = Color(0xFF39C58A))) {
                        append(trimmed.trim('`') + "\n")
                    }
                }
                trimmed.isBlank() -> append("\n")
                else -> append(inline(trimmed) + "\n")
            }
        }
    }

    private fun inline(text: String): String = text
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("\\[(.*?)]\\([^)]*\\)"), "$1")
        .replace(Regex("`([^`]*)`"), "$1")
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
