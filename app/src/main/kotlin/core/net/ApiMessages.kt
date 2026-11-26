// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.net

import android.content.Context
import com.yousuf.toolkit.R

fun authError(context: Context, t: Throwable): String {
    val e = t as? ApiException
    return when (e?.message) {
        Api.SERVER_HTML -> context.getString(R.string.auth_server_html)
        Api.NETWORK_ERROR -> context.getString(R.string.auth_network_error)
        Api.BAD_JSON -> context.getString(R.string.auth_server_bad_json)
        Api.INVALID_BASE -> context.getString(R.string.sync_no_server)
        else -> (e?.message ?: t.message).orEmpty().take(160)
            .ifBlank { context.getString(R.string.auth_failed) }
    }
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
