package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.toDomainModel
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.local.TokenWithNetwork
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.NetworkSummary
import com.repoint.network.util.NetworkApiService
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val networkApi: NetworkApiService,
    private val repository: NetworkDataSource
) : ViewModel() {



    private val _networks = MutableStateFlow<List<BlockchainNetwork>>(emptyList())
    val networks: StateFlow<List<BlockchainNetwork>?> = _networks.asStateFlow()

    private val _activeNetworks = MutableStateFlow<List<LocalActiveNetworks>>(emptyList())
    val activeNetworks: StateFlow<List<LocalActiveNetworks>> = _activeNetworks.asStateFlow()

    private val _specificNetworkByToken = MutableStateFlow<BlockchainNetworkEntity?>(null)
    val specificNetworkByToken : StateFlow<BlockchainNetworkEntity?> = _specificNetworkByToken.asStateFlow()


    private val _activeTokens = MutableStateFlow<List<TokenEntity>>(emptyList())
    val activeTokens : StateFlow<List<TokenEntity>> = _activeTokens.asStateFlow()

    private val _tokenWithNetworks = MutableStateFlow<TokenWithNetwork?>(null)
    val tokenWithNetworks: StateFlow<TokenWithNetwork?> = _tokenWithNetworks.asStateFlow()

    init {
        Log.d("NetworkViewModel", "NetworkViewModel Created!") // ✅ Add this log
    }

    fun initializeWithWallet(walletId : String){
        fetchActiveTokens(walletId)
        fetchNetworks(walletId)
    }


    private fun fetchNetworks(masterWalletId: String) {
        viewModelScope.launch {



                try {
                    Log.d("NetworkViewModel", "Fetching networks...")

                    val response = networkApi.getNetworkSummary()
                    Log.d("NetworkViewModel", "Response received: $response")

                    // Convert networks
                    val blockChainEntities = response.result.map { network ->
                        BlockchainNetworkEntity(
                            id = network.id,
                            name = network.name,
                            chainId = network.chainId,
                            rpcUrl = network.rpcUrl,
                            explorerUrl = network.explorerUrl,
                            nativeToken = network.nativeToken,
                            dexRouter = network.dexRouter,
                            coinType = network.coinType
                        )
                    }

                    // Convert tokens
                    val tokenEntities = response.result.flatMap { network ->
                        network.tokens.map { token ->
                            TokenEntity(
                                tokenId = token.tokenId,
                                name = token.name,
                                symbol = token.symbol,
                                contractAddress = token.contractAddress,
                                decimals = token.decimals,
                                logoUrl = token.logoUrl,
                                networkId = network.id, // Foreign key to BlockchainNetworkEntity
                                masterWalletId = masterWalletId
                            )
                        }
                    }

                    // Store networks & tokens
                    repository.insertNetworks(blockChainEntities)
                    repository.insertToken(tokenEntities) // ✅ Insert tokens into database
                    repository.debugActiveNetworks(masterWalletId)

                    Log.d("insert", "Inserted ${blockChainEntities.size} networks")
                    Log.d("insert", "Inserted ${tokenEntities.size} tokens")

                    blockChainEntities.find { it.name.contains("Polygon", ignoreCase = true) }?.let {
                        Log.d("insert", "Polygon network found: $it")
                    }
                    Log.d("NetworkViewModel", "Inserted networks & tokens into DB")

                    getLocalNetworks() // Load from DB after inserting
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("NetworkViewModel", "Error fetching networks: ${e.localizedMessage}", e)
                    getLocalNetworks()
                }
        }
    }


    private fun getLocalNetworks() {
        viewModelScope.launch {
            val storedNetworks = repository.getAllNetworks()
            Log.d("NetworkViewModel", "Stored networks in DB: $storedNetworks")
            storedNetworks.forEach {
                Log.d("net-debug", "Network ${it.name} ID ${it.id}")
                val tokens = repository.getTokensForNetwork(it.id)
                Log.d("net-debug", "${it.name} tokens: $tokens")
            }
            val mergedNetworks = storedNetworks.map { networkEntity ->
                val tokens = repository.getTokensForNetwork(networkEntity.id) // 🔹 Fetch tokens per network
                networkEntity.toDomainModel(tokens)
            }
            _networks.value = mergedNetworks
            Log.d("NetworkViewModel", "Merged networks: $_networks")
        }
    }

    fun insertActiveNetwork(activeNetwork: LocalActiveNetworks) {
        viewModelScope.launch {
            val networkExists = repository.getNetworkById(activeNetwork.tokenId)
            if (networkExists != null) {
                repository.insertActiveNetwork(activeNetwork)
                Log.d("NetworkViewModel", "Inserted active network: $activeNetwork")
            } else {
                Log.e(
                    "NetworkViewModel",
                    "Cannot insert active network: Parent network does not exist!"
                )
            }
        }
    }

    suspend fun getNetworkById(id: Int): BlockchainNetworkEntity? {
        return repository.getNetworkById(id)
    }

     fun getNetworkForToken(tokenId : Int)
    {
         viewModelScope.launch {
             _specificNetworkByToken.value = repository.getNetworkByTokenId(tokenId)
         }
    }
    fun fetchTokensWithNetwork(tokenId : Int) {
        viewModelScope.launch {
            _tokenWithNetworks.value = repository.getTokensWithNetwork(tokenId)
            Log.d("NetworkViewModel", "Fetched tokens with networks: ${_tokenWithNetworks.value}")
        }
    }

    suspend fun observeActiveTokens(walletId: String): StateFlow<List<TokenEntity>> {
        return repository.getActiveTokens(walletId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }


    /*
        private fun fetchActiveNetworks() {
            viewModelScope.launch {
                _activeNetworks.value = repository.getActiveNetworks(walletId = )
            }
        }
    */

    fun fetchActiveTokens(masterWalletId: String) {
        viewModelScope.launch {
            repository.getActiveTokens(masterWalletId)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                .collect {
                    _activeTokens.value = it
                }
        }
    }




    fun toggleActiveNetwork(tokenId : Int,isActive : Boolean,masterWalletId: String){
        viewModelScope.launch {
            if (isActive){
                Log.d("DEBUG", "Inserting token $tokenId for wallet $masterWalletId")
                repository.insertActiveNetwork(LocalActiveNetworks(tokenId, masterWalletId))
                val all = repository.debugActiveNetworks(masterWalletId)
                Log.d("DEBUG", "All actives now:\n" + all.joinToString("\n"))
            }
            else{
                repository.deleteActiveNetwork(tokenId,masterWalletId)
            }
            //fetchActiveNetworks()
            fetchActiveTokens(masterWalletId)
           // _activeTokens.value = repository.getActiveTokens()
            Log.d("toggle", "${if (isActive) "Added" else "Removed"} token $tokenId for wallet $masterWalletId")
        }
    }

    fun deleteActiveNetwork(networkId: Int,masterWalletId: String) {
        viewModelScope.launch {
            val networkExists = repository.getNetworkById(networkId)
            if (networkExists != null) {
                repository.deleteActiveNetwork(networkId, masterWalletId = masterWalletId)

                Log.d("NetworkViewModel", "Deleted active network: $networkId")
            } else {
                Log.e(
                    "NetworkViewModel",
                    "Cannot delete active network: Parent network does not exist!"
                )
            }
        }

    }

}







