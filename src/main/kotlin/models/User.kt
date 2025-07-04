// src/main/kotlin/models/User.kt
package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

/** ---------- 1) Objeto tabla Exposed ---------- */
object Users : UUIDTable("users") {
    // Columnas
    val username     = varchar("username", 80).uniqueIndex()
    val email        = varchar("email", 120).uniqueIndex()
    val passwordHash = varchar("password_hash", 72)
    val roles        = text("roles")    // almacenaremos CSV o JSON simple

    /** ---------- 2) DTO serializable ---------- */
    @Serializable
    data class DTO(
        val id: String,                 // UUID como String para JSON
        val username: String,
        val email: String,
        val roles: List<String>
    )

    /** ---------- 3) Helpers de mapeo ---------- */
    fun ResultRow.toDTO(): DTO = DTO(
        id        = this[id].value.toString(),
        username  = this[username],
        email     = this[email],
        roles     = this[roles].split(',').filter { it.isNotBlank() }
    )

    /** Convierte lista a CSV antes de insertar/actualizar */
    fun List<String>.toCsv() = joinToString(",")
}
