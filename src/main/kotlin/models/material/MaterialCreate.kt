// models/MaterialCreate.kt
package com.example.models.material

import kotlinx.serialization.Serializable

@Serializable
data class MaterialCreate(
    val nombre: String,
    val descripcion: String? = null,
    val cantidad: Int,
    val valor: Double,
    val unidad: String,
    val stockMinimo: Int
)
