package com.example.models.product


import com.example.models.Users
import com.example.models.Users.reference
import com.example.models.material.Materials
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Products : UUIDTable("products") {
    val userId      = reference("user_id", Users)
    val nombre      = varchar("nombre", 120)
    val descripcion = text("descripcion").nullable()
    val precio      = double("precio")

    val numero      = integer("numero")

    /** DTO que envías al cliente */
    @Serializable
    data class DTO(
        val id: String,
        val userId: String,
        val nombre: String,
        val descripcion: String?,
        val precio: Double,
        val numero: Int
    )

    /** Extensión para mapear fila → DTO */
    fun ResultRow.toDTO() = DTO(
        id          = this[id].value.toString(),
        userId      = this[userId].value.toString(),
        nombre      = this[nombre],
        descripcion = this[descripcion],
        precio      = this[precio],
        numero      = this[numero]

    )
}

object ProductMaterials : Table("product_material") {
    val product   = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val material  = reference("material_id", Materials, onDelete = ReferenceOption.CASCADE)
    val cantidad  = double("cantidad")
    override val primaryKey = PrimaryKey(product, material)
}
