// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.git

import android.os.Environment
import java.io.File

data class LocalRepoInfo(
    val name: String,
    val path: String,
    val branch: String,
    val headSha: String,
    val lastUpdate: Long,
    val description: String,
    val branches: List<String>,
    val dirty: Boolean
)

object LocalGit {

    fun scan(): List<LocalRepoInfo> {
        val root = runCatching { Environment.getExternalStorageDirectory() }.getOrNull() ?: return emptyList()
        val out = ArrayList<LocalRepoInfo>()
        walk(root, 0, out)
        return out.sortedByDescending { it.lastUpdate }
    }

    private fun walk(dir: File, depth: Int, out: MutableList<LocalRepoInfo>) {
        if (depth > 4 || out.size >= 40) return
        val git = File(dir, ".git")
        if (git.isDirectory) {
            out.add(read(dir, git))
            return
        }
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (!child.isDirectory || child.name.startsWith(".") || child.name == "Android") continue
            walk(child, depth + 1, out)
        }
    }

    private fun read(dir: File, git: File): LocalRepoInfo {
        val head = text(File(git, "HEAD"))
        val ref = head.removePrefix("ref: ").trim()
        val branch = ref.substringAfterLast('/').ifBlank { "HEAD" }
        val sha = if (ref.startsWith("refs/")) text(File(git, ref)) else head
        val heads = File(git, "refs/heads")
        val branches = ArrayList<String>()
        if (heads.isDirectory) heads.walkTopDown().forEach { if (it.isFile) branches.add(it.relativeTo(heads).path.replace('\\', '/')) }
        val packed = text(File(git, "packed-refs"))
        packed.lineSequence().filter { it.contains("refs/heads/") && !it.startsWith("#") }
            .map { it.substringAfter("refs/heads/").trim() }.forEach { branches.add(it) }
        val config = text(File(git, "config"))
        val description = text(File(git, "description")).ifBlank { config.lineSequence()
            .firstOrNull { it.trim().startsWith("url =") }?.substringAfter('=')?.trim().orEmpty() }
        val log = File(git, "logs/HEAD")
        val lastUpdate = if (log.isFile) log.lastModified() else git.lastModified()
        val index = File(git, "index")
        val dirty = index.isFile && index.lastModified() > shaFileTime(git, ref)
        return LocalRepoInfo(
            name = dir.name, path = dir.absolutePath, branch = branch,
            headSha = sha.trim(), lastUpdate = lastUpdate,
            description = description, branches = branches.distinct().sorted(),
            dirty = dirty
        )
    }

    private fun shaFileTime(git: File, ref: String): Long {
        val f = File(git, ref)
        return if (f.isFile) f.lastModified() else 0L
    }

    private fun text(file: File): String = runCatching {
        if (file.isFile) file.readText().trim() else ""
    }.getOrDefault("")
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
