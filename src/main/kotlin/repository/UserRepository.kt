package com.example.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.models.Users
import com.example.models.Users.toDTO
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

typealias UserDto = Users.DTO            // atajo opcional

class UserRepository {

    /* Helper genérico */
    private suspend inline fun <T> query(crossinline block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /* ---------- 1) Buscar por e-mail ---------- */
    suspend fun findByEmail(email: String): UserDto? = query {
        Users.select { Users.email eq email }
            .singleOrNull()
            ?.toDTO()
    }

    /* ---------- 2) Validar credenciales ---------- */
    suspend fun validateCredentials(email: String, password: String): UserDto? = query {
        Users
            .select { Users.email eq email }
            .singleOrNull()
            ?.takeIf { row ->
                val verified = BCrypt.verifyer()
                    .verify(password.toCharArray(), row[Users.passwordHash])
                    .verified
                verified
            }
            ?.toDTO()
    }

    /* ---------- 3) Registrar nuevo usuario ---------- */
    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        roles: List<String> = listOf("user")
    ): UserDto? = query {
        if (Users.select { Users.email eq email }.empty()) {
            val id = Users.insertAndGetId {
                it[Users.username]     = username
                it[Users.email]        = email
                it[Users.passwordHash] = BCrypt.withDefaults()
                    .hashToString(12, password.toCharArray())
                it[Users.roles]        = roles.joinToString(",")
            }.value

            Users.select { Users.id eq id }.single().toDTO()
        } else null
    }

    /* ---------- 4) Listar todos ---------- */
    suspend fun getAllUsers(): List<UserDto> = query {
        Users.selectAll().map { it.toDTO() }
    }

    /* ---------- 5) Buscar por ID ---------- */
    suspend fun findById(id: UUID): UserDto? = query {
        Users.select { Users.id eq id }.singleOrNull()?.toDTO()
    }
}
