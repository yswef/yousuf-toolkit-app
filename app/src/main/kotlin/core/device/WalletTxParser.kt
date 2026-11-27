// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.device

import android.content.Context
import domain.model.SmsMoney
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class WalletRules(
    val sources: List<String> = listOf("جيب", "Jaib", "jaib"),
    val incomeWords: List<String> = listOf("أضيف", "اضيف", "أُضيف", "إيداع", "ايداع", "دخل", "استلمت", "وارد"),
    val expenseWords: List<String> = listOf("خصم", "سحب", "دفع", "شراء"),
    val transferWords: List<String> = listOf("تحويل مشترك", "حوالة"),
    val purchaseWords: List<String> = listOf("دفع مشتريات", "مشتريات", "شراء"),
    val balanceWords: List<String> = listOf("رصيد", "الرصيد", "رص"),
    val fromWord: String = "من",
    val toWord: String = "إلى",
    val noteSeparator: String = " · ",
    val maxNoteChars: Int = 220
) {
    companion object {
        fun load(context: Context): WalletRules {
            val defaults = WalletRules()
            val text = try {
                val app = context.applicationContext
                val updated = File(app.filesDir, "content/wallet_notifications.json")
                if (updated.isFile && updated.length() > 0) {
                    updated.bufferedReader().use { it.readText() }
                } else {
                    app.assets.open("wallet_notifications.json").bufferedReader().use { it.readText() }
                }
            } catch (t: Throwable) {
                return defaults
            }
            return try {
                val root = JSONObject(text)
                WalletRules(
                    sources = stringList(root, "sources", defaults.sources),
                    incomeWords = stringList(root, "income_words", defaults.incomeWords),
                    expenseWords = stringList(root, "expense_words", defaults.expenseWords),
                    transferWords = stringList(root, "transfer_words", defaults.transferWords),
                    purchaseWords = stringList(root, "purchase_words", defaults.purchaseWords),
                    balanceWords = stringList(root, "balance_words", defaults.balanceWords),
                    fromWord = root.optString("from_word", defaults.fromWord).ifBlank { defaults.fromWord },
                    toWord = root.optString("to_word", defaults.toWord).ifBlank { defaults.toWord },
                    noteSeparator = root.optString("note_separator", defaults.noteSeparator),
                    maxNoteChars = root.optInt("max_note_chars", defaults.maxNoteChars).takeIf { it > 0 }
                        ?: defaults.maxNoteChars
                )
            } catch (t: Throwable) {
                defaults
            }
        }

        private fun stringList(root: JSONObject, key: String, fallback: List<String>): List<String> {
            val array = root.optJSONArray(key) ?: return fallback
            val out = ArrayList<String>()
            for (i in 0 until array.length()) {
                val value = array.optString(i)
                if (value.isNotBlank()) out.add(value)
            }
            return out.ifEmpty { fallback }
        }
    }
}

object WalletTxParser {
    fun parseAll(
        context: Context,
        source: String,
        sender: String,
        title: String,
        text: String,
        at: Long
    ): List<SmsMoney> = parseAll(WalletRules.load(context), source, sender, title, text, at)

    fun parseAll(
        rules: WalletRules,
        source: String,
        sender: String,
        title: String,
        text: String,
        at: Long
    ): List<SmsMoney> {
        val normalized = normalizeDigits(listOf(title, text).filter { it.isNotBlank() }.joinToString(" "))
        val knownSource = rules.sources.any {
            normalized.contains(it, ignoreCase = true) || sender.contains(it, ignoreCase = true)
        }
        if (source == "notification" && !knownSource) return emptyList()
        val directionWords = (rules.incomeWords + rules.expenseWords + listOf("مبلغ", "حوالة")).distinct()
        val amountRegex = Regex(
            """(?:${directionWords.joinToString("|") { Regex.escape(it) }})[^\d]{0,30}([\d,]+(?:\.\d+)?)"""
        )
        val balanceRegex = Regex(
            """(?:${rules.balanceWords.joinToString("|") { Regex.escape(it) }})\s*:?\s*([\d,]+(?:\.\d+)?)"""
        )
        val matches = amountRegex.findAll(normalized).toList()
        if (matches.isEmpty()) return emptyList()
        return matches.mapIndexed { index, match ->
            val start = match.range.first
            val end = if (index + 1 < matches.size) matches[index + 1].range.first else normalized.length
            parseSegment(
                rules = rules,
                source = source,
                sender = sender,
                title = title,
                segment = normalized.substring(start, end),
                amountRegex = amountRegex,
                balanceRegex = balanceRegex,
                at = at
            )
        }.filterNotNull()
    }

