package com.example.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/** Enum con los roles válidos del sistema */
enum class Role { user, admin }

/**
 * Devuelve true si el JWT del `call` contiene el rol solicitado.
 * El claim se llama "roles" y es un array JSON (["admin","user"]).
 */
fun ApplicationCall.hasRole(role: Role): Boolean {
    val roles = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("roles")
        ?.asList(String::class.java)
        ?: return false                  // no hay claim → falséa

    return role.name in roles            // "admin" o "user"
}
