package com.repoint.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.database.dao.NetworkDao
import com.repoint.models.sharedmodels.local.BlockchainNetworkEntity
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.NetworkSummary
import com.repoint.network.util.NetworkApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val networkApi : NetworkApiService,
    private val networkDao : NetworkDao
) : ViewModel() {

    private val _networks = MutableStateFlow<List<BlockchainNetwork>?>(null)
    val networks: StateFlow<List<BlockchainNetwork>?> = _networks



    private val _apiMessage = MutableStateFlow<String?>(null)
    val apiMessage: StateFlow<String?> = _apiMessage

    fun fetchNetworks() {
        viewModelScope.launch {
            try {
                val response = networkApi.getNetworkSummary()

                val blockChainEntities = response.result.map { network ->
                    BlockchainNetworkEntity(
                        id = network.id,
                        name = network.name,
                        chainId = network.chainId,
                        rpcUrl = network.rpcUrl,
                        explorerUrl = network.explorerUrl,
                        nativeToken = network.nativeToken,
                        dexRouter = network.dexRouter
                    )
                }
                networkDao.insertNetworks(blockChainEntities)

                //fetch from local db & update StateFlow
                val storedNetworks = networkDao.getAllNetworks()
                _networks.value = storedNetworks.map {
                    it.toDomainModel() //convert DB to Domain Model
                }
            }
            catch (e : Exception) {
                //TODO ADD API ERROR
                e.printStackTrace()
            }
        }

    }
    fun getLocalNetworks() {
        viewModelScope.launch {
            val storedNetworks = networkDao.getAllNetworks()
            _networks.value = storedNetworks.map { it.toDomainModel() } // ✅ Convert DB to domain model
        }
    }
}



fun BlockchainNetworkEntity.toDomainModel(): BlockchainNetwork {
    return BlockchainNetwork(
        id = this.id,
        name = this.name,
        chainId = this.chainId,
        rpcUrl = this.rpcUrl,
        explorerUrl = this.explorerUrl,
        nativeToken = this.nativeToken,
        dexRouter = this.dexRouter,
        tokens = emptyList() // Tokens are fetched separately
    )
}