package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UsuarioEntity
import com.example.ui.NavDestination
import com.example.ui.theme.NixtaSidebarBackground
import com.example.ui.theme.NixtaSidebarCardBg
import com.example.ui.theme.NixtaSidebarSelected
import com.example.ui.theme.NixtaSidebarTextUnselected
import com.example.ui.theme.NixtaTerracottaPrimary

@Composable
fun NixtaSidebar(
    currentDestination: NavDestination,
    currentUsuario: UsuarioEntity?,
    empresaNombre: String,
    sucursalNombre: String,
    onNavigate: (NavDestination) -> Unit,
    onLockClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onExitAppClick: () -> Unit = {},
    onToggleCollapse: (() -> Unit)? = null,
    cartItemsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var posExpanded by remember { mutableStateOf(true) }
    var inventarioExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(NixtaSidebarBackground)
            .padding(16.dp)
    ) {
        // Logo NIXTA ERP + Hamburger Menu Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NixtaTerracottaPrimary)
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NIXTA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ERP",
                        color = NixtaSidebarTextUnselected,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }

            if (onToggleCollapse != null) {
                IconButton(onClick = onToggleCollapse) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Ocultar Menú",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Branch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NixtaSidebarCardBg)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = null,
                    tint = NixtaSidebarTextUnselected,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = sucursalNombre.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = empresaNombre.uppercase(),
                        color = NixtaSidebarTextUnselected,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Scrollable Menu
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SidebarNavItem(
                label = "Panel de Control",
                icon = Icons.Default.Assessment,
                selected = currentDestination == NavDestination.DASHBOARD_PANEL,
                onClick = { onNavigate(NavDestination.DASHBOARD_PANEL) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category: Punto de Venta
            SidebarCategoryHeader(
                title = "Punto de Venta",
                icon = Icons.Default.ShoppingCart,
                expanded = posExpanded,
                onToggle = { posExpanded = !posExpanded }
            )

            AnimatedVisibility(visible = posExpanded) {
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    SidebarNavItem(
                        label = "Apertura / Cierre de Caja",
                        icon = Icons.Default.ReceiptLong,
                        selected = currentDestination == NavDestination.CAJA_SESION,
                        onClick = { onNavigate(NavDestination.CAJA_SESION) }
                    )
                    SidebarNavItem(
                        label = "Venta Mostrador",
                        icon = Icons.Default.PointOfSale,
                        selected = currentDestination == NavDestination.VENTA_MOSTRADOR,
                        badgeCount = cartItemsCount,
                        onClick = { onNavigate(NavDestination.VENTA_MOSTRADOR) }
                    )
                    SidebarNavItem(
                        label = "Entregas Mayoristas",
                        icon = Icons.Default.LocalShipping,
                        selected = currentDestination == NavDestination.ENTREGAS_MAYORISTAS,
                        onClick = { onNavigate(NavDestination.ENTREGAS_MAYORISTAS) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category: Inventario
            SidebarCategoryHeader(
                title = "Inventario",
                icon = Icons.Default.Inventory2,
                expanded = inventarioExpanded,
                onToggle = { inventarioExpanded = !inventarioExpanded }
            )

            AnimatedVisibility(visible = inventarioExpanded) {
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    SidebarNavItem(
                        label = "Niveles de Inventario",
                        icon = Icons.Default.Assessment,
                        selected = currentDestination == NavDestination.NIVELES_INVENTARIO,
                        onClick = { onNavigate(NavDestination.NIVELES_INVENTARIO) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category: Configuración & Sync
            SidebarNavItem(
                label = "Configuración & Sync",
                icon = Icons.Default.Settings,
                selected = currentDestination == NavDestination.CONFIG_SYNC,
                onClick = { onNavigate(NavDestination.CONFIG_SYNC) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom User Footer & Exit App Actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NixtaSidebarCardBg)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // User info & Logout button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NixtaSidebarSelected)
                        ) {
                            Text(
                                text = currentUsuario?.nombre?.take(1)?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUsuario?.nombre ?: "Sin Usuario",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = currentUsuario?.rol ?: "-",
                                color = NixtaSidebarTextUnselected,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onLockClick) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloquear Terminal",
                                tint = NixtaSidebarTextUnselected,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Exit Application Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF382319))
                        .clickable { onExitAppClick() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Salir de la App",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Salir de la Aplicación",
                                color = Color(0xFFFF8A80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarCategoryHeader(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NixtaSidebarTextUnselected,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = NixtaSidebarTextUnselected,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SidebarNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NixtaSidebarSelected else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else NixtaSidebarTextUnselected,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    color = if (selected) Color.White else NixtaSidebarTextUnselected,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NixtaTerracottaPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
