package com.repoint.models.sharedmodels

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(@PrimaryKey(autoGenerate = true) val id : Int,val passwordHash : String,val createdAt : Long)
