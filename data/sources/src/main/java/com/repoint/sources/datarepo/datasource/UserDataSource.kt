package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.User

interface UserDataSource
{

    suspend fun authUser(user : User)
    suspend fun getUser(id : Int) : User

}