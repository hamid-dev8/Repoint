package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "users")
data class User(@PrimaryKey val userId: String = UUID.randomUUID().toString(), val salt : String, val passwordHash : String = "0", val createdAt : String)
