package com.example.ui.screens

import com.example.data.local.entities.isGranel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.NixtaNumericPad
import com.example.data.local.entities.ClienteEntity
import com.example.data.local.entities.ProductoEntity
import com.example.data.local.entities.SesionCajaEntity
import com.example.data.repository.CartItem
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
import java.util.Locale

@Composable
fun VentaMostradorScreen(
    sesionActiva: SesionCajaEntity?,
    productos: List<ProductoEntity>,
    clientes: List<ClienteEntity>,
    cartItems: List<CartItem>,
    desglosarImpuestos: Boolean,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onAddToCart: (ProductoEntity) -> Unit,
    onAddToCartGranel: (ProductoEntity) -> Unit, // Callback para abrir modal de granel
    onUpdateQuantity: (String, Double) -> Unit,
    onTogglePapel: (String) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onClearCart: () -> Unit,
    onToggleDesglosarImpuestos: () -> Unit,
    onCobrar: (String, String?) -> Unit,
    onGoToApertura: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("EFECTIVO") }
    var cashPaidInput by remember { mutableStateOf("") }
    var selectedClienteId by remember { mutableStateOf<String?>(null) }

    if (sesionActiva == null) {
        // NO ACTIVE SESSION WARNING SCREEN (Matches Screenshot 5)
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier
                    .width(480.dp)
                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(NixtaGradientStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NixtaTerracottaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No hay caja abierta en esta sucursal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NixtaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Para poder cobrar, primero abre la caja del turno activo.",
                        fontSize = 13.sp,
                        color = NixtaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onGoToApertura,
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ir a apertura de caja", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // ACTIVE POS SHOPPING SCREEN (Matches Screenshots 7 & 8)
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Left Column: Catalog & Search (2/3 width)
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight()
        ) {
            // Header Title Path
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "POS · VENTA MOSTRADOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NixtaTerracottaPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cobrar al cliente",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = NixtaTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NixtaGradientStart)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Caja activa - Turno en curso",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NixtaTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Camera Scanner Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar por nombre, SKU o escanear código...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NixtaTextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NixtaTerracottaPrimary,
                        unfocusedBorderColor = NixtaSurfaceBorder,
                        focusedContainerColor = NixtaSurfaceCream,
                        unfocusedContainerColor = NixtaSurfaceCream
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onOpenScanner,
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Escáner",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            val categories = listOf("Todas") + productos.map { it.categoria }.distinct()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NixtaTerracottaPrimary else NixtaSurfaceCream)
                            .border(
                                1.dp,
                                if (isSelected) NixtaTerracottaPrimary else NixtaSurfaceBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategoryChange(cat) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else NixtaTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Products Grid
            val filteredProductos = productos.filter { prod ->
                (selectedCategory == "Todas" || prod.categoria == selectedCategory) &&
                        (searchQuery.isEmpty() || prod.nombre.contains(searchQuery, true) || prod.sku.contains(searchQuery, true))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredProductos) { prod ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                        modifier = Modifier
                            .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                if (prod.isGranel) {
                                    onAddToCartGranel(prod)
                                } else {
                                    onAddToCart(prod)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = prod.categoria.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = prod.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val pricePesos = prod.precio_con_impuestos / 100.0
                                val formatPrice = String.format(Locale.getDefault(), "%.2f", pricePesos)
                                Text(
                                    text = "$$formatPrice",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NixtaTerracottaPrimary
                                )

                                // Stock Badge
                                val isLow = prod.stock <= prod.alerta_minimo
                                val badgeBg = if (isLow) NixtaBadgeRedBg else NixtaBadgeGreenBg
                                val badgeText = if (isLow) NixtaBadgeRedText else NixtaBadgeGreenText

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Existencia: ${prod.stock} - ${if (isLow) "Bajo" else "Óptimo"}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Ticket en curso (Cart) (1/3 width)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Ticket Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = NixtaTerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ticket en curso",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTextPrimary
                        )
                    }

                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = onClearCart) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Vaciar",
                                tint = NixtaTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Items List
                if (cartItems.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Selecciona productos del catálogo.",
                            fontSize = 12.sp,
                            color = NixtaTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(cartItems) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = item.producto.nombre,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NixtaTextPrimary
                                            )
                                            val unitPricePesos = item.producto.precio_con_impuestos / 100.0
                                            val unidadLabel = if (item.producto.isGranel) "kg" else item.producto.unidad_medida
                                            Text(
                                                text = "$${String.format(Locale.getDefault(), "%.2f", unitPricePesos)} / $unidadLabel",
                                                fontSize = 10.sp,
                                                color = NixtaTextSecondary
                                            )
                                        }

                                        val itemTotal = (item.producto.precio_con_impuestos * item.cantidad / 100.0) + if (item.incluyePapel) 1.0 else 0.0
                                        Text(
                                            text = "$${String.format(Locale.getDefault(), "%.2f", itemTotal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NixtaTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Quantity controls
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF5EFE6))
                                        ) {
                                            val step = if (item.producto.isGranel) 0.25 else 1.0
                                            IconButton(
                                                onClick = { onUpdateQuantity(item.producto.id, item.cantidad - step) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                                            }

                                            val cantidadText = if (item.producto.isGranel) "${item.cantidad} kg" else item.cantidad.toInt().toString()
                                            Text(
                                                text = cantidadText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        if (item.producto.isGranel) {
                                                            onAddToCartGranel(item.producto)
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp)
                                            )

                                            IconButton(
                                                onClick = { onUpdateQuantity(item.producto.id, item.cantidad + step) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        // Checkbox for Papel (+$1)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = item.incluyePapel,
                                                onCheckedChange = { onTogglePapel(item.producto.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = NixtaTerracottaPrimary),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Papel (+$1)", fontSize = 10.sp, color = NixtaTextSecondary)
                                        }

                                        IconButton(
                                            onClick = { onRemoveFromCart(item.producto.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = NixtaBadgeRedText, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tax Breakdown Toggle
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Desglosar IVA/IEPS", fontSize = 11.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = desglosarImpuestos,
                        onCheckedChange = { onToggleDesglosarImpuestos() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NixtaTerracottaPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtotal & Total
                var cartSubtotalCentavos = 0L
                var taxBreakdownCentavos = 0L

                cartItems.forEach { item ->
                    val base = (item.producto.precio_con_impuestos * item.cantidad).toLong() + if (item.incluyePapel) 100L else 0L
                    cartSubtotalCentavos += base
                    taxBreakdownCentavos += (base * item.producto.tasa_iva).toLong()
                }

                if (desglosarImpuestos) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Subtotal s/impuesto", fontSize = 11.sp, color = NixtaTextSecondary)
                        Text("$${String.format("%.2f", (cartSubtotalCentavos - taxBreakdownCentavos) / 100.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    ) {
                        Text("IVA (16%)", fontSize = 11.sp, color = NixtaTextSecondary)
                        Text("$${String.format("%.2f", taxBreakdownCentavos / 100.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Subtotal", fontSize = 12.sp, color = NixtaTextSecondary)
                        Text("$${String.format("%.2f", cartSubtotalCentavos / 100.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                    Text(
                        text = "$${String.format("%.2f", cartSubtotalCentavos / 100.0)}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = NixtaTerracottaPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { if (cartItems.isNotEmpty()) showPaymentModal = true },
                    enabled = cartItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.PointOfSale, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cobrar $${String.format("%.2f", cartSubtotalCentavos / 100.0)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // PAYMENT MODAL DIALOG
    if (showPaymentModal) {
        val totalPesos = cartItems.sumOf { (it.producto.precio_con_impuestos * it.cantidad).toLong() + if (it.incluyePapel) 100L else 0L } / 100.0
        val cashPaidDouble = cashPaidInput.toDoubleOrNull() ?: 0.0
        val cambioPesos = cashPaidDouble - totalPesos

        Dialog(onDismissRequest = { showPaymentModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                modifier = Modifier
                    .width(420.dp)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Cobrar Venta - Total: $${String.format("%.2f", totalPesos)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)

                    Spacer(Modifier.height(16.dp))

                    Text("Método de Pago", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        listOf("EFECTIVO", "TARJETA", "TRANSFERENCIA", "CRÉDITO").forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NixtaTerracottaPrimary else Color(0xFFF5EFE6))
                                    .clickable { selectedPaymentMethod = method }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else NixtaTextPrimary)
                            }
                        }
                    }

                    if (selectedPaymentMethod == "EFECTIVO") {
                        Spacer(Modifier.height(16.dp))
                        
                        // Pantalla de visualización de entrada (Prominente)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5EFE6))
                                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = if (cashPaidInput.isEmpty()) "$ 0.00" else "$ $cashPaidInput",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (cashPaidInput.isEmpty()) NixtaTextSecondary else NixtaTerracottaPrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Teclado Numérico Integrado
                        NixtaNumericPad(
                            value = cashPaidInput,
                            onValueChange = { cashPaidInput = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Cambio a entregar:", fontSize = 14.sp, color = NixtaTextSecondary)
                            Text(
                                text = if (cambioPesos >= 0) "$${String.format("%.2f", cambioPesos)}" else "$0.00",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (cambioPesos >= 0) NixtaBadgeGreenText else NixtaBadgeRedText
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onCobrar(selectedPaymentMethod, selectedClienteId)
                            showPaymentModal = false
                            cashPaidInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Finalizar Venta y Generar Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
