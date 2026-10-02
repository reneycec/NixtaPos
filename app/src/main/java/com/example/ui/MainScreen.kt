package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArqueoControlDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ExitAppConfirmationDialog
import com.example.ui.components.NixtaSidebar
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.QuickSearchDialog
import com.example.ui.components.SupervisorOverrideDialog
import com.example.ui.components.VentaGranelDialog
import com.example.ui.screens.CajaSessionScreen
import com.example.ui.screens.ConfigSyncScreen
import com.example.ui.screens.DashboardPanelScreen
import com.example.ui.screens.EntregasMayoristasScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NivelesInventarioScreen
import com.example.ui.screens.VentaMostradorScreen
import com.example.ui.theme.NixtaBackgroundCream
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaBadgeRedBg
import com.example.ui.theme.NixtaBadgeRedText
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import com.example.ui.viewmodels.LoginViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    onExitApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSidebarOpen by remember { mutableStateOf(true) }
    var isExitDialogOpen by remember { mutableStateOf(false) }

    val currentDestination by viewModel.currentDestination.collectAsState()
    val currentUsuario by viewModel.currentUsuario.collectAsState()

    if (currentUsuario == null) {
        LoginScreen(
            viewModel = loginViewModel,
            onLoginSuccess = {
                // El StateFlow de currentUsuario se actualizará automáticamente
            }
        )
        return
    }

    val empresaNombre by viewModel.empresaNombre.collectAsState()
    val sucursalNombre by viewModel.sucursalNombre.collectAsState()
    val tenantId by viewModel.tenantId.collectAsState()
    val sucursalId by viewModel.sucursalId.collectAsState()
    val terminalId by viewModel.terminalId.collectAsState()

    val sesionActiva by viewModel.sesionActiva.collectAsState()
    val historialSesiones by viewModel.historialSesiones.collectAsState()
    val movimientos by viewModel.movimientosSesion.collectAsState()
    val ventasEfectivo by viewModel.ventasEfectivoSesion.collectAsState()
    val ventasOtros by viewModel.ventasOtrosSesion.collectAsState()
    val denominations by viewModel.denominations.collectAsState()
    val aperturaNotas by viewModel.aperturaNotas.collectAsState()

    val productos by viewModel.productos.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val desglosarImpuestos by viewModel.desglosarImpuestos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val scannerModalOpen by viewModel.scannerModalOpen.collectAsState()

    val clientes by viewModel.clientes.collectAsState()
    val pedidosMayorista by viewModel.pedidosMayorista.collectAsState()

    val supervisorModalOpen by viewModel.supervisorModalOpen.collectAsState()
    val supervisorPinInput by viewModel.supervisorPinInput.collectAsState()
    val supervisorError by viewModel.supervisorError.collectAsState()

    val pinModalOpen by viewModel.pinModalOpen.collectAsState()
    val enteredPin by viewModel.enteredPin.collectAsState()
    val pinError by viewModel.pinError.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()
    val lastAutoSyncTime by viewModel.lastAutoSyncTime.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val quickSearchOpen by viewModel.quickSearchOpen.collectAsState()
    val arqueoModalOpen by viewModel.arqueoModalOpen.collectAsState()

    val movimientosSesion by viewModel.movimientosSesion.collectAsState()
    val ventasEfectivoSesion by viewModel.ventasEfectivoSesion.collectAsState()
    val todasVentas by viewModel.todasVentas.collectAsState()

    val flotillaRepartidores by viewModel.flotillaRepartidores.collectAsState()
    val vehiculosDisponibles by viewModel.vehiculosDisponibles.collectAsState()
    val termosDisponibles by viewModel.termosDisponibles.collectAsState()
    val asignacionesTermo by viewModel.asignacionesTermo.collectAsState()
    val sucursales by viewModel.sucursales.collectAsState()
    val currentSucursal by viewModel.currentSucursal.collectAsState()

    val granelModalOpen by viewModel.granelModalOpen.collectAsState()
    val productoParaGranel by viewModel.productoParaGranel.collectAsState()


    val totalCartItemsCount = cartItems.sumOf { it.cantidad }.toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NixtaBackgroundCream)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Tablet Sidebar Navigation Drawer (Collapsible)
            AnimatedVisibility(
                visible = isSidebarOpen,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                NixtaSidebar(
                    currentDestination = currentDestination,
                    currentUsuario = currentUsuario,
                    empresaNombre = empresaNombre,
                    sucursalNombre = sucursalNombre,
                    cartItemsCount = totalCartItemsCount,
                    onNavigate = { viewModel.navigateTo(it) },
                    onLockClick = { viewModel.openPinModal() },
                    onLogoutClick = { viewModel.logout() },
                    onExitAppClick = { isExitDialogOpen = true },
                    onToggleCollapse = { isSidebarOpen = false }
                )
            }

            // Main Content Body with Top Action Bar
            Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                // Top Navigation Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NixtaSurfaceCream)
                        .border(1.dp, NixtaSurfaceBorder)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSidebarOpen = !isSidebarOpen }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú Principal",
                                tint = NixtaTerracottaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "NIXTA ERP · ${sucursalNombre.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = when (currentDestination) {
                                    NavDestination.DASHBOARD_PANEL -> "Panel de Control & Dashboard"
                                    NavDestination.CAJA_SESION -> "Apertura / Cierre de Caja"
                                    NavDestination.VENTA_MOSTRADOR -> "Venta Mostrador"
                                    NavDestination.ENTREGAS_MAYORISTAS -> "Entregas Mayoristas"
                                    NavDestination.NIVELES_INVENTARIO -> "Niveles de Inventario"
                                    NavDestination.CONFIG_SYNC -> "Configuración & Sync"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NixtaTextPrimary
                            )
                        }
                    }

                    // Status Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Búsqueda Rápida Shortcut Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(NixtaSurfaceBorder.copy(alpha = 0.4f))
                                .clickable { viewModel.openQuickSearch() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Búsqueda Rápida",
                                    tint = NixtaTerracottaPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buscar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NixtaTextPrimary
                                )
                            }
                        }

                        // Arqueo de Caja & Control Efectivo Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(NixtaSurfaceBorder.copy(alpha = 0.4f))
                                .clickable { viewModel.openArqueoModal() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Arqueo de Caja",
                                    tint = NixtaTerracottaPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Arqueo & Efectivo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NixtaTextPrimary
                                )
                            }
                        }

                        // Background Network Sync Indicator Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isOnline) NixtaBadgeGreenBg else NixtaBadgeRedBg)
                                .clickable { viewModel.triggerManualSync() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isOnline) NixtaBadgeGreenText else NixtaBadgeRedText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    color = if (isOnline) NixtaBadgeGreenText else NixtaBadgeRedText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Dark Mode Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Modo Oscuro POS",
                                tint = NixtaTerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Cart Badge shortcut
                        if (totalCartItemsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NixtaTerracottaPrimary)
                                    .clickable { viewModel.navigateTo(NavDestination.VENTA_MOSTRADOR) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$totalCartItemsCount items",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Session Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (sesionActiva != null) NixtaBadgeGreenBg else NixtaBadgeRedBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (sesionActiva != null) "CAJA ABIERTA" else "CAJA CERRADA",
                                color = if (sesionActiva != null) NixtaBadgeGreenText else NixtaBadgeRedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Lock Terminal Action
                        IconButton(
                            onClick = { viewModel.openPinModal() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloquear Terminal",
                                tint = NixtaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Exit App Action
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NixtaBadgeRedBg)
                                .clickable { isExitDialogOpen = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = "Salir",
                                    tint = NixtaBadgeRedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Salir",
                                    color = NixtaBadgeRedText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Main Screen Body
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (currentDestination) {
                        NavDestination.DASHBOARD_PANEL -> {
                            DashboardPanelScreen(
                                todasVentas = todasVentas,
                                isOnline = isOnline,
                                lastAutoSyncTime = lastAutoSyncTime,
                                onTriggerSync = { viewModel.triggerManualSync() }
                            )
                        }
                        NavDestination.CAJA_SESION -> {
                            CajaSessionScreen(
                                sesionActiva = sesionActiva,
                                historialSesiones = historialSesiones,
                                movimientos = movimientos,
                                ventasEfectivo = ventasEfectivo,
                                ventasOtros = ventasOtros,
                                denominations = denominations,
                                aperturaNotas = aperturaNotas,
                                onDenominationChange = { viewModel.updateDenomination(it) },
                                onNotasChange = { viewModel.updateAperturaNotas(it) },
                                onAbrirCaja = { viewModel.abrirCaja() },
                                onCerrarCaja = { montoCentavos, notas -> viewModel.cerrarCaja(montoCentavos, notas) },
                                onRegistrarMovimiento = { tipo, monto, concepto ->
                                    viewModel.registrarMovimiento(tipo, monto, concepto)
                                }
                            )
                        }
                        NavDestination.VENTA_MOSTRADOR -> {
                            VentaMostradorScreen(
                                sesionActiva = sesionActiva,
                                productos = productos,
                                clientes = clientes,
                                cartItems = cartItems,
                                desglosarImpuestos = desglosarImpuestos,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCategoryChange = { viewModel.setSelectedCategory(it) },
                                onOpenScanner = { viewModel.setScannerModalOpen(true) },
                                onAddToCart = { viewModel.addToCart(it) },
                                onAddToCartGranel = { viewModel.openGranelModal(it) },
                                onUpdateQuantity = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
                                onTogglePapel = { viewModel.toggleItemPapel(it) },
                                onRemoveFromCart = { viewModel.removeFromCart(it) },
                                onClearCart = { viewModel.clearCart() },
                                onToggleDesglosarImpuestos = { viewModel.toggleDesglosarImpuestos() },
                                onCobrar = { metodo, clienteId -> viewModel.cobroExitoso(metodo, clienteId) },
                                onGoToApertura = { viewModel.navigateTo(NavDestination.CAJA_SESION) }
                            )
                        }
                        NavDestination.ENTREGAS_MAYORISTAS -> {
                            EntregasMayoristasScreen(
                                repartidores = flotillaRepartidores,
                                vehiculos = vehiculosDisponibles,
                                termos = termosDisponibles,
                                asignaciones = asignacionesTermo,
                                pedidos = pedidosMayorista,
                                productos = productos,
                                sucursales = sucursales,
                                currentSucursal = currentSucursal,
                                onAsignarTermo = { rep, veh, ter, det -> viewModel.asignarTermo(rep, veh, ter, det) },
                                onRecibirTermo = { asig, det -> viewModel.recibirTermo(asig, det) },
                                onAsignarPedido = { ped, rep -> viewModel.asignarPedidoRegistrado(ped, rep) },
                                onNewPedidoFuturo = { ped -> viewModel.registrarNuevoPedidoConDetalles(ped, emptyList()) }
                            )
                        }
                        NavDestination.NIVELES_INVENTARIO -> {
                            NivelesInventarioScreen(
                                productos = productos
                            )
                        }
                        NavDestination.CONFIG_SYNC -> {
                            ConfigSyncScreen(
                                syncManager = viewModel.syncManager,
                                tenantId = tenantId,
                                sucursalId = sucursalId,
                                terminalId = terminalId,
                                empresaNombre = empresaNombre,
                                sucursalNombre = sucursalNombre,
                                isOnline = isOnline,
                                lastAutoSyncTime = lastAutoSyncTime,
                                isDarkMode = isDarkMode,
                                productosCount = productos.size,
                                clientesCount = clientes.size,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onTriggerSync = { viewModel.triggerManualSync() }
                            )
                        }
                    }
                }
            }
        }

        // MODAL DE TICKET DE VENTA E IMPRESIÓN
        val ticketData by viewModel.lastCompletedTicketData.collectAsState()
        ticketData?.let { ticket ->
            com.example.ui.components.TicketDialog(
                ticketData = ticket,
                onDismiss = { viewModel.dismissTicketDialog() }
            )
        }

        // Modals
        if (granelModalOpen && productoParaGranel != null) {
            VentaGranelDialog(
                producto = productoParaGranel!!,
                onDismiss = { viewModel.closeGranelModal() },
                onAddToCart = { cantidad -> viewModel.addToCart(productoParaGranel!!, cantidad) }
            )
        }

        SupervisorOverrideDialog(
            isOpen = supervisorModalOpen,
            pinInput = supervisorPinInput,
            errorMessage = supervisorError,
            onPasswordChange = { viewModel.updateSupervisorPassword(it) },
            onConfirm = { viewModel.submitSupervisorOverride() },
            onClose = { viewModel.closeSupervisorModal() }
        )

        PinKeypadDialog(
            isOpen = pinModalOpen,
            pinInput = enteredPin,
            errorMessage = pinError,
            onPasswordChange = { viewModel.updateCashierPassword(it) },
            onConfirm = { viewModel.submitCashierPassword() },
            onClose = { viewModel.closePinModal() }
        )

        BarcodeScannerDialog(
            isOpen = scannerModalOpen,
            productos = productos,
            onBarcodeScanned = { prod ->
                viewModel.addToCart(prod)
                viewModel.setScannerModalOpen(false)
            },
            onClose = { viewModel.setScannerModalOpen(false) }
        )

        QuickSearchDialog(
            isOpen = quickSearchOpen,
            productos = productos,
            clientes = clientes,
            onAddToCart = { prod ->
                viewModel.addToCart(prod)
            },
            onSelectCliente = { cli ->
                // Navigate to Mayoristas or select client
                viewModel.navigateTo(NavDestination.ENTREGAS_MAYORISTAS)
            },
            onDismiss = { viewModel.closeQuickSearch() }
        )

        ArqueoControlDialog(
            isOpen = arqueoModalOpen,
            sesionActiva = sesionActiva,
            denominations = denominations,
            movimientos = movimientosSesion,
            ventasEfectivoCentavos = ventasEfectivoSesion,
            onDenominationChange = { block -> viewModel.updateDenomination(block) },
            onRegistrarMovimiento = { tipo, montoCentavos, concepto ->
                viewModel.registrarMovimiento(tipo, montoCentavos, concepto)
            },
            onAbrirCaja = { viewModel.abrirCaja() },
            onCerrarCaja = { montoCentavos, notas -> viewModel.cerrarCaja(montoCentavos, notas) },
            onDismiss = { viewModel.closeArqueoModal() }
        )

        ExitAppConfirmationDialog(
            isOpen = isExitDialogOpen,
            onConfirmExit = onExitApp,
            onDismiss = { isExitDialogOpen = false }
        )
    }
}
