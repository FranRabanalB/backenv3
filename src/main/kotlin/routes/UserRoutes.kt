package com.example.routes           // 👈 mismo paquete que los demás routes

import com.example.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.*

fun Route.userRoutes(userRepo: UserRepository) {
    authenticate {                               // requiere JWT
        get("/me") {
            // 1) Obtén el claim como String?
            val principal = call.principal<JWTPrincipal>()
            val email: String? = principal?.getClaim("email", String::class)

            // 2) Si el claim es nulo, responde 401
            if (email.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                return@get
            }

            // 3) Busca el usuario (repo devuelve User?)
            val user = userRepo.findByEmail(email)
                ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

            // 4) Mapea a DTO y responde
            call.respond(UserDto(name = user.username))
        }
    }
    /**  lista de usuarios (protector opcional) **/
    authenticate(optional = true) {          // pon 'optional=true' si quieres que funcione con o sin token
        get("/users") {
            call.respond(userRepo.getAllUsers())
        }
    }
}
@kotlinx.serialization.Serializable
data class UserDto(val name: String)