package di

import com.repoint.sources.datarepo.AlchemyRepositoryImp
import com.repoint.sources.datarepo.TokenBalanceRepositoryImp
import com.repoint.sources.datarepo.Web3jWalletRepositoryImp
import com.repoint.sources.datarepo.datasource.HistoryDataSource
import com.repoint.sources.datarepo.BiometricRepositoryImp
import com.repoint.sources.datarepo.CmcRepositoryImp
import com.repoint.sources.datarepo.HistoryRepositoryImp
import com.repoint.sources.datarepo.NetworkRepositoryImp
import com.repoint.sources.datarepo.UserRepositoryImp
import com.repoint.sources.datarepo.WalletRepositoryImp
import com.repoint.sources.datarepo.datasource.AlchemyDataSource
import com.repoint.sources.datarepo.datasource.AuthDataSource
import com.repoint.sources.datarepo.datasource.BiometricDataSource
import com.repoint.sources.datarepo.datasource.CmcDataSource
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import com.repoint.sources.datarepo.datasource.TokenDataSource
import com.repoint.sources.datarepo.datasource.UserDataSource
import com.repoint.sources.datarepo.datasource.Web3DataSource
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

    @Binds
    @Singleton
    fun bindTokenDataSource(imp : TokenBalanceRepositoryImp) : TokenDataSource


    @Binds
    @Singleton
    fun bindWeb3DataSource(imp : Web3jWalletRepositoryImp) : Web3DataSource

    @Binds
    @Singleton
    fun bindNetworkDataSource(imp : NetworkRepositoryImp) : NetworkDataSource

    @Binds
    @Singleton
    fun bindCmcTokenDataSource(imp : CmcRepositoryImp) : CmcDataSource

    @Binds
    @Singleton
    fun bindAlchemyDataSource(imp : AlchemyRepositoryImp) : AlchemyDataSource
}