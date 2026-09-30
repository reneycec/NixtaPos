package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NixtaSurfaceCream
import com.example.ui.theme.NixtaTerracottaPrimary
import com.example.ui.theme.NixtaTextPrimary
import com.example.util.TicketPrinterUtil

@Composable
fun TicketDialog(
    ticketData: TicketPrinterUtil.TicketData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val plainText = TicketPrinterUtil.generatePlainTextTicket(ticketData)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .width(360.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📄 Ticket de Venta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NixtaTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(NixtaSurfaceCream, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2D7C5), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = plainText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Black,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cerrar")
                    }

                    Button(
                        onClick = {
                            TicketPrinterUtil.printViaAndroidPrintManager(context, ticketData)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
