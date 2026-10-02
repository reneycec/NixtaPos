package com.example.data.mapper

import com.example.data.local.entities.*
import com.example.data.remote.dto.*

fun SucursalDTO.toEntity(): SucursalEntity = SucursalEntity(
    id = id,
    nombre = nombre,
    empresa = empresa,
    tenant_id = tenantId,
    sync_status = "SINCRONIZADO",
    updated_at = updatedAt,
    is_deleted = isDeleted
)

fun UsuarioDTO.toEntity(): UsuarioEntity = UsuarioEntity(
    id = id,
    nombre = nombre,
    rol = rol,
    email = email,
    password_hash = passwordHash,
    tenant_id = tenantId,
    sucursal_id = sucursalId,
    sync_status = "SINCRONIZADO",
    updated_at = updatedAt,
    is_deleted = isDeleted
)

fun ProductoDTO.toEntity(
    fallbackTenantId: String = "",
    fallbackSucursalId: String = ""
): ProductoEntity {
    val finalPrecioCentavos = when {
        precioConImpuestos != null -> precioConImpuestos
        precio != null -> (precio * 100).toLong()
        else -> 0L
    }

    val finalTipoArticulo = when (tipoArticulo.lowercase()) {
        "terminado", "producto_terminado" -> "producto_terminado"
        "adicional", "productos_adicionales" -> "productos_adicionales"
        "materia_prima" -> "materia_prima"
        else -> tipoArticulo
    }

    val finalTenantId = tenantId.takeIf { !it.isNullOrEmpty() } ?: fallbackTenantId
    val finalSucursalId = sucursalId.takeIf { !it.isNullOrEmpty() } ?: fallbackSucursalId

    val inferredUnidad = when {
        unidadMedida.isNotBlank() && unidadMedida.lowercase() != "pieza" -> unidadMedida
        nombre.lowercase().contains("tortilla") || nombre.lowercase().contains("masa") -> "kg"
        else -> unidadMedida.ifBlank { "pieza" }
    }

    return ProductoEntity(
        id = id,
        sku = sku,
        nombre = nombre,
        categoria = categoria.ifBlank { "General" },
        precio_con_impuestos = finalPrecioCentavos,
        tasa_iva = tasaIva,
        tasa_ieps = tasaIeps,
        stock = stock,
        unidad_medida = inferredUnidad,
        alerta_minimo = alertaMinimo,
        tipo_articulo = finalTipoArticulo,
        tenant_id = finalTenantId,
        sucursal_id = finalSucursalId,
        sync_status = "SINCRONIZADO",
        updated_at = updatedAt ?: System.currentTimeMillis(),
        is_deleted = isDeleted
    )
}

fun ClienteDTO.toEntity(
    fallbackTenantId: String = "",
    fallbackSucursalId: String = ""
): ClienteEntity {
    val finalTenantId = tenantId.takeIf { !it.isNullOrEmpty() } ?: fallbackTenantId
    val finalSucursalId = sucursalId.takeIf { !it.isNullOrEmpty() } ?: fallbackSucursalId

    return ClienteEntity(
        id = id,
        codigo = codigo.ifBlank { id.take(8).uppercase() },
        nombre = nombre,
        telefono = telefono ?: "",
        direccion = direccion ?: "",
        empresa = empresa.ifBlank { "N/A" },
        puntos_acumulados = puntosAcumulados,
        condicion_pago = condicionPago.ifBlank { "CONTADO" },
        tenant_id = finalTenantId,
        sucursal_id = finalSucursalId,
        sync_status = "SINCRONIZADO",
        updated_at = updatedAt ?: System.currentTimeMillis(),
        is_deleted = isDeleted
    )
}

fun PedidoPullDTO.toEntity(
    fallbackTenantId: String = "",
    fallbackSucursalId: String = ""
): PedidoMayoristaEntity {
    val finalTenantId = tenantId.takeIf { !it.isNullOrEmpty() } ?: fallbackTenantId
    val finalSucursalId = sucursalId.takeIf { !it.isNullOrEmpty() } ?: fallbackSucursalId

    val rawState = estadoStr
        ?: estatusObj?.descripcion
        ?: estatusObj?.nombre
        ?: "PENDIENTE"

    val finalState = when (rawState.uppercase()) {
        "PENDIENTE" -> "PENDIENTE"
        "CONFIRMADO", "EN PROCESO" -> "EN PROCESO"
        "ENVIADO", "ENTREGADO" -> "ENTREGADO"
        "CANCELADO" -> "CANCELADO"
        else -> rawState.uppercase()
    }

    val finalCondicion = condicion.takeIf { !it.isNullOrEmpty() } ?: "CONTADO"
    val finalSaldo = saldo ?: if (finalCondicion.uppercase() == "CRÉDITO") total else 0L

    return PedidoMayoristaEntity(
        id = id,
        folio = folio,
        cliente_id = clienteId,
        cliente_nombre = clienteNombre,
        direccion = direccion,
        contacto = contacto,
        fecha_entrega = fechaEntrega.ifBlank { fechaOrden ?: "" },
        condicion = finalCondicion,
        total = total,
        saldo = finalSaldo,
        estado = finalState,
        usuario_id = usuarioId ?: "",
        tenant_id = finalTenantId,
        sucursal_id = finalSucursalId,
        sync_status = "SINCRONIZADO",
        updated_at = updatedAt ?: System.currentTimeMillis(),
        is_deleted = isDeleted
    )
}

