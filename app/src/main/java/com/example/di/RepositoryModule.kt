package com.example.di

import com.example.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSetupRepository(
        mockImplementation: MockSetupRepository // <--- PARA CAMBIAR A REAL, SOLO CAMBIA ESTA LÍNEA
    ): SetupRepository
}
