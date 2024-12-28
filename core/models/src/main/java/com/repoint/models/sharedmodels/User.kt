package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(@PrimaryKey(autoGenerate = true) val userId : Int,val passwordHash : String = "0",val createdAt : Long)
