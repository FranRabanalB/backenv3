package com.example.models.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val roles: List<String>
)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: Int
)