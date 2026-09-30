package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Iniciando sincronización programada en segundo plano...")
        
        return try {
            val result = syncRepository.pullData()
            if (result != null) {
                Log.d("SyncWorker", "Sincronización exitosa")
                Result.success()
            } else {
                Log.w("SyncWorker", "La sincronización falló pero se reintentará")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error crítico en SyncWorker", e)
            Result.retry() // Reintentar en caso de error de red
        }
    }
}
