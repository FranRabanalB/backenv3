package com.example.routes           // 👈 mismo paquete que los demás routes

import com.example.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.*
import java.util.UUID
import io.ktor.http.*
import com.example.security.hasRole
import com.example.security.Role

// routes/UserRoutes.kt
fun Route.userRoutes(repo: UserRepository) {      // ← asegúrate de recibir el repo

    authenticate("auth-jwt") {
        get("/users") {

                if (!call.hasRole(Role.admin))           // ⬅️ filtro de rol
                    return@get call.respond(HttpStatusCode.Forbidden, "Solo admins")

                call.respond(repo.getAllUsers())
                            // se serializa a JSON
        }

        /* ---------- 2) Obtener por ID ---------- */
        get("/users/{id}") {
            val idParam = call.parameters["id"]
            val id = try { UUID.fromString(idParam) }
            catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "UUID inválido o ausente"
                )
            }

            repo.findById(id)
                ?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound, "Usuario no existe")
        }

        delete("/users/{id}") {
            if (!call.hasRole(Role.admin))
                return@delete call.respond(HttpStatusCode.Forbidden, "Solo admins")

            val id = call.parameters["id"]?.let(UUID::fromString)
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "UUID inválido")

            if (repo.delete(id))
                call.respond(HttpStatusCode.NoContent)
            else
                call.respond(HttpStatusCode.NotFound, "Usuario no existe")
        }
    }
}
