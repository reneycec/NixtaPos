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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductoEntity
import com.example.ui.theme.NixtaBadgeGreenBg
import com.example.ui.theme.NixtaBadgeGreenText
import com.example.ui.theme.NixtaBadgeRedBg
import com.example.ui.theme.NixtaBadgeRedText
import com.example.ui.theme.NixtaGradientStart
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary

@Composable
fun NivelesInventarioScreen(
    productos: List<ProductoEntity>,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val filtered = productos.filter {
        query.isEmpty() || it.nombre.contains(query, true) || it.sku.contains(query, true)
    }

    val totalSkus = productos.size
    val bajosStock = productos.count { it.stock <= it.alerta_minimo && it.stock > 0 }
    val agotados = productos.count { it.stock <= 0 }
    val valorInventario = productos.sumOf { (it.precio_con_impuestos * it.stock).toLong() } / 100.0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Path Header
        item {
            Column {
                Text(
                    text = "INVENTARIO · NIVELES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NixtaTerracottaPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Niveles de Inventario",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NixtaTextPrimary
                )
            }
        }

        // Stats Box
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCardItem("SKUS ACTIVOS", totalSkus.toString(), modifier = Modifier.weight(1f))
                StatCardItem("BAJO MÍNIMO", bajosStock.toString(), warning = true, modifier = Modifier.weight(1f))
                StatCardItem("AGOTADOS", agotados.toString(), alert = true, modifier = Modifier.weight(1f))
                StatCardItem("VALOR INVENTARIO", "$${String.format("%.2f", valorInventario)}", highlight = true, modifier = Modifier.weight(1f))
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Filtrar por SKU o Nombre...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NixtaTerracottaPrimary,
                    unfocusedBorderColor = NixtaSurfaceBorder,
                    focusedContainerColor = NixtaSurfaceCream,
                    unfocusedContainerColor = NixtaSurfaceCream
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Inventory Table
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NixtaSurfaceCream),
                modifier = Modifier.fillMaxWidth().border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Stock Actual de Productos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NixtaGradientStart)
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text("SKU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                        Text("PRODUCTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1.3f))
                        Text("CATEGORÍA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1f))
                        Text("EXISTENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(1.1f))
                        Text("PRECIO UNIT.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                        Text("ESTADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary, modifier = Modifier.weight(0.9f))
                    }

                    filtered.forEach { prod ->
                        val isLow = prod.stock <= prod.alerta_minimo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, NixtaSurfaceBorder)
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(prod.sku, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTerracottaPrimary, modifier = Modifier.weight(0.9f))
                            Text(prod.nombre, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NixtaTextPrimary, modifier = Modifier.weight(1.3f))
                            Text(prod.categoria, fontSize = 10.sp, color = NixtaTextSecondary, modifier = Modifier.weight(1f))
                            Text("${prod.stock} ${prod.unidad_medida}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NixtaTextPrimary, modifier = Modifier.weight(1.1f))
                            Text("$${String.format("%.2f", prod.precio_con_impuestos / 100.0)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NixtaTextPrimary, modifier = Modifier.weight(0.9f))

                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isLow) NixtaBadgeRedBg else NixtaBadgeGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isLow) "BAJO" else "ÓPTIMO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLow) NixtaBadgeRedText else NixtaBadgeGreenText
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
private fun StatCardItem(
    label: String,
    value: String,
    highlight: Boolean = false,
    warning: Boolean = false,
    alert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) NixtaGradientStart else NixtaSurfaceCream),
        modifier = modifier.border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NixtaTextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = when {
                    highlight -> NixtaTerracottaPrimary
                    warning -> Color(0xFFD35400)
                    alert -> NixtaBadgeRedText
                    else -> NixtaTextPrimary
                }
            )
        }
    }
}
