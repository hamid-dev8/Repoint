package com.repoint.network.di

import com.repoint.network.TokenBalanceRepositoryImp
import com.repoint.network.Web3jWalletRepositoryImp
import com.repoint.network.datasource.HistoryDataSource
import com.repoint.network.datasource.TokenDataSource
import com.repoint.network.datasource.Web3DataSource
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


    @Binds
    @Singleton
    fun bindWeb3DataSource(imp : Web3jWalletRepositoryImp) : Web3DataSource
}