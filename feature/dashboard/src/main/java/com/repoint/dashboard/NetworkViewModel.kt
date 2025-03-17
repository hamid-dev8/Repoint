package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.toDomainModel
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.NetworkSummary
import com.repoint.network.util.NetworkApiService
import com.repoint.sources.datarepo.datasource.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val networkApi: NetworkApiService,
    private val repository: NetworkDataSource
) : ViewModel() {

    init {
        Log.d("NetworkViewModel", "NetworkViewModel Created!") // ✅ Add this log
    }

    private val _networks = MutableStateFlow<List<BlockchainNetwork>>(emptyList())
    val networks: StateFlow<List<BlockchainNetwork>?> = _networks.asStateFlow()

    private val _activeNetworks = MutableStateFlow<List<LocalActiveNetworks>>(emptyList())
    val activeNetworks: StateFlow<List<LocalActiveNetworks>> = _activeNetworks.asStateFlow()

    private val _activeTokens = MutableStateFlow<List<TokenEntity>>(emptyList())
    val activeTokens : StateFlow<List<TokenEntity>> = _activeTokens.asStateFlow()

    private val _apiMessage = MutableStateFlow<String?>(null)
    val apiMessage: StateFlow<String?> = _apiMessage

  /*  fun fetchNetworks() {
        viewModelScope.launch {
            val networkWithTokens = repository.getNetworkWithTokens()


            if (repository.isDatabaseEmpty()) {
                Log.d("NetworkViewModel", "Database empty! Fetching from Api")


                if (_networks.value.isNotEmpty()) { // ✅ Prevent unnecessary calls
                    Log.d("NetworkViewModel", "Skipping fetchNetworks(), data already exists")
                    return@launch
                }
                try {
                    Log.d("NetworkViewModel", "Fetching networks...")

                    val response = networkApi.getNetworkSummary()
                    Log.d("NetworkViewModel", "Response received: $response")

                    val blockChainEntities = response.result.map { network ->
                        BlockchainNetworkEntity(
                            id = network.id,
                            name = network.name,
                            chainId = network.chainId,
                            rpcUrl = network.rpcUrl,
                            explorerUrl = network.explorerUrl,
                            nativeToken = network.nativeToken,
                            dexRouter = network.dexRouter,
                        )
                    }

                    // 🔹 Prevent Overwriting Tokens
                    val existingNetworks = _networks.value.associateBy { it.id }
                    val updatedNetworks = response.result.map { network ->
                        existingNetworks[network.id]?.copy(tokens = network.tokens) ?: network
                    }

                    _networks.value = updatedNetworks
                    Log.d(
                        "NetworkViewModel",
                        "Inserting ${blockChainEntities.size} networks into DB"
                    )
                    repository.insertNetworks(blockChainEntities)

                    //fetch from local db & update StateFlow
                } catch (e: Exception) {
                    //TODO ADD API ERROR
                    e.printStackTrace()
                    Log.e("NetworkViewModel", "Error fetching networks: ${e.localizedMessage}", e)
                    getLocalNetworks()
                }
            } else {
                Log.d("NetworkViewModel", "Loading networks from database.")
                getLocalNetworks()
            }
        }
    }*/


    fun fetchNetworks() {
        viewModelScope.launch {
           // if (repository.isDatabaseEmpty()) {
                Log.d("NetworkViewModel", "Database empty! Fetching from API")

                if (_networks.value.isNotEmpty()) {
                    Log.d("NetworkViewModel", "Skipping fetchNetworks(), data already exists")
                    return@launch
                }

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
                                networkId = network.id // Foreign key to BlockchainNetworkEntity
                            )
                        }
                    }

                    // Store networks & tokens
                    repository.insertNetworks(blockChainEntities)
                    repository.insertToken(tokenEntities) // ✅ Insert tokens into database

                    Log.d("NetworkViewModel", "Inserted networks & tokens into DB")

                    getLocalNetworks() // Load from DB after inserting
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("NetworkViewModel", "Error fetching networks: ${e.localizedMessage}", e)
                    getLocalNetworks()
                }
            } /*else {
                Log.d("NetworkViewModel", "Loading networks from database.")
                getLocalNetworks()
            }*/
        }


    private fun getLocalNetworks() {
        viewModelScope.launch {
            val storedNetworks = repository.getAllNetworks()
            Log.d("NetworkViewModel", "Stored networks in DB: $storedNetworks")

        /*    // 🔹 Merge Local Data With Existing Networks
            val existingNetworks = _networks.value.associateBy { it.id }
            val mergedNetworks = storedNetworks.map { it.toDomainModel() }.map { network ->
                existingNetworks[network.id]?.copy(tokens = network.tokens) ?: network
            }
*/
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
            val networkExists = repository.getNetworkById(activeNetwork.networkId)
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


    private fun fetchActiveNetworks() {
        viewModelScope.launch {
            _activeNetworks.value = repository.getActiveNetworks()
        }
    }

    fun fetchActiveTokens(tokenId: Int) {
        viewModelScope.launch {
            _activeTokens.value = repository.getActiveTokens(tokenId)
        }
    }

    fun toggleActiveNetwork(tokenId : Int,isActive : Boolean){
        viewModelScope.launch {
            if (isActive){
                repository.insertActiveNetwork(LocalActiveNetworks(networkId = tokenId))
            }
            else{
                repository.deleteActiveNetwork(tokenId)
            }
            fetchActiveNetworks()
        }
    }

    fun deleteActiveNetwork(networkId: Int) {
        viewModelScope.launch {
            val networkExists = repository.getNetworkById(networkId)
            if (networkExists != null) {
                repository.deleteActiveNetwork(networkId)

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







