// src/main/kotlin/security/AuthUtils.kt
package com.example.security

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal
import java.util.UUID

fun ApplicationCall.userId(): UUID =
    principal<JWTPrincipal>()!!.payload.subject.let(UUID::fromString)
