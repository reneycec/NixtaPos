package com.example.data.repository

import com.example.data.local.dao.PosDao
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
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CartItem(
    val producto: ProductoEntity,
    val cantidad: Double,
    val notas: String? = null,
    val incluyePapel: Boolean = false // +$1.00 Papel option seen in screenshot!
)

class PosRepository(
    private val posDao: PosDao,
    private val configManager: TerminalConfigManager
) {

    fun getSucursales(tenantId: String): Flow<List<SucursalEntity>> =
        posDao.getSucursales(tenantId)

    fun getUsuarios(tenantId: String, sucursalId: String): Flow<List<UsuarioEntity>> =
        posDao.getUsuarios(tenantId, sucursalId)

    suspend fun getUsuarioByEmail(email: String): UsuarioEntity? =
        posDao.getUsuarioByEmail(email)

    suspend fun getSupervisorByEmail(email: String): UsuarioEntity? =
        posDao.getSupervisorByEmail(email)

    fun getSesionActiva(tenantId: String, sucursalId: String): Flow<SesionCajaEntity?> =
        posDao.getSesionActiva(tenantId, sucursalId)

    fun getHistorialSesiones(tenantId: String, sucursalId: String): Flow<List<SesionCajaEntity>> =
        posDao.getHistorialSesiones(tenantId, sucursalId)

    fun getMovimientosSesion(sesionId: String): Flow<List<MovimientoCajaEntity>> =
        posDao.getMovimientosSesion(sesionId)

    fun getProductos(tenantId: String, sucursalId: String): Flow<List<ProductoEntity>> =
        posDao.getProductos(tenantId, sucursalId)

    fun getClientes(tenantId: String, sucursalId: String): Flow<List<ClienteEntity>> =
        posDao.getClientes(tenantId, sucursalId)

    fun getTodasVentas(tenantId: String, sucursalId: String): Flow<List<VentaEntity>> =
        posDao.getTodasVentas(tenantId, sucursalId)

    fun getVentasPorSesion(sesionId: String): Flow<List<VentaEntity>> =
        posDao.getVentasPorSesion(sesionId)

    fun getTotalVentasEfectivo(sesionId: String): Flow<Long?> =
        posDao.getTotalVentasEfectivoSesion(sesionId)

    fun getTotalVentasOtros(sesionId: String): Flow<Long?> =
        posDao.getTotalVentasOtrosSesion(sesionId)

    fun getPedidosMayorista(tenantId: String, sucursalId: String): Flow<List<PedidoMayoristaEntity>> =
        posDao.getPedidosMayorista(tenantId, sucursalId)

    // --- Catálogos Logística ---
    fun getTermos(tenantId: String): Flow<List<TermoEntity>> = posDao.getTermos(tenantId)
    fun getVehiculos(tenantId: String): Flow<List<VehiculoEntity>> = posDao.getVehiculos(tenantId)
    fun getVendedores(tenantId: String): Flow<List<VendedorEntity>> = posDao.getVendedores(tenantId)

    // --- Transaccional Logística ---
    fun getAsignacionesTermo(tenantId: String, sucursalId: String) = posDao.getAsignacionesTermo(tenantId, sucursalId)
    fun getAsignacionesTermoEnRutaByRepartidor(repartidorId: String) = posDao.getAsignacionesTermoEnRutaByRepartidor(repartidorId)
    fun getAsignacionTermoDetalles(asignacionId: String) = posDao.getAsignacionTermoDetalles(asignacionId)

    suspend fun asignarTermo(asignacion: AsignacionTermoEntity, detalles: List<AsignacionTermoDetalleEntity>) {
        posDao.upsertAsignacionTermo(asignacion)
        posDao.upsertAsignacionTermoDetalles(detalles)
    }

    suspend fun recibirTermo(asignacion: AsignacionTermoEntity, detalles: List<AsignacionTermoDetalleEntity>) {
        posDao.upsertAsignacionTermo(asignacion.copy(estado = "RECIBIDO", sync_status = "PENDIENTE", updated_at = System.currentTimeMillis()))
        posDao.upsertAsignacionTermoDetalles(detalles)
    }

    suspend fun asignarPedidoRegistrado(pedido: PedidoMayoristaEntity, nuevoRepartidorId: String) {
        posDao.upsertPedidoMayorista(pedido.copy(
            usuario_id = nuevoRepartidorId,
            estado = "EN PROCESO", 
            sync_status = "PENDIENTE",
            updated_at = System.currentTimeMillis()
        ))
    }

    suspend fun abrirSesionCaja(
        fondoInicial: Long,
        notas: String?,
        tenantId: String,
        sucursalId: String,
        usuario: UsuarioEntity
    ): SesionCajaEntity {
        val sesion = SesionCajaEntity(
            id = UUID.randomUUID().toString(),
            sucursal_id = sucursalId,
            tenant_id = tenantId,
            usuario_apertura_id = usuario.id,
            usuario_apertura_nombre = usuario.nombre,
            fecha_apertura = System.currentTimeMillis(),
            fondo_inicial = fondoInicial,
            notas = notas,
            estado = "ABIERTA",
            sync_status = "PENDIENTE"
        )
        posDao.upsertSesionCaja(sesion)
        return sesion
    }

    suspend fun cerrarSesionCaja(
        sesion: SesionCajaEntity,
        montoArqueo: Long,
        montoEsperado: Long,
        notas: String?,
        usuarioCierre: UsuarioEntity
    ) {
        val ventasPendientes = posDao.getVentasPendientesSync()
        if (ventasPendientes.isNotEmpty()) {
            android.util.Log.w("PosRepository", "Cerrando caja con ${ventasPendientes.size} ventas pendientes de sincronizar. Se enviarán en la sincronización saliente.")
        }

        val diferencia = montoArqueo - montoEsperado
        val sesionCerrada = sesion.copy(
            usuario_cierre_id = usuarioCierre.id,
            usuario_cierre_nombre = usuarioCierre.nombre,
            fecha_cierre = System.currentTimeMillis(),
            monto_arqueo = montoArqueo,
            diferencia = diferencia,
            notas = if (notas.isNull_or_blank()) sesion.notas else notas,
            estado = "CERRADA",
            sync_status = "PENDIENTE",
            updated_at = System.currentTimeMillis()
        )
        posDao.upsertSesionCaja(sesionCerrada)
        
        // Registrar Auditoría
        posDao.upsertAuditLog(AuditLogEntity(
            usuario_id = usuarioCierre.id,
            usuario_nombre = usuarioCierre.nombre,
            accion = "CIERRE_CAJA",
            detalles = "Monto Arqueo: $montoArqueo, Diferencia: $diferencia",
            tenant_id = sesion.tenant_id,
            sucursal_id = sesion.sucursal_id
        ))
    }

    suspend fun registrarMovimiento(
        sesionId: String,
        tipo: String, // "DEPOSITO", "RETIRO"
        monto: Long,
        concepto: String,
        tenantId: String,
        sucursalId: String,
        usuarioId: String = "",
        medioPago: String = "EFECTIVO"
    ) {
        val movimiento = MovimientoCajaEntity(
            id = UUID.randomUUID().toString(),
            sesion_caja_id = sesionId,
            tenant_id = tenantId,
            sucursal_id = sucursalId,
            tipo = tipo,
            medio_pago = medioPago,
            monto = monto,
            concepto = concepto,
            usuario_id = usuarioId,
            fecha = System.currentTimeMillis(),
            sync_status = "PENDIENTE"
        )
        posDao.upsertMovimiento(movimiento)
    }

    suspend fun realizarVenta(
        cartItems: List<CartItem>,
        sesionId: String,
        metodoPago: String,
        clienteId: String?,
        tenantId: String,
        sucursalId: String,
        usuarioId: String = ""
    ): VentaEntity {
        var subtotal = 0L
        var totalImpuestos = 0L

        cartItems.forEach { item ->
            val basePrice = item.producto.precio_con_impuestos
            val cantidadMultiplier = item.cantidad
            val itemSubtotal = (basePrice * cantidadMultiplier).toLong() + if (item.incluyePapel) 100L else 0L
            subtotal += itemSubtotal

            // Calculate tax breakdown
            val ivaPart = (itemSubtotal * item.producto.tasa_iva).toLong()
            totalImpuestos += ivaPart
        }

        val total = subtotal
        
        // Obtener Prefijo de Terminal para evitar colisiones
        val prefix = configManager.folioPrefix.first()
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val random = (100..999).random()
        val folio = "$prefix-$timestamp-$random"

        val venta = VentaEntity(
            id = UUID.randomUUID().toString(),
            folio = folio,
            sesion_caja_id = sesionId,
            usuario_id = usuarioId,
            cliente_id = clienteId,
            fecha = System.currentTimeMillis(),
            subtotal = subtotal - totalImpuestos,
            impuestos_desglosados = totalImpuestos,
            total = total,
            metodo_pago = metodoPago,
            estado = "COMPLETADA",
            tenant_id = tenantId,
            sucursal_id = sucursalId,
            sync_status = "PENDIENTE"
        )

        posDao.upsertVenta(venta)

        val detalles = cartItems.map { item ->
            val basePrice = item.producto.precio_con_impuestos
            val itemSubtotal = (basePrice * item.cantidad).toLong() + if (item.incluyePapel) 100L else 0L
            
            // Reduce local stock in Room DB
            posDao.reducirStock(item.producto.id, item.cantidad)

            VentaDetalleEntity(
                id = UUID.randomUUID().toString(),
                venta_id = venta.id,
                producto_id = item.producto.id,
                producto_nombre = item.producto.nombre + if (item.incluyePapel) " (+Papel)" else "",
                cantidad = item.cantidad,
                precio_unitario = basePrice,
                subtotal = itemSubtotal,
                notas_extra = item.notas,
                tenant_id = tenantId,
                sucursal_id = sucursalId,
                sync_status = "PENDIENTE"
            )
        }

        posDao.upsertVentaDetalles(detalles)
        return venta
    }

    suspend fun agregarCliente(cliente: ClienteEntity) {
        posDao.upsertCliente(cliente)
    }

    suspend fun agregarPedidoMayorista(
        pedido: PedidoMayoristaEntity,
        detalles: List<PedidoMayoristaDetalleEntity> = emptyList()
    ) {
        posDao.upsertPedidoMayorista(pedido)
        if (detalles.isNotEmpty()) {
            posDao.upsertPedidoMayoristaDetalles(detalles)
            detalles.forEach { d ->
                posDao.reducirStock(d.producto_id, d.cantidad)
            }
        }
    }

    suspend fun agregarProducto(producto: ProductoEntity) {
        posDao.upsertProducto(producto)
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
