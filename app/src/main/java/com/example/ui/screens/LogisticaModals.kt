package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.*
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AsignarCargaDialog(
    repartidor: VendedorEntity,
    onDismiss: () -> Unit,
    onSelectAsignarPedidos: () -> Unit,
    onSelectAsignarTermo: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NixtaSurfaceCream,
            modifier = Modifier
                .width(600.dp)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asignar Carga a: ${repartidor.nombre}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NixtaTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAsignarPedidos() }
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NixtaSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = NixtaTerracottaPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Asignar Pedidos", fontWeight = FontWeight.Bold, color = NixtaTextPrimary, fontSize = 16.sp)
                            Text("Crear nuevo pedido o seleccionar uno registrado", color = NixtaTextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAsignarTermo() }
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NixtaSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NixtaTerracottaPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Asignar Termo", fontWeight = FontWeight.Bold, color = NixtaTextPrimary, fontSize = 16.sp)
                            Text("Venta libre / Contenedor con producto", color = NixtaTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsignarTermoDialog(
    repartidor: VendedorEntity,
    vehiculos: List<VehiculoEntity>,
    termos: List<TermoEntity>,
    productos: List<ProductoEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<AsignacionTermoDetalleEntity>) -> Unit
) {
    var selectedVehiculo by remember { mutableStateOf<VehiculoEntity?>(vehiculos.firstOrNull()) }
    var selectedTermo by remember { mutableStateOf<TermoEntity?>(termos.firstOrNull()) }
    var orderItems by remember { mutableStateOf(mutableMapOf<String, Double>()) } // producto.id -> cantidad

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(700.dp).fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Text("Asignar Termo a ${repartidor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. Seleccionar Vehículo", fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(vehiculos) { veh ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedVehiculo = veh }.padding(4.dp)
                                ) {
                                    RadioButton(selected = (selectedVehiculo?.id == veh.id), onClick = { selectedVehiculo = veh })
                                    Text("${veh.modelo} - ${veh.placa}", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Seleccionar Termo", fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(termos) { ter ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedTermo = ter }.padding(4.dp)
                                ) {
                                    RadioButton(selected = (selectedTermo?.id == ter.id), onClick = { selectedTermo = ter })
                                    Text("${ter.codigo} - ${ter.nombre}", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("3. Agregar Productos", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { prod ->
                        val currentQty = orderItems[prod.id] ?: 0.0
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, NixtaSurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (currentQty == 0.0) {
                                    Button(onClick = { orderItems = orderItems.toMutableMap().apply { put(prod.id, 1.0) } }, modifier = Modifier.fillMaxWidth()) {
                                        Text("+ Agregar", fontSize = 12.sp)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                        IconButton(onClick = { 
                                            val newQ = currentQty - 1
                                            orderItems = orderItems.toMutableMap().apply { if (newQ > 0) put(prod.id, newQ) else remove(prod.id) }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                                        }
                                        Text(currentQty.toString(), fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            orderItems = orderItems.toMutableMap().apply { put(prod.id, currentQty + 1) }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = "Mas")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Resumen de Carga", fontWeight = FontWeight.Bold)
                Surface(color = Color.White, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(orderItems.entries.toList()) { entry ->
                            val prod = productos.find { it.id == entry.key }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${entry.value} x ${prod?.nombre ?: "Producto"}")
                                Text("$${String.format("%.2f", (entry.value * (prod?.precio_con_impuestos ?: 0L)) / 100.0)}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val vehId = selectedVehiculo?.id
                        val termoId = selectedTermo?.id
                        if (vehId != null && termoId != null) {
                            val detalles = orderItems.map { (prodId, qty) ->
                                val prod = productos.find { it.id == prodId }
                                AsignacionTermoDetalleEntity(
                                    asignacion_id = "", // Will be replaced in ViewModel
                                    producto_id = prodId,
                                    cantidad_salida = qty,
                                    precio_unitario = prod?.precio_con_impuestos ?: 0L,
                                    tenant_id = "" // Replaced in ViewModel
                                )
                            }
                            onConfirm(vehId, termoId, detalles)
                        }
                    }) {
                        Text("Confirmar Asignación")
                    }
                }
            }
        }
    }
}

@Composable
fun AsignarPedidosDialog(
    repartidor: VendedorEntity,
    vehiculos: List<VehiculoEntity>,
    pedidosPendientes: List<PedidoMayoristaEntity>,
    productos: List<ProductoEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAssignPedido: (String, String) -> Unit, // pedidoId, vehiculoId
    onNewPedido: (PedidoMayoristaEntity) -> Unit // Change to take PedidoMayoristaEntity
) {
    var selectedVehiculo by remember { mutableStateOf<VehiculoEntity?>(vehiculos.firstOrNull()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // For Nuevo Pedido
    var clienteNombre by remember { mutableStateOf("") }
    var orderItems by remember { mutableStateOf(mutableMapOf<String, Double>()) } // producto.id -> cantidad

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(700.dp).fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Text("Asignar Pedidos a ${repartidor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Vehículo", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(vehiculos) { veh ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedVehiculo = veh }.padding(4.dp)
                        ) {
                            RadioButton(selected = (selectedVehiculo?.id == veh.id), onClick = { selectedVehiculo = veh })
                            Text("${veh.modelo} - ${veh.placa}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Pedido Registrado") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Nuevo Pedido") })
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Pedido Registrado
                    LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        if (pedidosPendientes.isEmpty()) {
                            item { Text("No hay pedidos pendientes.", color = NixtaTextSecondary) }
                        }
                        items(pedidosPendientes) { ped ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ped.folio, fontWeight = FontWeight.Bold)
                                        Text(ped.cliente_nombre)
                                    }
                                    Button(onClick = { 
                                        selectedVehiculo?.id?.let { v -> onAssignPedido(ped.id, v) }
                                    }) {
                                        Text("Asignar")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Nuevo Pedido
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = clienteNombre,
                            onValueChange = { clienteNombre = it },
                            label = { Text("Nombre del Cliente") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Agregar Productos", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(productos) { prod ->
                                val currentQty = orderItems[prod.id] ?: 0.0
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, NixtaSurfaceBorder),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (currentQty == 0.0) {
                                            Button(onClick = { orderItems = orderItems.toMutableMap().apply { put(prod.id, 1.0) } }, modifier = Modifier.fillMaxWidth()) {
                                                Text("+ Agregar", fontSize = 12.sp)
                                            }
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                                IconButton(onClick = { 
                                                    val newQ = currentQty - 1
                                                    orderItems = orderItems.toMutableMap().apply { if (newQ > 0) put(prod.id, newQ) else remove(prod.id) }
                                                }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                                                }
                                                Text(currentQty.toString(), fontWeight = FontWeight.Bold)
                                                IconButton(onClick = {
                                                    orderItems = orderItems.toMutableMap().apply { put(prod.id, currentQty + 1) }
                                                }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Add, contentDescription = "Mas")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        val totalAmount = orderItems.entries.sumOf { entry -> (entry.value * (productos.find { it.id == entry.key }?.precio_con_impuestos ?: 0L)).toLong() }
                        Text("Total: $${String.format(Locale.getDefault(), "%.2f", totalAmount / 100.0)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.End))
                        
                        Button(
                            onClick = {
                                val vehId = selectedVehiculo?.id
                                if (vehId != null && clienteNombre.isNotBlank() && orderItems.isNotEmpty()) {
                                    val newPedido = PedidoMayoristaEntity(
                                        id = UUID.randomUUID().toString(),
                                        folio = "PED-${System.currentTimeMillis() % 10000}",
                                        cliente_id = "",
                                        cliente_nombre = clienteNombre,
                                        direccion = "",
                                        contacto = "",
                                        fecha_entrega = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                        estado = "EN PROCESO",
                                        total = totalAmount,
                                        saldo = totalAmount,
                                        usuario_id = repartidor.id,
                                        tenant_id = "",
                                        sucursal_id = ""
                                    )
                                    onNewPedido(newPedido)
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Crear y Asignar Pedido")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
fun RecibirTermoDialog(
    asignacion: AsignacionTermoEntity,
    detalles: List<AsignacionTermoDetalleEntity>,
    sucursales: List<SucursalEntity>,
    productos: List<ProductoEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (String, List<AsignacionTermoDetalleEntity>) -> Unit
) {
    var selectedSucursal by remember { mutableStateOf<SucursalEntity?>(sucursales.firstOrNull()) }
    // local copy of quantities returned
    var regresoItems by remember { mutableStateOf(detalles.associate { it.id to it.cantidad_salida }.toMutableMap()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(600.dp).fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Text("Recibir Termo de Asignación", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                Text("Asignación: ${asignacion.id.takeLast(6).uppercase()}", fontSize = 14.sp, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Sucursal de Recepción", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(80.dp)) {
                    items(sucursales) { suc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedSucursal = suc }.padding(4.dp)
                        ) {
                            RadioButton(selected = (selectedSucursal?.id == suc.id), onClick = { selectedSucursal = suc })
                            Text(suc.nombre)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Conteo de Productos", fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text("Producto", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Salida", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold)
                    Text("Regreso", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                }
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (detalles.isEmpty()) {
                        item { Text("No hay detalles de productos (venta libre).", modifier = Modifier.padding(top = 8.dp)) }
                    }
                    items(detalles) { det ->
                        val prodNombre = productos.find { it.id == det.producto_id }?.nombre ?: det.producto_id
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prodNombre, modifier = Modifier.weight(1f))
                            Text(det.cantidad_salida.toString(), modifier = Modifier.width(60.dp))
                            
                            OutlinedTextField(
                                value = regresoItems[det.id]?.toString() ?: "0.0",
                                onValueChange = { 
                                    val q = it.toDoubleOrNull() ?: 0.0
                                    regresoItems[det.id] = q 
                                },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        val sucId = selectedSucursal?.id
                        if (sucId != null) {
                            val listToReturn = detalles.map { it.copy(cantidad_regreso = regresoItems[it.id] ?: 0.0) }
                            onConfirm(sucId, listToReturn)
                        }
                    }) {
                        Text("Confirmar Recepción")
                    }
                }
            }
        }
    }
}

@Composable
fun DetallePedidoDialog(
    pedido: PedidoMayoristaEntity,
    detalles: List<PedidoMayoristaDetalleEntity>,
    onDismiss: () -> Unit,
    onRegistrarAbono: (Long, String) -> Unit // amount, paymentMethod
) {
    var abonoAmount by remember { mutableStateOf("") }
    var selectedMetodo by remember { mutableStateOf("EFECTIVO") }
    val metodos = listOf("EFECTIVO", "TARJETA", "TRANSFERENCIA", "CRÉDITO")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(600.dp).fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Detalle de Pedido", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                        Text(pedido.folio, fontSize = 14.sp, color = NixtaTextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Info Section
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cliente", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NixtaTextSecondary)
                        Text(pedido.cliente_nombre, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fecha Entrega", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NixtaTextSecondary)
                        Text(pedido.fecha_entrega, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NixtaTextSecondary)
                        Text(pedido.estado, fontWeight = FontWeight.Bold, color = if(pedido.estado == "ENTREGADO") Color(0xFF4CAF50) else NixtaTerracottaPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Products Section
                Text("Productos Solicitados", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (detalles.isEmpty()) {
                        item { Text("Sin detalles registrados.", color = NixtaTextSecondary) }
                    } else {
                        items(detalles) { det ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${det.cantidad} x ${det.producto_nombre}")
                                Text("$${String.format(Locale.getDefault(), "%.2f", det.subtotal / 100.0)}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pedido", fontWeight = FontWeight.Bold)
                    Text("$${String.format(Locale.getDefault(), "%.2f", pedido.total / 100.0)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Saldo Pendiente", fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                    Text("$${String.format(Locale.getDefault(), "%.2f", pedido.saldo / 100.0)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NixtaTerracottaPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Registro de Abonos
                Text("Registro de Abonos", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = abonoAmount,
                        onValueChange = { abonoAmount = it },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(selectedMetodo)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            metodos.forEach { m ->
                                DropdownMenuItem(text = { Text(m) }, onClick = { selectedMetodo = m; expanded = false })
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val amount = abonoAmount.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                onRegistrarAbono((amount * 100).toLong(), selectedMetodo)
                                abonoAmount = ""
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Abonar")
                    }
                }
            }
        }
    }
}
