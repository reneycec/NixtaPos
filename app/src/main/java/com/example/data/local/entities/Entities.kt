package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sucursales")
data class SucursalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val empresa: String,
    val tenant_id: String,
    val sync_status: String = "SINCRONIZADO",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val rol: String, // "CAJERO", "SUPERVISOR"
    val email: String,
    val password_hash: String, // BCrypt or similar hash of password
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "SINCRONIZADO",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "sesiones_caja")
data class SesionCajaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sucursal_id: String,
    val tenant_id: String,
    val usuario_apertura_id: String,
    val usuario_apertura_nombre: String,
    val usuario_cierre_id: String? = null,
    val usuario_cierre_nombre: String? = null,
    val fecha_apertura: Long = System.currentTimeMillis(),
    val fecha_cierre: Long? = null,
    val fondo_inicial: Long, // en centavos (e.g., $1350.00 -> 135000L)
    val monto_arqueo: Long? = null, // en centavos
    val diferencia: Long? = null, // en centavos
    val notas: String? = null,
    val estado: String = "ABIERTA", // "ABIERTA", "CERRADA"
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "movimientos_caja")
data class MovimientoCajaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sesion_caja_id: String,
    val tenant_id: String,
    val sucursal_id: String,
    val tipo: String, // "DEPOSITO", "RETIRO"
    val medio_pago: String = "EFECTIVO",
    val monto: Long, // en centavos
    val concepto: String,
    val usuario_id: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sku: String,
    val nombre: String,
    val categoria: String,
    val precio_con_impuestos: Long, // en centavos (e.g. $18.00 -> 1800L)
    val tasa_iva: Double = 0.16,
    val tasa_ieps: Double = 0.00,
    val stock: Double, // en kg o pieza
    val unidad_medida: String, // "kg", "pieza", "Bulto 500g"
    val tipo_articulo: String = "producto_terminado", // "materia_prima", "producto_terminado", "productos_adicionales"
    val alerta_minimo: Double = 10.0,
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "SINCRONIZADO",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

val ProductoEntity.isGranel: Boolean
    get() {
        val u = unidad_medida.lowercase()
        if (u == "kg" || u == "kilogramo" || u == "granel" || u == "litro") return true
        val n = nombre.lowercase()
        return n.contains("tortilla") || n.contains("masa")
    }

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val codigo: String,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val empresa: String,
    val puntos_acumulados: Long = 0,
    val condicion_pago: String, // "CONTADO", "CRÉDITO"
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "SINCRONIZADO",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val folio: String,
    val sesion_caja_id: String,
    val usuario_id: String = "",
    val cliente_id: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val subtotal: Long, // en centavos
    val impuestos_desglosados: Long = 0L, // en centavos
    val total: Long, // en centavos
    val puntos_ganados: Long = 0,
    val puntos_canjeados: Long = 0,
    val metodo_pago: String = "EFECTIVO", // "EFECTIVO", "TARJETA", "TRANSFERENCIA", "CRÉDITO"
    val estado: String = "COMPLETADA", // "COMPLETADA", "CANCELADA"
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "ventas_detalle")
data class VentaDetalleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val venta_id: String,
    val producto_id: String,
    val producto_nombre: String,
    val cantidad: Double,
    val precio_unitario: Long, // en centavos
    val subtotal: Long, // en centavos
    val notas_extra: String? = null,
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "pedidos_mayorista")
data class PedidoMayoristaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val folio: String,
    val cliente_id: String,
    val cliente_nombre: String,
    val direccion: String,
    val contacto: String,
    val fecha_entrega: String,
    val condicion: String = "CONTADO", // "CONTADO", "CRÉDITO"
    val total: Long, // en centavos
    val saldo: Long, // en centavos
    val estado: String = "ENTREGADO", // "ENTREGADO", "EN PROCESO", "PENDIENTE"
    val usuario_id: String = "",
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "pedidos_mayorista_detalle")
data class PedidoMayoristaDetalleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pedido_id: String,
    val producto_id: String,
    val producto_nombre: String,
    val cantidad: Double,
    val precio_unitario: Long, // en centavos
    val subtotal: Long, // en centavos
    val tenant_id: String,
    val sucursal_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

// --- CATÁLOGOS LOGÍSTICA ---

@Entity(tableName = "termos")
data class TermoEntity(
    @PrimaryKey val id: String,
    val codigo: String,
    val nombre: String,
    val precio: Long,
    val tenant_id: String,
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey val id: String,
    val placa: String,
    val modelo: String,
    val capacidad_kg: Double,
    val tenant_id: String,
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "vendedores")
data class VendedorEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val rol: String, // e.g. "REPARTIDOR"
    val tenant_id: String,
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

// --- TRANSACCIONAL LOGÍSTICA (OFFLINE-FIRST) ---

@Entity(tableName = "asignacion_termo")
data class AsignacionTermoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val repartidor_id: String,
    val vehiculo_id: String,
    val termo_id: String,
    val estado: String = "EN_RUTA", // "EN_RUTA", "RECIBIDO"
    val fecha_asignacion: Long = System.currentTimeMillis(),
    val fecha_recepcion: Long? = null,
    val tenant_id: String,
    val sucursal_id: String,
    val sucursal_recepcion_id: String? = null,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

@Entity(tableName = "asignacion_termo_detalle")
data class AsignacionTermoDetalleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val asignacion_id: String,
    val producto_id: String,
    val cantidad_salida: Double,
    val cantidad_regreso: Double = 0.0,
    val precio_unitario: Long, // en centavos
    val tenant_id: String,
    val sync_status: String = "PENDIENTE",
    val updated_at: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false
)

