package com.repoint.network.di

import com.repoint.network.TokenBalanceRepositoryImp
import com.repoint.network.datasource.TokenDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface RepoModule {


    @Binds
    @Singleton
    fun bindTokenDataSource(imp : TokenBalanceRepositoryImp) : TokenDataSource


}