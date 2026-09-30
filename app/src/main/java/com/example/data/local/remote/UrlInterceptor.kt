package com.example.data.local.remote

import com.example.data.repository.TerminalConfigManager
import com.example.util.CryptoUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UrlInterceptor @Inject constructor(
    private val configManager: TerminalConfigManager
) : Interceptor {

    companion object {
        private const val API_SECRET_KEY = "Kumi.2026_!N#"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // Bloqueamos brevemente para obtener la URL actual y los identificadores de DataStore
        val baseUrl = runBlocking { configManager.apiBaseUrl.first() }
        val token = runBlocking { configManager.jwtToken.first() }
        val tenantId = runBlocking { configManager.tenantId.first() }
        val sucursalId = runBlocking { configManager.sucursalId.first() }
        val terminalId = runBlocking { configManager.terminalId.first() }
        
        // Generar firma dinámica per-request requerida por la API de Laravel
        val timestamp = System.currentTimeMillis().toString()
        val xToken = CryptoUtils.md5("$timestamp$API_SECRET_KEY")
        
        val builder = request.newBuilder()
            .header("Accept", "application/json")
            .header("X-Timestamp", timestamp)
            .header("X-Token", xToken)
            .header("X-Tenant-ID", tenantId)
            .header("X-Sucursal-ID", sucursalId)
            .header("X-Terminal-ID", terminalId)
        
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }
        
        baseUrl.toHttpUrlOrNull()?.let { newUrl ->
            val newFullUrl = request.url.newBuilder()
                .scheme(newUrl.scheme)
                .host(newUrl.host)
                .port(newUrl.port)
                .build()
            
            builder.url(newFullUrl)
        }
        
        return chain.proceed(builder.build())
    }
}

