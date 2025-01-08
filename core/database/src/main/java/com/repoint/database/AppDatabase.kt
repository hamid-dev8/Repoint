package com.repoint.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repoint.database.dao.AuthDao
import com.repoint.database.dao.UserDao
import com.repoint.models.sharedmodels.User
import com.repoint.models.sharedmodels.RepointWallet

@Database(entities = [User::class,RepointWallet::class] , version =  1 , exportSchema = false)
 abstract class AppDatabase : RoomDatabase()
{
    abstract fun authDao() : AuthDao
    abstract fun userDao() : UserDao
}