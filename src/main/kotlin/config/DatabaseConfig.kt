package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    private const val DB_URL = "jdbc:postgresql://pgsqltrans.face.ubiobio.cl/frabanal_bd"
    private const val DB_USER = "frabanal"
    private const val DB_PASSWORD = "francisca2025"

    fun init() {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = DB_URL
            username = DB_USER
            password = DB_PASSWORD
            maximumPoolSize = 5
            driverClassName = "org.postgresql.Driver"
        }

        Database.connect(HikariDataSource(hikariConfig))
    }
}