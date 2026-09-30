package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TenantDTO(
    val id: String,
    val nombre: String
)

@JsonClass(generateAdapter = true)
data class SucursalSetupDTO(
    val id: String,
    val nombre: String,
    @Json(name = "tenant_id") val tenantId: String
)

@JsonClass(generateAdapter = true)
data class UsuarioSetupDTO(
    val id: String,
    val nombre: String,
    val rol: String
)
