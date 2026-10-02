package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ProductoEntity
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary
import java.util.Locale
import kotlin.math.round

@Composable
fun VentaGranelDialog(
    producto: ProductoEntity,
    onDismiss: () -> Unit,
    onAddToCart: (Double) -> Unit
) {
    // El precio viene en centavos (ej: 4000 = $40.00), lo convertimos a pesos para la UI
    val precioKg = producto.precio_con_impuestos / 100.0

    var montoInput by remember { mutableStateOf("") }
    var pesoInput by remember { mutableStateOf("") }

    // Función para redondear a 3 decimales
    fun Double.roundTo3(): Double = round(this * 1000) / 1000

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NixtaSurfaceCream,
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Venta a Granel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixtaTextPrimary
                        )
                        Text(
                            text = "${producto.nombre} - $${String.format(Locale.getDefault(), "%.2f", precioKg)} / ${producto.unidad_medida}",
                            fontSize = 14.sp,
                            color = NixtaTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NixtaTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Inputs (Bidirectional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Input Peso
                    OutlinedTextField(
                        value = pesoInput,
                        onValueChange = { newPesoStr ->
                            pesoInput = newPesoStr
                            val p = newPesoStr.toDoubleOrNull()
                            if (p != null) {
                                val calculado = p * precioKg
                                montoInput = String.format(Locale.getDefault(), "%.2f", calculado)
                            } else if (newPesoStr.isEmpty()) {
                                montoInput = ""
                            }
                        },
                        label = { Text("Cantidad (${producto.unidad_medida})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Scale, null, tint = NixtaTextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NixtaTerracottaPrimary,
                            unfocusedBorderColor = NixtaSurfaceBorder
                        )
                    )

                    // Input Monto
                    OutlinedTextField(
                        value = montoInput,
                        onValueChange = { newMontoStr ->
                            montoInput = newMontoStr
                            val m = newMontoStr.toDoubleOrNull()
                            if (m != null) {
                                val calculado = m / precioKg
                                pesoInput = String.format(Locale.getDefault(), "%.3f", calculado.roundTo3())
                            } else if (newMontoStr.isEmpty()) {
                                pesoInput = ""
                            }
                        },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text("$", color = NixtaTextSecondary, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NixtaTerracottaPrimary,
                            unfocusedBorderColor = NixtaSurfaceBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones rápidos de monto
                Text("Monto rápido:", fontSize = 12.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10.0, 20.0, 30.0, 50.0).forEach { monto ->
                        OutlinedButton(
                            onClick = {
                                montoInput = monto.toString()
                                pesoInput = String.format(Locale.getDefault(), "%.3f", (monto / precioKg).roundTo3())
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NixtaTextPrimary),
                            border = BorderStroke(1.dp, NixtaSurfaceBorder)
                        ) {
                            Text("$$monto", fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botones rápidos de peso
                Text("Cantidad rápida:", fontSize = 12.sp, color = NixtaTextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.25, 0.5, 1.0, 2.0).forEach { peso ->
                        OutlinedButton(
                            onClick = {
                                pesoInput = peso.toString()
                                montoInput = String.format(Locale.getDefault(), "%.2f", (peso * precioKg))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NixtaTextPrimary),
                            border = BorderStroke(1.dp, NixtaSurfaceBorder)
                        ) {
                            val txt = if (peso == 0.25) "1/4" else if (peso == 0.5) "1/2" else "${peso.toInt()}"
                            Text("$txt ${producto.unidad_medida}", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Confirmar
                Button(
                    onClick = {
                        val finalPeso = pesoInput.toDoubleOrNull()
                        if (finalPeso != null && finalPeso > 0) {
                            onAddToCart(finalPeso)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    enabled = (pesoInput.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Agregar a Ticket", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
