package com.repoint.dashboard

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.remote.CmcAllTokens
import com.repoint.models.sharedmodels.remote.CmcStatus
import com.repoint.models.sharedmodels.remote.TokenInfoMetadataResponse
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.remote.TokenQuotesResponse
import com.repoint.models.sharedmodels.ui.ApiException
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.models.sharedmodels.ui.UiState
import com.repoint.sources.datarepo.datasource.CmcDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CmcTokenViewModel @Inject constructor(
    private val repository: CmcDataSource
) : ViewModel() {

    private val pageSize = 12
    private var currentStartIndex = 1
    private val buffer = mutableListOf<CmcAllTokens>()

    private val _tokens = MutableStateFlow<List<CmcTokenEntity>>(emptyList())
    val tokens: StateFlow<List<CmcTokenEntity>> = _tokens.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState =
        MutableStateFlow<UiState<List<CmcTokenEntity>>>(UiState.Success(emptyList()))
    val searchState: StateFlow<UiState<List<CmcTokenEntity>>> = _searchState.asStateFlow()

    /*    private val _activeTokenIds = MutableStateFlow<Set<Int>>(emptySet())
        val activeTokenIds: StateFlow<Set<Int>> = _activeTokenIds.asStateFlow()*/

    private val _isPaging = MutableStateFlow(false)
    val isPaging: StateFlow<Boolean> = _isPaging

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _activeNetworks = MutableStateFlow<LocalActiveNetworks?>(null)


    // 1️⃣ Which master-wallet are we looking at?
    private val _currentWalletId = MutableStateFlow<String?>(null)
    fun setCurrentWallet(walletId: String) {
        _currentWalletId.value = walletId
    }

    // 2️⃣ Stream the DB list of active IDs → as an Immutable Set
    val activeTokenIds: StateFlow<Set<Int>> =
        _currentWalletId
            .filterNotNull()
            .flatMapLatest { wid ->
                repository.getActiveTokenIds(wid)
            }
            .map { it.toSet() }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptySet()
            )


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _isInitialLoaded = MutableStateFlow(false)
    val isInitialLoaded: StateFlow<Boolean> = _isInitialLoaded

    fun loadInitialTokens() {
        viewModelScope.launch {
            currentStartIndex = 1
            buffer.clear()
            _tokens.value = emptyList()
            _isInitialLoaded.value = true
            loadMapPageAndShow()
        }
    }

    suspend fun loadMapPageAndShow() {
        if (_isPaging.value) return
        _isPaging.value = true

        val mapResult = repository.fetchTokenMapPage(currentStartIndex, 100)
        if (mapResult is ApiResult.Success) {
            val newMapTokens = mapResult.data.data
            buffer.addAll(newMapTokens)
            currentStartIndex += 100

            val nextSlice = buffer.take(pageSize)
            val nextIds = nextSlice.map { it.id }


            val metaResult = repository.fetchTokenMetadata(nextIds)
            when (metaResult) {
                is ApiResult.Success -> {
                    val metaMap = metaResult.data.data
                    val entities = nextSlice.mapNotNull { mapItem ->
                        val meta = metaMap[mapItem.id.toString()] ?: return@mapNotNull null
                        mergeMapAndMetadata(mapItem, meta)
                    }

                    Log.d("CmcPaging", " the entities are these : $entities")
                    if (entities.isNotEmpty()) {
                        _tokens.value = _tokens.value + entities
                        buffer.removeAll(nextSlice) // ← only remove on success
                    }
                }

                is ApiResult.Error -> {
                    Log.e("CmcPaging", "Metadata fetch failed: ${metaResult.exception}")
                    _error.value = metaResult.exception.localizedMessage ?: "Unknown metadata error"
                }
            }
        } else if (mapResult is ApiResult.Error) {
            Log.e("CmcPaging", "Map fetch failed: ${mapResult.exception}")
            _error.value = mapResult.exception.localizedMessage ?: "Unknown map error"
        }

        _isPaging.value = false
    }

    fun performSearch(query: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            val mapTokens = repository.searchTokensByQuery(query)
            Log.d("SearchDebug", "DB Results for '$query': ${mapTokens.map { it.name to it.id }}")

            if (mapTokens.isEmpty() || query.isBlank()) {
                _searchState.value = UiState.Success(emptyList())
                return@launch
            }

            val ids = mapTokens.map { it.id }
            val metadata = repository.fetchTokenMetadata(ids)

            if (metadata is ApiResult.Success) {
                val metaMap = metadata.data.data
                Log.d("SearchDebug", "Fetched metadata keys: ${metaMap.keys}")

                val result = mapTokens.mapNotNull {
                    val meta = metadata.data.data[it.id.toString()] ?: return@mapNotNull null
                    mergeEntityAndMetadata(it, meta)
                }
                Log.d("SearchDebug", "Final result count: ${result.size}")

                _searchState.value = UiState.Success(result)
            } else {
                _searchState.value = UiState.Error("Metadata fetch failed")
                Log.e("SearchDebug", "Metadata fetch failed: ${metadata}")
            }
        }
    }


    fun toggleToken(tokenId: Int) = viewModelScope.launch {
        val wid = _currentWalletId.value ?: return@launch

        if (tokenId in activeTokenIds.value) {
            repository.deleteActiveNetworks(tokenId, wid)
            Log.d("ToggleToken","deleted the token in $tokenId & $wid")
        } else {
            repository.insertActiveToken(LocalActiveNetworks(tokenId,wid))
            Log.d("ToggleToken","added the token in $tokenId & $wid")
        }
    }

    /**
     * Emits a new ApiResult<TokenInfoMetadataResponse> whenever
     * the set of active IDs in the DB changes.
     */
    suspend fun getActivatedTokensInfo(
        walletId: String
    ): ApiResult<TokenInfoMetadataResponse> = withContext(Dispatchers.IO) {
        // 1) Read your stored IDs once
        val ids = repository
            .getActiveTokenIds(walletId)   // suspend fun → Flow<List<Int>>
            .first()                       // pull the first (and only) emission

        // 2) If empty, return a dummy‐empty success
        if (ids.isEmpty()) {
            ApiResult.Success(
                TokenInfoMetadataResponse(
                    status = CmcStatus.EMPTY,
                    data   = emptyMap()
                )
            )
        } else {
            // 3) Otherwise do one network call only
            repository.fetchTokenMetadata(ids)
        }
    }

    suspend fun getPricesForActiveTokens(walletId: String) : ApiResult<TokenQuotesResponse> {
        val ids = repository.getActiveTokenIds(walletId = walletId).first()

        if (ids.isEmpty())  return ApiResult.Success(
            TokenQuotesResponse(status = CmcStatus.EMPTY, data = emptyMap())
        )
        return repository.fetchTokenPrices(ids)
    }

}

private fun mergeEntityAndMetadata(
    entity: CmcTokenEntity,
    meta: TokenMetaData
): CmcTokenEntity {
    return entity.copy(
        logo = meta.logo,
        description = meta.description,
        websiteUrl = meta.urls?.website?.firstOrNull(),
        lastUpdated = System.currentTimeMillis()
    )
}


private fun mergeMapAndMetadata(
    map: CmcAllTokens,
    meta: TokenMetaData
): CmcTokenEntity {
    return CmcTokenEntity(
        id = map.id,
        rank = map.rank,
        name = map.name,
        symbol = map.symbol,
        slug = map.slug,
        isActive = map.isActive,
        firstHistoricalData = map.firstHistoricalData,
        lastHistoricalData = map.lastHistoricalData,
        platformId = map.platform?.id,
        platformName = map.platform?.name,
        platformSymbol = map.platform?.symbol,
        platformSlug = map.platform?.slug,
        tokenAddress = map.platform?.tokenAddress,
        logo = meta.logo,
        description = meta.description,
        websiteUrl = meta.urls?.website?.firstOrNull(),
        lastUpdated = System.currentTimeMillis()
    )
}
