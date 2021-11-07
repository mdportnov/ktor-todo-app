package mephi.ru.auth

import io.ktor.util.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

var SECRET_KEY = "898748674728934843" // для хэширования
var JWT_SECRET = "898748674728934843" // для аутентификации

val hashKey = hex(SECRET_KEY)

val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")

fun hash(password: String): String {
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}