package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetworkSyncObserver(
    private val context: Context,
    private val syncManager: SyncManager
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastAutoSyncTime = MutableStateFlow<Long?>(null)
    val lastAutoSyncTime: StateFlow<Long?> = _lastAutoSyncTime.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var periodicSyncJob: Job? = null

    init {
        registerNetworkCallback()
        startPeriodicSyncLoop()
    }

    private fun checkInitialConnectivity(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                // Automatically trigger background outbound sync when internet connection becomes available
                triggerAutoSync("Conexión restablecida")
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        })
    }

    fun triggerAutoSync(reason: String) {
        if (!_isOnline.value) return
        scope.launch {
            try {
                val syncedCount = syncManager.executeOutboundSync()
                if (syncedCount > 0) {
                    _lastAutoSyncTime.value = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPeriodicSyncLoop() {
        periodicSyncJob = scope.launch {
            while (isActive) {
                delay(30_000) // Poll sync every 30s if online
                if (_isOnline.value) {
                    try {
                        val syncedCount = syncManager.executeOutboundSync()
                        if (syncedCount > 0) {
                            _lastAutoSyncTime.value = System.currentTimeMillis()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
