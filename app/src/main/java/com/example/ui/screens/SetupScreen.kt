package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodels.SetupViewModel
import com.example.ui.viewmodels.SetupUiState
import com.example.ui.components.NixtaNumericPad
import com.example.data.remote.dto.TenantDTO
import com.example.data.remote.dto.SucursalSetupDTO
import com.example.data.remote.dto.UsuarioSetupDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tenants by viewModel.tenants.collectAsStateWithLifecycle()
    val sucursales by viewModel.sucursales.collectAsStateWithLifecycle()
    val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()

    var step by remember { mutableStateOf(1) }
    var urlBase by remember { mutableStateOf("https://") }
    
    var selectedTenant by remember { mutableStateOf<String?>(null) }
    var selectedSucursal by remember { mutableStateOf<String?>(null) }
    var selectedUsuario by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }

    // Manejo de éxito
    LaunchedEffect(uiState) {
        if (uiState is SetupUiState.Success) {
            onFinish()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8F3))
            .padding(24.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .width(550.dp)
                .border(1.dp, NixtaSurfaceBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Mismo que antes...)
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = NixtaTerracottaPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Configuración Inicial", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NixtaTextPrimary)
                
                Spacer(modifier = Modifier.height(32.dp))

                // Contenido por Pasos
                when (step) {
                    1 -> StepServer(urlBase, { urlBase = it })
                    2 -> StepIdentification(
                        tenants = tenants,
                        sucursales = sucursales,
                        usuarios = usuarios,
                        selectedTenant = selectedTenant,
                        selectedSucursal = selectedSucursal,
                        selectedUsuario = selectedUsuario,
                        onTenantSelect = { 
                            selectedTenant = it
                            viewModel.fetchSucursales(it)
                        },
                        onSucursalSelect = {
                            selectedSucursal = it
                            viewModel.fetchUsuarios(it)
                        },
                        onUsuarioSelect = { selectedUsuario = it }
                    )
                    3 -> StepSecurity(pin) { pin = it }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Estado de Carga / Error
                if (uiState is SetupUiState.Loading) {
                    CircularProgressIndicator(color = NixtaTerracottaPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (uiState is SetupUiState.Error) {
                    Text((uiState as SetupUiState.Error).message, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botones
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 1) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text("Atrás") }
                    }
                    Button(
                        onClick = {
                            when (step) {
                                1 -> viewModel.testConnectionAndFetchTenants(urlBase)
                                2 -> if (selectedUsuario != null) step = 3
                                3 -> viewModel.completeSetup(urlBase, selectedTenant!!, selectedSucursal!!, selectedUsuario!!, pin)
                            }
                            if (step == 1 && uiState is SetupUiState.TenantsLoaded) step = 2
                        },
                        enabled = uiState !is SetupUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = NixtaTerracottaPrimary),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text(if (step < 3) "Siguiente" else "Finalizar")
                    }
                }
            }
        }
    }
}

@Composable
fun StepServer(url: String, onUrlChange: (String) -> Unit) {
    Column {
        Text("Paso 1: Conexión al Servidor", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("URL API Laravel") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StepIdentification(
    tenants: List<TenantDTO>,
    sucursales: List<SucursalSetupDTO>,
    usuarios: List<UsuarioSetupDTO>,
    selectedTenant: String?,
    selectedSucursal: String?,
    selectedUsuario: String?,
    onTenantSelect: (String) -> Unit,
    onSucursalSelect: (String) -> Unit,
    onUsuarioSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Paso 2: Selección de Sucursal", fontWeight = FontWeight.Bold)
        
        // Simulación de Dropdowns (Para brevedad, usaremos botones de selección)
        Text("Empresa:", fontSize = 12.sp, color = NixtaTextSecondary)
        tenants.forEach { 
            val isSel = it.id == selectedTenant
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(isSel) NixtaTerracottaPrimary else Color(0xFFF5EFE6)).clickable { onTenantSelect(it.id) }.padding(12.dp)) {
                Text(it.nombre, color = if(isSel) Color.White else NixtaTextPrimary)
            }
        }

        if (selectedTenant != null) {
            Text("Sucursal:", fontSize = 12.sp, color = NixtaTextSecondary)
            sucursales.forEach { 
                val isSel = it.id == selectedSucursal
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(isSel) NixtaTerracottaPrimary else Color(0xFFF5EFE6)).clickable { onSucursalSelect(it.id) }.padding(12.dp)) {
                    Text(it.nombre, color = if(isSel) Color.White else NixtaTextPrimary)
                }
            }
        }

        if (selectedSucursal != null) {
            Text("Usuario/Empleado:", fontSize = 12.sp, color = NixtaTextSecondary)
            usuarios.forEach { 
                val isSel = it.id == selectedUsuario
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(isSel) NixtaTerracottaPrimary else Color(0xFFF5EFE6)).clickable { onUsuarioSelect(it.id) }.padding(12.dp)) {
                    Text("${it.nombre} (${it.rol})", color = if(isSel) Color.White else NixtaTextPrimary)
                }
            }
        }
    }
}

@Composable
fun StepSecurity(pin: String, onPinChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Paso 3: Seguridad", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { i ->
                Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(if(pin.length > i) NixtaTerracottaPrimary else Color(0xFFE5E5E5)))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        NixtaNumericPad(value = pin, onValueChange = { if(it.length <= 4) onPinChange(it) })
    }
}
