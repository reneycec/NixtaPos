package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.dto.*
import com.example.data.repository.SetupRepository
import com.example.data.repository.TerminalConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: SetupRepository,
    private val configManager: TerminalConfigManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _tenants = MutableStateFlow<List<TenantDTO>>(emptyList())
    val tenants: StateFlow<List<TenantDTO>> = _tenants.asStateFlow()

    private val _sucursales = MutableStateFlow<List<SucursalSetupDTO>>(emptyList())
    val sucursales: StateFlow<List<SucursalSetupDTO>> = _sucursales.asStateFlow()

    private val _usuarios = MutableStateFlow<List<UsuarioSetupDTO>>(emptyList())
    val usuarios: StateFlow<List<UsuarioSetupDTO>> = _usuarios.asStateFlow()

    fun testConnectionAndFetchTenants(url: String) {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            // Primero actualizamos la URL en el DataStore para que el Interceptor la use
            configManager.updateApiBaseUrl(url)
            
            repository.getTenants().onSuccess {
                _tenants.value = it
                _uiState.value = SetupUiState.TenantsLoaded
            }.onFailure {
                _uiState.value = SetupUiState.Error("No se pudo conectar al servidor: ${it.message}")
            }
        }
    }

    fun fetchSucursales(tenantId: String) {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            repository.getSucursales(tenantId).onSuccess {
                _sucursales.value = it
                _uiState.value = SetupUiState.SucursalesLoaded
            }.onFailure {
                _uiState.value = SetupUiState.Error(it.message ?: "Error al cargar sucursales")
            }
        }
    }

    fun fetchUsuarios(sucursalId: String) {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            repository.getUsuarios(sucursalId).onSuccess {
                _usuarios.value = it
                _uiState.value = SetupUiState.UsuariosLoaded
            }.onFailure {
                _uiState.value = SetupUiState.Error(it.message ?: "Error al cargar usuarios")
            }
        }
    }

    fun completeSetup(url: String, tenantId: String, sucursalId: String, userId: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            repository.validarPin(userId, pin).onSuccess { isValid ->
                if (isValid) {
                    // Guardamos la configuración definitiva
                    configManager.updateApiBaseUrl(url)
                    configManager.updateTenantId(tenantId)
                    configManager.updateSucursalId(sucursalId)
                    configManager.updateCurrentUserId(userId)
                    _uiState.value = SetupUiState.Success
                } else {
                    _uiState.value = SetupUiState.Error("PIN incorrecto")
                }
            }.onFailure {
                _uiState.value = SetupUiState.Error("Error de validación")
            }
        }
    }
}

sealed class SetupUiState {
    object Idle : SetupUiState()
    object Loading : SetupUiState()
    object TenantsLoaded : SetupUiState()
    object SucursalesLoaded : SetupUiState()
    object UsuariosLoaded : SetupUiState()
    object Success : SetupUiState()
    data class Error(val message: String) : SetupUiState()
}
