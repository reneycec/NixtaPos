package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.*
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
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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

    var vehiculoDropdownExpanded by remember { mutableStateOf(false) }
    var termoDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(750.dp).fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Asignar Carga a: ${repartidor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Selectores de Vehículo y Termo
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("VEHÍCULO ASIGNADO PARA LA RUTA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Box {
                            OutlinedButton(
                                onClick = { vehiculoDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFAF6EE))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedVehiculo?.let { "${it.modelo} — Placa: ${it.placa} (${it.capacidad_kg} kg)" } ?: "-- Seleccione un vehículo --", color = NixtaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = vehiculoDropdownExpanded, onDismissRequest = { vehiculoDropdownExpanded = false }) {
                                vehiculos.forEach { veh ->
                                    DropdownMenuItem(
                                        text = { Text("${veh.modelo} — Placa: ${veh.placa} (${veh.capacidad_kg} kg)") },
                                        onClick = {
                                            selectedVehiculo = veh
                                            vehiculoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("SELEACCIONE EL TERMO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Box {
                            OutlinedButton(
                                onClick = { termoDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFAF6EE))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedTermo?.let { "${it.nombre} (${it.codigo})" } ?: "-- Seleccione un termo --", color = NixtaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = termoDropdownExpanded, onDismissRequest = { termoDropdownExpanded = false }) {
                                termos.forEach { ter ->
                                    DropdownMenuItem(
                                        text = { Text("${ter.nombre} (${ter.codigo})") },
                                        onClick = {
                                            selectedTermo = ter
                                            termoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("TOCA UN PRODUCTO PARA AGREGARLO AL TERMO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { prod ->
                        val currentQty = orderItems[prod.id] ?: 0.0
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (currentQty > 0) Color(0xFFFDF3E7) else Color.White),
                            border = BorderStroke(1.dp, if (currentQty > 0) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("$${String.format(Locale.getDefault(), "%.2f", prod.precio_con_impuestos / 100.0)}", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold)
                                }
                                Text("Stock: ${prod.stock}", fontSize = 10.sp, color = NixtaTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (currentQty == 0.0) {
                                    Text(
                                        text = "+ Agregar",
                                        color = NixtaTerracottaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable { orderItems = orderItems.toMutableMap().apply { put(prod.id, 1.0) } }
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("✓ Agregado (${currentQty.toInt()})", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("DETALLE DE PRODUCTOS EN TERMO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Surface(color = Color.White, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth().height(110.dp)) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(orderItems.entries.toList()) { entry ->
                            val prod = productos.find { it.id == entry.key }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${prod?.nombre} x ${entry.value}")
                                Text("$${String.format(Locale.getDefault(), "%.2f", (entry.value * (prod?.precio_con_impuestos ?: 0L)) / 100.0)}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val vehId = selectedVehiculo?.id
                            val termoId = selectedTermo?.id
                            if (vehId != null && termoId != null && orderItems.isNotEmpty()) {
                                val detalles = orderItems.map { (prodId, qty) ->
                                    val prod = productos.find { it.id == prodId }
                                    AsignacionTermoDetalleEntity(
                                        asignacion_id = "",
                                        producto_id = prodId,
                                        cantidad_salida = qty,
                                        precio_unitario = prod?.precio_con_impuestos ?: 0L,
                                        tenant_id = ""
                                    )
                                }
                                onConfirm(vehId, termoId, detalles)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary)
                    ) {
                        Text("Confirmar Asignación", color = Color.White)
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
    onNewPedido: (PedidoMayoristaEntity) -> Unit
) {
    var selectedVehiculo by remember { mutableStateOf<VehiculoEntity?>(vehiculos.firstOrNull()) }
    var selectedTab by remember { mutableStateOf(1) } // Default 1 (Nuevo Pedido)
    var vehiculoDropdownExpanded by remember { mutableStateOf(false) }

    // For Nuevo Pedido
    var clienteNombre by remember { mutableStateOf("") }
    var clienteTelefono by remember { mutableStateOf("") }
    var clienteDireccion by remember { mutableStateOf("") }
    
    val currentFormattedDate = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a. m.", Locale.getDefault()).format(Date()) }
    var fechaEntrega by remember { mutableStateOf(currentFormattedDate) }
    var orderItems by remember { mutableStateOf(mutableMapOf<String, Double>()) } // producto.id -> cantidad

    // For Pedido Registrado Preview
    var selectedPedidoForAssign by remember { mutableStateOf<PedidoMayoristaEntity?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(750.dp).fillMaxHeight(0.92f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Asignar Carga a: ${repartidor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown Vehículo
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("VEHÍCULO ASIGNADO PARA LA RUTA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedButton(
                                onClick = { vehiculoDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFAF6EE))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedVehiculo?.let { "${it.modelo} — Placa: ${it.placa} (${it.capacidad_kg} kg)" } ?: "-- Seleccione un vehículo --", color = NixtaTextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = vehiculoDropdownExpanded, onDismissRequest = { vehiculoDropdownExpanded = false }) {
                                vehiculos.forEach { veh ->
                                    DropdownMenuItem(
                                        text = { Text("${veh.modelo} — Placa: ${veh.placa} (${veh.capacidad_kg} kg)") },
                                        onClick = {
                                            selectedVehiculo = veh
                                            vehiculoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("SELECCIONE EL TIPO DE PEDIDO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 1) NixtaTerracottaPrimary else Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selectedTab == 1) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Nuevo Pedido", color = if (selectedTab == 1) Color.White else NixtaTextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedTab = 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 0) NixtaTerracottaPrimary else Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selectedTab == 0) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Pedido Registrado", color = if (selectedTab == 0) Color.White else NixtaTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB: PEDIDO REGISTRADO (Muestra los pendientes para hoy)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SELECCIONA LOS PEDIDOS PENDIENTES PARA HOY:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        val hoyStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val pedidosHoy = pedidosPendientes.filter { it.estado == "PENDIENTE" }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            if (pedidosHoy.isEmpty()) {
                                item { Text("No hay pedidos pendientes para entregar hoy.", color = NixtaTextSecondary, fontSize = 12.sp) }
                            }
                            items(pedidosHoy) { ped ->
                                val isSelected = selectedPedidoForAssign?.id == ped.id
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedPedidoForAssign = ped },
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFDF3E7) else Color.White),
                                    border = BorderStroke(1.dp, if (isSelected) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(ped.folio, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                                            Text("$${String.format(Locale.getDefault(), "%.2f", ped.total / 100.0)}", fontWeight = FontWeight.Black)
                                        }
                                        Text(ped.cliente_nombre, fontWeight = FontWeight.Bold)
                                        Text("Dirección: ${ped.direccion}", fontSize = 12.sp, color = NixtaTextSecondary)
                                    }
                                }
                            }
                        }

                        if (selectedPedidoForAssign != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("DETALLE DEL PEDIDO SELECCIONADO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                    Text("Folio: ${selectedPedidoForAssign!!.folio}", fontWeight = FontWeight.Bold)
                                    Text("Cliente: ${selectedPedidoForAssign!!.cliente_nombre}")
                                    Text("Dirección: ${selectedPedidoForAssign!!.direccion}")
                                    Text("Total General: $${String.format(Locale.getDefault(), "%.2f", selectedPedidoForAssign!!.total / 100.0)}", fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val vehId = selectedVehiculo?.id
                                val pedId = selectedPedidoForAssign?.id
                                if (vehId != null && pedId != null) {
                                    onAssignPedido(pedId, vehId)
                                }
                            },
                            enabled = selectedVehiculo != null && selectedPedidoForAssign != null,
                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Text("Confirmar Asignación", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // TAB: NUEVO PEDIDO
                    Column(modifier = Modifier.weight(1f)) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("DATOS DEL CLIENTE DE ENTREGA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = clienteNombre,
                                        onValueChange = { clienteNombre = it },
                                        label = { Text("Nombre del cliente") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = clienteTelefono,
                                        onValueChange = { clienteTelefono = it },
                                        label = { Text("Teléfono") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true
                                    )
                                }
                                OutlinedTextField(
                                    value = clienteDireccion,
                                    onValueChange = { clienteDireccion = it },
                                    label = { Text("Dirección / Lugar de entrega") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = fechaEntrega,
                                    onValueChange = { fechaEntrega = it },
                                    label = { Text("Fecha y hora programada de entrega") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("TOCA UN PRODUCTO PARA AGREGARLO AL NUEVO PEDIDO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(productos) { prod ->
                                val currentQty = orderItems[prod.id] ?: 0.0
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (currentQty > 0) Color(0xFFFDF3E7) else Color.White),
                                    border = BorderStroke(1.dp, if (currentQty > 0) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("$${String.format(Locale.getDefault(), "%.2f", prod.precio_con_impuestos / 100.0)}", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Text("Stock: ${prod.stock}", fontSize = 10.sp, color = NixtaTextSecondary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (currentQty == 0.0) {
                                            Text(
                                                text = "+ Agregar",
                                                color = NixtaTerracottaPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.clickable { orderItems = orderItems.toMutableMap().apply { put(prod.id, 1.0) } }
                                            )
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("✓ Agregado (${currentQty.toInt()})", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val totalAmount = orderItems.entries.sumOf { entry -> (entry.value * (productos.find { it.id == entry.key }?.precio_con_impuestos ?: 0L)).toLong() }

                        Button(
                            onClick = {
                                if (clienteNombre.isNotBlank() && orderItems.isNotEmpty()) {
                                    val newPedido = PedidoMayoristaEntity(
                                        id = UUID.randomUUID().toString(),
                                        folio = "P-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}",
                                        cliente_id = UUID.randomUUID().toString(),
                                        cliente_nombre = clienteNombre,
                                        direccion = clienteDireccion,
                                        contacto = clienteTelefono,
                                        fecha_entrega = fechaEntrega,
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
                            enabled = clienteNombre.isNotBlank() && orderItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Confirmar Asignación ($${String.format(Locale.getDefault(), "%.2f", totalAmount / 100.0)})", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
fun NuevoPedidoSinVehiculoDialog(
    productos: List<ProductoEntity>,
    onDismiss: () -> Unit,
    onConfirm: (PedidoMayoristaEntity) -> Unit
) {
    var clienteNombre by remember { mutableStateOf("") }
    var clienteTelefono by remember { mutableStateOf("") }
    var clienteDireccion by remember { mutableStateOf("") }
    val currentFormattedDate = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a. m.", Locale.getDefault()).format(Date()) }
    var fechaEntrega by remember { mutableStateOf(currentFormattedDate) }
    var orderItems by remember { mutableStateOf(mutableMapOf<String, Double>()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(750.dp).fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Registrar Nuevo Pedido (Agenda / Futuro)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DATOS DEL CLIENTE DE ENTREGA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = clienteNombre, onValueChange = { clienteNombre = it }, label = { Text("Nombre del cliente") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = clienteTelefono, onValueChange = { clienteTelefono = it }, label = { Text("Teléfono") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                        }
                        OutlinedTextField(value = clienteDireccion, onValueChange = { clienteDireccion = it }, label = { Text("Dirección / Lugar de entrega") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = fechaEntrega, onValueChange = { fechaEntrega = it }, label = { Text("Fecha y hora programada de entrega") }, leadingIcon = { Icon(Icons.Default.CalendarToday, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("TOCA UN PRODUCTO PARA AGREGARLO AL PEDIDO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { prod ->
                        val currentQty = orderItems[prod.id] ?: 0.0
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (currentQty > 0) Color(0xFFFDF3E7) else Color.White),
                            border = BorderStroke(1.dp, if (currentQty > 0) NixtaTerracottaPrimary else NixtaSurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("$${String.format(Locale.getDefault(), "%.2f", prod.precio_con_impuestos / 100.0)}", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold)
                                }
                                Text("Stock: ${prod.stock}", fontSize = 10.sp, color = NixtaTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (currentQty == 0.0) {
                                    Text(
                                        text = "+ Agregar",
                                        color = NixtaTerracottaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable { orderItems = orderItems.toMutableMap().apply { put(prod.id, 1.0) } }
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("✓ Agregado (${currentQty.toInt()})", color = NixtaTerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                val totalAmount = orderItems.entries.sumOf { entry -> (entry.value * (productos.find { it.id == entry.key }?.precio_con_impuestos ?: 0L)).toLong() }

                Button(
                    onClick = {
                        if (clienteNombre.isNotBlank() && orderItems.isNotEmpty()) {
                            val newPedido = PedidoMayoristaEntity(
                                id = UUID.randomUUID().toString(),
                                folio = "P-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}",
                                cliente_id = UUID.randomUUID().toString(),
                                cliente_nombre = clienteNombre,
                                direccion = clienteDireccion,
                                contacto = clienteTelefono,
                                fecha_entrega = fechaEntrega,
                                estado = "PENDIENTE", // Pedido futuro sin asignar
                                total = totalAmount,
                                saldo = totalAmount,
                                usuario_id = "",
                                tenant_id = "",
                                sucursal_id = ""
                            )
                            onConfirm(newPedido)
                        }
                    },
                    enabled = clienteNombre.isNotBlank() && orderItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Guardar Pedido Pendiente ($${String.format(Locale.getDefault(), "%.2f", totalAmount / 100.0)})", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
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
    var sucursalDropdownExpanded by remember { mutableStateOf(false) }
    
    // Regreso de productos (id -> cantidad devuelta)
    var regresoItems by remember { mutableStateOf(detalles.associate { it.id to 0.0 }.toMutableMap()) }

    // Cálculo en vivo de lo vendido e importe a entregar
    val totalCalculadoAEntregarCentavos = detalles.sumOf { det ->
        val regreso = regresoItems[det.id] ?: 0.0
        val vendida = (det.cantidad_salida - regreso).coerceAtLeast(0.0)
        (vendida * det.precio_unitario).toLong()
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(650.dp).fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recibir Termo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de sucursal
                Text("SUCURSAL DE RECEPCIÓN:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { sucursalDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFAF6EE))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedSucursal?.nombre ?: "-- Seleccione una sucursal --", color = NixtaTextPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = sucursalDropdownExpanded, onDismissRequest = { sucursalDropdownExpanded = false }) {
                        sucursales.forEach { suc ->
                            DropdownMenuItem(
                                text = { Text(suc.nombre) },
                                onClick = {
                                    selectedSucursal = suc
                                    sucursalDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("DETALLE DE PRODUCTOS Y LIQUIDACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PRODUCTO", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                    Text("SALIDA", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, textAlign = TextAlign.Center)
                    Text("REGRESO", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, textAlign = TextAlign.Center)
                    Text("PRECIO UNIT.", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, textAlign = TextAlign.End)
                    Text("TOTAL VENTA", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, textAlign = TextAlign.End)
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (detalles.isEmpty()) {
                        item { Text("Sin productos registrados en este termo.", modifier = Modifier.padding(top = 16.dp), color = NixtaTextSecondary) }
                    }
                    items(detalles) { det ->
                        val prodNombre = productos.find { it.id == det.producto_id }?.nombre ?: "Producto"
                        val regVal = regresoItems[det.id] ?: 0.0
                        val cantidadVendida = (det.cantidad_salida - regVal).coerceAtLeast(0.0)
                        val totalVentaProd = (cantidadVendida * det.precio_unitario) / 100.0

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, NixtaSurfaceBorder),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prodNombre, modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${det.cantidad_salida}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
                                
                                OutlinedTextField(
                                    value = if (regVal == 0.0) "0" else regVal.toString(),
                                    onValueChange = { 
                                        val valNum = it.toDoubleOrNull() ?: 0.0
                                        regresoItems = regresoItems.toMutableMap().apply { put(det.id, valNum) }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Text("$${String.format(Locale.getDefault(), "%.2f", det.precio_unitario / 100.0)}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 13.sp)
                                Text("$${String.format(Locale.getDefault(), "%.2f", totalVentaProd)}", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card de Total Calculado a Entregar
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF3E7)), border = BorderStroke(1.dp, NixtaTerracottaPrimary), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL CALCULADO A ENTREGAR:", fontWeight = FontWeight.Bold, color = NixtaTextPrimary, fontSize = 14.sp)
                        Text("$${String.format(Locale.getDefault(), "%.2f", totalCalculadoAEntregarCentavos / 100.0)}", fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sucId = selectedSucursal?.id
                            if (sucId != null) {
                                val listToReturn = detalles.map { det ->
                                    det.copy(cantidad_regreso = regresoItems[det.id] ?: 0.0)
                                }
                                onConfirm(sucId, listToReturn)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary)
                    ) {
                        Text("Confirmar Recepción", color = Color.White, fontWeight = FontWeight.Bold)
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
    var abonoInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Efectivo") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(16.dp), color = NixtaSurfaceCream, modifier = Modifier.width(650.dp).fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pedido ${pedido.folio}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, NixtaSurfaceBorder), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CLIENTE", fontSize = 11.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                Text(pedido.cliente_nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (pedido.contacto.isNotBlank()) Text("Contacto: ${pedido.contacto}", fontSize = 12.sp, color = NixtaTextSecondary)
                                if (pedido.direccion.isNotBlank()) Text("Dirección: ${pedido.direccion}", fontSize = 12.sp, color = NixtaTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ESTADO", fontSize = 11.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                                    Text(pedido.estado, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = NixtaSurfaceBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("TOTAL DEL PEDIDO", fontSize = 11.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.getDefault(), "%.2f", pedido.total / 100.0)}", fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                            }
                            Column {
                                Text("SALDO PENDIENTE", fontSize = 11.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.getDefault(), "%.2f", pedido.saldo / 100.0)}", fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Detalle de Productos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(detalles) { det ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${det.cantidad}x ${det.producto_nombre}")
                            Text("$${String.format(Locale.getDefault(), "%.2f", det.subtotal / 100.0)}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Registro de Abonos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = abonoInput,
                        onValueChange = { abonoInput = it },
                        label = { Text("Monto") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val amountCents = ((abonoInput.toDoubleOrNull() ?: 0.0) * 100).toLong()
                            if (amountCents > 0) {
                                onRegistrarAbono(amountCents, selectedMethod)
                                abonoInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary)
                    ) {
                        Text("Abonar", color = Color.White)
                    }
                }
            }
        }
    }
}
