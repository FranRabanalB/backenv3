package com.example.routes

import com.example.models.product.ProductCreate
import com.example.models.product.Products
import com.example.repository.ProductRepository
import com.example.security.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.productRoutes(repo: ProductRepository) = authenticate("auth-jwt") {

    route("/products") {
        post {
            val userId = call.userId()
            val req    = call.receive<ProductCreate>()
            val dto    = repo.create(userId, req)
            call.respond(HttpStatusCode.Created, dto)
        }

        get {
            val userId = call.userId()
            call.respond(repo.listByUser(userId))
        }

        route("{id}") {
            get {
                val userId = call.userId()
                val id     = call.parameters["id"]!!.let(UUID::fromString)
                repo.findById(userId, id)
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.NotFound)
            }

            put {
                val userId = call.userId()
                val id     = call.parameters["id"]!!.let(UUID::fromString)
                val req    = call.receive<ProductCreate>()
                repo.update(userId, id, req)
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.NotFound)
            }

            delete {
                val userId = call.userId()
                val id     = call.parameters["id"]!!.let(UUID::fromString)
                if (repo.delete(userId, id))
                    call.respond(HttpStatusCode.NoContent)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
