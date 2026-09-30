package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DenominationCount
import com.example.ui.theme.NixtaSurfaceBorder
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTextPrimary
import com.example.ui.theme.NixtaTextSecondary

@Composable
fun CashDenominationGrid(
    denominations: DenominationCount,
    onDenominationChange: ((DenominationCount) -> DenominationCount) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        "$1000" to (denominations.b1000 to { count: Int -> onDenominationChange { it.copy(b1000 = count) } }),
        "$500" to (denominations.b500 to { count: Int -> onDenominationChange { it.copy(b500 = count) } }),
        "$200" to (denominations.b200 to { count: Int -> onDenominationChange { it.copy(b200 = count) } }),
        "$100" to (denominations.b100 to { count: Int -> onDenominationChange { it.copy(b100 = count) } }),
        "$50" to (denominations.b50 to { count: Int -> onDenominationChange { it.copy(b50 = count) } }),
        "$20" to (denominations.b20 to { count: Int -> onDenominationChange { it.copy(b20 = count) } }),
        "$10" to (denominations.m10 to { count: Int -> onDenominationChange { it.copy(m10 = count) } }),
        "$5" to (denominations.m5 to { count: Int -> onDenominationChange { it.copy(m5 = count) } }),
        "$2" to (denominations.m2 to { count: Int -> onDenominationChange { it.copy(m2 = count) } }),
        "$1" to (denominations.m1 to { count: Int -> onDenominationChange { it.copy(m1 = count) } }),
        "$0.5" to (denominations.m05 to { count: Int -> onDenominationChange { it.copy(m05 = count) } })
    )

    Column(modifier = modifier) {
        items.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                rowItems.forEach { (label, data) ->
                    val (count, updateFunc) = data
                    val faceVal = label.replace("$", "").toDoubleOrNull() ?: 0.0
                    val totalPesos = faceVal * count

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NixtaSurfaceCream)
                            .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NixtaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = if (count == 0) "" else count.toString(),
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }
                                    updateFunc(clean.toIntOrNull() ?: 0)
                                },
                                placeholder = { Text("0", fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD16645),
                                    unfocusedBorderColor = NixtaSurfaceBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "= $${String.format("%.2f", totalPesos)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = NixtaTextSecondary,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
                // Fill empty slots if last row has less than 3
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
