package di

import com.repoint.sources.datarepo.WalletRepositoryImp
import com.repoint.sources.datarepo.datasource.AuthDataSource
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
    fun bindAuthDataSource(imp: WalletRepositoryImp ) : AuthDataSource


}