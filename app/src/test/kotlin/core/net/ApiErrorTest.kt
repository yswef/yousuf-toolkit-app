// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorTest {
    @Test
    fun cleanJsonKeepsArabicMessageAndCode() {
        val error = parseServerError(
            "{\"code\":\"missing_app_token\",\"message\":\"مفتاح ربط التطبيق مطلوب\"}",
            401
        )
        assertEquals("missing_app_token", error.code)
        assertEquals("مفتاح ربط التطبيق مطلوب", error.message)
    }

    @Test
    fun truncatedJsonStillFindsTheCode() {
        val error = parseServerError("code\":\"missing_app_token\",\"mess\"}", 401)
        assertEquals("missing_app_token", error.code)
        assertEquals("missing_app_token", error.message)
    }

    @Test
    fun legacyErrorFieldIsUsedAsMessage() {
        val error = parseServerError("{\"error\":\"invalid credentials\"}", 400)
        assertEquals("", error.code)
        assertEquals("invalid credentials", error.message)
    }

    @Test
    fun emptyOrHtmlBodyFallsBackToHttpCode() {
        assertEquals("HTTP 401", parseServerError("", 401).message)
        assertEquals("HTTP 500", parseServerError("<html><body>oops</body></html>", 500).message)
    }

    @Test
    fun unrelatedCodeIsNotTreatedAsServerError() {
        val error = parseServerError("{\"code\":\"LIC-2026\",\"token\":\"abc\"}", 200)
        assertEquals("", error.code)
        assertTrue(error.message.isNotBlank())
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
