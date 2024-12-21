package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.User

interface AuthDataSource
{
    suspend fun authUser(user : User)
    suspend fun getUser() : User

}