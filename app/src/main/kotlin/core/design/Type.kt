package core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yousuf.toolkit.R

/** خط كايرو (OFL) واضح للعربية. */
val AppFont = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_bold, FontWeight.Bold)
)

private fun Typography.withFont(family: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family)
)

fun professionalType() = Typography().withFont(AppFont)

fun simpleType(): Typography {
    val base = Typography().withFont(AppFont)
    return base.copy(
        bodyLarge = TextStyle(fontFamily = AppFont, fontSize = 20.sp, lineHeight = 30.sp),
        bodyMedium = TextStyle(fontFamily = AppFont, fontSize = 18.sp, lineHeight = 27.sp),
        titleLarge = TextStyle(fontFamily = AppFont, fontSize = 26.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontFamily = AppFont, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    )
}
