package com.example.ui.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.PosDao
import com.example.data.local.remote.ApiService
import com.example.data.local.remote.LoginRequest
import com.example.data.mapper.toEntity
import com.example.data.repository.TerminalConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject

import com.example.data.repository.SyncRepository

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val posDao: PosDao,
    private val configManager: TerminalConfigManager,
    private val syncRepository: SyncRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val apiBaseUrl: StateFlow<String> = configManager.apiBaseUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "https://nixta.kuminova.com/api/v1/"
    )

    fun checkNetworkConnectivity(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val currentUrl = apiBaseUrl.value
            val isConnected = checkNetworkConnectivity()
            
            try {
                // 1. Intentar Login Online para obtener/actualizar credenciales
                var terminalId = configManager.terminalId.first()
                if (terminalId.isBlank()) {
                    val androidId = try {
                        android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                    } catch (e: Exception) { null }
                    terminalId = if (!androidId.isNullOrBlank()) "POS-$androidId" else "POS-TABLET-01"
                    configManager.updateTerminalId(terminalId)
                }

                val response = apiService.login(LoginRequest(email, password, terminalId))

                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()!!
                    
                    // Guardar usuario en DB local para permitir login offline futuro
                    loginData.usuario?.let { dto ->
                        posDao.upsertUsuario(dto.toEntity())
                        configManager.updateCurrentUserId(dto.id)
                        configManager.updateCurrentUserEmail(dto.email)
                        configManager.updateTenantId(dto.tenantId)
                        configManager.updateSucursalId(dto.sucursalId)
                    }

                    // Guardar Token y Prefijo de Folio estratégico
                    configManager.updateJwtToken(loginData.token)
                    loginData.folioPrefix?.let { configManager.updateFolioPrefix(it) }
                    
                    // Descargar catálogo de productos y clientes inmediatamente tras login exitoso (GET /api/v1/sync/pull)
                    viewModelScope.launch {
                        try {
                            syncRepository.pullData()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    _uiState.value = LoginUiState.Success
                } else {
                    val httpCode = response.code()
                    val serverMsg = response.body()?.message ?: response.errorBody()?.string()?.take(200) ?: response.message()
                    val detailedError = "Error Servidor (HTTP $httpCode): $serverMsg"
                    performLocalLogin(email, password, detailedError)
                }
            } catch (e: Exception) {
                val connMsg = if (!isConnected) "Sin conexión a Internet/WiFi en la terminal"
                else "No fue posible conectar a $currentUrl. Detalle: ${e.localizedMessage ?: e.message ?: e.javaClass.simpleName}"
                performLocalLogin(email, password, connMsg)
            }
        }
    }

    private suspend fun performLocalLogin(email: String, password: String, serverError: String?) {
        val localUser = posDao.getUsuarioByEmail(email)
        val isPasswordValid = localUser != null && localUser.password_hash.isNotEmpty() && BCrypt.verifyer()
            .verify(password.toCharArray(), localUser.password_hash)
            .verified

        if (isPasswordValid && localUser != null) {
            configManager.updateCurrentUserId(localUser.id)
            configManager.updateCurrentUserEmail(localUser.email)
            _uiState.value = LoginUiState.Success
        } else {
            val errorMsg = if (localUser == null) 
                "Fallo de conexión online ($serverError).\n\nNo existe el usuario '$email' guardado localmente en este teléfono." 
            else 
                serverError ?: "Contraseña incorrecta"
            _uiState.value = LoginUiState.Error(errorMsg)
        }
    }
}

