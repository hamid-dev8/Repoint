package com.repoint.database.di

import android.content.Context
import androidx.room.Room
import com.repoint.database.AppDatabase
import com.repoint.database.dao.AuthDao
import com.repoint.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesRepointDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "repoint_database"
    ).build()

    @Provides
    @Singleton
    fun providesAuthDao(database: AppDatabase): AuthDao = database.authDao()

    @Provides
    @Singleton
    fun providesUserDao(database: AppDatabase) : UserDao = database.userDao()

}