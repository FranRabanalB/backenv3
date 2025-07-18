// models/product/ProductCreate.kt
package com.example.models.product

import kotlinx.serialization.Serializable
@Serializable
data class ProductCreate(
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val numero: Int,
    val materiales: List<MaterialQty> = emptyList()

)

@Serializable
data class MaterialQty(
    val materialId: String,
    val cantidad: Double)
