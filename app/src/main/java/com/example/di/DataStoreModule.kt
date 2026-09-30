package com.example.di

import android.content.Context
import com.example.data.repository.TerminalConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTerminalConfigManager(@ApplicationContext context: Context): TerminalConfigManager {
        return TerminalConfigManager(context)
    }
}
