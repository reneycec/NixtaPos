package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.MovimientoCajaEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.ui.DenominationCount
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaBadgeRedBg
import com.example.ui.theme.NixtaBadgeRedText
import com.example.ui.theme.NixtaGradientStart
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaContainer
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArqueoControlDialog(
    isOpen: Boolean,
    sesionActiva: SesionCajaEntity?,
    denominations: DenominationCount,
    movimientos: List<MovimientoCajaEntity>,
    ventasEfectivoCentavos: Long,
    onDenominationChange: ((DenominationCount) -> DenominationCount) -> Unit,
    onRegistrarMovimiento: (tipo: String, montoCentavos: Long, concepto: String) -> Unit,
    onAbrirCaja: () -> Unit,
    onCerrarCaja: (montoCentavos: Long?, notas: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var activeTab by remember { mutableStateOf(0) } // 0: Conteo de Efectivo, 1: Entradas y Salidas, 2: Balance de Turno
    var movimientoTipo by remember { mutableStateOf("DEPOSITO") }
    var movimientoMonto by remember { mutableStateOf("") }
    var movimientoConcepto by remember { mutableStateOf("") }
    var notasTurno by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Summary calculations
    val fondoInicialPesos = (sesionActiva?.fondo_inicial ?: 0L) / 100.0
    val ventasEfectivoPesos = ventasEfectivoCentavos / 100.0

    val totalDepositosPesos = movimientos.filter { it.tipo == "DEPOSITO" }.sumOf { it.monto } / 100.0
    val totalRetirosPesos = movimientos.filter { it.tipo == "RETIRO" }.sumOf { it.monto } / 100.0

    val esperadoPesos = fondoInicialPesos + ventasEfectivoPesos + totalDepositosPesos - totalRetirosPesos
    val arqueoPesos = denominations.calculateTotalCents() / 100.0
    val diferenciaPesos = arqueoPesos - esperadoPesos

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
            modifier = Modifier
                .width(680.dp)
                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Bar
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NixtaTerracottaContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = NixtaTerracottaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Arqueo de Caja & Control de Efectivo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NixtaTextPrimary
                            )
                            Text(
                                text = if (sesionActiva != null) "Control de Turno Activo · ${sesionActiva.usuario_apertura_nombre}" else "Inicio de Turno · Apertura de Caja",
                                fontSize = 11.sp,
                                color = NixtaTextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (sesionActiva != null) NixtaBadgeGreenBg else NixtaBadgeRedBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (sesionActiva != null) "TURNO ABIERTO" else "TURNO CERRADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sesionActiva != null) NixtaBadgeGreenText else NixtaBadgeRedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs Navigation
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = NixtaTerracottaPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = NixtaTerracottaPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Conteo por Billetes/Monedas", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Entradas y Salidas (${movimientos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Resumen & Cierre de Turno", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activeTab) {
                    0 -> {
                        // TAB 0: CONTEO DE EFECTIVO POR DENOMINACION
                        Column {
                            CashDenominationGrid(
                                denominations = denominations,
                                onDenominationChange = onDenominationChange
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NixtaGradientStart)
                                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("TOTAL ARQUEADO / CONTADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                        Text("$${String.format("%.2f", arqueoPesos)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary)
                                    }

                                    if (sesionActiva != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("DIFERENCIA VS ESPERADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                            Text(
                                                text = "${if (diferenciaPesos >= 0) "+" else ""}$${String.format("%.2f", diferenciaPesos)}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (diferenciaPesos >= 0) NixtaBadgeGreenText else NixtaBadgeRedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: ENTRADAS Y SALIDAS DE EFECTIVO
                        Column {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Registrar Movimiento de Efectivo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { movimientoTipo = "DEPOSITO" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (movimientoTipo == "DEPOSITO") NixtaBadgeGreenText else NixtaBadgeGreenBg
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddCircle, null, tint = if (movimientoTipo == "DEPOSITO") Color.White else NixtaBadgeGreenText, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Entrada / Aportación", color = if (movimientoTipo == "DEPOSITO") Color.White else NixtaBadgeGreenText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { movimientoTipo = "RETIRO" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (movimientoTipo == "RETIRO") NixtaBadgeRedText else NixtaBadgeRedBg
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.RemoveCircle, null, tint = if (movimientoTipo == "RETIRO") Color.White else NixtaBadgeRedText, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Salida / Retiro / Pago", color = if (movimientoTipo == "RETIRO") Color.White else NixtaBadgeRedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = movimientoMonto,
                                            onValueChange = { movimientoMonto = it },
                                            placeholder = { Text("Monto ($)", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = movimientoConcepto,
                                            onValueChange = { movimientoConcepto = it },
                                            placeholder = { Text("Concepto (ej. Fondo cambio, Maíz, Caja chica)", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(2f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val mDouble = movimientoMonto.toDoubleOrNull() ?: 0.0
                                            if (mDouble > 0 && movimientoConcepto.isNotBlank()) {
                                                onRegistrarMovimiento(movimientoTipo, (mDouble * 100).toLong(), movimientoConcepto)
                                                movimientoMonto = ""
                                                movimientoConcepto = ""
                                            }
                                        },
                                        enabled = sesionActiva != null,
                                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Guardar Entrada / Salida", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Historial de Entradas/Salidas en este Turno", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                            ) {
                                if (movimientos.isEmpty()) {
                                    item {
                                        Text("Sin entradas ni salidas registradas aún.", fontSize = 12.sp, color = NixtaTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                                    }
                                } else {
                                    items(movimientos) { mov ->
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text(mov.concepto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                                Text(dateFormat.format(Date(mov.fecha)), fontSize = 10.sp, color = NixtaTextSecondary)
                                            }
                                            Text(
                                                text = "${if (mov.tipo == "DEPOSITO") "+" else "-"}$${String.format("%.2f", mov.monto / 100.0)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (mov.tipo == "DEPOSITO") NixtaBadgeGreenText else NixtaBadgeRedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: BALANCE Y ACCION DE TURNO (INICIO / FIN)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Balance Summary Grid
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                StatCardSmall("Fondo Inicial", "$${String.format("%.2f", fondoInicialPesos)}", modifier = Modifier.weight(1f))
                                StatCardSmall("Ventas Efectivo", "$${String.format("%.2f", ventasEfectivoPesos)}", modifier = Modifier.weight(1f))
                                StatCardSmall("Entradas (+)", "$${String.format("%.2f", totalDepositosPesos)}", modifier = Modifier.weight(1f))
                                StatCardSmall("Salidas (-)", "$${String.format("%.2f", totalRetirosPesos)}", modifier = Modifier.weight(1f))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                StatCardSmall("Efectivo Esperado", "$${String.format("%.2f", esperadoPesos)}", highlight = true, modifier = Modifier.weight(1f))
                                StatCardSmall("Conteo Arqueo", "$${String.format("%.2f", arqueoPesos)}", highlight = true, modifier = Modifier.weight(1f))
                                StatCardSmall(
                                    "Diferencia",
                                    "$${String.format("%.2f", diferenciaPesos)}",
                                    diffColor = if (diferenciaPesos >= 0) NixtaBadgeGreenText else NixtaBadgeRedText,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = notasTurno,
                                onValueChange = { notasTurno = it },
                                placeholder = { Text("Observaciones del arqueo / turno (opcional)...", fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NixtaTerracottaPrimary,
                                    unfocusedBorderColor = NixtaSurfaceBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (sesionActiva == null) {
                                Button(
                                    onClick = {
                                        onAbrirCaja()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(Icons.Default.LockOpen, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Confirmar Arqueo e Iniciar Turno", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onCerrarCaja(denominations.calculateTotalCents(), notasTurno)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NixtaBadgeRedText),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Confirmar Arqueo y Finalizar Turno", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cerrar Ventana", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCardSmall(
    label: String,
    value: String,
    highlight: Boolean = false,
    diffColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) NixtaGradientStart else Color.White),
        modifier = modifier.border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = diffColor ?: if (highlight) NixtaTerracottaPrimary else NixtaTextPrimary
            )
        }
    }
}
