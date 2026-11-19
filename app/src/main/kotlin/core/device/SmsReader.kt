package core.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import domain.model.SmsMoney
import java.util.Locale

object SmsReader {
    private val amountPattern = Regex(
        """(?:مبلغ|أضيف|اضيف|أُضيف|خصم|سحب|دفع|شراء|إيداع|ايداع|حوالة)[^\d]{0,30}([\d,]+(?:\.\d+)?)"""
    )
    private val balancePattern = Regex(
        """(?:رصيد|الرصيد|رص)\s*:?\s*([\d,]+(?:\.\d+)?)"""
    )
    private val incomeWords = listOf("أضيف", "اضيف", "إيداع", "ايداع", "دخل", "استلمت", "وارد")

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED

    fun read(context: Context, limit: Int = 300): List<SmsMoney> {
        if (!hasPermission(context)) return emptyList()
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val out = ArrayList<SmsMoney>()
        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            while (cursor.moveToNext() && out.size < limit) {
                val body = cursor.getString(bodyIndex).orEmpty()
                val parsed = parse(
                    sender = cursor.getString(addressIndex).orEmpty(),
                    body = body,
                    at = cursor.getLong(dateIndex)
                ) ?: continue
                out.add(parsed)
            }
        }
        return out
    }

    fun parse(sender: String, body: String, at: Long): SmsMoney? {
        val amount = amountPattern.find(body)?.groupValues?.getOrNull(1)
            ?.replace(",", "")?.toDoubleOrNull() ?: return null
        if (amount <= 0.0) return null
        val balance = balancePattern.find(body)?.groupValues?.getOrNull(1)
            ?.replace(",", "")?.toDoubleOrNull()
        val income = incomeWords.any { body.contains(it) }
        val key = String.format(Locale.US, "%d:%.2f:%d", at, amount, body.hashCode())
        return SmsMoney(
            key = key,
            sender = sender,
            body = body,
            amount = amount,
            balance = balance,
            income = income,
            at = at
        )
    }
}
