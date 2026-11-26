// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

data class MediaEntry(val name: String, val uri: Uri, val mime: String, val size: Long, val date: Long)

object MediaVault {

    fun vaultDir(context: Context): File =
        File(context.filesDir, "vault/media").apply { mkdirs() }

    fun list(context: Context): List<MediaEntry> {
        val out = ArrayList<MediaEntry>()
        query(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, out)
        query(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, out)
        return out.sortedByDescending { it.date }
    }

    private fun query(context: Context, uri: Uri, out: MutableList<MediaEntry>) {
        val columns = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED
        )
        runCatching {
            context.contentResolver.query(uri, columns, null, null, null)?.use { c ->
                val id = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val name = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mime = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val size = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val date = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                while (c.moveToNext()) {
                    val rowId = c.getLong(id)
                    out.add(
                        MediaEntry(
                            name = c.getString(name) ?: "media-$rowId",
                            uri = Uri.withAppendedPath(uri, rowId.toString()),
                            mime = c.getString(mime) ?: "image/*",
                            size = c.getLong(size),
                            date = c.getLong(date) * 1000L
                        )
                    )
                }
            }
        }
    }

    fun hide(context: Context, entries: List<MediaEntry>): Int {
        val dir = vaultDir(context)
        var count = 0
        for (entry in entries) {
            val bytes = runCatching {
                context.contentResolver.openInputStream(entry.uri)?.use { it.readBytes() }
            }.getOrNull() ?: continue
            val ext = if (entry.mime.startsWith("video")) "mp4" else "jpg"
            val safe = entry.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
            File(dir, "$safe.$ext").writeBytes(bytes)
            count++
        }
        return count
    }

    fun hidden(context: Context): List<File> =
        vaultDir(context).listFiles()?.filter { it.isFile && !it.name.endsWith(".json") }?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun restore(context: Context, file: File): Boolean {
        val video = file.extension.lowercase() in listOf("mp4", "mkv", "webm", "3gp")
        val mime = if (video) "video/mp4" else "image/jpeg"
        val uri = if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val folder = if (video) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$folder/Y-Dimensions")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val target = runCatching { context.contentResolver.insert(uri, values) }.getOrNull() ?: return false
        val ok = runCatching {
            context.contentResolver.openOutputStream(target)?.use { out -> file.inputStream().use { it.copyTo(out) } }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(target, values, null, null)
            true
        }.getOrDefault(false)
        return ok
    }

    fun deleteUris(): List<Uri> = emptyList()
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
