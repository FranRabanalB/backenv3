
package com.example.repository

import com.example.database.DatabaseFactory.dbQuery
import com.example.models.material.Materials
import com.example.models.product.Products.toDTO
import com.example.models.product.ProductCreate
import com.example.models.product.ProductMaterials
import com.example.models.product.Products
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ProductRepository {

    suspend fun create(ownerId: UUID, req: ProductCreate): Products.DTO = dbQuery {
        // 1️⃣ Creamos el product y obtenemos su ID
        val productId = Products.insertAndGetId {
            it[userId]      = ownerId
            it[nombre]      = req.nombre
            it[descripcion] = req.descripcion
            it[precio]      = req.precio
            it[numero]      = req.numero
        }.value

        // 2️⃣ Por cada material, comprobamos stock, actualizamos y relacionamos
        req.materiales.forEach { m ->
            val matId = UUID.fromString(m.materialId)

            // 2a. Verificar stock (Int)
            val stockActual: Int = Materials
                .slice(Materials.cantidad)
                .select { Materials.id eq matId }
                .single()[Materials.cantidad]

            if (stockActual < m.cantidad) {
                error("No hay suficiente stock del material $matId (tienes $stockActual, pediste ${m.cantidad})")
            }

            // 2b. Calcular nuevo stock como Int
            val nuevoStock: Int = stockActual - m.cantidad.toInt()

            // 2c. Actualizar la tabla Materials
            Materials.update({ Materials.id eq matId }) {
                it[cantidad] = nuevoStock
            }
        }

        // 3️⃣ Finalmente, devolvemos el producto completo
        Products
            .select { Products.id eq productId }
            .single()
            .toDTO()
    }
    suspend fun listByUser(ownerId: UUID): List<Products.DTO> = dbQuery {
        Products
            .select { Products.userId eq ownerId }
            .map { it.toDTO() }
    }

    suspend fun findById(ownerId: UUID, id: UUID): Products.DTO? = dbQuery {
        Products
            .select { (Products.userId eq ownerId) and (Products.id eq id) }
            .singleOrNull()
            ?.toDTO()
    }

    suspend fun update(ownerId: UUID, id: UUID, req: ProductCreate): Products.DTO? = dbQuery {
        // 1️⃣ Actualizar campos básicos del producto
        val updated = Products.update({
            (Products.userId eq ownerId) and (Products.id eq id)
        }) {
            it[nombre]      = req.nombre
            it[descripcion] = req.descripcion
            it[precio]      = req.precio
            it[numero]      = req.numero
        }
        if (updated == 0) return@dbQuery null

        // 2️⃣ Revertir stock de los materiales antiguos
        ProductMaterials
            .select { ProductMaterials.product eq id }
            .forEach { row ->
                val matId = row[ProductMaterials.material]
                val qty   = row[ProductMaterials.cantidad]
                // Sumar de vuelta al stock
                Materials.update({ Materials.id eq matId }) {
                    with(org.jetbrains.exposed.sql.SqlExpressionBuilder) {
                        it[cantidad] = Materials.cantidad + qty.toInt()
                    }
                }
            }

        // 3️⃣ Borrar las entradas antiguas en la tabla intermedia
        ProductMaterials.deleteWhere { ProductMaterials.product eq id }

        // 4️⃣ Insertar las nuevas y descontar stock
        req.materiales.forEach { m ->
            val matId = UUID.fromString(m.materialId)

            // Verificar stock
            val stockActual: Int = Materials
                .slice(Materials.cantidad)
                .select  { Materials.id eq matId }
                .single()[Materials.cantidad]

            require(stockActual >= m.cantidad.toInt()) {
                "Stock insuficiente para material $matId"
            }

            // Descontar stock
            Materials.update({ Materials.id eq matId }) {
                it[cantidad] = stockActual - m.cantidad.toInt()
            }

            // Registrar en intermedia
            ProductMaterials.insert {
                it[product]  = id
                it[material] = matId
                it[cantidad] = m.cantidad
            }
        }

        // 5️⃣ Devolver el DTO actualizado
        Products
            .select { Products.id eq id }
            .single()
            .toDTO()
    }


    suspend fun delete(ownerId: UUID, id: UUID): Boolean = dbQuery {
        Products.deleteWhere { (Products.userId eq ownerId) and (Products.id eq id) } > 0
    }
    /* listByUser, findById, update, delete → mismos patrones que MaterialRepository */
}
