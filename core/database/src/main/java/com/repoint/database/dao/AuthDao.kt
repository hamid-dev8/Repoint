package com.repoint.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.repoint.models.sharedmodels.User


@Dao
interface AuthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun authUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getUser() : User


}