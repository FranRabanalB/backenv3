package com.example.database

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.models.Users
import io.ktor.server.config.ApplicationConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val jdbcUrl = config.property("ktor.db.url").getString()
        val user    = config.property("ktor.db.user").getString()
        val pass    = config.property("ktor.db.password").getString()

        val hikari = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            username = user
            password = pass
            driverClassName =                       // 👈  CAMBIO
                if (jdbcUrl.startsWith("jdbc:h2")) "org.h2.Driver"
                else     "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(hikari))
        transaction {
            SchemaUtils.create(Users)
            if (Users.selectAll().empty()) {
                Users.insert {
                    it[username] = "admin"
                    it[email] = "admin@artistas.com"
                    it[passwordHash] = BCrypt.withDefaults()
                        .hashToString(12, "admin123".toCharArray())
                    it[roles] = "admin"
                }
            }
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
