package com.example

import com.example.database.DatabaseFactory
import com.example.repository.UserRepository            // ✔
import com.example.routes.JwtConfig
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.module() {

    DatabaseFactory.init(environment.config)

    val jwtCfg = JwtConfig(
        issuer   = environment.config.property("ktor.jwt.issuer").getString(),
        audience = environment.config.property("ktor.jwt.audience").getString(),
        secret   = environment.config.property("ktor.jwt.secret").getString()
    )
    val userRepo = UserRepository()

    /* 1️⃣  Instalar Authentication */
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "artistas-app"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtCfg.secret))
                    .withIssuer(jwtCfg.issuer)
                    .withAudience(jwtCfg.audience)
                    .build()
            )
            validate { cred ->
                if (cred.payload.subject != null) JWTPrincipal(cred.payload) else null
            }
        }
    }

    /* 2️⃣  Resto de plugins */
    configureSerialization()

    /* 3️⃣  Rutas (ya pueden usar `authenticate { … }`) */
    configureRouting(userRepo, jwtCfg)
}
