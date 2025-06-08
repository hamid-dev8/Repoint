package com.repoint.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.repoint.database.dao.AuthDao
import com.repoint.database.dao.CmcTokenDao
import com.repoint.database.dao.NetworkDao
import com.repoint.database.dao.UserDao
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.local.User
import com.repoint.models.sharedmodels.local.CmcTokenEntity

@Database(
    entities = [BlockchainNetworkEntity::class, LocalActiveNetworks::class, User::class, RepointWallet::class, TokenEntity::class, MasterWallet::class, ChainWallet::class , CmcTokenEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao
    abstract fun userDao(): UserDao
    abstract fun networkDao(): NetworkDao
    abstract fun cmcTokenDao() : CmcTokenDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS tokens (" +
                            "tokenId INTEGER PRIMARY KEY NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "symbol TEXT NOT NULL, " +
                            "contractAddress TEXT NOT NULL, " +
                            "decimals INTEGER NOT NULL, " +
                            "logoUrl TEXT NOT NULL, " +
                            "networkId INTEGER NOT NULL, " +
                            "FOREIGN KEY(networkId) REFERENCES networks(id) ON DELETE CASCADE)"
                )
            }
        }
    }

}