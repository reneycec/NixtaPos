package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.NixtaPosDatabase
import com.example.data.local.entities.AsignacionTermoDetalleEntity
import com.example.data.local.entities.AsignacionTermoEntity
import com.example.data.local.entities.ClienteEntity
import com.example.data.local.entities.PedidoMayoristaDetalleEntity
import com.example.data.local.entities.PedidoMayoristaEntity
import com.example.data.local.entities.ProductoEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.data.local.entities.SucursalEntity
import com.example.data.local.entities.TermoEntity
import com.example.data.local.entities.UsuarioEntity
import com.example.data.local.entities.VehiculoEntity
import com.example.data.local.entities.VendedorEntity
import com.example.data.local.entities.VentaEntity
import com.example.data.remote.NetworkSyncObserver
import com.example.data.remote.SyncManager
import com.example.data.repository.CartItem
import com.example.data.repository.PosRepository
import com.example.data.repository.TerminalConfigManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.repository.SyncRepository
import com.example.data.local.remote.UrlInterceptor
import com.example.di.NetworkModule
import java.util.UUID

enum class NavDestination(val title: String, val category: String) {
    DASHBOARD_PANEL("Panel de Control & Dashboard", "Reportes"),
    CAJA_SESION("Apertura / Cierre de Caja", "Punto de Venta"),
    VENTA_MOSTRADOR("Venta Mostrador", "Punto de Venta"),
    ENTREGAS_MAYORISTAS("Entregas Mayoristas", "Punto de Venta"),
    NIVELES_INVENTARIO("Niveles de Inventario", "Inventario"),
    CONFIG_SYNC("Configuración & Sync", "Sistema")
}

