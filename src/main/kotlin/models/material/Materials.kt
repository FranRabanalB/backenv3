// src/main/kotlin/models/Materials.kt
package com.example.models.material

import com.example.models.Users
import com.example.models.product.Products
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow

object Materials : UUIDTable("materials") {
    /** FK al dueño del material */
    val userId       = reference("user_id", Users)    // Users.id (UUID)

    val nombre       = varchar("nombre", 120)
    val descripcion  = text("descripcion").nullable()
    val cantidad     = integer("cantidad")
    val valor        = double("valor")
    val unidad       = varchar("unidad", 30)
    val stockMinimo  = integer("stock_minimo")

    /* ---------- DTO ---------- */
    @Serializable
    data class DTO(
        val id: String,
        val userId: String,
        val nombre: String,
        val descripcion: String? = null,
        val cantidad: Int,
        val valor: Double,
        val unidad: String,
        val stockMinimo: Int
    )

    /* ---------- mapeo Row → DTO ---------- */
    fun ResultRow.toDTO(): DTO = DTO(
        id          = this[id].value.toString(),
        userId      = this[userId].value.toString(),
        nombre      = this[nombre],
        descripcion = this[descripcion],
        cantidad    = this[cantidad],
        valor       = this[valor],
        unidad      = this[unidad],
        stockMinimo = this[stockMinimo]
    )
}
