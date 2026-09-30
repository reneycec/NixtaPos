package com.example.data.repository

import com.example.data.remote.dto.*

interface SetupRepository {
    suspend fun getTenants(): Result<List<TenantDTO>>
    suspend fun getSucursales(tenantId: String): Result<List<SucursalSetupDTO>>
    suspend fun getUsuarios(sucursalId: String): Result<List<UsuarioSetupDTO>>
    suspend fun validarPin(usuarioId: String, pin: String): Result<Boolean>
}
