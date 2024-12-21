package com.repoint.database.di

import com.repoint.database.AppDatabase
import com.repoint.database.dao.AuthDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {


    @Provides
    fun providesAuthDao(
        database: AppDatabase
    ) : AuthDao = database.AuthDao()

}