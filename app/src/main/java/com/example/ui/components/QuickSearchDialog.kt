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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
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
import com.example.data.local.entities.ClienteEntity
import com.example.data.local.entities.ProductoEntity
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaGradientStart
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaContainer
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary

@Composable
fun QuickSearchDialog(
    isOpen: Boolean,
    productos: List<ProductoEntity>,
    clientes: List<ClienteEntity>,
    onAddToCart: (ProductoEntity) -> Unit,
    onSelectCliente: (ClienteEntity) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Todos, 1: Productos, 2: Clientes

    val trimmedQuery = query.trim().lowercase()

    val filteredProductos = remember(productos, trimmedQuery) {
        if (trimmedQuery.isEmpty()) productos.take(10)
        else productos.filter {
            it.nombre.lowercase().contains(trimmedQuery) ||
                    it.sku.lowercase().contains(trimmedQuery) ||
                    it.categoria.lowercase().contains(trimmedQuery)
        }
    }

    val filteredClientes = remember(clientes, trimmedQuery) {
        if (trimmedQuery.isEmpty()) clientes.take(10)
        else clientes.filter {
            it.nombre.lowercase().contains(trimmedQuery) ||
                    it.codigo.lowercase().contains(trimmedQuery) ||
                    it.telefono.lowercase().contains(trimmedQuery)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
            modifier = Modifier
                .width(620.dp)
                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
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
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = NixtaTerracottaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Búsqueda Rápida POS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NixtaTextPrimary
                            )
                            Text(
                                text = "Encuentra productos, precios, existencias y clientes mayoristas",
                                fontSize = 11.sp,
                                color = NixtaTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NixtaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar Input
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Escribe código, producto o cliente...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NixtaTerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, null, tint = NixtaTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NixtaTerracottaPrimary,
                        unfocusedBorderColor = NixtaSurfaceBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = NixtaTerracottaPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NixtaTerracottaPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Todos (${filteredProductos.size + filteredClientes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Productos (${filteredProductos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Clientes (${filteredClientes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    if ((selectedTab == 0 || selectedTab == 1) && filteredProductos.isNotEmpty()) {
                        item {
                            Text(
                                text = "PRODUCTOS (${filteredProductos.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(filteredProductos) { prod ->
                            ProductSearchResultItem(
                                producto = prod,
                                onAddToCart = {
                                    onAddToCart(prod)
                                }
                            )
                        }
                    }

                    if ((selectedTab == 0 || selectedTab == 2) && filteredClientes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "CLIENTES MAYORISTAS (${filteredClientes.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(filteredClientes) { cli ->
                            ClienteSearchResultItem(
                                cliente = cli,
                                onSelect = {
                                    onSelectCliente(cli)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    if (filteredProductos.isEmpty() && filteredClientes.isEmpty()) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            ) {
                                Text(
                                    text = "No se encontraron coincidencias para \"$query\"",
                                    fontSize = 13.sp,
                                    color = NixtaTextSecondary
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
private fun ProductSearchResultItem(
    producto: ProductoEntity,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NixtaGradientStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = NixtaTerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = producto.nombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF2EBE1))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = producto.sku,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextSecondary
                            )
                        }
                    }
                    Text(
                        text = "Categoría: ${producto.categoria} · Stock: ${producto.stock} ${producto.unidad_medida}",
                        fontSize = 11.sp,
                        color = NixtaTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${String.format("%.2f", producto.precio_con_impuestos / 100.0)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = NixtaTerracottaPrimary,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Agregar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Agregar",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ClienteSearchResultItem(
    cliente: ClienteEntity,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NixtaBadgeGreenBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NixtaBadgeGreenText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cliente.nombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${cliente.codigo})",
                            fontSize = 11.sp,
                            color = NixtaTextSecondary
                        )
                    }
                    Text(
                        text = "Tel: ${cliente.telefono} · Dir: ${cliente.direccion} · Pago: ${cliente.condicion_pago}",
                        fontSize = 11.sp,
                        color = NixtaTextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NixtaBadgeGreenBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Seleccionar",
                    color = NixtaBadgeGreenText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
