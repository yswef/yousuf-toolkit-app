// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletTxParserTest {
    private val rules = WalletRules()
    private val at = 1_700_000_000_000L

    @Test
    fun incomingTransferHasCounterpartyAndBalance() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "جيب - تحويل مشترك",
            "أضيف 500 ر.صدي تحويل مشترك رص:1100 من الجهة أ 700000001",
            at
        )
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(500.0, item.amount, 0.001)
        assertEquals(1100.0, item.balance!!, 0.001)
        assertTrue(item.income)
        assertTrue(item.note.contains("من الجهة أ"))
        assertEquals("other", item.category)
    }

    @Test
    fun outgoingTransferIsExpense() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "جيب - تحويل مشترك",
            "خصم 800 ر.صدي تحويل مشترك رص:600 إلى الجهة ب 700000002",
            at
        )
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(800.0, item.amount, 0.001)
        assertFalse(item.income)
        assertTrue(item.note.contains("إلى الجهة ب"))
        assertEquals("other", item.category)
    }

    @Test
    fun purchaseUsesShoppingCategory() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "جيب - دفع مشتريات",
            "خصم 350 ر.صدي مشتريات رص:250 إلى نقطة تاجر تجريبي 500000003",
            at
        )
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(350.0, item.amount, 0.001)
        assertFalse(item.income)
        assertEquals("shopping", item.category)
        assertTrue(item.note.contains("إلى نقطة تاجر تجريبي"))
    }

    @Test
    fun groupedNotificationYieldsOneEntryPerOperation() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "الرسائل • Jaib",
            "أضيف 500 ر.صدي تحويل مشترك رص:1100 من الجهة أ 700000001\nأضيف 300 ر.صدي تحويل مشترك رص:1400 من الجهة ب 700000002",
            at
        )
        assertEquals(2, items.size)
        assertEquals(500.0, items[0].amount, 0.001)
        assertEquals(300.0, items[1].amount, 0.001)
    }

    @Test
    fun smsAndNotificationShareDedupeKey() {
        val notification = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "جيب - تحويل مشترك",
            "أضيف 500 ر.صدي تحويل مشترك رص:1100 من الجهة أ 700000001",
            at
        ).first()
        val sms = WalletTxParser.parseAll(
            rules,
            "sms",
            "Jaib",
            "",
            "أضيف 500 ر.صدي تحويل مشترك رص:1100 من الجهة أ 700000001",
            at
        ).first()
        assertEquals(notification.key, sms.key)
    }

    @Test
    fun unrelatedNotificationIsIgnored() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.shop",
            "خصم وعرض",
            "خصم 50 ر.صدي لفترة محدودة",
            at
        )
        assertTrue(items.isEmpty())
    }

    @Test
    fun arabicIndicDigitsAreNormalized() {
        val items = WalletTxParser.parseAll(
            rules,
            "notification",
            "com.example.wallet",
            "جيب - تحويل مشترك",
            "أضيف ٣٠٠ ر.صدي تحويل مشترك رص:٩٠٠ من الجهة أ ٧٠٠٠٠٠٠٠١",
            at
        )
        assertEquals(1, items.size)
        assertEquals(300.0, items.first().amount, 0.001)
        assertEquals(900.0, items.first().balance!!, 0.001)
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
