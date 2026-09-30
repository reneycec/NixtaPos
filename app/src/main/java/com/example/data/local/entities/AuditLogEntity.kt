package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val usuario_id: String,
    val usuario_nombre: String,
    val accion: String, // "CANCELACION_VENTA", "CIERRE_CAJA_FORZADO", "CAMBIO_PRECIO", etc.
    val detalles: String, // JSON o texto libre con detalles
    val fecha: Long = System.currentTimeMillis(),
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis()
)
