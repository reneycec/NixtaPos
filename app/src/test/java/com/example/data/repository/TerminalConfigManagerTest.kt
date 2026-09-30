package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TerminalConfigManagerTest {

    private lateinit var context: Context
    private lateinit var configManager: TerminalConfigManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        configManager = TerminalConfigManager(context)
    }

    @Test
    fun `guardar y recuperar URL de API debe funcionar correctamente`() = runTest {
        val testUrl = "https://mi-servidor-laravel.com/api/v1/"
        
        configManager.updateApiBaseUrl(testUrl)
        
        val savedUrl = configManager.apiBaseUrl.first()
        assertEquals(testUrl, savedUrl)
    }

    @Test
    fun `guardar y recuperar last_sync debe persistir el valor Long`() = runTest {
        val timestamp = 1721515000000L
        
        configManager.updateLastSync(timestamp)
        
        val savedSync = configManager.lastSync.first()
        assertEquals(timestamp, savedSync)
    }
}
