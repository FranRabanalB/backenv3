package com.example.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm


/* ---------- 1) DTOs de entrada / salida ---------- */
@Serializable data class RegisterRequest(val username: String, val email: String, val password: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class AuthResponse(val token: String, val expiresAt: Long)

/* ---------- 2) Configuración JWT que leeremos desde Application ---------- */
data class JwtConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val validityMs: Long = 36_000_00L  // 1 h
)

/* ---------- 3) Función para generar el token ---------- */
fun generateToken(email: String, roles: List<String>, cfg: JwtConfig): Pair<String, Long> {
    val expiresAt = Instant.now().plusMillis(cfg.validityMs)
    val token = JWT.create()
        .withIssuer(cfg.issuer)
        .withAudience(cfg.audience)
        .withSubject(email)
        .withArrayClaim("roles", roles.toTypedArray())   // ← array, no List
        .withExpiresAt(Date.from(expiresAt))
        .sign(Algorithm.HMAC256(cfg.secret))             // firma HMAC-SHA256
    return token to expiresAt.toEpochMilli()
}


/* ---------- 4) Definición de las rutas ---------- */
fun Route.authRoutes(userRepo: UserRepository, jwtConfig: JwtConfig) {

    route("/auth") {

        post("/register") {
            val body = call.receive<RegisterRequest>()

            /* Validaciones mínimas */
            if (body.username.isBlank() || body.email.isBlank() || body.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, "Datos inválidos")
                return@post
            }

            val created = userRepo.registerUser(body.username, body.email, body.password)
            if (created == null) {
                call.respond(HttpStatusCode.Conflict, "Ese correo ya está registrado")
                return@post
            }

            val (jwt, exp) = generateToken(created.email, created.roles, jwtConfig)
            call.respond(HttpStatusCode.Created, AuthResponse(jwt, exp))
        }

        post("/login") {

            val body = call.receive<LoginRequest>()

            val user = userRepo.validateCredentials(body.email, body.password)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales incorrectas")
                return@post
            }

            val (jwt, exp) = generateToken(user.email, user.roles, jwtConfig)

            call.respond(AuthResponse(jwt, exp))
        }

    }
}
