package core.security

import java.security.SecureRandom

data class ImportedAccount(
    val title: String,
    val username: String,
    val password: String,
    val url: String
)

fun importedAccount(title: String, username: String, password: String, url: String): ImportedAccount =
    ImportedAccount(title, username, password, url)

object PasswordTools {
    private const val LOWER = "abcdefghijkmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val DIGITS = "23456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}?"

    fun generate(length: Int, symbols: Boolean = true): String {
        val alphabet = LOWER + UPPER + DIGITS + if (symbols) SYMBOLS else ""
        val random = SecureRandom()
        val out = StringBuilder(length.coerceIn(8, 64))
        out.append(LOWER[random.nextInt(LOWER.length)])
        out.append(UPPER[random.nextInt(UPPER.length)])
        out.append(DIGITS[random.nextInt(DIGITS.length)])
        if (symbols) out.append(SYMBOLS[random.nextInt(SYMBOLS.length)])
        while (out.length < length.coerceIn(8, 64)) {
            out.append(alphabet[random.nextInt(alphabet.length)])
        }
        return out.toString().toList().shuffled(random).joinToString("")
    }

    fun strength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score.coerceIn(0, 5)
    }

    fun parseCsv(text: String): List<ImportedAccount> {
        val rows = parseRows(text)
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.trim().lowercase() }
        val hasHeader = header.contains("password") || header.contains("login_password")
        val body = if (hasHeader) rows.drop(1) else rows
        val titleIndex = header.indexOfFirst { it == "name" || it == "title" || it == "login_uri" || it == "url" }
        val userIndex = header.indexOfFirst { it == "username" || it == "login_username" || it == "user" }
        val passIndex = header.indexOfFirst { it == "password" || it == "login_password" }
        val urlIndex = header.indexOfFirst { it == "url" || it == "login_uri" || it == "website" }
        val out = ArrayList<ImportedAccount>()
        for (row in body) {
            if (row.size < 2) continue
            val password = row.getOrNull(if (hasHeader && passIndex >= 0) passIndex else row.lastIndex).orEmpty()
            if (password.isBlank()) continue
            val username = row.getOrNull(if (hasHeader && userIndex >= 0) userIndex else 0).orEmpty()
            val title = row.getOrNull(if (hasHeader && titleIndex >= 0) titleIndex else 0).orEmpty().ifBlank { username }
            val url = row.getOrNull(if (hasHeader && urlIndex >= 0) urlIndex else -1).orEmpty()
            out.add(ImportedAccount(title, username, password, url))
        }
        return out
    }

    fun csv(rows: List<ImportedAccount>): String {
        val out = StringBuilder("name,url,username,password\n")
        rows.forEach { out.append(quote(it.title)).append(',').append(quote(it.url)).append(',')
            .append(quote(it.username)).append(',').append(quote(it.password)).append('\n') }
        return out.toString()
    }

    private fun quote(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""

    private fun parseRows(text: String): List<List<String>> {
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        var field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (quoted) {
                if (c == '"' && i + 1 < text.length && text[i + 1] == '"') {
                    field.append('"')
                    i++
                } else if (c == '"') {
                    quoted = false
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> quoted = true
                    ',' -> {
                        row.add(field.toString())
                        field = StringBuilder()
                    }
                    '\n' -> {
                        row.add(field.toString())
                        if (row.any { it.isNotBlank() }) rows.add(row)
                        row = ArrayList()
                        field = StringBuilder()
                    }
                    '\r' -> Unit
                    else -> field.append(c)
                }
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            if (row.any { it.isNotBlank() }) rows.add(row)
        }
        return rows
    }
}
