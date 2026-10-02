package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EntregasMayoristasScreen(
    repartidores: List<VendedorEntity>,
    vehiculos: List<VehiculoEntity>,
    termos: List<TermoEntity>,
    asignaciones: List<AsignacionTermoEntity>,
    pedidos: List<PedidoMayoristaEntity>,
    productos: List<ProductoEntity> = emptyList(),
    sucursales: List<SucursalEntity> = emptyList(),
    currentSucursal: SucursalEntity? = null,
    onAsignarTermo: (String, String, String, List<AsignacionTermoDetalleEntity>) -> Unit,
    onRecibirTermo: (AsignacionTermoEntity, List<AsignacionTermoDetalleEntity>) -> Unit,
    onAsignarPedido: (String, String) -> Unit,
    onNewPedidoFuturo: (PedidoMayoristaEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repartidoresFlotilla = repartidores.filter { it.rol.equals("REPARTIDOR", ignoreCase = true) }

    var selectedRepartidorForCarga by remember { mutableStateOf<VendedorEntity?>(null) }
    var actionForRepartidor by remember { mutableStateOf<VendedorEntity?>(null) }
    var showAsignarTermoDialog by remember { mutableStateOf(false) }
    var showAsignarPedidosDialog by remember { mutableStateOf(false) }
    var showNuevoPedidoSinVehiculoDialog by remember { mutableStateOf(false) }
    
    var receivingAsignacion by remember { mutableStateOf<AsignacionTermoEntity?>(null) }
    var selectedPedidoForDetail by remember { mutableStateOf<PedidoMayoristaEntity?>(null) }

    var expandedRepartidores by remember { mutableStateOf(setOf<String>()) }
    var viewMode by remember { mutableStateOf("Despacho De Hoy") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "POS · ENTREGAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NixtaTerracottaPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Mesa de control de rutas",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NixtaTextPrimary
                    )
                }
                
                Row(
                    modifier = Modifier.background(Color(0xFFE0E0E0), RoundedCornerShape(24.dp)).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isDespacho = viewMode == "Despacho De Hoy"
                    Box(
                        modifier = Modifier
                            .background(if (isDespacho) Color.White else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { viewMode = "Despacho De Hoy" }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Despacho De Hoy", fontWeight = if (isDespacho) FontWeight.Bold else FontWeight.Normal, color = if (isDespacho) NixtaTextPrimary else NixtaTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (!isDespacho) Color.White else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { viewMode = "Agenda De Pedidos" }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Agenda De Pedidos", fontWeight = if (!isDespacho) FontWeight.Bold else FontWeight.Normal, color = if (!isDespacho) NixtaTextPrimary else NixtaTextSecondary)
                    }
                }
            }
        }

        if (viewMode == "Despacho De Hoy") {
            item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Flotilla disponible", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    if (repartidoresFlotilla.isEmpty()) {
                        Text("No hay repartidores registrados.", color = NixtaTextSecondary, fontSize = 14.sp)
                    }

                    repartidoresFlotilla.forEach { repartidor ->
                        val isExpanded = expandedRepartidores.contains(repartidor.id)
                        val activeAsignaciones = asignaciones.filter { it.repartidor_id == repartidor.id && it.estado != "RECIBIDO" }
                        val activePedidos = pedidos.filter { it.usuario_id == repartidor.id && it.estado != "ENTREGADO" }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedRepartidores = if (isExpanded) expandedRepartidores - repartidor.id else expandedRepartidores + repartidor.id
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expandir",
                                        tint = NixtaTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NixtaTerracottaPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = repartidor.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NixtaTextPrimary
                                    )
                                }
                                
                                Button(
                                    onClick = { selectedRepartidorForCarga = repartidor },
                                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Asignar Carga", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    HorizontalDivider(color = NixtaSurfaceBorder)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text("TERMOS ASIGNADOS SIN RECIBIR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (activeAsignaciones.isEmpty()) {
                                        Text("Sin termos activos", fontSize = 12.sp, color = NixtaTextSecondary)
                                    } else {
                                        activeAsignaciones.forEach { asig ->
                                            val termoName = termos.find { it.id == asig.termo_id }?.nombre ?: "Desconocido"
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(NixtaSurfaceCream, RoundedCornerShape(6.dp))
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(termoName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Fecha: ${formatDate(asig.fecha_asignacion)}", fontSize = 12.sp, color = NixtaTextSecondary)
                                                }
                                                Button(
                                                    onClick = { receivingAsignacion = asig },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                                ) {
                                                    Text("Recibir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text("PEDIDOS A ENTREGAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (activePedidos.isEmpty()) {
                                        Text("Sin pedidos activos", fontSize = 12.sp, color = NixtaTextSecondary)
                                    } else {
                                        activePedidos.forEach { ped ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(Color(0xFFF9F9F9), RoundedCornerShape(6.dp))
                                                    .border(0.5.dp, NixtaSurfaceBorder, RoundedCornerShape(6.dp))
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(ped.folio, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary, fontSize = 12.sp)
                                                    Text(ped.cliente_nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Text(ped.direccion, fontSize = 12.sp, color = NixtaTextSecondary)
                                                }
                                                Text(
                                                    "$${String.format("%.2f", ped.total / 100.0)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        } // Close if (viewMode == "Despacho De Hoy")
        if (viewMode == "Agenda De Pedidos") {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Pedidos", color = NixtaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${pedidos.size}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("En Proceso", color = NixtaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${pedidos.count { it.estado == "EN PROCESO" }}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Valor Total", color = NixtaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format(Locale.getDefault(), "%.2f", pedidos.sumOf { it.total } / 100.0)}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Por Cobrar", color = NixtaTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format(Locale.getDefault(), "%.2f", pedidos.sumOf { it.saldo } / 100.0)}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                    modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Listado de Pedidos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                            Button(
                                onClick = { showNuevoPedidoSinVehiculoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Nuevo Pedido", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Folio", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Cliente", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Estado", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Saldo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        pedidos.forEach { ped ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { selectedPedidoForDetail = ped }.padding(8.dp).border(0.5.dp, NixtaSurfaceBorder).background(Color.White).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ped.folio, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                Text(ped.cliente_nombre, modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(ped.estado, modifier = Modifier.weight(1f), fontSize = 12.sp, color = if(ped.estado=="ENTREGADO") Color(0xFF4CAF50) else NixtaTerracottaPrimary)
                                Text("$${String.format(Locale.getDefault(), "%.2f", ped.total / 100.0)}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$${String.format(Locale.getDefault(), "%.2f", ped.saldo / 100.0)}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NixtaTerracottaPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedRepartidorForCarga?.let { repartidor ->
        AsignarCargaDialog(
            repartidor = repartidor,
            onDismiss = { selectedRepartidorForCarga = null },
            onSelectAsignarPedidos = {
                actionForRepartidor = repartidor
                showAsignarPedidosDialog = true
                selectedRepartidorForCarga = null
            },
            onSelectAsignarTermo = {
                actionForRepartidor = repartidor
                showAsignarTermoDialog = true
                selectedRepartidorForCarga = null
            }
        )
    }
    
    if (showAsignarTermoDialog && actionForRepartidor != null) {
        AsignarTermoDialog(
            repartidor = actionForRepartidor!!,
            vehiculos = vehiculos,
            termos = termos,
            productos = productos,
            onDismiss = { showAsignarTermoDialog = false },
            onConfirm = { vehId, termoId, det -> 
                onAsignarTermo(actionForRepartidor!!.id, vehId, termoId, det)
                showAsignarTermoDialog = false
            }
        )
    }

    if (showAsignarPedidosDialog && actionForRepartidor != null) {
        AsignarPedidosDialog(
            repartidor = actionForRepartidor!!,
            vehiculos = vehiculos,
            pedidosPendientes = pedidos.filter { it.estado == "EN PROCESO" || it.estado == "PENDIENTE" },
            productos = productos,
            onDismiss = { showAsignarPedidosDialog = false },
            onAssignPedido = { pedidoId, vehId ->
                onAsignarPedido(pedidoId, actionForRepartidor!!.id)
                showAsignarPedidosDialog = false
            },
            onNewPedido = { newPedido ->
                onNewPedidoFuturo(newPedido)
                showAsignarPedidosDialog = false
            }
        )
    }

    if (showNuevoPedidoSinVehiculoDialog) {
        NuevoPedidoSinVehiculoDialog(
            productos = productos,
            onDismiss = { showNuevoPedidoSinVehiculoDialog = false },
            onConfirm = { newPedido ->
                onNewPedidoFuturo(newPedido)
                showNuevoPedidoSinVehiculoDialog = false
            }
        )
    }

    receivingAsignacion?.let { asig ->
        RecibirTermoDialog(
            asignacion = asig,
            detalles = emptyList<AsignacionTermoDetalleEntity>(), 
            sucursales = sucursales.ifEmpty { currentSucursal?.let { listOf(it) } ?: emptyList() },
            productos = productos,
            onDismiss = { receivingAsignacion = null },
            onConfirm = { sucId, detRecibidos ->
                onRecibirTermo(asig.copy(sucursal_recepcion_id = sucId), detRecibidos)
                receivingAsignacion = null
            }
        )
    }

    selectedPedidoForDetail?.let { pedido ->
        DetallePedidoDialog(
            pedido = pedido,
            detalles = emptyList(), // In a real scenario we'd query details
            onDismiss = { selectedPedidoForDetail = null },
            onRegistrarAbono = { amount, method ->
                // This would go to ViewModel, simple dismiss for now
                selectedPedidoForDetail = null
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}