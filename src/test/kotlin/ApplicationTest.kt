package com.example

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.issuer" to "http://test",
                "ktor.jwt.audience" to "test-audience",
                "ktor.jwt.secret" to "TEST-SECRET"
            )
        }

        application { module() }   // ← se ejecuta después de inyectar config

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("API activa", bodyAsText())
        }
    }



}
