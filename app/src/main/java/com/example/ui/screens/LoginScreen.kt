package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.LoginUiState
import com.example.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()
    val isOnline = remember { viewModel.checkNetworkConnectivity() }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NixtaBackgroundCream),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(440.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NIXTA POS v2",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = NixtaTerracottaPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Indicador de Estado de Red (WiFi / Móvil)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (isOnline) NixtaBadgeGreenBg else NixtaBadgeRedBg,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isOnline) NixtaBadgeGreenText else NixtaBadgeRedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "RED CONECTADA (WiFi/Móvil)" else "SIN CONEXIÓN A RED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) NixtaBadgeGreenText else NixtaBadgeRedText
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Servidor: $apiBaseUrl",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = NixtaTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Contenedor de Error Detallado
                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NixtaBadgeRedBg, RoundedCornerShape(12.dp))
                            .border(1.dp, NixtaBadgeRedText.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = NixtaBadgeRedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (uiState as LoginUiState.Error).message,
                                color = NixtaBadgeRedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                    enabled = uiState !is LoginUiState.Loading && email.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                
                Text(
                    text = "Se requiere conexión online activa para la primera sesión",
                    fontSize = 10.sp,
                    color = NixtaTextSecondary,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}

