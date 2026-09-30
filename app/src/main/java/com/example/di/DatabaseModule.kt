package com.example.di

import android.content.Context
import com.example.data.local.dao.PosDao
import com.example.data.local.database.NixtaPosDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NixtaPosDatabase {
        return NixtaPosDatabase.getDatabase(context)
    }

    @Provides
    fun providePosDao(database: NixtaPosDatabase): PosDao {
        return database.posDao()
    }
}
