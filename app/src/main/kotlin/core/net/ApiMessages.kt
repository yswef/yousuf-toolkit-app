// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.net

import android.content.Context
import com.yousuf.toolkit.R

fun authError(context: Context, t: Throwable): String {
    val e = t as? ApiException
    val raw = e?.message ?: t.message
    val recovered = parseServerError(raw.orEmpty(), e?.code ?: 0)
    val fallback = context.getString(R.string.auth_failed)

    if (e?.serverCode == "missing_app_token" || recovered.code == "missing_app_token") {
        return context.getString(R.string.auth_missing_app_token)
    }
    if (e?.serverCode in setOf(
            "invalid_app_token", "app_token_invalid", "invalid_token",
            "missing_token", "unauthorized", "forbidden"
        )
    ) {
        return context.getString(R.string.auth_invalid_app_token)
    }
    return when (e?.message) {
        Api.SERVER_HTML -> context.getString(R.string.auth_server_html)
        Api.NETWORK_ERROR -> context.getString(R.string.auth_network_error)
        Api.BAD_JSON -> context.getString(R.string.auth_server_bad_json)
        Api.INVALID_BASE -> context.getString(R.string.sync_no_server)
        else -> displayMessage(recovered.message.takeIf { it.isNotBlank() } ?: raw, fallback)
    }
}

private fun displayMessage(raw: String?, fallback: String): String {
    val clean = raw.orEmpty()
        .replace(Regex("[\\r\\n]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('{', '}', '"', ',')
        .take(180)
        .trim()
    return clean.ifBlank { fallback }
}

fun healthStatus(context: Context, result: String): String = when (result) {
    "ok" -> context.getString(R.string.auth_server_ok)
    Api.SERVER_HTML -> context.getString(R.string.auth_server_html)
    Api.NETWORK_ERROR -> context.getString(R.string.auth_network_error)
    Api.BAD_JSON -> context.getString(R.string.auth_server_bad_json)
    Api.INVALID_BASE -> context.getString(R.string.sync_no_server)
    else -> if (result.contains("config.php") || result.contains("database") || result.contains("invalid config")) {
        context.getString(R.string.auth_server_config, result.take(120))
    } else {
        context.getString(R.string.auth_server_fail, result.take(120))
    }
}

// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
