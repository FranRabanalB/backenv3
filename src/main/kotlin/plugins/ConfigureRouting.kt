package com.example.plugins

import com.example.repository.UserRepository
import com.example.repository.MaterialRepository
import com.example.routes.materialRoutes
import com.example.routes.authRoutes
import com.example.routes.userRoutes
import com.example.routes.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/* ahora con parámetros */
fun Application.configureRouting(
    userRepo: UserRepository,
    materialRepo: MaterialRepository,
    jwtCfg: JwtConfig
) {
    routing {
        get("/") { call.respondText("API activa") }
        authRoutes(userRepo, jwtCfg)
        userRoutes(userRepo)
        materialRoutes(materialRepo)
    }
}
