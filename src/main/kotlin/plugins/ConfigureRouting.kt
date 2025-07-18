package com.example.plugins

import com.example.repository.UserRepository
import com.example.repository.MaterialRepository
import com.example.repository.ProductRepository
import com.example.routes.materialRoutes
import com.example.routes.authRoutes
import com.example.routes.userRoutes
import com.example.routes.JwtConfig
import com.example.routes.productRoutes
import io.ktor.server.auth.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/* ahora con parámetros */
fun Application.configureRouting(
    userRepo: UserRepository,
    materialRepo: MaterialRepository,
    jwtCfg: JwtConfig,
    productRepo:ProductRepository
) {
    routing {
        get("/") { call.respondText("API activa") }
        authRoutes(userRepo, jwtCfg)
        authenticate("auth-jwt") {
            userRoutes(userRepo)
            materialRoutes(materialRepo)
            productRoutes(productRepo)
        }
    }
}
