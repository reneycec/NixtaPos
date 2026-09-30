package com.example.data.local.remote

import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.data.local.entities.VentaEntity
import com.example.data.remote.dto.PullResponseDTO
import com.example.data.remote.dto.UsuarioDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class FcmTokenRequest(
    @Json(name = "terminal_id") val terminalId: String,
    @Json(name = "fcm_token") val fcmToken: String,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String
)

@JsonClass(generateAdapter = true)
data class FcmTokenResponse(
    val success: Boolean,
    val message: String
)

@JsonClass(generateAdapter = true)
data class VentaDetallePushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "producto_id") val productoId: String,
    @Json(name = "producto_nombre") val productoNombre: String,
    @Json(name = "cantidad") val cantidad: Double,
    @Json(name = "precio_unitario") val precioUnitario: Long,
    @Json(name = "subtotal") val subtotal: Long,
    @Json(name = "notas_extra") val notasExtra: String? = null
)

@JsonClass(generateAdapter = true)
data class VentaPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "folio") val folio: String,
    @Json(name = "sesion_caja_id") val sesionCajaId: String,
    @Json(name = "usuario_id") val usuarioId: String,
    @Json(name = "cliente_id") val clienteId: String? = null,
    @Json(name = "fecha") val fecha: Long,
    @Json(name = "subtotal") val subtotal: Long,
    @Json(name = "impuestos_desglosados") val impuestosDesglosados: Long = 0L,
    @Json(name = "total") val total: Long,
    @Json(name = "puntos_ganados") val puntosGanados: Long = 0,
    @Json(name = "puntos_canjeados") val puntosCanjeados: Long = 0,
    @Json(name = "metodo_pago") val metodoPago: String,
    @Json(name = "estado") val estado: String,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "detalles") val detalles: List<VentaDetallePushDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PedidoDetallePushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "producto_id") val productoId: String,
    @Json(name = "producto_nombre") val productoNombre: String,
    @Json(name = "cantidad") val cantidad: Double,
    @Json(name = "precio_unitario") val precioUnitario: Long,
    @Json(name = "subtotal") val subtotal: Long
)

@JsonClass(generateAdapter = true)
data class PedidoPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "folio") val folio: String,
    @Json(name = "cliente_id") val clienteId: String,
    @Json(name = "cliente_nombre") val clienteNombre: String,
    @Json(name = "direccion") val direccion: String,
    @Json(name = "contacto") val contacto: String,
    @Json(name = "fecha_entrega") val fechaEntrega: String,
    @Json(name = "condicion") val condicion: String = "CONTADO",
    @Json(name = "total") val total: Long,
    @Json(name = "saldo") val saldo: Long,
    @Json(name = "estado") val estado: String,
    @Json(name = "usuario_id") val usuarioId: String,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "detalles") val detalles: List<PedidoDetallePushDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SesionCajaPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "usuario_apertura_id") val usuarioAperturaId: String,
    @Json(name = "usuario_apertura_nombre") val usuarioAperturaNombre: String,
    @Json(name = "usuario_cierre_id") val usuarioCierreId: String? = null,
    @Json(name = "usuario_cierre_nombre") val usuarioCierreNombre: String? = null,
    @Json(name = "fecha_apertura") val fechaApertura: Long,
    @Json(name = "fecha_cierre") val fechaCierre: Long? = null,
    @Json(name = "fondo_inicial") val fondoInicial: Long,
    @Json(name = "monto_arqueo") val montoArqueo: Long? = null,
    @Json(name = "diferencia") val diferencia: Long? = null,
    @Json(name = "notas") val notas: String? = null,
    @Json(name = "estado") val estado: String,
    @Json(name = "sync_status") val syncStatus: String = "PENDIENTE",
    @Json(name = "updated_at") val updatedAt: Long,
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AuditLogPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "usuario_id") val usuarioId: String,
    @Json(name = "usuario_nombre") val usuarioNombre: String,
    @Json(name = "accion") val accion: String,
    @Json(name = "detalles") val detalles: String,
    @Json(name = "fecha") val fecha: Long,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "sync_status") val syncStatus: String = "PENDIENTE",
    @Json(name = "updated_at") val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class MovimientoCajaPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "sesion_caja_id") val sesionCajaId: String,
    @Json(name = "tipo") val tipo: String,
    @Json(name = "medio_pago") val medioPago: String = "EFECTIVO",
    @Json(name = "monto") val monto: Long,
    @Json(name = "concepto") val concepto: String,
    @Json(name = "referencia") val referencia: String? = concepto,
    @Json(name = "usuario_id") val usuarioId: String,
    @Json(name = "persona_registra_id") val personaRegistraId: String? = usuarioId,
    @Json(name = "fecha") val fecha: Long,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "sync_status") val syncStatus: String = "PENDIENTE"
)

@JsonClass(generateAdapter = true)
data class SyncPushMovimientosPayload(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "tipo") val tipo: String = "movimiento",
    @Json(name = "movimientos") val movimientos: List<MovimientoCajaPushDTO>
)