    private fun parseSegment(
        rules: WalletRules,
        source: String,
        sender: String,
        title: String,
        segment: String,
        amountRegex: Regex,
        balanceRegex: Regex,
        at: Long
    ): SmsMoney? {
        val amountMatch = amountRegex.find(segment) ?: return null
        val amount = amountMatch.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null
        val firstIncome = rules.incomeWords.mapNotNull { word ->
            segment.indexOf(word).takeIf { it >= 0 }
        }.minOrNull()
        val firstExpense = rules.expenseWords.mapNotNull { word ->
            segment.indexOf(word).takeIf { it >= 0 }
        }.minOrNull()
        if (firstIncome == null && firstExpense == null) return null
        val isIncome = when {
            firstExpense == null -> true
            firstIncome == null -> false
            else -> firstIncome < firstExpense
        }
        val balanceMatch = balanceRegex.find(segment)
        val balance = balanceMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
        val counterparty = extractCounterparty(rules, segment)
        if (balance == null && counterparty.isBlank()) return null
        val noteParts = ArrayList<String>()
        balanceMatch?.value?.trim()?.let { if (it.isNotBlank()) noteParts.add(it) }
        if (counterparty.isNotBlank()) noteParts.add(counterparty)
        val note = noteParts.joinToString(rules.noteSeparator).take(rules.maxNoteChars)
        val phone = Regex("""\d{6,}""").find(counterparty)?.value.orEmpty()
        val fingerprint = if (phone.isNotBlank()) phone else counterparty.hashCode().toString()
        val day = at / 86_400_000L
        val key = String.format(
            Locale.US,
            "wallet:%d:%.2f:%.2f:%s:%d",
            if (isIncome) 1 else 0,
            amount,
            balance ?: -1.0,
            fingerprint,
            day
        )
        val category = if (rules.purchaseWords.any { segment.contains(it) }) "shopping" else "other"
        return SmsMoney(
            key = key,
            sender = sender,
            body = segment.trim().take(400),
            amount = amount,
            balance = balance,
            income = isIncome,
            at = at,
            title = title.trim().take(120),
            counterparty = counterparty,
            note = note,
            category = category,
            source = source
        )
    }

    private fun extractCounterparty(rules: WalletRules, segment: String): String {
        val fromMatch = Regex("""(?:^|\s)${Regex.escape(rules.fromWord)}\s+""").find(segment)
        val toMatch = Regex("""(?:^|\s)${Regex.escape(rules.toWord)}\s+""").find(segment)
        val chosen = when {
            fromMatch != null && (toMatch == null || fromMatch.range.first < toMatch.range.first) -> fromMatch
            toMatch != null -> toMatch
            else -> return ""
        }
        val marker = if (chosen === fromMatch) rules.fromWord else rules.toWord
        val detail = segment.substring(chosen.range.last + 1)
            .trim()
            .replace(Regex("""\s+"""), " ")
        return ("$marker $detail").take(rules.maxNoteChars)
    }

    private fun normalizeDigits(input: String): String {
        var out = input
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        for (i in 0..9) {
            val digit = i.toString()[0]
            out = out.replace(arabic[i], digit).replace(persian[i], digit)
        }
        return out.replace('٬', ',').replace('٫', '.')
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
