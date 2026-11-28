// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.net

internal val KNOWN_ERROR_CODES = setOf(
    "missing_app_token", "invalid_app_token", "app_token_invalid",
    "missing_token", "invalid_token", "unauthorized", "forbidden"
)

data class ServerError(val code: String, val message: String)

private val CODE_RE = Regex("\"?code\"?\\s*:\\s*\"([^\"]+)\"")
private val MESSAGE_RE = Regex("\"?(?:message|mess|error)\"?\\s*:\\s*\"([^\"]*)\"")

/** قراءة رمز ورسالة الخطأ من رد JSON سليم أو مقطوع، دون تغيير الطلب أو إضافة أي توكن. */
fun parseServerError(text: String, httpCode: Int): ServerError {
    val clean = text.trim().removePrefix("\uFEFF")
    val rawCode = CODE_RE.find(clean)?.groupValues?.get(1).orEmpty()
    val code = rawCode.takeIf { it in KNOWN_ERROR_CODES }.orEmpty()
    val message = MESSAGE_RE.find(clean)?.groupValues?.get(1)
        ?.replace("\\n", " ")
        ?.trim()
        .orEmpty()
    return ServerError(
        code = code,
        message = message.ifBlank { rawCode.ifBlank { "HTTP $httpCode" } }
    )
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
