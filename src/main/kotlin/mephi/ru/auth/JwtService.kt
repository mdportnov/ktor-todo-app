package mephi.ru.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import mephi.ru.model.User
import java.util.*

class JwtService {
    private val issuer = "TodoServer"
    private val algorithm = Algorithm.HMAC512(JWT_SECRET)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", user.userId)
        .withExpiresAt(expiresAt())
        .sign(algorithm)

    private fun expiresAt() =  // 24 часа
        Date(System.currentTimeMillis() + 3_600_000 * 24)
//    Date(System.currentTimeMillis() + 4000) // 4 секунды
}