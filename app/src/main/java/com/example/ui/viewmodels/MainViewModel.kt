package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TerminalConfigManager
import com.example.data.worker.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val configManager: TerminalConfigManager,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        checkStartDestination()
    }

    private fun checkStartDestination() {
        viewModelScope.launch {
            val tenantId = configManager.tenantId.first()
            val userId = configManager.currentUserId.first()

            val destination = when {
                tenantId == "TENANT-NIXTA-01" -> "setup" 
                userId == null -> "login"
                else -> "pos"
            }

            // REQUERIMIENTO 3 y 5: Sincronizar al abrir si ya está configurado
            if (destination != "setup") {
                syncManager.runImmediateSync()
                syncManager.schedulePeriodicSync() // REQUERIMIENTO 6: Segundo plano
            }

            _startDestination.value = destination
        }
    }
}
