package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.PosDao
import com.example.data.local.remote.ApiService
import com.example.data.local.remote.AsignacionTermoDetallePushDTO
import com.example.data.mapper.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncStepResult {
    data class Success(val message: String, val count: Int = 0, val serverTime: Long? = null) : SyncStepResult()
    data class Error(val userMessage: String, val location: String, val detail: String) : SyncStepResult()
}

@Singleton
class SyncRepository @Inject constructor(
    private val apiService: ApiService,
    private val posDao: PosDao,
    private val configManager: TerminalConfigManager
) {

    suspend fun pushDataDetailed(): SyncStepResult = withContext(Dispatchers.IO) {
        try {
            val tenantId = configManager.tenantId.first()
            val sucursalId = configManager.sucursalId.first()

            var totalSyncedCount = 0
            val currentUserId = configManager.currentUserId.first() ?: ""

            // 1. PUSH VENTA_CAJA
            val ventas = posDao.getVentasPendientesSync()
            val sesionesPendientes = posDao.getSesionesPendientesSync()
            val auditLogs = posDao.getAuditLogsPendientesSync()

            // Incluir siempre la sesión de caja vinculada a las ventas enviadas
            val sesionIdsDeVentas = ventas.map { it.sesion_caja_id }.filter { it.isNotBlank() }.distinct()
            val sesionesDeVentas = if (sesionIdsDeVentas.isNotEmpty()) posDao.getSesionesByIds(sesionIdsDeVentas) else emptyList()
            val todasSesiones = (sesionesPendientes + sesionesDeVentas).associateBy { it.id }.values.toList()

            val ventasPushDTOs = ventas.map { v ->
                val detalles = posDao.getVentaDetallesSync(v.id).map { d ->
                    com.example.data.local.remote.VentaDetallePushDTO(
                        id = d.id,
                        productoId = d.producto_id,
                        productoNombre = d.producto_nombre,
                        cantidad = d.cantidad,
                        precioUnitario = d.precio_unitario,
                        subtotal = d.subtotal,
                        notasExtra = d.notas_extra
                    )
                }
                val finalUsuarioId = if (!v.usuario_id.isNullOrBlank() && v.usuario_id != "USR-001") v.usuario_id else currentUserId.ifBlank { v.usuario_id }
                com.example.data.local.remote.VentaPushDTO(
                    id = v.id,
                    folio = v.folio,
                    sesionCajaId = v.sesion_caja_id,
                    usuarioId = finalUsuarioId,
                    clienteId = v.cliente_id,
                    fecha = v.fecha,
                    subtotal = v.subtotal,
                    impuestosDesglosados = v.impuestos_desglosados,
                    total = v.total,
                    puntosGanados = v.puntos_ganados,
                    puntosCanjeados = v.puntos_canjeados,
                    metodoPago = v.metodo_pago,
                    estado = v.estado,
                    tenantId = v.tenant_id,
                    sucursalId = v.sucursal_id,
                    detalles = detalles
                )
            }

            val sesionesPushDTOs = todasSesiones.map { s ->
                val finalAperturaUserId = if (!s.usuario_apertura_id.isNullOrBlank() && s.usuario_apertura_id != "USR-001") s.usuario_apertura_id else currentUserId.ifBlank { s.usuario_apertura_id }
                com.example.data.local.remote.SesionCajaPushDTO(
                    id = s.id,
                    sucursalId = s.sucursal_id,
                    tenantId = s.tenant_id,
                    usuarioAperturaId = finalAperturaUserId,
                    usuarioAperturaNombre = s.usuario_apertura_nombre,
                    usuarioCierreId = s.usuario_cierre_id,
                    usuarioCierreNombre = s.usuario_cierre_nombre,
                    fechaApertura = s.fecha_apertura,
                    fechaCierre = s.fecha_cierre,
                    fondoInicial = s.fondo_inicial,
                    montoArqueo = s.monto_arqueo,
                    diferencia = s.diferencia,
                    notas = s.notas,
                    estado = s.estado,
                    syncStatus = s.sync_status,
                    updatedAt = s.updated_at,
                    isDeleted = s.is_deleted
                )
            }

            val movimientosPendientes = posDao.getMovimientosPendientesSync()
            val movimientosPushDTOs = movimientosPendientes.map { m ->
                val finalMovUserId = if (!m.usuario_id.isNullOrBlank() && m.usuario_id != "USR-001") m.usuario_id else currentUserId.ifBlank { m.usuario_id }
                com.example.data.local.remote.MovimientoCajaPushDTO(
                    id = m.id,
                    sesionCajaId = m.sesion_caja_id,
                    tipo = m.tipo,
                    medioPago = m.medio_pago,
                    monto = m.monto,
                    concepto = m.concepto,
                    referencia = m.concepto,
                    usuarioId = finalMovUserId,
                    personaRegistraId = finalMovUserId,
                    fecha = m.fecha,
                    tenantId = m.tenant_id,
                    sucursalId = m.sucursal_id,
                    syncStatus = m.sync_status
                )
            }

            val auditLogsPushDTOs = auditLogs.map { a ->
                val finalAuditUserId = if (!a.usuario_id.isNullOrBlank() && a.usuario_id != "USR-001") a.usuario_id else currentUserId.ifBlank { a.usuario_id }
                com.example.data.local.remote.AuditLogPushDTO(
                    id = a.id,
                    usuarioId = finalAuditUserId,
                    usuarioNombre = a.usuario_nombre,
                    accion = a.accion,
                    detalles = a.detalles,
                    fecha = a.fecha,
                    tenantId = a.tenant_id,
                    sucursalId = a.sucursal_id,
                    syncStatus = a.sync_status,
                    updatedAt = a.updated_at
                )
            }

            val pendingCajaCount = ventas.size + sesionesPendientes.size + auditLogs.size
            if (pendingCajaCount > 0) {
                Log.d("SyncRepository", "Ejecutando Push Venta Caja: ${ventas.size} ventas, ${todasSesiones.size} sesiones, ${auditLogs.size} audit logs")
                val payloadCaja = com.example.data.local.remote.SyncPushVentasPayload(
                    tenantId = tenantId,
                    sucursalId = sucursalId,
                    tipo = "caja",
                    ventas = ventasPushDTOs,
                    sesiones = sesionesPushDTOs,
                    movimientos = movimientosPushDTOs,
                    auditLogs = auditLogsPushDTOs
                )
                val responseCaja = apiService.pushVentasCajaData(tipo = "venta_caja", payload = payloadCaja)
                if (responseCaja.isSuccessful && responseCaja.body()?.success != false) {
                    if (ventas.isNotEmpty()) posDao.marcarVentasSincronizadas(ventas.map { it.id })
                    if (sesionesPendientes.isNotEmpty()) posDao.marcarSesionesSincronizadas(sesionesPendientes.map { it.id })
                    if (auditLogs.isNotEmpty()) posDao.marcarAuditLogsSincronizados(auditLogs.map { it.id })
                    totalSyncedCount += pendingCajaCount
                }
            }

            // 1.5. PUSH MOVIMIENTOS (DEDICADO CON x-tipo: movimiento)
            if (movimientosPendientes.isNotEmpty()) {
                Log.d("SyncRepository", "Ejecutando Push Movimientos de Caja: ${movimientosPendientes.size} registros")
                val payloadMovimientos = com.example.data.local.remote.SyncPushMovimientosPayload(
                    tenantId = tenantId,
                    sucursalId = sucursalId,
                    tipo = "movimiento",
                    movimientos = movimientosPushDTOs
                )
                val responseMov = apiService.pushMovimientosData(tipo = "movimiento", payload = payloadMovimientos)
                if (responseMov.isSuccessful && responseMov.body()?.success != false) {
                    posDao.marcarMovimientosSincronizados(movimientosPendientes.map { it.id })
                    totalSyncedCount += movimientosPendientes.size
                }
            }

            // 2. PUSH VENTA_MAYOREO (PEDIDOS)
            val pedidos = posDao.getPedidosPendientesSync()
            Log.d("SyncRepository", "Buscando pedidos pendientes de sync: ${pedidos.size} encontrados")
            if (pedidos.isNotEmpty()) {
                val pedidosPushDTOs = pedidos.map { p ->
                    val detalles = posDao.getPedidoDetallesSync(p.id).map { d ->
                        com.example.data.local.remote.PedidoDetallePushDTO(
                            id = d.id,
                            productoId = d.producto_id,
                            productoNombre = d.producto_nombre,
                            cantidad = d.cantidad,
                            precioUnitario = d.precio_unitario,
                            subtotal = d.subtotal
                        )
                    }
                    val rawUserId = if (!p.usuario_id.isNullOrBlank() && p.usuario_id != "USR-001") p.usuario_id else currentUserId.ifBlank { p.usuario_id }
                    com.example.data.local.remote.PedidoPushDTO(
                        id = p.id,
                        folio = p.folio,
                        clienteId = p.cliente_id,
                        clienteNombre = p.cliente_nombre,
                        direccion = p.direccion,
                        contacto = p.contacto,
                        fechaEntrega = p.fecha_entrega,
                        condicion = p.condicion,
                        total = p.total,
                        saldo = p.saldo,
                        estado = p.estado,
                        usuarioId = rawUserId,
                        tenantId = p.tenant_id.ifBlank { tenantId },
                        sucursalId = p.sucursal_id.ifBlank { sucursalId },
                        detalles = detalles
                    )
                }

                val payloadPedidos = com.example.data.local.remote.SyncPushPedidosPayload(
                    tenantId = tenantId.ifBlank { "8c5e065c-6622-4a00-9854-47b794170068" },
                    sucursalId = sucursalId.ifBlank { "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
                    tipo = "mayoreo",
                    pedidos = pedidosPushDTOs
                )
                Log.d("SyncRepository", "Iniciando POST /api/v1/sync/push [x-tipo: venta_mayoreo] para ${pedidos.size} pedidos. Tenant=${payloadPedidos.tenantId}, Sucursal=${payloadPedidos.sucursalId}")
                
                try {
                    val responsePedidos = apiService.pushPedidosMayoreoData(tipo = "venta_mayoreo", payload = payloadPedidos)
                    if (responsePedidos.isSuccessful && responsePedidos.body()?.success != false) {
                        Log.d("SyncRepository", "Push Venta Mayoreo EXITOSO: ${responsePedidos.code()} - Body: ${responsePedidos.body()}")
                        posDao.marcarPedidosSincronizados(pedidos.map { it.id })
                        totalSyncedCount += pedidos.size
                    } else {
                        val errStr = responsePedidos.errorBody()?.string() ?: "sin cuerpo de error"
                        Log.e("SyncRepository", "Push Venta Mayoreo RECHAZADO por servidor: Code ${responsePedidos.code()} - Error: $errStr")
                    }
                } catch (e: Exception) {
                    Log.e("SyncRepository", "Excepción de red en Push Venta Mayoreo", e)
                }
            }

            // 3. PUSH ASIGNACIONES DE TERMOS (LOGISTICA)
            val asignacionesTermo = posDao.getAsignacionesTermoPendientesSync()
            if (asignacionesTermo.isNotEmpty()) {
                val asignacionesPushDTOs = asignacionesTermo.map { a ->
                    val detalles = posDao.getAsignacionTermoDetalles(a.id).first().map { d ->
                        AsignacionTermoDetallePushDTO(
                            id = d.id,
                            productoId = d.producto_id,
                            cantidadSalida = d.cantidad_salida,
                            cantidadRegreso = d.cantidad_regreso,
                            precioUnitario = d.precio_unitario
                        )
                    }
                    com.example.data.local.remote.AsignacionTermoPushDTO(
                        id = a.id,
                        repartidorId = a.repartidor_id,
                        vehiculoId = a.vehiculo_id,
                        termoId = a.termo_id,
                        estado = a.estado,
                        fechaAsignacion = a.fecha_asignacion,
                        fechaRecepcion = a.fecha_recepcion,
                        sucursalRecepcionId = a.sucursal_recepcion_id,
                        detalles = detalles
                    )
                }

                val payloadLogistica = com.example.data.local.remote.SyncPushLogisticaPayload(
                    tenantId = tenantId.ifBlank { "8c5e065c-6622-4a00-9854-47b794170068" },
                    sucursalId = sucursalId.ifBlank { "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
                    tipo = "logistica",
                    asignacionesTermo = asignacionesPushDTOs
                )

                try {
                    val responseLogistica = apiService.pushLogisticaData(tipo = "logistica", payload = payloadLogistica)
                    if (responseLogistica.isSuccessful && responseLogistica.body()?.success != false) {
                        Log.d("SyncRepository", "Push Logistica EXITOSO")
                        // Marcar asignaciones como sincronizadas - necesitamos la func en DAO
                        val ids = asignacionesTermo.map { it.id }
                        // Para evitar modificar mas el dao ahora (que no tengo la funcion marcarAsignacionesTermoSincronizados),
                        // actualizamos individualmente o upsert
                        val actualizadas = asignacionesTermo.map { it.copy(sync_status = "SINCRONIZADO") }
                        for (a in actualizadas) posDao.upsertAsignacionTermo(a)

                        val detallesPendientes = posDao.getAsignacionTermoDetallesPendientesSync()
                        val detActualizados = detallesPendientes.filter { ids.contains(it.asignacion_id) }.map { it.copy(sync_status = "SINCRONIZADO") }
                        posDao.upsertAsignacionTermoDetalles(detActualizados)

                        totalSyncedCount += asignacionesTermo.size
                    } else {
                        Log.e("SyncRepository", "Push Logistica RECHAZADO: ${responseLogistica.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("SyncRepository", "Excepción en Push Logistica", e)
                }
            }

            return@withContext SyncStepResult.Success(
                message = "Sincronización saliente completada ($totalSyncedCount registros)",
                count = totalSyncedCount
            )
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error en PushDataDetailed", e)
            val netErrorDetail = when (e) {
                is java.net.UnknownHostException -> "No se encuentra el host del servidor. Verifique la IP/Dominio en configuración."
                is java.net.ConnectException -> "Conexión rechazada. Verifique que el servidor Laravel esté encendido y accesible en la red."
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado (Timeout) contactando al servidor."
                else -> e.localizedMessage ?: e.message ?: "Excepción de red no especificada"
            }
            return@withContext SyncStepResult.Error(
                userMessage = "Sincronización fallida, intente de nuevo.",
                location = "Endpoint POST /api/v1/sync/push",
                detail = netErrorDetail
            )
        }
    }

    suspend fun pushData(): Boolean = withContext(Dispatchers.IO) {
        val result = pushDataDetailed()
        return@withContext result is SyncStepResult.Success
    }

    suspend fun pullDataDetailed(): SyncStepResult = withContext(Dispatchers.IO) {
        try {
            val lastSync = configManager.lastSync.first()
            val tenantId = configManager.tenantId.first()
            val sucursalId = configManager.sucursalId.first()

            Log.d("SyncRepository", "Iniciando Pull: lastSync=$lastSync, tenant=$tenantId")
            configManager.updateSyncingStatus(true)

            val response = apiService.pullIncrementalData(lastSync, tenantId, sucursalId)

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    try {
                        if (data.sucursales.isNotEmpty()) {
                            posDao.upsertSucursales(data.sucursales.toSucursalEntities())
                        }
                        if (data.usuarios.isNotEmpty()) {
                            posDao.upsertUsuarios(data.usuarios.toUsuarioEntities())
                        }
                        if (data.productos.isNotEmpty()) {
                            posDao.upsertProductos(data.productos.toProductoEntities(tenantId, sucursalId))
                        }
                        if (data.clientes.isNotEmpty()) {
                            posDao.upsertClientes(data.clientes.toClienteEntities(tenantId, sucursalId))
                        }
                        if (data.pedidos.isNotEmpty()) {
                            posDao.upsertPedidosMayorista(data.pedidos.toPedidoEntities(tenantId, sucursalId))
                            val detallesEntities = data.pedidos.flatMap { p ->
                                p.detalles.map { d -> d.toEntity(p.id, p.tenantId ?: tenantId, p.sucursalId ?: sucursalId) }
                            }
                            if (detallesEntities.isNotEmpty()) {
                                posDao.upsertPedidoMayoristaDetalles(detallesEntities)
                            }
                        }
                        if (data.sesiones.isNotEmpty()) {
                            val sesionesEntities = data.sesiones.toSesionEntities(tenantId, sucursalId)
                            posDao.upsertSesionesCaja(sesionesEntities)

                            val activeSessionIds = data.sesiones.filter { it.estado.equals("ABIERTA", ignoreCase = true) }.map { it.id }
                            posDao.cerrarSesionesObsoletas(activeSessionIds.ifEmpty { listOf("dummynone") })
                            Log.d("SyncRepository", "Sesiones sincronizadas desde Pull: ${data.sesiones.size} registros. Sesiones abiertas en servidor: ${activeSessionIds.size}")
                        } else {
                            posDao.cerrarSesionesObsoletas(emptyList())
                        }

                        // Procesar catálogos de logística
                        if (data.termos.isNotEmpty()) {
                            posDao.upsertTermos(data.termos.toTermoEntities())
                        }
                        if (data.vehiculos.isNotEmpty()) {
                            posDao.upsertVehiculos(data.vehiculos.toVehiculoEntities())
                        }
                        if (data.vendedores.isNotEmpty()) {
                            posDao.upsertVendedores(data.vendedores.toVendedorEntities())
                        }

                        configManager.updateLastSync(data.serverTime)
                        Log.d("SyncRepository", "Pull exitoso. Nuevo server_time: ${data.serverTime}")
                        return@withContext SyncStepResult.Success(
                            message = "Catálogos actualizados correctamente",
                            serverTime = data.serverTime
                        )
                    } catch (dbEx: Exception) {
                        return@withContext SyncStepResult.Error(
                            userMessage = "Sincronización fallida, intente de nuevo.",
                            location = "Base de datos local (SQLite / Room)",
                            detail = "Error al insertar catálogos descargados en la DB local: ${dbEx.localizedMessage}"
                        )
                    }
                } else {
                    return@withContext SyncStepResult.Error(
                        userMessage = "Sincronización fallida, intente de nuevo.",
                        location = "Respuesta del servidor /api/v1/sync/pull",
                        detail = "El servidor devolvió un cuerpo de respuesta nulo o vacío."
                    )
                }
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val errDetail = when (response.code()) {
                    401 -> "Error HTTP 401 (No Autorizado): Token JWT caducado. Vuelva a iniciar sesión."
                    404 -> "Error HTTP 404: Endpoint /api/v1/sync/pull no disponible en el backend."
                    500 -> "Error HTTP 500: Excepción interna en Laravel al consultar catálogos. $errBody"
                    else -> "Error HTTP ${response.code()}: $errBody"
                }
                return@withContext SyncStepResult.Error(
                    userMessage = "Sincronización fallida, intente de nuevo.",
                    location = "Endpoint GET /api/v1/sync/pull (HTTP ${response.code()})",
                    detail = errDetail
                )
            }
        } catch (e: Exception) {
            Log.e("SyncRepository", "Error durante PullDetailed", e)
            val netErrorDetail = when (e) {
                is java.net.UnknownHostException -> "No se resolvió el servidor. Verifique la dirección URL."
                is java.net.ConnectException -> "No se pudo conectar al servidor Laravel. Verifique red/VPN."
                is java.net.SocketTimeoutException -> "Timeout en descarga de catálogos."
                else -> e.localizedMessage ?: "Excepción no especificada"
            }
            return@withContext SyncStepResult.Error(
                userMessage = "Sincronización fallida, intente de nuevo.",
                location = "Red / Conexión APK -> Servidor",
                detail = netErrorDetail
            )
        } finally {
            configManager.updateSyncingStatus(false)
        }
    }

    suspend fun pullData(): Long? = withContext(Dispatchers.IO) {
        val result = pullDataDetailed()
        return@withContext if (result is SyncStepResult.Success) result.serverTime else null
    }
}
