package com.example.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object AuthConfig {
    const val SECRET = "your_secret_key" // Cambia esto en producción!
    const val ISSUER = "your_app_name"
    const val AUDIENCE = "your_app_audience"
    const val REALM = "your_app_realm"

    val algorithm = Algorithm.HMAC256(SECRET)

    fun makeJwtVerifier() = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: Int, roles: List<String>): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withArrayClaim("roles", roles.toTypedArray())
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24 horas
            .sign(algorithm)
    }
}