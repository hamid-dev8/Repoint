package com.repoint.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.User
import com.repoint.models.sharedmodels.Wallet

@Database(entities = [User::class,Wallet::class] , version =  1 , exportSchema = false)
 abstract class AppDatabase : RoomDatabase()
{
    abstract fun AuthDao() : AuthDao
}