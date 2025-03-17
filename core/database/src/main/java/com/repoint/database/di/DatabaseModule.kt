package com.repoint.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.repoint.database.AppDatabase
import com.repoint.database.dao.AuthDao
import com.repoint.database.dao.NetworkDao
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
    ).fallbackToDestructiveMigration().addCallback(object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("PRAGMA foreign_keys=ON;") // ✅ Enable foreign keys
        }
    }).addMigrations(AppDatabase.MIGRATION_1_2).build()


    @Provides
    @Singleton
    fun providesAuthDao(database: AppDatabase): AuthDao = database.authDao()

    @Provides
    @Singleton
    fun providesUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideNetworkDao(database: AppDatabase) : NetworkDao = database.networkDao()

}