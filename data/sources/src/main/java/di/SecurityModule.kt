package di

import com.repoint.sources.datarepo.security.SecureKeyStore
import com.repoint.sources.datarepo.security.impl.NoOpSecureKeyStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SecurityModule {

    @Binds
    @Singleton
    fun bindSecureKeyStore(imp : NoOpSecureKeyStore) : SecureKeyStore

}