data class DenominationCount(
    val b1000: Int = 0,
    val b500: Int = 0,
    val b200: Int = 0,
    val b100: Int = 0,
    val b50: Int = 0,
    val b20: Int = 0,
    val m10: Int = 0,
    val m5: Int = 0,
    val m2: Int = 0,
    val m1: Int = 0,
    val m05: Int = 0
) {
    fun calculateTotalCents(): Long {
        return (b1000 * 1000L +
                b500 * 500L +
                b200 * 200L +
                b100 * 100L +
                b50 * 50L +
                b20 * 20L +
                m10 * 10L +
                m5 * 5L +
                m2 * 2L +
                m1 * 1L +
                m05 * 0.5).toLong() * 100L
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NixtaPosDatabase.getDatabase(application)
    private val configManager = TerminalConfigManager(application)
    
    private val apiService = NetworkModule.provideApiService(
        NetworkModule.provideRetrofit(
            NetworkModule.provideOkHttpClient(UrlInterceptor(configManager)),
            NetworkModule.provideMoshi()
        )
    )
    val syncRepository = SyncRepository(apiService, db.posDao(), configManager)
    val repository = PosRepository(db.posDao(), configManager)
    val syncManager = SyncManager(db.posDao(), syncRepository)

    // Background Network Synchronization Observer
    val networkObserver = NetworkSyncObserver(application, syncManager)
    val isOnline: StateFlow<Boolean> = networkObserver.isOnline
    val lastAutoSyncTime: StateFlow<Long?> = networkObserver.lastAutoSyncTime

    // POS Dark Theme State
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    init {
        triggerManualSync()
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            syncManager.executeRealSync(syncRepository)
        }
    }

    // Config state
    val tenantId: StateFlow<String> = configManager.tenantId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TENANT-NIXTA-01")
    val sucursalId: StateFlow<String> = configManager.sucursalId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SUCURSAL-CENTRO")
    val terminalId: StateFlow<String> = configManager.terminalId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "POS-TABLET-01")
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSucursal: StateFlow<com.example.data.local.entities.SucursalEntity?> = sucursalId
        .flatMapLatest { id -> db.posDao().getSucursalById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sucursales: StateFlow<List<SucursalEntity>> = tenantId
        .flatMapLatest { t -> db.posDao().getSucursales(t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sucursalNombre: StateFlow<String> = currentSucursal
        .map { it?.nombre?.ifBlank { "SUCURSAL 01" } ?: "SUCURSAL 01" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SUCURSAL 01")

    val empresaNombre: StateFlow<String> = currentSucursal
        .map { it?.empresa?.ifBlank { "EMPRESA NIXTA" } ?: "EMPRESA NIXTA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EMPRESA NIXTA")

    // Navigation state
    private val _currentDestination = MutableStateFlow(NavDestination.CAJA_SESION)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    // Active User (Cashier)
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUsuario: StateFlow<UsuarioEntity?> = configManager.currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else db.posDao().getUsuarioById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        viewModelScope.launch {
            try {
                syncRepository.pushData()
                apiService.logoutRemote()
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error en logout remoto", e)
            }
            configManager.updateCurrentUserId(null)
            configManager.updateCurrentUserEmail(null)
            configManager.updateJwtToken(null)
            navigateTo(NavDestination.CAJA_SESION)
        }
    }

    // Active Cash Session from Room
    @OptIn(ExperimentalCoroutinesApi::class)
    val sesionActiva: StateFlow<SesionCajaEntity?> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getSesionActiva(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Historical Cash Sessions
    @OptIn(ExperimentalCoroutinesApi::class)
    val historialSesiones: StateFlow<List<SesionCajaEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getHistorialSesiones(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash Denominations Input State
    private val _denominations = MutableStateFlow(DenominationCount())
    val denominations: StateFlow<DenominationCount> = _denominations.asStateFlow()

    private val _aperturaNotas = MutableStateFlow("")
    val aperturaNotas: StateFlow<String> = _aperturaNotas.asStateFlow()

    // Cash Movements for Active Session
    @OptIn(ExperimentalCoroutinesApi::class)
    val movimientosSesion = sesionActiva.flatMapLatest { sesion ->
        if (sesion != null) {
            repository.getMovimientosSesion(sesion.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Session Sales Summaries
    @OptIn(ExperimentalCoroutinesApi::class)
    val ventasEfectivoSesion = sesionActiva.flatMapLatest { sesion ->
        if (sesion != null) {
            repository.getTotalVentasEfectivo(sesion.id).map { it ?: 0L }
        } else {
            flowOf(0L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ventasOtrosSesion = sesionActiva.flatMapLatest { sesion ->
        if (sesion != null) {
            repository.getTotalVentasOtros(sesion.id).map { it ?: 0L }
        } else {
            flowOf(0L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // POS Products Catalog
    @OptIn(ExperimentalCoroutinesApi::class)
    val productos: StateFlow<List<ProductoEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getProductos(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _desglosarImpuestos = MutableStateFlow(false)
    val desglosarImpuestos: StateFlow<Boolean> = _desglosarImpuestos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _scannerModalOpen = MutableStateFlow(false)
    val scannerModalOpen: StateFlow<Boolean> = _scannerModalOpen.asStateFlow()

    // Quick Search Modal State
    private val _quickSearchOpen = MutableStateFlow(false)
    val quickSearchOpen: StateFlow<Boolean> = _quickSearchOpen.asStateFlow()

    fun openQuickSearch() { _quickSearchOpen.value = true }
    fun closeQuickSearch() { _quickSearchOpen.value = false }

    // Arqueo de Caja & Cash Control Modal State
    private val _arqueoModalOpen = MutableStateFlow(false)
    val arqueoModalOpen: StateFlow<Boolean> = _arqueoModalOpen.asStateFlow()

    fun openArqueoModal() { _arqueoModalOpen.value = true }
    fun closeArqueoModal() { _arqueoModalOpen.value = false }

    // All Sales for Dashboard Analytics
    @OptIn(ExperimentalCoroutinesApi::class)
    val todasVentas: StateFlow<List<VentaEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getTodasVentas(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wholesale Orders & Customers
    @OptIn(ExperimentalCoroutinesApi::class)
    val clientes: StateFlow<List<ClienteEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getClientes(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pedidosMayorista: StateFlow<List<PedidoMayoristaEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getPedidosMayorista(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LOGISTICA (Mesa de Control) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val flotillaRepartidores: StateFlow<List<VendedorEntity>> = tenantId
        .flatMapLatest { t -> repository.getVendedores(t) }
        .map { list -> list.filter { it.rol.equals("REPARTIDOR", ignoreCase = true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val vehiculosDisponibles: StateFlow<List<VehiculoEntity>> = tenantId
        .flatMapLatest { t -> repository.getVehiculos(t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val termosDisponibles: StateFlow<List<TermoEntity>> = tenantId
        .flatMapLatest { t -> repository.getTermos(t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val asignacionesTermo: StateFlow<List<AsignacionTermoEntity>> = combine(tenantId, sucursalId) { t, s -> t to s }
        .flatMapLatest { (t, s) -> repository.getAsignacionesTermo(t, s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supervisor Override Dialog State
    private val _supervisorModalOpen = MutableStateFlow(false)
    val supervisorModalOpen: StateFlow<Boolean> = _supervisorModalOpen.asStateFlow()

    private val _supervisorPinInput = MutableStateFlow("")
    val supervisorPinInput: StateFlow<String> = _supervisorPinInput.asStateFlow()

    private val _supervisorError = MutableStateFlow<String?>(null)
    val supervisorError: StateFlow<String?> = _supervisorError.asStateFlow()

    private var pendingSupervisorAction: (() -> Unit)? = null

    // Cashier Switch / PIN Pad Dialog
    private val _pinModalOpen = MutableStateFlow(false)
    val pinModalOpen: StateFlow<Boolean> = _pinModalOpen.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // --- ACTIONS ---

    // Modal de Venta a Granel
    private val _granelModalOpen = MutableStateFlow(false)
    val granelModalOpen: StateFlow<Boolean> = _granelModalOpen.asStateFlow()

    private val _productoParaGranel = MutableStateFlow<ProductoEntity?>(null)
    val productoParaGranel: StateFlow<ProductoEntity?> = _productoParaGranel.asStateFlow()

    fun navigateTo(destination: NavDestination) {
        _currentDestination.value = destination
    }

    fun updateDenomination(updateBlock: (DenominationCount) -> DenominationCount) {
        _denominations.value = updateBlock(_denominations.value)
    }

    fun updateAperturaNotas(notas: String) {
        _aperturaNotas.value = notas
    }

    fun abrirCaja() {
        viewModelScope.launch {
            val user = currentUsuario.value ?: return@launch
            val fondo = _denominations.value.calculateTotalCents()
            val notas = _aperturaNotas.value
            syncManager.executeCheckpointSync("Apertura de Turno")
            repository.abrirSesionCaja(
                fondoInicial = fondo,
                notas = notas,
                tenantId = tenantId.value,
                sucursalId = sucursalId.value,
                usuario = user
            )
            _denominations.value = DenominationCount()
            _aperturaNotas.value = ""
        }
    }

    fun cerrarCaja(montoArqueoDirecto: Long? = null, notasCierre: String = "") {
        val sesion = sesionActiva.value ?: return
        val user = currentUsuario.value ?: return
        viewModelScope.launch {
            val arqueo = montoArqueoDirecto ?: _denominations.value.calculateTotalCents()
            val ventasEfectivo = ventasEfectivoSesion.value
            val esperado = sesion.fondo_inicial + ventasEfectivo

            // Push pending sales before updating cash session state
            try {
                syncRepository.pushDataDetailed()
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error al sincronizar ventas previas al cierre de caja", e)
            }

            try {
                repository.cerrarSesionCaja(
                    sesion = sesion,
                    montoArqueo = arqueo,
                    montoEsperado = esperado,
                    notas = notasCierre,
                    usuarioCierre = user
                )

                // Try explicit POST /api/v1/caja/cierre-movil endpoint
                try {
                    val req = com.example.data.local.remote.CierreCajaRequest(
                        sesionCajaId = sesion.id,
                        montoArqueo = arqueo,
                        notas = notasCierre,
                        usuarioCierreId = user.id,
                        usuarioCierreNombre = user.nombre,
                        fechaCierre = System.currentTimeMillis(),
                        tenantId = tenantId.value,
                        sucursalId = sucursalId.value
                    )
                    Log.d("MainViewModel", "Enviando petición POST /api/v1/caja/cierre-movil -> $req")
                    val res = apiService.cerrarCajaRemote(req)
                    Log.d("MainViewModel", "Respuesta POST /api/v1/caja/cierre-movil -> code=${res.code()}, isSuccessful=${res.isSuccessful}, body=${res.body()}")
                    if (res.isSuccessful && res.body()?.success == true) {
                        db.posDao().marcarSesionesSincronizadas(listOf(sesion.id))
                        Log.i("MainViewModel", "Sesión de caja ${sesion.id} marcada como sincronizada exitosamente en el servidor.")
                    } else {
                        Log.e("MainViewModel", "Fallo al registrar cierre en /api/v1/caja/cierre-movil: code=${res.code()}, errorBody=${res.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Excepción al contactar endpoint POST /api/v1/caja/cierre-movil: ${e.message}", e)
                }

                _denominations.value = DenominationCount()
                triggerManualSync()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error al cerrar caja", e)
            }
        }
    }

    fun registrarMovimiento(tipo: String, montoCentavos: Long, concepto: String) {
        val sesion = sesionActiva.value ?: return
        val currentUserId = currentUsuario.value?.id ?: ""
        viewModelScope.launch {
            repository.registrarMovimiento(
                sesionId = sesion.id,
                tipo = tipo,
                monto = montoCentavos,
                concepto = concepto,
                tenantId = tenantId.value,
                sucursalId = sucursalId.value,
                usuarioId = currentUserId
            )
            try {
                syncRepository.pushData()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error al sincronizar movimiento de caja: ${e.message}")
            }
        }
    }

    // POS Cart Actions
    // Venta a Granel Actions
    fun openGranelModal(producto: ProductoEntity) {
        _productoParaGranel.value = producto
        _granelModalOpen.value = true
    }

    fun closeGranelModal() {
        _granelModalOpen.value = false
        _productoParaGranel.value = null
    }

    fun addToCart(producto: ProductoEntity, cantidad: Double = 1.0) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.producto.id == producto.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(cantidad = existing.cantidad + cantidad)
        } else {
            current.add(CartItem(producto = producto, cantidad = cantidad))
        }
        _cartItems.value = current
        if (_granelModalOpen.value) {
            closeGranelModal()
        }
    }

    fun updateCartItemQuantity(productoId: String, newCantidad: Double) {
        if (newCantidad <= 0.001) {
            removeFromCart(productoId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.producto.id == productoId }
        if (index >= 0) {
            current[index] = current[index].copy(cantidad = newCantidad)
            _cartItems.value = current
        }
    }

    fun toggleItemPapel(productoId: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.producto.id == productoId }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(incluyePapel = !item.incluyePapel)
            _cartItems.value = current
        }
    }

    fun removeFromCart(productoId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.producto.id == productoId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun toggleDesglosarImpuestos() {
        _desglosarImpuestos.value = !_desglosarImpuestos.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setScannerModalOpen(open: Boolean) {
        _scannerModalOpen.value = open
    }

    val lastCompletedTicketData = MutableStateFlow<com.example.util.TicketPrinterUtil.TicketData?>(null)

    fun cobroExitoso(metodoPago: String, clienteId: String? = null) {
        val sesion = sesionActiva.value ?: return
        val currentUserName = currentUsuario.value?.nombre ?: "Cajero"
        val cartSnapshot = _cartItems.value.toList()

        viewModelScope.launch {
            val loggedInUserId = configManager.currentUserId.first()
            val effectiveUserId = if (!loggedInUserId.isNullOrBlank()) loggedInUserId else (currentUsuario.value?.id ?: "")

            val venta = repository.realizarVenta(
                cartItems = cartSnapshot,
                sesionId = sesion.id,
                metodoPago = metodoPago,
                clienteId = clienteId,
                tenantId = tenantId.value,
                sucursalId = sucursalId.value,
                usuarioId = effectiveUserId
            )

            val detalles = cartSnapshot.map { item ->
                com.example.data.local.entities.VentaDetalleEntity(
                    venta_id = venta.id,
                    producto_id = item.producto.id,
                    producto_nombre = item.producto.nombre + if (item.incluyePapel) " (+Papel)" else "",
                    cantidad = item.cantidad,
                    precio_unitario = item.producto.precio_con_impuestos,
                    subtotal = (item.producto.precio_con_impuestos * item.cantidad).toLong() + if (item.incluyePapel) 100L else 0L,
                    tenant_id = tenantId.value,
                    sucursal_id = sucursalId.value
                )
            }

            val cliente = clientes.value.find { it.id == clienteId }

            lastCompletedTicketData.value = com.example.util.TicketPrinterUtil.TicketData(
                empresaNombre = empresaNombre.value.ifBlank { "EMPRESA NIXTA" },
                sucursalNombre = sucursalNombre.value.ifBlank { "SUCURSAL CENTRAL" },
                folio = venta.folio,
                fechaMs = venta.fecha,
                cajeroNombre = currentUserName,
                clienteNombre = cliente?.nombre,
                metodoPago = metodoPago,
                subtotalCentavos = venta.subtotal,
                impuestosCentavos = venta.impuestos_desglosados,
                totalCentavos = venta.total,
                detalles = detalles
            )

            clearCart()
            triggerManualSync()
        }
    }

    fun dismissTicketDialog() {
        lastCompletedTicketData.value = null
    }

    // Supervisor Override Modal
    fun requestSupervisorOverride(actionName: String, action: () -> Unit) {
        pendingSupervisorAction = action
        _supervisorPinInput.value = ""
        _supervisorError.value = null
        _supervisorModalOpen.value = true
    }

    fun updateSupervisorPassword(input: String) {
        _supervisorPinInput.value = input
    }

    fun submitSupervisorOverride() {
        verifySupervisorPin()
    }

    private fun verifySupervisorPin() {
        viewModelScope.launch {
            val supervisor = repository.getSupervisorByEmail("super.roberto@nixta.com")
            if (supervisor != null && supervisor.password_hash == _supervisorPinInput.value) {
                _supervisorModalOpen.value = false
                _supervisorError.value = null
                _supervisorPinInput.value = ""
                pendingSupervisorAction?.invoke()
                pendingSupervisorAction = null
            } else {
                _supervisorError.value = "Contraseña de Supervisor incorrecta"
            }
        }
    }

    fun closeSupervisorModal() {
        _supervisorModalOpen.value = false
        pendingSupervisorAction = null
    }

    // Cashier Switch / Lock
    fun openPinModal() {
        _enteredPin.value = ""
        _pinError.value = null
        _pinModalOpen.value = true
    }

    fun updateCashierPassword(input: String) {
        _enteredPin.value = input
    }

    fun submitCashierPassword() {
        verifyCashierPin()
    }

    private fun verifyCashierPin() {
        viewModelScope.launch {
            val currentUser = currentUsuario.value ?: return@launch
            if (currentUser.password_hash == _enteredPin.value) {
                syncManager.executeCheckpointSync("Desbloqueo de Terminal")
                _pinModalOpen.value = false
                _enteredPin.value = ""
                _pinError.value = null
            } else {
                _pinError.value = "Contraseña de Usuario incorrecta"
            }
        }
    }

    fun closePinModal() {
        _pinModalOpen.value = false
    }

    fun registrarNuevoCliente(codigo: String, nombre: String, telefono: String, direccion: String, condicion: String) {
        viewModelScope.launch {
            repository.agregarCliente(
                ClienteEntity(
                    codigo = codigo,
                    nombre = nombre,
                    telefono = telefono,
                    direccion = direccion,
                    empresa = empresaNombre.value,
                    condicion_pago = condicion,
                    tenant_id = tenantId.value,
                    sucursal_id = sucursalId.value
                )
            )
        }
    }

    fun registrarNuevoPedido(clienteNombre: String, direccion: String, contacto: String, fechaEntrega: String, condicion: String, totalPesos: Double) {
        viewModelScope.launch {
            val count = pedidosMayorista.value.size + 1
            val totalCentavos = (totalPesos * 100).toLong()
            val currentUserId = currentUsuario.value?.id
            val currentTenant = tenantId.value
            val currentSucursal = sucursalId.value
            
            if (currentUserId.isNullOrBlank() || currentTenant.isBlank() || currentSucursal.isBlank()) {
                Log.e("MainViewModel", "Error: Sesión incompleta (Faltan IDs de usuario/sucursal/tenant) para registrarNuevoPedido")
                return@launch
            }
            
            // Si el nombre del cliente ingresado no está en la base local, intentaremos buscarlo o generar uno temporal en DB.
            // Para simplificar, buscamos si hay coincidencia de nombre exacto.
            val clienteExistente = clientes.value.find { it.nombre.equals(clienteNombre, ignoreCase = true) }
            val resolvedClienteId = clienteExistente?.id ?: UUID.randomUUID().toString()

            repository.agregarPedidoMayorista(
                PedidoMayoristaEntity(
                    folio = "P-20260721-$count${(100..999).random()}", //TODO: generate sequential folio properly
                    cliente_id = resolvedClienteId,
                    cliente_nombre = clienteNombre,
                    direccion = direccion,
                    contacto = contacto,
                    fecha_entrega = fechaEntrega,
                    condicion = condicion,
                    total = totalCentavos,
                    saldo = if (condicion == "CRÉDITO") totalCentavos else 0L,
                    estado = "EN PROCESO",
                    usuario_id = currentUserId,
                    tenant_id = currentTenant,
                    sucursal_id = currentSucursal,
                    sync_status = "PENDIENTE"
                ),
                emptyList()
            )
            triggerManualSync()
        }
    }

    fun registrarNuevoPedidoConDetalles(
        pedido: PedidoMayoristaEntity,
        detalles: List<PedidoMayoristaDetalleEntity>
    ) {
        viewModelScope.launch {
            val currentUserId = currentUsuario.value?.id
            val currentTenant = tenantId.value
            val currentSucursal = sucursalId.value

            if (currentTenant.isBlank() || currentSucursal.isBlank()) {
                Log.e("MainViewModel", "Error: Sesión incompleta (Faltan IDs de sucursal/tenant) para registrarNuevoPedidoConDetalles")
                return@launch
            }

            val updatedPedido = pedido.copy(
                usuario_id = if (pedido.usuario_id.isNotBlank()) pedido.usuario_id else (currentUserId ?: ""),
                tenant_id = if (pedido.tenant_id.isNotBlank()) pedido.tenant_id else currentTenant,
                sucursal_id = if (pedido.sucursal_id.isNotBlank()) pedido.sucursal_id else currentSucursal,
                sync_status = "PENDIENTE"
            )
            val updatedDetalles = detalles.map { d ->
                d.copy(
                    tenant_id = updatedPedido.tenant_id,
                    sucursal_id = updatedPedido.sucursal_id
                )
            }
            repository.agregarPedidoMayorista(updatedPedido, updatedDetalles)
            triggerManualSync()
        }
    }

    // --- Acciones de Logística ---
    fun asignarTermo(
        repartidorId: String,
        vehiculoId: String,
        termoId: String,
        detalles: List<AsignacionTermoDetalleEntity>
    ) {
        viewModelScope.launch {
            val currentTenant = tenantId.value
            val currentSucursal = sucursalId.value
            
            if (currentTenant.isBlank() || currentSucursal.isBlank()) {
                Log.e("MainViewModel", "Error: Sesión incompleta (Faltan IDs de sucursal/tenant) para asignarTermo")
                return@launch
            }

            val asignacion = AsignacionTermoEntity(
                repartidor_id = repartidorId,
                vehiculo_id = vehiculoId,
                termo_id = termoId,
                tenant_id = currentTenant,
                sucursal_id = currentSucursal,
                sync_status = "PENDIENTE"
            )
            // Actualizar el asignacion_id de cada detalle
            val detallesAjustados = detalles.map { it.copy(asignacion_id = asignacion.id, tenant_id = currentTenant) }
            repository.asignarTermo(asignacion, detallesAjustados)
            triggerManualSync()
        }
    }

    fun recibirTermo(
        asignacion: AsignacionTermoEntity,
        detallesRecibidos: List<AsignacionTermoDetalleEntity>
    ) {
        viewModelScope.launch {
            repository.recibirTermo(asignacion, detallesRecibidos)
            triggerManualSync()
        }
    }

    fun asignarPedidoRegistrado(pedidoId: String, nuevoRepartidorId: String) {
        viewModelScope.launch {
            val pedido = pedidosMayorista.value.find { it.id == pedidoId } ?: return@launch
            repository.asignarPedidoRegistrado(pedido, nuevoRepartidorId)
            triggerManualSync()
        }
    }
}
