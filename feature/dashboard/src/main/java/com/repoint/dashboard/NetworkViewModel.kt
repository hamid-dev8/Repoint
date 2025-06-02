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
import com.repoint.models.sharedmodels.remote.Token
import com.repoint.models.sharedmodels.remote.moralisChainMap
import com.repoint.models.sharedmodels.ui.UiState
import com.repoint.network.util.NetworkApiService
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val specificNetworkByToken: StateFlow<BlockchainNetworkEntity?> =
        _specificNetworkByToken.asStateFlow()

    private val _specificNetworkForToken = MutableStateFlow<BlockchainNetworkEntity?>(null)
    val specificNetworkForToken: StateFlow<BlockchainNetworkEntity?> =
        _specificNetworkForToken.asStateFlow()


    private val _activeTokens = MutableStateFlow<List<TokenEntity>>(emptyList())
    val activeTokens: StateFlow<List<TokenEntity>> = _activeTokens.asStateFlow()

    private val _tokenWithNetworks = MutableStateFlow<TokenWithNetwork?>(null)
    val tokenWithNetworks: StateFlow<TokenWithNetwork?> = _tokenWithNetworks.asStateFlow()

    val _uiState = MutableStateFlow<UiState<List<TokenEntity>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()


    val _uiStateNetworkBlockChain =
        MutableStateFlow<UiState<List<BlockchainNetwork>>>(UiState.Loading)
    val uiStateNetworkBlockChain = _uiState.asStateFlow()

    init {
        Log.d("NetworkViewModel", "NetworkViewModel Created!") // ✅ Add this log
    }

    fun initializeWithWallet(walletId: String) {
        Log.d("init", "initialize called again")
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                fetchNetworks(walletId)
                fetchActiveTokens(walletId)
                _uiState.value = UiState.Success(_activeTokens.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown Error")
                Log.d("init", "errored init called again")

            }
        }
    }

    suspend fun getEnabledMoralisChains(walletId: String): List<String> {
        val fullMoralisChainMap: Map<Int, String> = mapOf(
            1 to "eth",
            56 to "bsc",
            137 to "polygon",
            43114 to "avalanche",
            250 to "fantom",
            42161 to "arbitrum",
            10 to "optimism"
        )


        val active = repository.getActiveNetworks(walletId)
        val allTokens = repository.getAllTokens(walletId)
        val allNetworks = repository.getAllNetworks()

        // Match tokenIds to actual network entities
        val enabledChainIds = active.mapNotNull { activeToken ->
            val token = allTokens.find { it.tokenId == activeToken.tokenId }
            val network = token?.networkId.let { id ->
                allNetworks.find { it.id == id } }
            network?.chainId
        }

        Log.d("chains", " enabled chain id is : $enabledChainIds")

        return enabledChainIds.distinct().mapNotNull { chainId -> fullMoralisChainMap[chainId] }
    }


    private suspend fun fetchNetworks(masterWalletId: String) {
        Log.d("init", "fetch networks called")
        _uiState.value = UiState.Loading
        try {
            Log.d("NetworkViewModel", "Fetching networks...")

            val response = networkApi.getNetworkSummary()
            Log.d("NetworkViewModel", "Response received: $response")

            // Convert networks
            val blockChainEntities = response.result.map { network ->
                Log.d("network-save", "Saving: ${network.name} with chainId: ${network.chainId}")

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
            _uiState.value = UiState.Success(tokenEntities)
            Log.d("insert", "Inserted ${blockChainEntities.size} networks")
            Log.d("insert", "Inserted ${tokenEntities.size} tokens")
            blockChainEntities.find { it.name.contains("Polygon", ignoreCase = true) }?.let {
                Log.d("insert", "Polygon network found: $it")
            }
            Log.d("NetworkViewModel", "Inserted networks & tokens into DB")

            getLocalNetworks() // Load from DB after inserting
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = UiState.Error("Error fetching networks")
            Log.e("NetworkViewModel", "Error fetching networks: ${e.localizedMessage}", e)
            getLocalNetworks()
        }

    }


    private fun getLocalNetworks() {
        _uiStateNetworkBlockChain.value = UiState.Loading
        try {
            viewModelScope.launch {
                val storedNetworks = repository.getAllNetworks()
                Log.d("NetworkViewModel", "Stored networks in DB: $storedNetworks")
                storedNetworks.forEach {
                    Log.d("net-debug", "Network ${it.name} ID ${it.id}")
                    val tokens = repository.getTokensForNetwork(it.id)
                    Log.d("net-debug", "${it.name} tokens: $tokens")
                }
                val mergedNetworks = storedNetworks.map { networkEntity ->
                    val tokens =
                        repository.getTokensForNetwork(networkEntity.id) // 🔹 Fetch tokens per network
                    networkEntity.toDomainModel(tokens)
                }
                _networks.value = mergedNetworks
                _uiStateNetworkBlockChain.value = UiState.Success(mergedNetworks)
                Log.d("NetworkViewModel", "Merged networks: $_networks")
            }
        } catch (e: Exception) {
            _uiStateNetworkBlockChain.value = UiState.Error("$e + fetching from database")
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
        val networkById = repository.getNetworkById(id)
        _specificNetworkByToken.value = networkById
        return networkById
    }

    fun getNetworkForToken(tokenId: Int) {
        viewModelScope.launch {
            _specificNetworkForToken.value = repository.getNetworkByTokenId(tokenId)
        }
    }

    fun fetchTokensWithNetwork(tokenId: Int) {
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

    suspend fun fetchActiveTokens(masterWalletId: String) {
        Log.d("init", "fetch active tokens called")
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                repository.getActiveTokens(masterWalletId)
                    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
                    .collect {
                        _activeTokens.value = it
                        _uiState.value = UiState.Success(it)
                    }
            } catch (e: Exception) {
                _uiState.value =
                    UiState.Error(e.localizedMessage.toString() + " something went wrong")
            }
        }


    }

    fun updateTokenInNetwork(updatedToken: Token, networkId: Int) {
        val currentNetworks = _networks.value
        val updatedNetworks = currentNetworks.map { blockchain ->
            if (blockchain.id == networkId) {
                blockchain.copy(tokens = blockchain.tokens.map {
                    if (it.contractAddress == updatedToken.contractAddress) updatedToken else it
                })
            } else blockchain
        }
        _networks.value = updatedNetworks
    }

    fun toggleActiveNetwork(
        tokenId: Int,
        isActive: Boolean,
        masterWalletId: String,
        updatedToken: Token,
        networkId: Int
    ) {
        viewModelScope.launch {
            if (isActive) {
                Log.d("DEBUG", "Inserting token $tokenId for wallet $masterWalletId")
                repository.insertActiveNetwork(LocalActiveNetworks(tokenId, masterWalletId))
                val all = repository.debugActiveNetworks(masterWalletId)
                Log.d("DEBUG", "All actives now:\n" + all.joinToString("\n"))
            } else {
                repository.deleteActiveNetwork(tokenId, masterWalletId)
            }
            //fetchActiveNetworks()
            //fetchActiveTokens(masterWalletId)
            // _activeTokens.value = repository.getActiveTokens()
            updateTokenInNetwork(updatedToken, networkId)
            Log.d(
                "toggle",
                "${if (isActive) "Added" else "Removed"} token $tokenId for wallet $masterWalletId"
            )
        }
    }

    fun deleteActiveNetwork(networkId: Int, masterWalletId: String) {
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







