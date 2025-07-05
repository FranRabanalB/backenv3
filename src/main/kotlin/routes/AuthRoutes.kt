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
fun generateToken(subject: String, roles: List<String>, cfg: JwtConfig): Pair<String, Long> {
    val expiresAt = Instant.now().plusMillis(cfg.validityMs)

    val token = JWT.create()
        .withIssuer(cfg.issuer)
        .withAudience(cfg.audience)
        .withSubject(subject)                   // <-- ahora será el UUID
        .withArrayClaim("roles", roles.toTypedArray())
        .withExpiresAt(Date.from(expiresAt))
        .sign(Algorithm.HMAC256(cfg.secret))

    return token to expiresAt.toEpochMilli()
}


/* ---------- 4) Definición de las rutas ---------- */
fun Route.authRoutes(userRepo: UserRepository, jwtConfig: JwtConfig) {

    route("/auth") {

        post("/register") {
            val body = call.receive<RegisterRequest>()
            // …validaciones…

            val created = userRepo.registerUser(body.username, body.email, body.password)
                ?: return@post call.respond(HttpStatusCode.Conflict, "Ese correo ya está registrado")

            // Usa el ID recién creado (UUID) como subject
            val (jwt, exp) = generateToken(created.id, created.roles, jwtConfig)

            call.respond(HttpStatusCode.Created, AuthResponse(jwt, exp))
        }


        post("/login") {
            val body = call.receive<LoginRequest>()
            val user = userRepo.validateCredentials(body.email, body.password)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, "Credenciales incorrectas")

            // Usa también aquí el ID
            val (jwt, exp) = generateToken(user.id, user.roles, jwtConfig)

            call.respond(AuthResponse(jwt, exp))
        }


    }
}
