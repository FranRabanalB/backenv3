// src/main/kotlin/routes/MaterialRoutes.kt
package com.example.routes

import com.example.models.material.MaterialCreate
import com.example.repository.MaterialRepository
import com.example.security.userId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.materialRoutes(repo: MaterialRepository) {

    authenticate("auth-jwt") {

        /** Crear material */
        post("/materials") {
            val req = call.receive<MaterialCreate>()           // ← nuevo DTO
            val created = repo.create(call.userId(), req)
            call.respond(HttpStatusCode.Created, created)
        }


        /** Listar mis materiales */
        get("/materials") {
            val list = repo.listByUser(call.userId())
            call.respond(list)
        }

        /** Obtener uno */
        get("/materials/{id}") {
            val id = call.parameters["id"]?.let(UUID::fromString)
                ?: return@get call.respond(HttpStatusCode.BadRequest, "UUID inválido")

            repo.findById(id, call.userId())
                ?.let { call.respond(it) }
                ?: call.respond(HttpStatusCode.NotFound, "No encontrado")
        }

        /** Actualizar */
        put("/materials/{id}") {
            val id = call.parameters["id"]?.let(UUID::fromString)
                ?: return@put call.respond(HttpStatusCode.BadRequest, "UUID inválido")

            val req = call.receive<MaterialCreate>()         // recibe el DTO de entrada
            val ok  = repo.update(id, call.userId(), req)    // ahora coincide la firma

            if (ok) call.respond(HttpStatusCode.NoContent)
            else   call.respond(HttpStatusCode.NotFound, "No encontrado")
        }


        /** Eliminar */
        delete("/materials/{id}") {
            val id = call.parameters["id"]?.let(UUID::fromString)
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "UUID inválido")

            val ok = repo.delete(id, call.userId())
            if (ok) call.respond(HttpStatusCode.NoContent)
            else    call.respond(HttpStatusCode.NotFound, "No encontrado")
        }
    }
}