@JsonClass(generateAdapter = true)
data class SyncPushVentasPayload(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "tipo") val tipo: String = "caja",
    @Json(name = "ventas") val ventas: List<VentaPushDTO>,
    @Json(name = "sesiones") val sesiones: List<SesionCajaPushDTO>,
    @Json(name = "movimientos") val movimientos: List<MovimientoCajaPushDTO> = emptyList(),
    @Json(name = "audit_logs") val auditLogs: List<AuditLogPushDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SyncPushPedidosPayload(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "tipo") val tipo: String = "mayoreo",
    @Json(name = "pedidos") val pedidos: List<PedidoPushDTO>
)

@JsonClass(generateAdapter = true)
data class AsignacionTermoDetallePushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "producto_id") val productoId: String,
    @Json(name = "cantidad_salida") val cantidadSalida: Double,
    @Json(name = "cantidad_regreso") val cantidadRegreso: Double,
    @Json(name = "precio_unitario") val precioUnitario: Long
)

@JsonClass(generateAdapter = true)
data class AsignacionTermoPushDTO(
    @Json(name = "id") val id: String,
    @Json(name = "repartidor_id") val repartidorId: String,
    @Json(name = "vehiculo_id") val vehiculoId: String,
    @Json(name = "termo_id") val termoId: String,
    @Json(name = "estado") val estado: String,
    @Json(name = "fecha_asignacion") val fechaAsignacion: Long,
    @Json(name = "fecha_recepcion") val fechaRecepcion: Long?,
    @Json(name = "sucursal_recepcion_id") val sucursalRecepcionId: String?,
    @Json(name = "detalles") val detalles: List<AsignacionTermoDetallePushDTO>
)

@JsonClass(generateAdapter = true)
data class SyncPushLogisticaPayload(
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String,
    @Json(name = "tipo") val tipo: String = "logistica",
    @Json(name = "asignaciones_termo") val asignacionesTermo: List<AsignacionTermoPushDTO>
)

@JsonClass(generateAdapter = true)
data class SyncPushResponse(
    val success: Boolean,
    @Json(name = "synced_ids") val syncedIds: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PointsResponse(
    val puntos: Long,
    val nombre: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
    @Json(name = "terminal_id") val terminalId: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    @Json(name = "folio_prefix") val folioPrefix: String?,
    val usuario: UsuarioDTO?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class CierreCajaRequest(
    @Json(name = "sesion_caja_id") val sesionCajaId: String,
    @Json(name = "monto_arqueo") val montoArqueo: Long,
    @Json(name = "notas") val notas: String?,
    @Json(name = "usuario_cierre_id") val usuarioCierreId: String,
    @Json(name = "usuario_cierre_nombre") val usuarioCierreNombre: String? = null,
    @Json(name = "fecha_cierre") val fechaCierre: Long,
    @Json(name = "tenant_id") val tenantId: String,
    @Json(name = "sucursal_id") val sucursalId: String
)

@JsonClass(generateAdapter = true)
data class CierreCajaResponse(
    val success: Boolean,
    val message: String?,
    @Json(name = "sesion_caja_id") val sesionCajaId: String? = null
)

interface ApiService {

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/v1/auth/logout")
    suspend fun logoutRemote(): Response<Unit>

    @POST("/api/v1/caja/cierre-movil")
    suspend fun cerrarCajaRemote(@Body request: CierreCajaRequest): Response<CierreCajaResponse>

    @POST("/api/v1/terminals/register-fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<FcmTokenResponse>

    @POST("/api/v1/sync/push")
    suspend fun pushVentasCajaData(
        @retrofit2.http.Header("x-tipo") tipo: String = "venta_caja",
        @Body payload: SyncPushVentasPayload
    ): Response<SyncPushResponse>

    @POST("/api/v1/sync/push")
    suspend fun pushPedidosMayoreoData(
        @retrofit2.http.Header("x-tipo") tipo: String = "venta_mayoreo",
        @Body payload: SyncPushPedidosPayload
    ): Response<SyncPushResponse>

    @POST("/api/v1/sync/push")
    suspend fun pushMovimientosData(
        @retrofit2.http.Header("x-tipo") tipo: String = "movimiento",
        @Body payload: SyncPushMovimientosPayload
    ): Response<SyncPushResponse>

    @POST("/api/v1/sync/push")
    suspend fun pushLogisticaData(
        @Header("x-tipo") tipo: String = "logistica",
        @Body payload: SyncPushLogisticaPayload
    ): Response<SyncPushResponse>

    @GET("/api/v1/sync/pull")
    suspend fun pullIncrementalData(
        @Query("last_sync") lastSync: Long,
        @Query("tenant_id") tenantId: String,
        @Query("sucursal_id") sucursalId: String
    ): Response<PullResponseDTO>

    @GET("/api/v1/clientes/{uuid}/puntos")
    suspend fun getCustomerPoints(
        @retrofit2.http.Path("uuid") uuid: String,
        @Query("tenant_id") tenantId: String
    ): Response<PointsResponse>
}

