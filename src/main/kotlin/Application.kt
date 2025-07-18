// src/main/kotlin/com/example/Application.kt
package com.example

import com.example.config.DatabaseConfig
import com.example.models.Users
import com.example.models.material.Materials
import com.example.models.product.Products
import com.example.models.product.ProductMaterials
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.repository.UserRepository
import com.example.repository.MaterialRepository
import com.example.repository.ProductRepository
import com.example.routes.JwtConfig

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

// **Estos imports son para la creación de tablas**
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.module() {
    DatabaseConfig.init()

    transaction {
        SchemaUtils.create(Users, Materials, Products, ProductMaterials)

    }


    // 4️⃣ Configura JWT, Serialización y Rutas…
    val jwtCfg = JwtConfig(
        issuer   = environment.config.property("ktor.jwt.issuer").getString(),
        audience = environment.config.property("ktor.jwt.audience").getString(),
        secret   = environment.config.property("ktor.jwt.secret").getString()
    )
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "artistas-app"
            verifier(
                JWT.require(Algorithm.HMAC256(jwtCfg.secret))
                    .withIssuer(jwtCfg.issuer)
                    .withAudience(jwtCfg.audience)
                    .build()
            )
            validate { cred -> if (cred.payload.subject != null) JWTPrincipal(cred.payload) else null }
        }
    }

    configureSerialization()

    val userRepo     = UserRepository()
    val materialRepo = MaterialRepository()
    val productRepo  = ProductRepository()
    configureRouting(userRepo, materialRepo, jwtCfg, productRepo)
}
