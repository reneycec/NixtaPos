package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MovimientoCajaEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.ui.DenominationCount
import com.example.ui.components.CashDenominationGrid
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaBadgeRedBg
import com.example.ui.theme.NixtaBadgeRedText
import com.example.ui.theme.NixtaGradientEnd
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
fun CajaSessionScreen(
    sesionActiva: SesionCajaEntity?,
    historialSesiones: List<SesionCajaEntity>,
    movimientos: List<MovimientoCajaEntity>,
    ventasEfectivo: Long,
    ventasOtros: Long,
    denominations: DenominationCount,
    aperturaNotas: String,
    onDenominationChange: ((DenominationCount) -> DenominationCount) -> Unit,
    onNotasChange: (String) -> Unit,
    onAbrirCaja: () -> Unit,
    onCerrarCaja: (Long?, String) -> Unit,
    onRegistrarMovimiento: (String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cierreMontoInput by remember { mutableStateOf("") }
    var cierreNotas by remember { mutableStateOf("") }
    var movimientoTipo by remember { mutableStateOf("DEPOSITO") }
    var movimientoMonto by remember { mutableStateOf("") }
    var movimientoConcepto by remember { mutableStateOf("") }
    var showMovimientoForm by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Top Banner Gradient Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NixtaGradientStart, NixtaGradientEnd)
                        )
                    )
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "POS · CAJA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTerracottaPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Apertura / Cierre de Caja",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NixtaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (sesionActiva == null)
                                "No hay sesión abierta en esta sucursal."
                            else
                                "Sesión abierta el ${dateFormat.format(Date(sesionActiva.fecha_apertura))}",
                            fontSize = 13.sp,
                            color = NixtaTextSecondary
                        )
                    }

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sesionActiva == null) Color.White.copy(alpha = 0.8f) else NixtaBadgeGreenBg)
                            .border(
                                1.dp,
                                if (sesionActiva == null) NixtaSurfaceBorder else NixtaBadgeGreenText,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sesionActiva == null) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (sesionActiva == null) NixtaTextPrimary else NixtaBadgeGreenText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sesionActiva == null) "CAJA CERRADA" else "CAJA ABIERTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sesionActiva == null) NixtaTextPrimary else NixtaBadgeGreenText
                            )
                        }
                    }
                }
            }
        }

        // IF CAJA IS CLOSED -> SHOW APERTURA FORM
        if (sesionActiva == null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NixtaTerracottaContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = NixtaTerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Apertura de caja",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NixtaTextPrimary
                                )
                                Text(
                                    text = "Captura el fondo inicial por denominación.",
                                    fontSize = 12.sp,
                                    color = NixtaTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Denominations grid
                        CashDenominationGrid(
                            denominations = denominations,
                            onDenominationChange = onDenominationChange
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Notes & Total Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = "NOTAS (OPCIONAL)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NixtaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = aperturaNotas,
                                    onValueChange = onNotasChange,
                                    placeholder = { Text("Observaciones del fondo inicial...", fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NixtaTerracottaPrimary,
                                        unfocusedBorderColor = NixtaSurfaceBorder
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                )
                            }

                            // Total card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NixtaGradientStart)
                                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "FONDO INICIAL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NixtaTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val totalPesos = denominations.calculateTotalCents() / 100.0
                                    Text(
                                        text = "$${String.format("%.2f", totalPesos)}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NixtaTerracottaPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onAbrirCaja,
                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Abrir caja",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // IF CAJA IS OPEN -> SHOW ACTIVE SESSION SUMMARY & CLOSING FORM
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 4 Stat Cards
                    val fondoInicialPesos = sesionActiva.fondo_inicial / 100.0
                    val ventasEfectivoPesos = ventasEfectivo / 100.0
                    val ventasOtrosPesos = ventasOtros / 100.0
                    val esperadoPesos = fondoInicialPesos + ventasEfectivoPesos

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            label = "FONDO INICIAL",
                            value = "$${String.format("%.2f", fondoInicialPesos)}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "VENTAS EFECTIVO",
                            value = "$${String.format("%.2f", ventasEfectivoPesos)}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "TARJETA + TRANSF.",
                            value = "$${String.format("%.2f", ventasOtrosPesos)}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "EFECTIVO ESPERADO",
                            value = "$${String.format("%.2f", esperadoPesos)}",
                            highlight = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row: Session Movements + Closing Form
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left: Movimientos de la sesión
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                            modifier = Modifier
                                .weight(1.3f)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "Movimientos de la sesión",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NixtaTextPrimary
                                        )
                                        Text(
                                            text = "${movimientos.size} registrados",
                                            fontSize = 11.sp,
                                            color = NixtaTextSecondary
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                movimientoTipo = "DEPOSITO"
                                                showMovimientoForm = !showMovimientoForm
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixtaBadgeGreenBg),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, null, tint = NixtaBadgeGreenText, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Depósito", color = NixtaBadgeGreenText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                movimientoTipo = "RETIRO"
                                                showMovimientoForm = !showMovimientoForm
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixtaBadgeRedBg),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircle, null, tint = NixtaBadgeRedText, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Retiro", color = NixtaBadgeRedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (showMovimientoForm) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NixtaGradientStart)
                                            .padding(12.dp)
                                    ) {
                                        Text("Registrar $movimientoTipo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                        Spacer(Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = movimientoMonto,
                                            onValueChange = { movimientoMonto = it },
                                            placeholder = { Text("Monto ($)", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = movimientoConcepto,
                                            onValueChange = { movimientoConcepto = it },
                                            placeholder = { Text("Concepto / Motivo", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val mDouble = movimientoMonto.toDoubleOrNull() ?: 0.0
                                                if (mDouble > 0) {
                                                    onRegistrarMovimiento(movimientoTipo, (mDouble * 100).toLong(), movimientoConcepto)
                                                    movimientoMonto = ""
                                                    movimientoConcepto = ""
                                                    showMovimientoForm = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Guardar Movimiento", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (movimientos.isEmpty()) {
                                    Text(
                                        text = "Sin movimientos aún.",
                                        fontSize = 12.sp,
                                        color = NixtaTextSecondary,
                                        modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    movimientos.forEach { mov ->
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
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

                        // Right: Cerrar caja form
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cerrar caja",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NixtaTextPrimary
                                )
                                Text(
                                    text = "Captura el arqueo final por denominación. Se calculará automáticamente la diferencia contra lo esperado.",
                                    fontSize = 11.sp,
                                    color = NixtaTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val inputParsed = cierreMontoInput.toDoubleOrNull()
                                val arqueoPesos = inputParsed ?: (denominations.calculateTotalCents() / 100.0)
                                val diferenciaPesos = arqueoPesos - esperadoPesos

                                OutlinedTextField(
                                    value = cierreMontoInput,
                                    onValueChange = { cierreMontoInput = it },
                                    label = { Text("Efectivo de Cierre ($)", fontSize = 12.sp) },
                                    placeholder = { Text("Ej. ${String.format("%.2f", esperadoPesos)}", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Esperado", fontSize = 12.sp, color = NixtaTextSecondary)
                                    Text("$${String.format("%.2f", esperadoPesos)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                                    Text("Arqueado (Final)", fontSize = 12.sp, color = NixtaTextSecondary)
                                    Text("$${String.format("%.2f", arqueoPesos)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                                    Text("Diferencia", fontSize = 12.sp, color = NixtaTextSecondary)
                                    Text(
                                        text = "${if (diferenciaPesos >= 0) "+" else ""}$${String.format("%.2f", diferenciaPesos)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (diferenciaPesos >= 0) NixtaBadgeGreenText else NixtaBadgeRedText
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = cierreNotas,
                                    onValueChange = { cierreNotas = it },
                                    placeholder = { Text("Notas de cierre...", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().height(60.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val montoCentavos = inputParsed?.let { (it * 100).toLong() }
                                        onCerrarCaja(montoCentavos, cierreNotas)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Iniciar cierre", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // HISTORIAL DE SESIONES TABLE
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NixtaTerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial de sesiones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NixtaGradientStart)
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text("PERTENECE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1.2f))
                        Text("APERTURA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1f))
                        Text("CIERRE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1f))
                        Text("PERSONA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1.2f))
                        Text("FONDO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                        Text("ARQUEADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                        Text("DIFERENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                        Text("NOTAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1.2f))
                        Text("ESTADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.8f))
                    }

                    historialSesiones.forEach { sesion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, NixtaSurfaceBorder)
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("EMPRESA QWERTY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                Text("SUCURSAL CENTRO", fontSize = 9.sp, color = NixtaTextSecondary)
                            }
                            Text(
                                dateFormat.format(Date(sesion.fecha_apertura)),
                                fontSize = 10.sp,
                                color = NixtaTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (sesion.fecha_cierre != null) dateFormat.format(Date(sesion.fecha_cierre)) else "-",
                                fontSize = 10.sp,
                                color = NixtaTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("ABRE:", fontSize = 8.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                Text(sesion.usuario_apertura_nombre, fontSize = 9.sp, color = NixtaTextPrimary)
                                if (sesion.usuario_cierre_nombre != null) {
                                    Text("CIERRA:", fontSize = 8.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                                    Text(sesion.usuario_cierre_nombre, fontSize = 9.sp, color = NixtaTextPrimary)
                                }
                            }
                            Text(
                                "$${String.format("%.2f", sesion.fondo_inicial / 100.0)}",
                                fontSize = 10.sp,
                                color = NixtaTextPrimary,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                if (sesion.monto_arqueo != null) "$${String.format("%.2f", sesion.monto_arqueo / 100.0)}" else "-",
                                fontSize = 10.sp,
                                color = NixtaTextPrimary,
                                modifier = Modifier.weight(0.9f)
                            )
                            val diff = (sesion.diferencia ?: 0L) / 100.0
                            Text(
                                if (sesion.diferencia != null) "$${String.format("%.2f", diff)}" else "-",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) NixtaBadgeGreenText else NixtaBadgeRedText,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                sesion.notas ?: "-",
                                fontSize = 9.sp,
                                color = NixtaTextSecondary,
                                modifier = Modifier.weight(1.2f)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (sesion.estado == "CERRADA") Color(0xFFF0EAE1) else NixtaBadgeGreenBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sesion.estado.lowercase().capitalize(Locale.getDefault()),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sesion.estado == "CERRADA") NixtaTextPrimary else NixtaBadgeGreenText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) NixtaGradientStart else NixtaSurfaceCream),
        modifier = modifier.border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NixtaTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (highlight) NixtaTerracottaPrimary else NixtaTextPrimary
            )
        }
    }
}