fun PedidoDetallePullDTO.toEntity(
    pedidoId: String,
    fallbackTenantId: String = "",
    fallbackSucursalId: String = ""
): PedidoMayoristaDetalleEntity = PedidoMayoristaDetalleEntity(
    id = id,
    pedido_id = pedidoId,
    producto_id = productoId,
    producto_nombre = productoNombre,
    cantidad = cantidad,
    precio_unitario = precioUnitario,
    subtotal = subtotal,
    tenant_id = fallbackTenantId,
    sucursal_id = fallbackSucursalId,
    sync_status = "SINCRONIZADO",
    updated_at = System.currentTimeMillis(),
    is_deleted = false
)

fun SesionCajaPullDTO.toEntity(
    fallbackTenantId: String = "",
    fallbackSucursalId: String = ""
): SesionCajaEntity = SesionCajaEntity(
    id = id,
    sucursal_id = sucursalId.takeIf { !it.isNullOrEmpty() } ?: fallbackSucursalId,
    tenant_id = tenantId.takeIf { !it.isNullOrEmpty() } ?: fallbackTenantId,
    usuario_apertura_id = usuarioAperturaId ?: "USR-001",
    usuario_apertura_nombre = usuarioAperturaNombre ?: "CAJERO",
    usuario_cierre_id = usuarioCierreId ?: "",
    usuario_cierre_nombre = usuarioCierreNombre ?: "",
    fecha_apertura = fechaApertura ?: System.currentTimeMillis(),
    fecha_cierre = fechaCierre,
    fondo_inicial = fondoInicial ?: 0L,
    monto_arqueo = montoArqueo,
    diferencia = diferencia,
    notas = notas ?: "",
    estado = estado.uppercase(),
    sync_status = "SINCRONIZADO",
    updated_at = updatedAt ?: System.currentTimeMillis(),
    is_deleted = isDeleted
)

// Extensiones para listas
fun List<SucursalDTO>.toSucursalEntities() = map { it.toEntity() }
fun List<UsuarioDTO>.toUsuarioEntities() = map { it.toEntity() }
fun List<ProductoDTO>.toProductoEntities(
    tenantId: String = "",
    sucursalId: String = ""
) = map { it.toEntity(tenantId, sucursalId) }
fun List<ClienteDTO>.toClienteEntities(
    tenantId: String = "",
    sucursalId: String = ""
) = map { it.toEntity(tenantId, sucursalId) }
fun List<PedidoPullDTO>.toPedidoEntities(
    tenantId: String = "",
    sucursalId: String = ""
) = map { it.toEntity(tenantId, sucursalId) }
fun List<SesionCajaPullDTO>.toSesionEntities(
    tenantId: String = "",
    sucursalId: String = ""
) = map { it.toEntity(tenantId, sucursalId) }

fun TermoDTO.toEntity(): TermoEntity = TermoEntity(
    id = id,
    codigo = codigo,
    nombre = nombre,
    precio = precio,
    tenant_id = tenantId,
    updated_at = updatedAt,
    is_deleted = isDeleted
)

fun VehiculoDTO.toEntity(): VehiculoEntity = VehiculoEntity(
    id = id,
    placa = placa,
    modelo = modelo,
    capacidad_kg = capacidadKg,
    tenant_id = tenantId,
    updated_at = updatedAt,
    is_deleted = isDeleted
)

fun VendedorDTO.toEntity(): VendedorEntity = VendedorEntity(
    id = id,
    nombre = nombre,
    rol = rol,
    tenant_id = tenantId,
    updated_at = updatedAt,
    is_deleted = isDeleted
)

fun List<TermoDTO>.toTermoEntities(): List<TermoEntity> = this.map { it.toEntity() }
fun List<VehiculoDTO>.toVehiculoEntities(): List<VehiculoEntity> = this.map { it.toEntity() }
fun List<VendedorDTO>.toVendedorEntities(): List<VendedorEntity> = this.map { it.toEntity() }



