package com.example.data.local.database

import android.content.Context
import java.util.UUID
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PosDao
import com.example.data.local.entities.AsignacionTermoDetalleEntity
import com.example.data.local.entities.AsignacionTermoEntity
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.ClienteEntity
import com.example.data.local.entities.MovimientoCajaEntity
import com.example.data.local.entities.PedidoMayoristaDetalleEntity
import com.example.data.local.entities.PedidoMayoristaEntity
import com.example.data.local.entities.ProductoEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.data.local.entities.SucursalEntity
import com.example.data.local.entities.TermoEntity
import com.example.data.local.entities.UsuarioEntity
import com.example.data.local.entities.VehiculoEntity
import com.example.data.local.entities.VendedorEntity
import com.example.data.local.entities.VentaDetalleEntity
import com.example.data.local.entities.VentaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SucursalEntity::class,
        UsuarioEntity::class,
        SesionCajaEntity::class,
        MovimientoCajaEntity::class,
        ProductoEntity::class,
        ClienteEntity::class,
        VentaEntity::class,
        VentaDetalleEntity::class,
        PedidoMayoristaEntity::class,
        PedidoMayoristaDetalleEntity::class,
        TermoEntity::class,
        VehiculoEntity::class,
        VendedorEntity::class,
        AsignacionTermoEntity::class,
        AsignacionTermoDetalleEntity::class,
        AuditLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class NixtaPosDatabase : RoomDatabase() {

    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: NixtaPosDatabase? = null

        fun getDatabase(context: Context): NixtaPosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NixtaPosDatabase::class.java,
                    "nixta_pos_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial default NIXTA ERP data on database creation
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(database.posDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(dao: PosDao) {
            val tenantId = "TENANT-NIXTA-01"
            val sucursalId = "SUCURSAL-CENTRO"

            // 1. Sucursal
            dao.upsertSucursal(
                SucursalEntity(
                    id = sucursalId,
                    nombre = "SUCURSAL CENTRO",
                    empresa = "EMPRESA QWERTY",
                    tenant_id = tenantId
                )
            )

            // 2. Usuarios (Cajero y Supervisor)
            dao.upsertUsuarios(
                listOf(
                    UsuarioEntity(
                        id = "USR-001",
                        nombre = "Caja Sucursal",
                        rol = "CAJERO",
                        email = "caja.centro@nixta.com",
                        password_hash = "1234",
                        tenant_id = tenantId,
                        sucursal_id = sucursalId
                    ),
                    UsuarioEntity(
                        id = "USR-002",
                        nombre = "Supervisor Don Roberto",
                        rol = "SUPERVISOR",
                        email = "super.roberto@nixta.com",
                        password_hash = "9999",
                        tenant_id = tenantId,
                        sucursal_id = sucursalId
                    )
                )
            )

            // 3. Productos (matching screenshot)
            dao.upsertProductos(
                listOf(
                    ProductoEntity(
                        id = "PROD-001",
                        sku = "TOR-001",
                        nombre = "Tortilla",
                        categoria = "TORTILLERÍA",
                        precio_con_impuestos = 1800L, // $18.00
                        tasa_iva = 0.16,
                        tasa_ieps = 0.00,
                        stock = 6.223,
                        unidad_medida = "Kilogramo",
                        alerta_minimo = 10.0,
                        tenant_id = tenantId,
                        sucursal_id = sucursalId
                    ),
                    ProductoEntity(
                        id = "PROD-002",
                        sku = "TAM-002",
                        nombre = "Tamal Rojo",
                        categoria = "TORTILLERÍA",
                        precio_con_impuestos = 800L, // $8.00
                        tasa_iva = 0.16,
                        tasa_ieps = 0.00,
                        stock = 82.0,
                        unidad_medida = "Pieza",
                        alerta_minimo = 15.0,
                        tenant_id = tenantId,
                        sucursal_id = sucursalId
                    ),
                    ProductoEntity(
                        id = "PROD-003",
                        sku = "ADI-001",
                        nombre = "Adicional 1.1",
                        categoria = "TORTILLERÍA",
                        precio_con_impuestos = 30000L, // $300.00
                        tasa_iva = 0.16,
                        tasa_ieps = 0.00,
                        stock = 994.0,
                        unidad_medida = "Bulto 500g",
                        alerta_minimo = 50.0,
                        tenant_id = tenantId,
                        sucursal_id = sucursalId
                    )
                )
            )

            // 4. Clientes (matching screenshot)
            val clientePatito = ClienteEntity(
                id = UUID.nameUUIDFromBytes("CLI-001".toByteArray()).toString(),
                codigo = "PRU-1",
                nombre = "Patito",
                telefono = "2222222222",
                direccion = "conocida",
                empresa = "EMPRESA QWERTY",
                condicion_pago = "CONTADO",
                tenant_id = tenantId,
                sucursal_id = sucursalId,
                sync_status = "SINCRONIZADO"
            )
            val clienteEsquina = ClienteEntity(
                id = UUID.nameUUIDFromBytes("CLI-002".toByteArray()).toString(),
                codigo = "CLI-001",
                nombre = "Tienda de la Esquina",
                telefono = "5555555555",
                direccion = "24 poniente",
                empresa = "EMPRESA QWERTY",
                condicion_pago = "CONTADO",
                tenant_id = tenantId,
                sucursal_id = sucursalId,
                sync_status = "SINCRONIZADO"
            )
            dao.upsertCliente(clientePatito)
            dao.upsertCliente(clienteEsquina)
            dao.upsertCliente(
                ClienteEntity(
                    id = UUID.nameUUIDFromBytes("CLI-003".toByteArray()).toString(),
                    codigo = "CLI-002",
                    nombre = "Prueba",
                    telefono = "2525252525",
                    direccion = "24 sur",
                    empresa = "EMPRESA QWERTY",
                    condicion_pago = "CONTADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                )
            )

            // 5. Historial de Sesiones previas (matching screenshots)
            dao.upsertSesionCaja(
                SesionCajaEntity(
                    id = "SES-HIST-001",
                    sucursal_id = sucursalId,
                    tenant_id = tenantId,
                    usuario_apertura_id = "USR-001",
                    usuario_apertura_nombre = "Caja Sucursal Centro",
                    usuario_cierre_id = "USR-001",
                    usuario_cierre_nombre = "Caja Sucursal Centro",
                    fecha_apertura = 1721510000000L,
                    fecha_cierre = 1721515000000L,
                    fondo_inicial = 10230000L, // $102300.00
                    monto_arqueo = 10240000L, // $102400.00
                    diferencia = -4349L, // -$43.49
                    notas = "se compraron 43.49 pesos de papel",
                    estado = "CERRADA",
                    sync_status = "SINCRONIZADO"
                )
            )
            dao.upsertSesionCaja(
                SesionCajaEntity(
                    id = "SES-HIST-002",
                    sucursal_id = sucursalId,
                    tenant_id = tenantId,
                    usuario_apertura_id = "USR-001",
                    usuario_apertura_nombre = "Caja Sucursal Centro",
                    usuario_cierre_id = "USR-001",
                    usuario_cierre_nombre = "Caja Sucursal Centro",
                    fecha_apertura = 1720450000000L,
                    fecha_cierre = 1721510000000L,
                    fondo_inicial = 240000L, // $2400.00
                    monto_arqueo = 8800000L, // $88000.00
                    diferencia = 8382731L, // $83827.31
                    notas = "donacion",
                    estado = "CERRADA",
                    sync_status = "SINCRONIZADO"
                )
            )
            dao.upsertSesionCaja(
                SesionCajaEntity(
                    id = "SES-HIST-003",
                    sucursal_id = sucursalId,
                    tenant_id = tenantId,
                    usuario_apertura_id = "USR-001",
                    usuario_apertura_nombre = "Caja Sucursal Centro",
                    usuario_cierre_id = "USR-001",
                    usuario_cierre_nombre = "Caja Sucursal Centro",
                    fecha_apertura = 1719950000000L,
                    fecha_cierre = 1720380000000L,
                    fondo_inicial = 582500L, // $5825.00
                    monto_arqueo = 500000L, // $5000.00
                    diferencia = -94200L, // -$942.00
                    notas = "xxxx",
                    estado = "CERRADA",
                    sync_status = "SINCRONIZADO"
                )
            )

            // 6. Catálogos Semilla de Logística (Vehículos, Termos y Vendedores)
            dao.upsertVehiculos(
                listOf(
                    VehiculoEntity(id = "10", placa = "XXX", modelo = "van", capacidad_kg = 1000.0, tenant_id = tenantId),
                    VehiculoEntity(id = "11", placa = "CCCC", modelo = "van", capacidad_kg = 1000.0, tenant_id = tenantId)
                )
            )
            dao.upsertTermos(
                listOf(
                    TermoEntity(id = "TERM-01", codigo = "TERM-01", nombre = "Termo 01", precio = 0L, tenant_id = tenantId),
                    TermoEntity(id = "TERM-02", codigo = "TERM-02", nombre = "Termo 02", precio = 0L, tenant_id = tenantId)
                )
            )
            dao.upsertVendedores(
                listOf(
                    VendedorEntity(id = "01a044f9-0d88-7b77-8409-7573ad5b02c8", nombre = "Repartidor Paterno Materno", rol = "REPARTIDOR", tenant_id = tenantId),
                    VendedorEntity(id = "01a044f9-8d35-7cfa-830b-307083d46389", nombre = "Rep B B B", rol = "REPARTIDOR", tenant_id = tenantId),
                    VendedorEntity(id = "01a044f9-f51f-7091-87f6-a50e97865d24", nombre = "Rep C C C", rol = "REPARTIDOR", tenant_id = tenantId)
                )
            )

            // 6. Pedidos Mayoristas (matching screenshot 9) - marcados como SINCRONIZADO para evitar envio de semillas sinteticas
            val pedidosSeed = listOf(
                PedidoMayoristaEntity(
                    id = UUID.nameUUIDFromBytes("PED-001".toByteArray()).toString(),
                    folio = "P-20260720-6784",
                    cliente_id = clientePatito.id,
                    cliente_nombre = "Patito",
                    direccion = "conocida",
                    contacto = "2222222222",
                    fecha_entrega = "20 jul 2026",
                    condicion = "CONTADO",
                    total = 5400L,
                    saldo = 0L,
                    estado = "ENTREGADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                ),
                PedidoMayoristaEntity(
                    id = UUID.nameUUIDFromBytes("PED-002".toByteArray()).toString(),
                    folio = "P-20260720-8867",
                    cliente_id = clientePatito.id,
                    cliente_nombre = "Patito",
                    direccion = "conocida",
                    contacto = "2222222222",
                    fecha_entrega = "20 jul 2026",
                    condicion = "CONTADO",
                    total = 14400L,
                    saldo = 0L,
                    estado = "ENTREGADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                ),
                PedidoMayoristaEntity(
                    id = UUID.nameUUIDFromBytes("PED-003".toByteArray()).toString(),
                    folio = "P-20260720-7541",
                    cliente_id = clientePatito.id,
                    cliente_nombre = "Patito",
                    direccion = "conocida",
                    contacto = "2222222222",
                    fecha_entrega = "30 jul 2026",
                    condicion = "CONTADO",
                    total = 130000L,
                    saldo = 0L,
                    estado = "ENTREGADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                ),
                PedidoMayoristaEntity(
                    id = UUID.nameUUIDFromBytes("PED-004".toByteArray()).toString(),
                    folio = "P-20260710-4068",
                    cliente_id = clienteEsquina.id,
                    cliente_nombre = "Tienda de la Esquina",
                    direccion = "24 poniente",
                    contacto = "5555555555",
                    fecha_entrega = "10 jul 2026",
                    condicion = "CONTADO",
                    total = 1800L,
                    saldo = 0L,
                    estado = "ENTREGADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                ),
                PedidoMayoristaEntity(
                    id = UUID.nameUUIDFromBytes("PED-005".toByteArray()).toString(),
                    folio = "P-20260708-4621",
                    cliente_id = clienteEsquina.id,
                    cliente_nombre = "Tienda de la Esquina",
                    direccion = "24 poniente",
                    contacto = "5555555555",
                    fecha_entrega = "8 jul 2026",
                    condicion = "CONTADO",
                    total = 1800L,
                    saldo = 0L,
                    estado = "ENTREGADO",
                    tenant_id = tenantId,
                    sucursal_id = sucursalId,
                    sync_status = "SINCRONIZADO"
                )
            )
            pedidosSeed.forEach { dao.upsertPedidoMayorista(it) }
        }
    }
}
