package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EstatusDTO(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "descripcion") val descripcion: String? = null,
    @Json(name = "nombre") val nombre: String? = null
)

@JsonClass(generateAdapter = true)
data class PedidoDetallePullDTO(
    @Json(name = "id") val id: String,
    @Json(name = "producto_id") val productoId: String,
    @Json(name = "producto_nombre") val productoNombre: String = "",
    @Json(name = "cantidad") val cantidad: Double = 0.0,
    @Json(name = "precio_unitario") val precioUnitario: Long = 0L,
    @Json(name = "subtotal") val subtotal: Long = 0L
)

@JsonClass(generateAdapter = true)
data class PedidoPullDTO(
    @Json(name = "id") val id: String,
    @Json(name = "folio") val folio: String = "",
    @Json(name = "cliente_id") val clienteId: String = "",
    @Json(name = "cliente_nombre") val clienteNombre: String = "",
    @Json(name = "direccion") val direccion: String = "",
    @Json(name = "contacto") val contacto: String = "",
    @Json(name = "fecha_orden") val fechaOrden: String? = null,
    @Json(name = "fecha_entrega") val fechaEntrega: String = "",
    @Json(name = "condicion") val condicion: String? = null,
    @Json(name = "subtotal") val subtotal: Long? = null,
    @Json(name = "descuento") val descuento: Long? = null,
    @Json(name = "total") val total: Long = 0L,
    @Json(name = "saldo") val saldo: Long? = null,
    @Json(name = "usuario_id") val usuarioId: String? = null,
    @Json(name = "estado") val estadoStr: String? = null,
    @Json(name = "estatus") val estatusObj: EstatusDTO? = null,
    @Json(name = "tenant_id") val tenantId: String? = null,
    @Json(name = "sucursal_id") val sucursalId: String? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean = false,
    @Json(name = "detalles") val detalles: List<PedidoDetallePullDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SesionCajaPullDTO(
    @Json(name = "id") val id: String,
    @Json(name = "sucursal_id") val sucursalId: String? = null,
    @Json(name = "tenant_id") val tenantId: String? = null,
    @Json(name = "usuario_apertura_id") val usuarioAperturaId: String? = null,
    @Json(name = "usuario_apertura_nombre") val usuarioAperturaNombre: String? = null,
    @Json(name = "usuario_cierre_id") val usuarioCierreId: String? = null,
    @Json(name = "usuario_cierre_nombre") val usuarioCierreNombre: String? = null,
    @Json(name = "fecha_apertura") val fechaApertura: Long? = null,
    @Json(name = "fecha_cierre") val fechaCierre: Long? = null,
    @Json(name = "fondo_inicial") val fondoInicial: Long? = null,
    @Json(name = "monto_arqueo") val montoArqueo: Long? = null,
    @Json(name = "diferencia") val diferencia: Long? = null,
    @Json(name = "notas") val notas: String? = null,
    @Json(name = "estado") val estado: String = "ABIERTA",
    @Json(name = "updated_at") val updatedAt: Long? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PullResponseDTO(
    @Json(name = "sucursales") val sucursales: List<SucursalDTO> = emptyList(),
    @Json(name = "usuarios") val usuarios: List<UsuarioDTO> = emptyList(),
    @Json(name = "productos") val productos: List<ProductoDTO> = emptyList(),
    @Json(name = "clientes") val clientes: List<ClienteDTO> = emptyList(),
    @Json(name = "pedidos") val pedidos: List<PedidoPullDTO> = emptyList(),
    @Json(name = "sesiones") val sesiones: List<SesionCajaPullDTO> = emptyList(),
    @Json(name = "termos") val termos: List<TermoDTO> = emptyList(),
    @Json(name = "vehiculos") val vehiculos: List<VehiculoDTO> = emptyList(),
    @Json(name = "vendedores") val vendedores: List<VendedorDTO> = emptyList(),
    @Json(name = "server_time") val serverTime: Long
)

@JsonClass(generateAdapter = true)
data class SucursalDTO(
    @Json(name = "id") val id: String,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "empresa") val empresa: String = "",
    @Json(name = "tenant_id") val tenantId: String = "",
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UsuarioDTO(
    @Json(name = "id") val id: String,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "rol") val rol: String = "CAJERO",
    @Json(name = "email") val email: String = "",
    @Json(name = "password_hash") val passwordHash: String = "",
    @Json(name = "tenant_id") val tenantId: String = "",
    @Json(name = "sucursal_id") val sucursalId: String = "",
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

// --- CATÁLOGOS LOGÍSTICA ---
@JsonClass(generateAdapter = true)
data class TermoDTO(
    @Json(name = "id") val id: String,
    @Json(name = "codigo") val codigo: String = "",
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "precio") val precio: Long = 0,
    @Json(name = "tenant_id") val tenantId: String = "",
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VehiculoDTO(
    @Json(name = "id") val id: String, // String por si acaso en lugar de Int, en JSON el id era 10, pero usaremos String
    @Json(name = "placa") val placa: String = "",
    @Json(name = "modelo") val modelo: String = "",
    @Json(name = "capacidad_kg") val capacidadKg: Double = 0.0,
    @Json(name = "tenant_id") val tenantId: String = "",
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VendedorDTO(
    @Json(name = "id") val id: String,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "rol") val rol: String = "REPARTIDOR",
    @Json(name = "tenant_id") val tenantId: String = "",
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ProductoDTO(
    @Json(name = "id") val id: String,
    @Json(name = "sku") val sku: String = "",
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "categoria") val categoria: String = "General",
    @Json(name = "precio") val precio: Double? = null,
    @Json(name = "precio_con_impuestos") val precioConImpuestos: Long? = null,
    @Json(name = "tasa_iva") val tasaIva: Double = 0.16,
    @Json(name = "tasa_ieps") val tasaIeps: Double = 0.0,
    @Json(name = "stock") val stock: Double = 0.0,
    @Json(name = "unidad_medida") val unidadMedida: String = "pieza",
    @Json(name = "alerta_minimo") val alertaMinimo: Double = 10.0,
    @Json(name = "tipo_articulo") val tipoArticulo: String = "producto_terminado",
    @Json(name = "tenant_id") val tenantId: String? = null,
    @Json(name = "sucursal_id") val sucursalId: String? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ClienteDTO(
    @Json(name = "id") val id: String,
    @Json(name = "codigo") val codigo: String = "",
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "telefono") val telefono: String? = null,
    @Json(name = "direccion") val direccion: String? = null,
    @Json(name = "empresa") val empresa: String = "",
    @Json(name = "puntos_acumulados") val puntosAcumulados: Long = 0,
    @Json(name = "condicion_pago") val condicionPago: String = "CONTADO",
    @Json(name = "tenant_id") val tenantId: String? = null,
    @Json(name = "sucursal_id") val sucursalId: String? = null,
    @Json(name = "updated_at") val updatedAt: Long? = null,
    @Json(name = "is_deleted") val isDeleted: Boolean = false
)
