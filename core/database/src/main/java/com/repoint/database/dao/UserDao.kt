package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.local.User


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun authUser(user : User)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser() : User?

    @Query("UPDATE users SET salt = :salt, passwordHash = :passwordHash WHERE userId = :userId")
    suspend fun updateUser(userId: String,salt :String,passwordHash : String)

}