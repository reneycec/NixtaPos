package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entities.AsignacionTermoDetalleEntity
import com.example.data.local.entities.AsignacionTermoEntity
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.ClienteEntity
import com.example.data.local.entities.MovimientoCajaEntity
import com.example.data.local.entities.PedidoMayoristaDetalleEntity
import com.example.data.local.entities.PedidoMayoristaEntity
import com.example.data.local.entities.ProductoEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.data.local.entities.SucursalEntity
import com.example.data.local.entities.TermoEntity
import com.example.data.local.entities.UsuarioEntity
import com.example.data.local.entities.VehiculoEntity
import com.example.data.local.entities.VendedorEntity
import com.example.data.local.entities.VentaDetalleEntity
import com.example.data.local.entities.VentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {

    // --- SUCURSALES ---
    @Query("SELECT * FROM sucursales WHERE tenant_id = :tenantId AND is_deleted = 0")
    fun getSucursales(tenantId: String): Flow<List<SucursalEntity>>

    @Query("SELECT * FROM sucursales WHERE id = :sucursalId LIMIT 1")
    fun getSucursalById(sucursalId: String): Flow<SucursalEntity?>

    @Upsert
    suspend fun upsertSucursal(sucursal: SucursalEntity)

    @Upsert
    suspend fun upsertSucursales(sucursales: List<SucursalEntity>)

    // --- USUARIOS ---
    @Query("SELECT * FROM usuarios WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0")
    fun getUsuarios(tenantId: String, sucursalId: String): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getUsuarioById(id: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE email = :email AND is_deleted = 0 LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email AND rol = 'SUPERVISOR' AND is_deleted = 0 LIMIT 1")
    suspend fun getSupervisorByEmail(email: String): UsuarioEntity?

    @Upsert
    suspend fun upsertUsuario(usuario: UsuarioEntity)

    @Upsert
    suspend fun upsertUsuarios(usuarios: List<UsuarioEntity>)

    // --- SESIONES DE CAJA ---
    @Query("SELECT * FROM sesiones_caja WHERE sucursal_id = :sucursalId AND tenant_id = :tenantId AND estado = 'ABIERTA' AND is_deleted = 0 LIMIT 1")
    fun getSesionActiva(tenantId: String, sucursalId: String): Flow<SesionCajaEntity?>

    @Query("SELECT * FROM sesiones_caja WHERE sucursal_id = :sucursalId AND tenant_id = :tenantId AND estado = 'ABIERTA' AND is_deleted = 0 LIMIT 1")
    suspend fun getSesionActivaSync(tenantId: String, sucursalId: String): SesionCajaEntity?

    @Query("SELECT * FROM sesiones_caja WHERE sucursal_id = :sucursalId AND tenant_id = :tenantId AND is_deleted = 0 ORDER BY fecha_apertura DESC")
    fun getHistorialSesiones(tenantId: String, sucursalId: String): Flow<List<SesionCajaEntity>>

    @Query("SELECT * FROM sesiones_caja WHERE id IN (:ids)")
    suspend fun getSesionesByIds(ids: List<String>): List<SesionCajaEntity>

    @Upsert
    suspend fun upsertSesionCaja(sesion: SesionCajaEntity)

    @Upsert
    suspend fun upsertSesionesCaja(sesiones: List<SesionCajaEntity>)

    @Query("UPDATE sesiones_caja SET estado = 'CERRADA', sync_status = 'SINCRONIZADO' WHERE estado = 'ABIERTA' AND sync_status = 'SINCRONIZADO' AND id NOT IN (:activeIds)")
    suspend fun cerrarSesionesObsoletas(activeIds: List<String>)

    // --- MOVIMIENTOS DE CAJA ---
    @Query("SELECT * FROM movimientos_caja WHERE sesion_caja_id = :sesionId AND is_deleted = 0 ORDER BY fecha DESC")
    fun getMovimientosSesion(sesionId: String): Flow<List<MovimientoCajaEntity>>

    @Upsert
    suspend fun upsertMovimiento(movimiento: MovimientoCajaEntity)

    @Query("SELECT * FROM movimientos_caja WHERE sync_status = 'PENDIENTE'")
    suspend fun getMovimientosPendientesSync(): List<MovimientoCajaEntity>

    @Query("UPDATE movimientos_caja SET sync_status = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarMovimientosSincronizados(ids: List<String>)

    // --- PRODUCTOS ---
    @Query("SELECT * FROM productos WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 ORDER BY nombre ASC")
    fun getProductos(tenantId: String, sucursalId: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getProductoById(id: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE (sku = :query OR nombre LIKE '%' || :query || '%') AND tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 LIMIT 1")
    suspend fun getProductoBySkuOrName(query: String, tenantId: String, sucursalId: String): ProductoEntity?

    @Upsert
    suspend fun upsertProducto(producto: ProductoEntity)

    @Upsert
    suspend fun upsertProductos(productos: List<ProductoEntity>)

    @Query("UPDATE productos SET stock = stock - :cantidad WHERE id = :productoId")
    suspend fun reducirStock(productoId: String, cantidad: Double)

    // --- CLIENTES ---
    @Query("SELECT * FROM clientes WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 ORDER BY nombre ASC")
    fun getClientes(tenantId: String, sucursalId: String): Flow<List<ClienteEntity>>

    @Upsert
    suspend fun upsertCliente(cliente: ClienteEntity)

    @Upsert
    suspend fun upsertClientes(clientes: List<ClienteEntity>)

    // --- VENTAS ---
    @Query("SELECT * FROM ventas WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 ORDER BY fecha DESC")
    fun getTodasVentas(tenantId: String, sucursalId: String): Flow<List<VentaEntity>>

    @Query("SELECT * FROM ventas WHERE sesion_caja_id = :sesionId AND is_deleted = 0 ORDER BY fecha DESC")
    fun getVentasPorSesion(sesionId: String): Flow<List<VentaEntity>>

    @Query("SELECT SUM(total) FROM ventas WHERE sesion_caja_id = :sesionId AND metodo_pago = 'EFECTIVO' AND estado = 'COMPLETADA' AND is_deleted = 0")
    fun getTotalVentasEfectivoSesion(sesionId: String): Flow<Long?>

    @Query("SELECT SUM(total) FROM ventas WHERE sesion_caja_id = :sesionId AND metodo_pago IN ('TARJETA', 'TRANSFERENCIA') AND estado = 'COMPLETADA' AND is_deleted = 0")
    fun getTotalVentasOtrosSesion(sesionId: String): Flow<Long?>

    @Upsert
    suspend fun upsertVenta(venta: VentaEntity)

    @Upsert
    suspend fun upsertVentaDetalles(detalles: List<VentaDetalleEntity>)

    @Query("SELECT * FROM ventas_detalle WHERE venta_id = :ventaId AND is_deleted = 0")
    suspend fun getVentaDetallesSync(ventaId: String): List<VentaDetalleEntity>

    @Query("SELECT * FROM ventas_detalle WHERE venta_id = :ventaId AND is_deleted = 0")
    fun getVentaDetallesFlow(ventaId: String): Flow<List<VentaDetalleEntity>>

    // --- PEDIDOS MAYORISTAS ---
    @Query("SELECT * FROM pedidos_mayorista WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 ORDER BY fecha_entrega DESC")
    fun getPedidosMayorista(tenantId: String, sucursalId: String): Flow<List<PedidoMayoristaEntity>>

    @Upsert
    suspend fun upsertPedidoMayorista(pedido: PedidoMayoristaEntity)

    @Upsert
    suspend fun upsertPedidosMayorista(pedidos: List<PedidoMayoristaEntity>)

    @Upsert
    suspend fun upsertPedidoMayoristaDetalles(detalles: List<PedidoMayoristaDetalleEntity>)

    @Query("SELECT * FROM pedidos_mayorista_detalle WHERE pedido_id = :pedidoId AND is_deleted = 0")
    suspend fun getPedidoDetallesSync(pedidoId: String): List<PedidoMayoristaDetalleEntity>

    @Query("SELECT * FROM pedidos_mayorista_detalle WHERE pedido_id = :pedidoId AND is_deleted = 0")
    fun getPedidoDetallesFlow(pedidoId: String): Flow<List<PedidoMayoristaDetalleEntity>>

    // --- CATÁLOGOS LOGÍSTICA ---
    @Query("SELECT * FROM termos WHERE (tenant_id = :tenantId OR tenant_id = '' OR tenant_id = '8c5e065c-6622-4a00-9854-47b794170068') AND is_deleted = 0 ORDER BY nombre ASC")
    fun getTermos(tenantId: String): Flow<List<TermoEntity>>

    @Upsert
    suspend fun upsertTermos(termos: List<TermoEntity>)

    @Query("SELECT * FROM vehiculos WHERE (tenant_id = :tenantId OR tenant_id = '' OR tenant_id = '8c5e065c-6622-4a00-9854-47b794170068') AND is_deleted = 0 ORDER BY placa ASC")
    fun getVehiculos(tenantId: String): Flow<List<VehiculoEntity>>

    @Upsert
    suspend fun upsertVehiculos(vehiculos: List<VehiculoEntity>)

    @Query("SELECT * FROM vendedores WHERE (tenant_id = :tenantId OR tenant_id = '' OR tenant_id = '8c5e065c-6622-4a00-9854-47b794170068') AND is_deleted = 0 ORDER BY nombre ASC")
    fun getVendedores(tenantId: String): Flow<List<VendedorEntity>>

    @Upsert
    suspend fun upsertVendedores(vendedores: List<VendedorEntity>)

    // --- TRANSACCIONAL LOGÍSTICA ---
    @Query("SELECT * FROM asignacion_termo WHERE tenant_id = :tenantId AND sucursal_id = :sucursalId AND is_deleted = 0 ORDER BY fecha_asignacion DESC")
    fun getAsignacionesTermo(tenantId: String, sucursalId: String): Flow<List<AsignacionTermoEntity>>

    @Query("SELECT * FROM asignacion_termo WHERE repartidor_id = :repartidorId AND estado = 'EN_RUTA' AND is_deleted = 0")
    fun getAsignacionesTermoEnRutaByRepartidor(repartidorId: String): Flow<List<AsignacionTermoEntity>>

    @Upsert
    suspend fun upsertAsignacionTermo(asignacion: AsignacionTermoEntity)

    @Query("SELECT * FROM asignacion_termo_detalle WHERE asignacion_id = :asignacionId AND is_deleted = 0")
    fun getAsignacionTermoDetalles(asignacionId: String): Flow<List<AsignacionTermoDetalleEntity>>

    @Upsert
    suspend fun upsertAsignacionTermoDetalles(detalles: List<AsignacionTermoDetalleEntity>)

    @Query("SELECT * FROM asignacion_termo WHERE sync_status = 'PENDIENTE'")
    suspend fun getAsignacionesTermoPendientesSync(): List<AsignacionTermoEntity>

    @Query("SELECT * FROM asignacion_termo_detalle WHERE sync_status = 'PENDIENTE'")
    suspend fun getAsignacionTermoDetallesPendientesSync(): List<AsignacionTermoDetalleEntity>

    // --- AUDIT LOGS ---
    @Upsert
    suspend fun upsertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs WHERE sync_status = 'PENDIENTE'")
    suspend fun getAuditLogsPendientesSync(): List<AuditLogEntity>

    @Query("UPDATE audit_logs SET sync_status = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarAuditLogsSincronizados(ids: List<String>)

    // --- SYNC QUERIES ---
    @Query("SELECT * FROM ventas WHERE sync_status = 'PENDIENTE'")
    suspend fun getVentasPendientesSync(): List<VentaEntity>

    @Query("SELECT * FROM sesiones_caja WHERE sync_status = 'PENDIENTE'")
    suspend fun getSesionesPendientesSync(): List<SesionCajaEntity>

    @Query("SELECT * FROM pedidos_mayorista WHERE sync_status = 'PENDIENTE'")
    suspend fun getPedidosPendientesSync(): List<PedidoMayoristaEntity>

    @Query("UPDATE ventas SET sync_status = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarVentasSincronizadas(ids: List<String>)

    @Query("UPDATE sesiones_caja SET sync_status = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarSesionesSincronizadas(ids: List<String>)

    @Query("UPDATE pedidos_mayorista SET sync_status = 'SINCRONIZADO' WHERE id IN (:ids)")
    suspend fun marcarPedidosSincronizados(ids: List<String>)
}
