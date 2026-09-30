package com.example.data.repository

import com.example.data.local.dao.PosDao
import com.example.data.local.remote.ApiService
import com.example.data.mapper.*
import com.example.data.remote.dto.PullResponseDTO
import com.example.data.remote.dto.ProductoDTO
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SyncRepositoryTest {

    private val apiService = mockk<ApiService>()
    private val posDao = mockk<PosDao>(relaxed = true)
    private val configManager = mockk<TerminalConfigManager>(relaxed = true)
    
    private lateinit var syncRepository: SyncRepository

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        syncRepository = SyncRepository(apiService, posDao, configManager)
    }

    @Test
    fun `pullData exitoso debe guardar datos y actualizar lastSync`() = runTest {
        // GIVEN: Configuración inicial y respuesta simulada de Laravel
        every { configManager.lastSync } returns flowOf(0L)
        every { configManager.tenantId } returns flowOf("TENANT-01")
        every { configManager.sucursalId } returns flowOf("SUC-01")

        val mockResponse = PullResponseDTO(
            productos = listOf(
                ProductoDTO(
                    id = "p1", sku = "sku1", nombre = "Masa", categoria = "Cat1",
                    precioConImpuestos = 1800, tasaIva = 0.16, tasaIeps = 0.0,
                    stock = 10.0, unidadMedida = "kg", alertaMinimo = 5.0,
                    tipoArticulo = "materia_prima", tenantId = "TENANT-01",
                    sucursalId = "SUC-01", updatedAt = 1000L, isDeleted = false
                )
            ),
            serverTime = 2000L
        )

        coEvery { apiService.pullIncrementalData(any(), any(), any()) } returns Response.success(mockResponse)

        // WHEN: Ejecutamos la sincronización
        val result = syncRepository.pullData()

        // THEN: Verificamos que se llamó al DAO y se actualizó el DataStore
        coVerify { posDao.upsertProductos(any()) }
        coVerify { configManager.updateLastSync(2000L) }
        assertEquals(2000L, result)
    }

    @Test
    fun `pullData con error de red debe retornar null y no actualizar lastSync`() = runTest {
        // GIVEN: Error de servidor
        coEvery { apiService.pullIncrementalData(any(), any(), any()) } returns Response.error(500, mockk(relaxed = true))

        // WHEN: Ejecutamos
        val result = syncRepository.pullData()

        // THEN: No se actualiza el timestamp
        coVerify(exactly = 0) { configManager.updateLastSync(any()) }
        assertEquals(null, result)
    }

    @Test
    fun `Moshi debe deserializar correctamente el JSON real enviado por Laravel`() {
        // GIVEN: JSON real de producción compartido por el usuario
        val json = """
        {
          "server_time": 1787092925000,
          "sucursales": [
            {
              "id": "019fc01c-e5f3-7e60-8c27-08f55f9d34c6",
              "nombre": "Central",
              "empresa": "EMPRESA NIXTA",
              "tenant_id": "019fc01c-57fc-7491-8af0-37a1214b8f95",
              "updated_at": 1787092925000,
              "is_deleted": false
            }
          ],
          "usuarios": [
            {
              "id": "019fc01f-db88-7013-839a-18b27311214d",
              "nombre": "admin@correo.com",
              "rol": "CAJERO",
              "email": "admin@correo.com",
              "password_hash": "hash",
              "tenant_id": "019fc01c-57fc-7491-8af0-37a1214b8f95",
              "sucursal_id": "019fc01c-e5f3-7e60-8c27-08f55f9d34c6",
              "updated_at": 1787092925000,
              "is_deleted": false
            }
          ],
          "productos": [
            {
              "id": "019fc02e-c6a6-75ec-8bbd-ee6e4cb7405a",
              "sku": "TORT-001",
              "nombre": "TORTILLA",
              "tipo_articulo": "terminado",
              "precio": 40,
              "stock": 49,
              "is_deleted": false
            },
            {
              "id": "019fc030-06a7-7bbc-877e-80fcff1a3244",
              "sku": "TAM-001",
              "nombre": "TAMAL ROJO",
              "tipo_articulo": "adicional",
              "precio": 30,
              "stock": 50,
              "is_deleted": false
            }
          ],
          "clientes": [
            {
              "id": "019fc035-45c0-7015-853e-9911b3dcf4d2",
              "nombre": "TIENDA DE LA ESQUINA",
              "telefono": "565656565656",
              "direccion": "3 PONIENTE 512",
              "is_deleted": false
            }
          ]
        }
        """.trimIndent()

        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter(PullResponseDTO::class.java)

        // WHEN: Moshi parsea el JSON
        val dto = adapter.fromJson(json)

        // THEN: Se valida que se parsearon los arreglos sin lanzar JsonDataException
        assertEquals(1787092925000L, dto?.serverTime)
        assertEquals(2, dto?.productos?.size)
        assertEquals(1, dto?.clientes?.size)

        val prod1 = dto?.productos?.get(0)
        assertEquals("TORTILLA", prod1?.nombre)
        assertEquals(40.0, prod1?.precio)

        // WHEN: Convertimos los DTOs a Entidades usando los IDs de la tablet como fallback
        val productEntities = dto?.productos?.toProductoEntities(
            tenantId = "019fc01c-57fc-7491-8af0-37a1214b8f95",
            sucursalId = "019fc01c-e5f3-7e60-8c27-08f55f9d34c6"
        ) ?: emptyList()

        // THEN: Validamos la conversión a centavos y tenant/sucursal inyectados
        assertEquals(4000L, productEntities[0].precio_con_impuestos)
        assertEquals("producto_terminado", productEntities[0].tipo_articulo)
        assertEquals("019fc01c-57fc-7491-8af0-37a1214b8f95", productEntities[0].tenant_id)
        assertEquals("019fc01c-e5f3-7e60-8c27-08f55f9d34c6", productEntities[0].sucursal_id)

        assertEquals(3000L, productEntities[1].precio_con_impuestos)
        assertEquals("productos_adicionales", productEntities[1].tipo_articulo)
    }
}
