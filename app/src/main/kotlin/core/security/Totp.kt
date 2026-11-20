package core.security

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Totp {
    fun code(secret: String, at: Long = System.currentTimeMillis()): String? {
        val key = decodeBase32(secret) ?: return null
        if (key.isEmpty()) return null
        val counter = at / 1000L / 30L
        val data = ByteArray(8) { i -> ((counter shr (56 - i * 8)) and 0xFF).toByte() }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val value = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)
        return (value % 1_000_000).toString().padStart(6, '0')
    }

    fun secondsLeft(at: Long = System.currentTimeMillis()): Int =
        30 - ((at / 1000L) % 30L).toInt()

    fun normalize(secret: String): String =
        secret.uppercase().filter { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" }

    private fun decodeBase32(value: String): ByteArray? {
        val clean = normalize(value).trimEnd('=')
        if (clean.isEmpty()) return null
        val out = ArrayList<Byte>(clean.length * 5 / 8)
        var buffer = 0
        var bits = 0
        for (char in clean) {
            val index = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(char)
            if (index < 0) return null
            buffer = (buffer shl 5) or index
            bits += 5
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xFF).toByte())
            }
            buffer = buffer and ((1 shl bits) - 1)
        }
        return out.toByteArray()
    }
}
