package di

import com.repoint.network.datasource.HistoryDataSource
import com.repoint.sources.datarepo.BiometricRepositoryImp
import com.repoint.sources.datarepo.HistoryRepositoryImp
import com.repoint.sources.datarepo.UserRepositoryImp
import com.repoint.sources.datarepo.WalletRepositoryImp
import com.repoint.sources.datarepo.datasource.AuthDataSource
import com.repoint.sources.datarepo.datasource.BiometricDataSource
import com.repoint.sources.datarepo.datasource.UserDataSource
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

    @Binds
    @Singleton
    fun bindUserDataSource(imp : UserRepositoryImp) : UserDataSource

    @Binds
    @Singleton
    fun bindBiometricDataSource(imp : BiometricRepositoryImp) : BiometricDataSource

    @Binds
    @Singleton
    fun bindHistoryDataSource(imp : HistoryRepositoryImp) : HistoryDataSource
}