// src/main/kotlin/repository/MaterialRepository.kt
package com.example.repository

import com.example.database.DatabaseFactory.dbQuery
import com.example.models.material.MaterialCreate
import com.example.models.material.Materials
import com.example.models.material.Materials.toDTO
import com.example.models.material.Materials.DTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class MaterialRepository {
    //cambio de usar lit materialcreate
    suspend fun create(ownerId: UUID, req: MaterialCreate): Materials.DTO = dbQuery {
        val id = Materials.insertAndGetId {
            it[userId]      = ownerId
            it[nombre]      = req.nombre
            it[descripcion] = req.descripcion
            it[cantidad]    = req.cantidad
            it[valor]       = req.valor
            it[unidad]      = req.unidad
            it[stockMinimo] = req.stockMinimo
        }.value
        Materials.select { Materials.id eq id }.single().toDTO()
    }


    suspend fun listByUser(ownerId: UUID): List<DTO> = dbQuery {
        Materials.select { Materials.userId eq ownerId }
            .map { it.toDTO() }
    }

    suspend fun findById(id: UUID, ownerId: UUID): DTO? = dbQuery {
        Materials
            .select { (Materials.id eq id) and (Materials.userId eq ownerId) }
            .singleOrNull()
            ?.toDTO()
    }

    suspend fun delete(id: UUID, ownerId: UUID): Boolean = dbQuery {
        Materials.deleteWhere {
            (Materials.id eq id) and (Materials.userId eq ownerId)
        } > 0
    }

    // ANTES
    suspend fun update(id: UUID, ownerId: UUID, dto: MaterialCreate): Boolean = dbQuery {
        Materials.update({ (Materials.id eq id) and (Materials.userId eq ownerId) }) {
            it[nombre]      = dto.nombre
            it[descripcion] = dto.descripcion
            it[cantidad]    = dto.cantidad
            it[valor]       = dto.valor
            it[unidad]      = dto.unidad
            it[stockMinimo] = dto.stockMinimo
        } > 0
    }

}
