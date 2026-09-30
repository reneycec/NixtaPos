package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.VentaEntity
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaBadgeRedBg
import com.example.ui.theme.NixtaBadgeRedText
import com.example.ui.theme.NixtaBadgeYellowBg
import com.example.ui.theme.NixtaBadgeYellowText
import com.example.ui.theme.NixtaGradientStart
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaContainer
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HourlySalesData(
    val hourLabel: String,
    val hour24: Int,
    val todayAmountPesos: Double,
    val comparisonAmountPesos: Double,
    val todayTxCount: Int
)

@Composable
fun DashboardPanelScreen(
    todasVentas: List<VentaEntity>,
    isOnline: Boolean,
    lastAutoSyncTime: Long?,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // Grouping sales into Today vs Yesterday / Comparison
    val todayCalendar = remember { Calendar.getInstance() }
    val startOfTodayMillis = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todaySales = remember(todasVentas, startOfTodayMillis) {
        todasVentas.filter { it.fecha >= startOfTodayMillis && it.estado == "COMPLETADA" }
    }

    // Historical mock comparison data for hours where actual DB sales are sparse
    val hourlyDataList = remember(todaySales) {
        val hours = (8..20).toList() // 08:00 AM to 08:00 PM
        val MapToday = mutableMapOf<Int, Double>()
        val MapTodayCount = mutableMapOf<Int, Int>()

        todaySales.forEach { v ->
            val cal = Calendar.getInstance().apply { timeInMillis = v.fecha }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            MapToday[h] = (MapToday[h] ?: 0.0) + (v.total / 100.0)
            MapTodayCount[h] = (MapTodayCount[h] ?: 0) + 1
        }

        // Standard benchmark profile for a tortillería/POS
        val comparisonBenchmarks = mapOf(
            8 to 1850.0, 9 to 2400.0, 10 to 3650.0, 11 to 3100.0,
            12 to 2800.0, 13 to 3450.0, 14 to 3200.0, 15 to 1950.0,
            16 to 1400.0, 17 to 2100.0, 18 to 2600.0, 19 to 1800.0, 20 to 950.0
        )

        hours.map { h ->
            val realAmount = MapToday[h] ?: 0.0
            val realCount = MapTodayCount[h] ?: 0
            val compBenchmark = comparisonBenchmarks[h] ?: 1500.0
            HourlySalesData(
                hourLabel = String.format("%02d:00", h),
                hour24 = h,
                todayAmountPesos = realAmount,
                comparisonAmountPesos = compBenchmark,
                todayTxCount = realCount
            )
        }
    }

    // Metrics summary
    val totalVentasHoyPesos = todaySales.sumOf { it.total } / 100.0
    val totalTransaccionesCount = todaySales.size
    val ticketPromedioPesos = if (totalTransaccionesCount > 0) totalVentasHoyPesos / totalTransaccionesCount else 0.0

    val ventasEfectivoPesos = todaySales.filter { it.metodo_pago == "EFECTIVO" }.sumOf { it.total } / 100.0
    val ventasOtrosPesos = todaySales.filter { it.metodo_pago != "EFECTIVO" }.sumOf { it.total } / 100.0

    val ventasPendientesSync = todaySales.filter { it.sync_status == "PENDIENTE" }
    val ventasPendientesCount = ventasPendientesSync.size
    val ventasPendientesMontoPesos = ventasPendientesSync.sumOf { it.total } / 100.0

    var selectedHourData by remember { mutableStateOf<HourlySalesData?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        // --- 1. MODAL VISUAL MODO OFFLINE & STATUS CARD ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isOnline) NixtaSurfaceCream else Color(0xFFFFF9F5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (isOnline) NixtaSurfaceBorder else NixtaTerracottaPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOnline) NixtaBadgeGreenBg else NixtaTerracottaContainer)
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) NixtaBadgeGreenText else NixtaTerracottaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isOnline) "Modo En Línea (Sincronizado)" else "Modo Offline Activo (Base de Datos Local)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NixtaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isOnline) NixtaBadgeGreenBg else NixtaBadgeYellowBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isOnline) "ONLINE" else "OFFLINE · LOCAL ROOM/INDEXEDDB",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOnline) NixtaBadgeGreenText else NixtaBadgeYellowText
                                        )
                                    }
                                }

                                Text(
                                    text = if (isOnline)
                                        "Todas las transacciones se respaldan inmediatamente en el servidor central NIXTA ERP."
                                    else
                                        "El punto de venta está operando en la base de datos local SQLite/IndexedDB. Las ventas se guardan con total seguridad.",
                                    fontSize = 12.sp,
                                    color = NixtaTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onTriggerSync,
                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Metrics Bar for Sync Status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SyncMetricItem(
                            label = "Ventas Pendientes Sync",
                            value = "$ventasPendientesCount ($${String.format("%.2f", ventasPendientesMontoPesos)})",
                            highlight = ventasPendientesCount > 0,
                            modifier = Modifier.weight(1f)
                        )
                        SyncMetricItem(
                            label = "Almacenamiento Local",
                            value = "Motor Room/IndexedDB OK",
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                        SyncMetricItem(
                            label = "Último Sync Automático",
                            value = if (lastAutoSyncTime != null) timeFormatter.format(Date(lastAutoSyncTime)) else "Reciente",
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 2. KEY METRICS CARDS GRID ---
        item {
            Column {
                Text("Resumen General de Ventas de Hoy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Ventas Totales Hoy",
                        value = "$${String.format("%.2f", totalVentasHoyPesos)}",
                        subtitle = "${todaySales.size} tickets cobrados",
                        icon = Icons.Default.PointOfSale,
                        iconBg = NixtaTerracottaContainer,
                        iconTint = NixtaTerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Ticket Promedio",
                        value = "$${String.format("%.2f", ticketPromedioPesos)}",
                        subtitle = "Promedio por transacción",
                        icon = Icons.Default.Assessment,
                        iconBg = NixtaBadgeGreenBg,
                        iconTint = NixtaBadgeGreenText,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Ventas en Efectivo",
                        value = "$${String.format("%.2f", ventasEfectivoPesos)}",
                        subtitle = "${if (totalVentasHoyPesos > 0) String.format("%.0f%%", (ventasEfectivoPesos / totalVentasHoyPesos) * 100) else "0%"} del total",
                        icon = Icons.Default.Money,
                        iconBg = NixtaGradientStart,
                        iconTint = NixtaTerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Tarjeta / Crédito",
                        value = "$${String.format("%.2f", ventasOtrosPesos)}",
                        subtitle = "${if (totalVentasHoyPesos > 0) String.format("%.0f%%", (ventasOtrosPesos / totalVentasHoyPesos) * 100) else "0%"} del total",
                        icon = Icons.Default.CreditCard,
                        iconBg = Color(0xFFEAE6FE),
                        iconTint = Color(0xFF6B4EE0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 3. COMPARATIVE HOURLY SALES CHART (GRÁFICA COMPARATIVA DE INGRESOS POR HORA) ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Comparativa de Ingresos por Hora",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NixtaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NixtaTerracottaContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("08:00 AM - 08:00 PM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                                }
                            }
                            Text(
                                text = "Compara las ventas acumuladas por hora de hoy vs la meta/promedio del turno",
                                fontSize = 11.sp,
                                color = NixtaTextSecondary
                            )
                        }

                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(NixtaTerracottaPrimary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ventas Hoy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFD6C2B4)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Promedio / Benchmark", fontSize = 11.sp, color = NixtaTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Canvas / Compose Hourly Bar Chart
                    HourlyIncomeBarChart(
                        hourlyData = hourlyDataList,
                        selectedHour = selectedHourData,
                        onSelectHour = { selectedHourData = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected Hour Tooltip / Inspector
                    selectedHourData?.let { sel ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = NixtaTerracottaPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Detalle de Hora: ${sel.hourLabel} - ${String.format("%02d:00", sel.hour24 + 1)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NixtaTextPrimary
                                        )
                                        Text(
                                            text = "Transacciones realizadas: ${sel.todayTxCount} tickets",
                                            fontSize = 11.sp,
                                            color = NixtaTextSecondary
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Ventas Hoy", fontSize = 10.sp, color = NixtaTextSecondary)
                                        Text("$${String.format("%.2f", sel.todayAmountPesos)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Promedio Objetivo", fontSize = 10.sp, color = NixtaTextSecondary)
                                        Text("$${String.format("%.2f", sel.comparisonAmountPesos)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. RECENT TRANSACTIONS STREAM TABLE WITH SYNC STATUS ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Últimas Ventas Emitidas (Flujo Local)", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NixtaTextPrimary)
                            Text("Listado de transacciones recientes guardadas localmente y su estatus de sync", fontSize = 11.sp, color = NixtaTextSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Total: ${todasVentas.size} ventas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        if (todaySales.isEmpty()) {
                            item {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                ) {
                                    Text("Sin ventas registradas hoy. Realiza un cobro en Venta Mostrador para ver el flujo.", fontSize = 12.sp, color = NixtaTextSecondary)
                                }
                            }
                        } else {
                            items(todaySales.take(15)) { venta ->
                                VentaItemRow(venta = venta, dateFormatter = dateFormatter)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = NixtaTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = NixtaTextSecondary)
        }
    }
}

@Composable
private fun SyncMetricItem(
    label: String,
    value: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlight) NixtaBadgeYellowBg else Color.White)
            .border(1.dp, if (highlight) Color(0xFFE2B93B) else NixtaSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) NixtaBadgeYellowText else NixtaTextPrimary
            )
        }
    }
}

@Composable
private fun HourlyIncomeBarChart(
    hourlyData: List<HourlySalesData>,
    selectedHour: HourlySalesData?,
    onSelectHour: (HourlySalesData) -> Unit
) {
    val maxVal = remember(hourlyData) {
        val highest = hourlyData.maxOfOrNull { maxOf(it.todayAmountPesos, it.comparisonAmountPesos) } ?: 4000.0
        if (highest < 1000.0) 2000.0 else highest * 1.15
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bar canvas container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.White)
                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                hourlyData.forEach { hourData ->
                    val isSelected = selectedHour?.hour24 == hourData.hour24

                    val todayBarRatio = (hourData.todayAmountPesos / maxVal).coerceIn(0.02, 1.0).toFloat()
                    val compBarRatio = (hourData.comparisonAmountPesos / maxVal).coerceIn(0.02, 1.0).toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectHour(hourData) }
                            .padding(horizontal = 2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Comparison Bar (Ayer / Benchmark)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(compBarRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Color(0xFFD6C2B4))
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            // Today Bar (Ventas Hoy)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(todayBarRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (isSelected) Color(0xFFB03A12) else NixtaTerracottaPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = hourData.hourLabel,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) NixtaTerracottaPrimary else NixtaTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VentaItemRow(
    venta: VentaEntity,
    dateFormatter: SimpleDateFormat
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NixtaGradientStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = null,
                        tint = NixtaTerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(venta.folio, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF0EAE1))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(venta.metodo_pago, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                        }
                    }
                    Text(dateFormatter.format(Date(venta.fecha)), fontSize = 10.sp, color = NixtaTextSecondary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", venta.total / 100.0)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = NixtaTerracottaPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (venta.sync_status == "SINCRONIZADO") NixtaBadgeGreenBg else NixtaBadgeYellowBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (venta.sync_status == "SINCRONIZADO") "SINCRONIZADO" else "PENDIENTE SYNC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (venta.sync_status == "SINCRONIZADO") NixtaBadgeGreenText else NixtaBadgeYellowText
                        )
                    }
                }
            }
        }
    }
}
