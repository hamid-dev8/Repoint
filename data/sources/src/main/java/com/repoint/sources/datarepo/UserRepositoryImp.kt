package com.repoint.sources.datarepo

import com.repoint.database.dao.UserDao
import com.repoint.models.sharedmodels.local.User
import com.repoint.sources.datarepo.datasource.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImp @Inject constructor(private val userDao: UserDao) : UserDataSource
{
    override suspend fun authUser(user: User)  {
         userDao.authUser(user)
    }

    override suspend fun getUser(): User {
        return userDao.getUser()
    }

    override suspend fun updateUser(userId: String, salt: String, passwordHash: String) {
        return userDao.updateUser(userId,salt,passwordHash)
    }


}