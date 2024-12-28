package com.repoint.sources.datarepo

import com.repoint.database.dao.AuthDao
import com.repoint.models.sharedmodels.User
import com.repoint.sources.datarepo.datasource.AuthDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
 class UserRepositoryImp @Inject constructor(private val authDao: AuthDao) : AuthDataSource {


    override suspend fun authUser(user: User) {
        authDao.authUser(user)
    }

    override suspend fun getUser(): User {
        TODO("Not yet implemented")
    }
}