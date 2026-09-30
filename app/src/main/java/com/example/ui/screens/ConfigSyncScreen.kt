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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SyncManager
import com.example.data.remote.SyncStatus
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaGradientStart
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConfigSyncScreen(
    syncManager: SyncManager,
    tenantId: String,
    sucursalId: String,
    terminalId: String,
    empresaNombre: String,
    sucursalNombre: String,
    isOnline: Boolean = true,
    lastAutoSyncTime: Long? = null,
    isDarkMode: Boolean = false,
    productosCount: Int = 0,
    clientesCount: Int = 0,
    onToggleDarkMode: () -> Unit = {},
    onTriggerSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val syncStatus by syncManager.syncState.collectAsState()
    val fcmToken by syncManager.fcmToken.collectAsState()
    val fcmRegistered by syncManager.fcmRegistered.collectAsState()
    val scope = rememberCoroutineScope()

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SISTEMA · CONFIGURACIÓN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NixtaTerracottaPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Terminal & Sincronización NIXTA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NixtaTextPrimary
                )
            }
        }

        // Live Network & Auto-Sync Service Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) NixtaBadgeGreenText else Color(0xFFC0392B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Servicio de Sincronización en Segundo Plano", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                                Text(
                                    text = if (isOnline) "Monitoreo en tiempo real: Conexión Activa" else "Dispositivo Offline: Las ventas se acumulan localmente",
                                    fontSize = 11.sp,
                                    color = NixtaTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isOnline) NixtaBadgeGreenBg else Color(0xFFFCE4E4))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnline) "ONLINE · AUTO-SYNC" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) NixtaBadgeGreenText else Color(0xFFC0392B)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (lastAutoSyncTime != null) {
                        Text(
                            text = "Última sincronización automática en segundo plano: ${timeFormatter.format(Date(lastAutoSyncTime))}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaBadgeGreenText
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        text = "Al detectar conexión a internet, las ventas y movimientos guardados offline se envían de forma transparente al servidor NIXTA ERP.",
                        fontSize = 12.sp,
                        color = NixtaTextSecondary
                    )
                }
            }
        }

        // Dark Theme Mode Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NixtaGradientStart)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = NixtaTerracottaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Modo Oscuro POS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                            Text("Ajusta la interfaz para entornos de poca luz o turnos nocturnos", fontSize = 11.sp, color = NixtaTextSecondary)
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NixtaTerracottaPrimary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }
        }

        // Active Sync Status Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, null, tint = NixtaTerracottaPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Estado de la Sincronización", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(NixtaBadgeGreenBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("OFFLINE-FIRST ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaBadgeGreenText)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    when (val status = syncStatus) {
                        is SyncStatus.Idle -> {
                            Text("Modo Autónomo: Datos resguardados localmente en Room (SQLite).", fontSize = 12.sp, color = NixtaTextSecondary)
                        }
                        is SyncStatus.Syncing -> {
                            Column {
                                Text(status.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary)
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { status.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = NixtaTerracottaPrimary,
                                    trackColor = NixtaGradientStart
                                )
                            }
                        }
                        is SyncStatus.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = NixtaBadgeGreenText, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(status.message, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaBadgeGreenText)
                            }
                        }
                        is SyncStatus.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF0F0))
                                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Sincronización Fallida · Intente De Nuevo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = status.message,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB71C1C),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Datos Descargados de Sucursal (BD Local)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Datos de la Sucursal en Base de Datos Local (SQLite/Room)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    Text("Resumen en tiempo real del catálogo e información descargada desde NIXTA ERP", fontSize = 11.sp, color = NixtaTextSecondary)
                    
                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("🛍️ Catálogo Productos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("$productosCount items", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("👥 Clientes Registrados", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("$clientesCount registros", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NixtaTerracottaPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Terminal Parameters Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Parámetros Multi-tenant de la Terminal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = tenantId, onValueChange = {}, label = { Text("Tenant ID") }, readOnly = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sucursalId, onValueChange = {}, label = { Text("Sucursal ID") }, readOnly = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = terminalId, onValueChange = {}, label = { Text("Terminal ID") }, readOnly = true, modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = empresaNombre, onValueChange = {}, label = { Text("Empresa") }, readOnly = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sucursalNombre, onValueChange = {}, label = { Text("Sucursal") }, readOnly = true, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // FCM Integration & Actions Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Integración FCM (Firebase Cloud Messaging)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fcmToken,
                        onValueChange = {},
                        label = { Text("Token FCM de la Tablet") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Acciones de Sincronización Bipolares (Local <-> Nube)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                onTriggerSync()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Forzar Sync Outbound", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    syncManager.handleFcmPushEvent("CATALOG_UPDATED")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261A13)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Simular Push FCM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
