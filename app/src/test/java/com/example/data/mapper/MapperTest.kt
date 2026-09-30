package com.example.data.mapper

import com.example.data.remote.dto.ProductoDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `ProductoDTO toEntity debe mapear centavos y tipo_articulo correctamente`() {
        // GIVEN
        val dto = ProductoDTO(
            id = "uuid-123",
            sku = "SKU-TEST",
            nombre = "Tortilla Premium",
            categoria = "TORTILLERÍA",
            precioConImpuestos = 1850, // $18.50
            tasaIva = 0.16,
            tasaIeps = 0.0,
            stock = 50.5,
            unidadMedida = "kg",
            alertaMinimo = 10.0,
            tipoArticulo = "producto_terminado",
            tenantId = "T1",
            sucursalId = "S1",
            updatedAt = 1721515000000L,
            isDeleted = false
        )

        // WHEN
        val entity = dto.toEntity()

        // THEN
        assertEquals("uuid-123", entity.id)
        assertEquals(1850L, entity.precio_con_impuestos)
        assertEquals("producto_terminado", entity.tipo_articulo)
        assertEquals("SINCRONIZADO", entity.sync_status)
    }

    @Test
    fun `ProductoDTO con campos omitidos de Laravel debe mapear precio en pesos a centavos y tenant sucursal fallback`() {
        // GIVEN: DTO proveniente de un JSON de Laravel con campos omitidos
        val dto = ProductoDTO(
            id = "019fc02e-c6a6-75ec-8bbd-ee6e4cb7405a",
            sku = "TORT-001",
            nombre = "TORTILLA",
            tipoArticulo = "terminado",
            precio = 40.0,
            stock = 49.0,
            isDeleted = false
        )

        // WHEN: Se mapea con tenant y sucursal de la tablet como fallback
        val entity = dto.toEntity(
            fallbackTenantId = "019fc01c-57fc-7491-8af0-37a1214b8f95",
            fallbackSucursalId = "019fc01c-e5f3-7e60-8c27-08f55f9d34c6"
        )

        // THEN
        assertEquals("019fc02e-c6a6-75ec-8bbd-ee6e4cb7405a", entity.id)
        assertEquals("TORT-001", entity.sku)
        assertEquals("TORTILLA", entity.nombre)
        assertEquals(4000L, entity.precio_con_impuestos) // $40.00 -> 4000 centavos
        assertEquals("producto_terminado", entity.tipo_articulo) // "terminado" -> "producto_terminado"
        assertEquals("019fc01c-57fc-7491-8af0-37a1214b8f95", entity.tenant_id)
        assertEquals("019fc01c-e5f3-7e60-8c27-08f55f9d34c6", entity.sucursal_id)
        assertEquals("General", entity.categoria)
        assertEquals("pieza", entity.unidad_medida)
    }
}
