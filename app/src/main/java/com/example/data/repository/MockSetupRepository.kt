package com.example.data.repository

import com.example.data.remote.dto.*
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockSetupRepository @Inject constructor() : SetupRepository {
    
    override suspend fun getTenants(): Result<List<TenantDTO>> {
        delay(1000) // Simulamos latencia de red
        return Result.success(listOf(
            TenantDTO("TENANT-01", "Nixta Corporativo"),
            TenantDTO("TENANT-02", "Tortillería Doña Rosa")
        ))
    }

    override suspend fun getSucursales(tenantId: String): Result<List<SucursalSetupDTO>> {
        delay(800)
        return Result.success(listOf(
            SucursalSetupDTO("SUC-01", "Sucursal Matriz", tenantId),
            SucursalSetupDTO("SUC-02", "Sucursal Poniente", tenantId)
        ))
    }

    override suspend fun getUsuarios(sucursalId: String): Result<List<UsuarioSetupDTO>> {
        delay(800)
        return Result.success(listOf(
            UsuarioSetupDTO("USR-01", "Admin Juan", "SUPERVISOR"),
            UsuarioSetupDTO("USR-02", "Cajero Pedro", "CAJERO")
        ))
    }

    override suspend fun validarPin(usuarioId: String, pin: String): Result<Boolean> {
        delay(1200)
        return if (pin == "1234") Result.success(true) else Result.failure(Exception("PIN incorrecto"))
    }
}
