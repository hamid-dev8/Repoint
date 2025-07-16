package com.repoint.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.basics.logic.mapPlatformToAlchemyChain
import com.repoint.dashboard.ui.getDecimals
import com.repoint.models.sharedmodels.local.ActiveTokenKey
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.local.MatchedTokenMeta
import com.repoint.models.sharedmodels.local.ResolvedTokenInstance
import com.repoint.models.sharedmodels.local.TokenPerChainUiModel
import com.repoint.models.sharedmodels.remote.CmcAllTokens
import com.repoint.models.sharedmodels.remote.CmcStatus
import com.repoint.models.sharedmodels.remote.TokenInfoMetadataResponse
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.remote.TokenQuotesResponse
import com.repoint.models.sharedmodels.remote.normalizeSlug
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.models.sharedmodels.ui.UiState
import com.repoint.network.util.resolveChainFromPlatform
import com.repoint.sources.datarepo.datasource.CmcDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CmcTokenViewModel @Inject constructor(
    private val repository: CmcDataSource
) : ViewModel() {

    private val pageSize = 12
    private var currentStartIndex = 1
    private var currentPage = 0

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

    private val _activeTokenEntities = MutableStateFlow<List<LocalActiveNetworks>>(emptyList())
    val activeTokenEntities: StateFlow<List<LocalActiveNetworks>> = _activeTokenEntities.asStateFlow()


    private val _perChainTokens = MutableStateFlow<List<TokenPerChainUiModel>>(emptyList())
    val perChainTokens: StateFlow<List<TokenPerChainUiModel>> = _perChainTokens.asStateFlow()

    private val _infoResult = MutableStateFlow<ApiResult<TokenInfoMetadataResponse>>(
        ApiResult.Success(
            TokenInfoMetadataResponse(
                status = CmcStatus.EMPTY,
                data = emptyMap()
            )
        )
    )
    val infoResult: StateFlow<ApiResult<TokenInfoMetadataResponse>> = _infoResult.asStateFlow()


    // 1️⃣ Which master-wallet are we looking at?
    private val _currentWalletId = MutableStateFlow<String?>(null)
    fun setCurrentWallet(walletId: String) {
        _currentWalletId.value = walletId
        Log.d("wallety","the current wallet id : $_currentWalletId and the actual wallet id is setting : $walletId")
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

    private val _activeTokenKeys = MutableStateFlow<Set<ActiveTokenKey>>(emptySet())
    val activeTokenKeys: StateFlow<Set<ActiveTokenKey>> = _activeTokenKeys.asStateFlow()


    val activeTokenKeyFlow: StateFlow<Set<ActiveTokenKey>> =
        _currentWalletId
            .filterNotNull()
            .flatMapLatest { wid ->
              repository.getActiveTokenKeys(wid)
            }
            .map {
                Log.d("activeToken","active token key is : $it")
                it.toSet() }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptySet()
            )

    init {
        viewModelScope.launch {
            _currentWalletId
                .filterNotNull()
                .flatMapLatest { walletId ->
                    repository.getActiveTokenEntities(walletId)
                }
                .collect { actives ->
                    _activeTokenEntities.value = actives
                }
        }

        viewModelScope.launch {
            activeTokenIds.collect { ids ->
                val result = if (ids.isEmpty()) {
                    ApiResult.Success(
                        TokenInfoMetadataResponse(
                            status = CmcStatus.EMPTY,
                            data = emptyMap()
                        )
                    )
                } else {
                    repository.fetchTokenMetadata(ids.toList())
                }
                Log.d("ViewModel", "Fetched info result: $result")
                _infoResult.value = result
            }
        }
    }

    val tokenMetasFlow: StateFlow<List<TokenMetaData>> = _infoResult
        .map { result ->
            when (result) {
                is ApiResult.Success -> result.data.data.values.toList()
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    val filteredMetas: StateFlow<List<ResolvedTokenInstance>> = combine(
        tokenMetasFlow,
        activeTokenKeyFlow
    ) { metas, keys ->
        Log.d("flowDebug","metas and keys are $metas & $keys")
        getFilteredMetas(metas, keys)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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
            //loadMapPageAndShow()
        }
    }

    fun getTokenMetaFlow(tokenId: Int): Flow<TokenMetaData?> {
        return tokenMetasFlow
            .onEach { Log.d("SlugMatch", "🔍 Searching tokenId=$tokenId in ${it.map { t -> t.id }}") }
            .map { list -> list.firstOrNull { it.id == tokenId } }
    }

    fun loadTokenMetaById(tokenId: Int) {
        viewModelScope.launch {
            val result = repository.fetchTokenMetadata(ids = listOf(tokenId))
            if (result is ApiResult.Success) {
                _infoResult.value = result
            }
        }
    }

    fun getFilteredMetas(
        allMetas: List<TokenMetaData>,
        activeKeys: Set<ActiveTokenKey>
    ): List<ResolvedTokenInstance> {
        return allMetas.flatMap { meta ->
            meta.contractAddress.mapNotNull { contract ->
                val slug = normalizeSlug(contract.platform.coin.slug)
                val key = ActiveTokenKey(meta.id, slug)

                if (key in activeKeys) {
                    ResolvedTokenInstance(
                        tokenId = meta.id,
                        symbol = meta.symbol,
                        name = meta.name,
                        logo = meta.logo,
                        contractAddress = contract.contractAddress,
                        chain = slug,
                        decimals = getDecimals(meta),
                        tokenMeta = meta
                    )
                } else null
            }
        }
    }



    suspend fun loadMapPageAndShow() {
        if (_isPaging.value) return
        _isPaging.value = true

        val mapResult = repository.fetchTokenMapPageByLimit(currentStartIndex, 100)
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

                   /*     //only include EVM-compatible tokens
                        val allowedChains = setOf("ethereum" , "bnb" , "polygon" , "arbitrum" , "optimism" , "avalanche")
                        if (merged.platformSlug != null && merged.tokenAddress != null && merged.platformSlug in allowedChains)
                            merged
                        else null*/
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

    fun loadNextDbPage() {
        viewModelScope.launch {
            val page = currentPage++
            val pageSize = 12

            val tokens = withContext(Dispatchers.IO) {
                repository.getDbTokensPaged(page * pageSize, pageSize) // You must implement this in DAO
            }

            val ids = tokens.map { it.id }

            val metaDataResult = repository.fetchTokenMetadata(ids)
            if (metaDataResult !is ApiResult.Success){
                Log.e("PerChainTokens","Meta data failed")
                return@launch
            }

            val metaMap = metaDataResult.data.data
            val perChain = toPerChainUiModels(metaMap)
            _perChainTokens.update { it + perChain }

            Log.d("PerChainTokens", "Loaded page with ${perChain.size} per-chain tokens")
        }
    }


    /*    fun fetchAllTokenSortedInBackground(sort: String = "cmc_rank", onDone: (List<CmcAllTokens>) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                val mapResult = repository.fetchTokenMapBySort(sort)

                if (mapResult is ApiResult.Success) {
                    Log.d("SortedTokens", "Fetched ${mapResult.data.data.size} tokens sorted by $sort")
                    onDone(mapResult.data.data)
                } else if (mapResult is ApiResult.Error) {
                    Log.e("SortedTokens", "Failed to fetch sorted tokens: ${mapResult.exception}")
                    onDone(emptyList())
                }
            }
        }*/

    suspend fun syncTopTokensToDb(): Boolean {
        val tokenCount = repository.getTokenCount()
        val lastUpdateTime = repository.getLastUpdatedTime() ?: 0L
        val now = System.currentTimeMillis()

        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        val needsRefresh = tokenCount == 0 || (now - lastUpdateTime) > threeDaysMillis

        if (!needsRefresh) {
            Log.d("CMC_SYNC", "Skipping sync — cache is fresh")
            return true
        }

        val mapResult = repository.fetchTokenMapBySort("cmc_rank")
        if (mapResult !is ApiResult.Success) {
            Log.e("CMC_SYNC", "Failed to fetch map")
            return false
        }

        val mapTokens = mapResult.data.data
        val mapById = mapTokens.associateBy { it.id }
        val ids = mapTokens.map { it.id }

        val allEntities = mutableListOf<CmcTokenEntity>()
        ids.chunked(100).forEach { chunk ->
            when (val infoResult = repository.fetchTokenMetadata(chunk)) {
                is ApiResult.Success -> {
                    val metaMap = infoResult.data.data
                    chunk.forEach { id ->
                        val mapItem = mapById[id] ?: return@forEach
                        val meta = metaMap[id.toString()] ?: return@forEach
                        val entity = mergeMapAndMetadata(mapItem, meta)
                        allEntities.add(entity)
                    }
                }
                is ApiResult.Error -> {
                    Log.e("CMC_SYNC", "Metadata error: ${infoResult.exception}")
                }
            }
        }

        repository.insertAllTokens(allEntities)
        Log.d("CMC_SYNC", "DB updated with ${allEntities.size} tokens")
        return true
    }



    private suspend fun searchTokenInLoadedMap(query: String): List<CmcTokenEntity> {
        if (query.isBlank()) return emptyList()

        return repository.searchTokensByQuery(query)
    }

    //search
    private suspend fun fetchMetadataAndEmit(mapTokens: List<CmcTokenEntity>) {
        val ids = mapTokens.map { it.id }
        val metadata = repository.fetchTokenMetadata(ids)

        if (metadata is ApiResult.Success) {
            val metaMap = metadata.data.data
            Log.d("SearchDebug", "Fetched metadata keys: ${metaMap.keys}")

            val allowedChains = setOf("ethereum" , "bnb" , "polygon" , "arbitrum" ,"optimism","avalanche")

            val result = mapTokens.mapNotNull {
                val meta = metaMap[it.id.toString()] ?: return@mapNotNull null
                mergeEntityAndMetadata(it, meta)
/*
                if (merged.platformSlug != null && merged.tokenAddress != null && merged.platformSlug in allowedChains)
                    merged
                else null*/
            }
            Log.d("SearchDebug", "Final result count: ${result.size}")
            _searchState.value = UiState.Success(result)
        } else {
            _searchState.value = UiState.Error("Metadata fetch failed")
            Log.e("SearchDebug", "Metadata fetch failed: ${metadata}")
        }
    }

    fun performSearch(query: String) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading

            val trimmed = query.trim().lowercase()
            if (trimmed.isBlank()) {
                _searchState.value = UiState.Success(emptyList())
                return@launch
            }

            // Step 1: Search token map in DB
            val results = withContext(Dispatchers.IO) {
                repository.searchTokensByQuery(trimmed)
            }

            val ids = results.map { it.id }

            // Step 2: Fetch metadata for search matches
            val metaResult = repository.fetchTokenMetadata(ids)
            val metaMap = if (metaResult is ApiResult.Success) {
                metaResult.data.data
            } else {
                emptyMap()
            }

            // Step 3: Try slug match (additional)
            val slugMeta = when (val slugResult = repository.fetchTokenMetadataBySlug(trimmed)) {
                is ApiResult.Success -> slugResult.data.data
                else -> emptyMap()
            }

            // Merge slug + db metas
            val combinedMeta = (metaMap + slugMeta).values.associateBy { it.id.toString() }

            // Step 4: Expand into per-chain UI models
            val perChainList = toPerChainUiModels(combinedMeta)

            // Step 5: Prioritize best matches (exact symbol or name)
            val sorted = perChainList.sortedWith(
                compareByDescending<TokenPerChainUiModel> {
                    it.symbol.equals(query, ignoreCase = true)
                }.thenBy {
                    it.name.lowercase().contains(query.lowercase())
                            || it.symbol.lowercase().contains(query.lowercase())
                }
            )

            _perChainTokens.value = sorted
            _searchState.value = UiState.Success(results)

            Log.d("SearchDebug", "Final matches: ${sorted.map { it.symbol + " on " + it.chainDisplayName }}")
        }
    }




    fun expandMetaInstances(
        metas: List<TokenMetaData>,
        activeKeys: Set<ActiveTokenKey>
    ): List<ResolvedTokenInstance> {
        return metas.flatMap { meta ->
            meta.contractAddress.mapNotNull { contract ->
                val chain = mapPlatformToAlchemyChain(contract)?.name?.lowercase() ?: return@mapNotNull null
                val key = ActiveTokenKey(meta.id, chain)
                if (key in activeKeys) {
                    ResolvedTokenInstance(
                        tokenId = meta.id,
                        symbol = meta.symbol,
                        name = meta.name,
                        logo = meta.logo,
                        contractAddress = contract.contractAddress,
                        chain = chain,
                        decimals = getDecimals(meta),
                        tokenMeta = meta
                    )
                } else null
            }
        }
    }

    fun toPerChainUiModels(metaMap: Map<String, TokenMetaData>): List<TokenPerChainUiModel> {
        return metaMap.values.flatMap { meta ->
            meta.contractAddress.mapNotNull { contract ->
                val chain = resolveChainFromPlatform(contract.platform.name)
                    ?: resolveChainFromPlatform(contract.platform.coin.slug)
                    ?: AlchemyChain.entries.find {
                        it.name.equals(contract.platform.coin.slug, ignoreCase = true)
                    }
                    ?: return@mapNotNull null
                TokenPerChainUiModel(
                    tokenId = meta.id,
                    name = meta.name,
                    symbol = meta.symbol,
                    logo = meta.logo,
                    description = meta.description,
                    contractAddress = contract.contractAddress,
                    chainName = chain,
                    chainDisplayName = contract.platform.name
                )
            }
        }
    }


    suspend fun fetchMetadata(ids: List<Int>): ApiResult<TokenInfoMetadataResponse> {
        return repository.fetchTokenMetadata(ids)
    }
    private suspend fun fetchMetadataAndEmitFromMapTokens(tokens: List<CmcAllTokens>) {
        val ids = tokens.map { it.id }
        val metadata = repository.fetchTokenMetadata(ids)

        if (metadata is ApiResult.Success) {
            val metaMap = metadata.data.data
            Log.d("SearchDebug", "Fetched metadata keys (fallback): ${metaMap.keys}")

            val result = tokens.mapNotNull { mapToken ->
                val meta = metaMap[mapToken.id.toString()] ?: return@mapNotNull null

                // Convert CmcAllTokens -> CmcTokenEntity (temporary/synthetic)
                val syntheticEntity = CmcTokenEntity(
                    id = mapToken.id,
                    rank = mapToken.rank,
                    name = mapToken.name,
                    symbol = mapToken.symbol,
                    slug = mapToken.slug,
                    isActive = mapToken.isActive,
                    firstHistoricalData = mapToken.firstHistoricalData,
                    lastHistoricalData = mapToken.lastHistoricalData,
                    platformId = mapToken.platform?.id,
                    platformName = mapToken.platform?.name,
                    platformSymbol = mapToken.platform?.symbol,
                    platformSlug = mapToken.platform?.slug,
                    tokenAddress = mapToken.platform?.tokenAddress,
                    logo = null, // will be filled from metadata
                    description = null,
                    websiteUrl = null,
                    lastUpdated = System.currentTimeMillis()
                )

                mergeEntityAndMetadata(syntheticEntity, meta)
            }

            _searchState.value = UiState.Success(result)
            Log.d("SearchDebug", "Fallback final result count: ${result.size}")
        } else {
            _searchState.value = UiState.Error("Metadata fetch failed (fallback)")
            Log.e("SearchDebug", "Metadata fetch failed (fallback): $metadata")
        }
    }


    private suspend fun searchInGlobalMapBySymbol(symbol : String): CmcAllTokens? {

        val result = repository.fetchTokenMapBySymbol(symbol)
        return if (result is ApiResult.Success) {
            result.data.firstOrNull() // return first match
        } else {
            Log.e("SearchDebug", "Symbol-based global map search failed: $result")
            null
        }
    }





    fun toggleToken(tokenId: Int,tokenAddress : String,chain : AlchemyChain) = viewModelScope.launch {
        val wid = _currentWalletId.value ?: return@launch
        val chainName = chain.name.lowercase(Locale.US)
        val key = ActiveTokenKey(tokenId, chainName)

        Log.d("ToggleToken", "Current active keys: ${activeTokenKeyFlow.value}")
        Log.d("ToggleToken", "Toggling key: $key")

        if (key in activeTokenKeyFlow.value) {
            repository.deleteActiveNetworks(tokenId, wid , chainName)
            Log.d("ToggleToken","deleted the token in $tokenId & $wid")
        } else {
            repository.insertActiveToken(LocalActiveNetworks(tokenId,tokenAddress,wid,chainName))
            Log.d("ToggleToken", "adding token: id=$tokenId, address=$tokenAddress, wid=$wid , chain name : $chainName")
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



    suspend fun getPricesForActiveTokens(walletId: String): ApiResult<TokenQuotesResponse> {
        val ids = repository.getActiveTokenIds(walletId = walletId).first()
        Log.d("PriceFetch", "Active token IDs for wallet [$walletId]: $ids")

        if (ids.isEmpty()) {
            Log.d("PriceFetch", "No active tokens — returning empty result.")
            return ApiResult.Success(
                TokenQuotesResponse(status = CmcStatus.EMPTY, data = emptyMap())
            )
        }

        val result = repository.fetchTokenPrices(ids)
        if (result !is ApiResult.Success) {
            Log.e("PriceFetch", "Failed to fetch prices: ${result}")
            return result
        }

        val originalData = result.data.data
        Log.d("PriceFetch", "Fetched prices: ${originalData.mapValues { it.value.quote["USD"]?.price }}")

        // 🔁 Group by symbol
        val groupedBySymbol = originalData.values.groupBy { it.symbol.uppercase() }
        val groupedBySlug = originalData.values.groupBy { it.slug.lowercase() }

        val manualFallbacks = mapOf(
            3890 to 28321, // MATIC → POL (new token)
            // add more if needed
        )

        // 🛠 Patch missing prices
        val patchedData = originalData.mapValues { (id, quoteData) ->
            val primaryPrice = quoteData.quote["USD"]?.price

            if (primaryPrice != null && primaryPrice > 0.0) {
                quoteData
            } else {
                val fallbackBySymbol = groupedBySymbol[quoteData.symbol.uppercase()]
                    ?.firstOrNull {
                        it.id != quoteData.id &&
                                it.quote["USD"]?.price != null &&
                                it.quote["USD"]!!.price > 0.0
                    }

                val fallbackBySlug = groupedBySlug[quoteData.slug.lowercase()]
                    ?.firstOrNull {
                        it.id != quoteData.id &&
                                it.quote["USD"]?.price != null &&
                                it.quote["USD"]!!.price > 0.0
                    }
                val fallbackByManual = manualFallbacks[id.toIntOrNull() ?: -1]?.let { fallbackId ->
                    originalData[fallbackId.toString()]
                }?.takeIf {
                    it.quote["USD"]?.price != null && it.quote["USD"]!!.price > 0.0
                }
                val fallback = fallbackBySymbol ?: fallbackBySlug ?: fallbackByManual

                Log.d("PriceFallback" , "fall back by slug is : $fallbackBySlug and fallback by symbol is : $fallbackBySymbol and fallback is :$fallback")
                if (fallback != null) {
                    Log.w(
                        "PriceFallback",
                        "Price missing for token ID $id (${quoteData.symbol}/${quoteData.slug}), " +
                                "falling back to ID ${fallback.id} (${fallback.symbol}/${fallback.slug}) with price ${fallback.quote["USD"]?.price}"
                    )
                    quoteData.copy(
                        quote = mapOf("USD" to fallback.quote["USD"]!!)
                    )
                } else {
                    Log.w("PriceFallback", "No fallback found for token ID $id (${quoteData.symbol})")
                    quoteData
                }
            }
        }

        Log.d("PriceFetch", "Final patched prices: ${patchedData.mapValues { it.value.quote["USD"]?.price }}")

        return ApiResult.Success(TokenQuotesResponse(result.data.status, patchedData))
    }




    /*    fun filterActivatedMetadata(
            allMetas: List<TokenMetaData>,
            activeTokenEntities: List<LocalActiveNetworks>
        ): List<TokenMetaData> {
            val activeKeys = activeTokenEntities.map {
                ActiveTokenKey(it.tokenId, it.chain.lowercase())
            }.toSet()

            return getFilteredMetas(allMetas, activeKeys)
        }*/

    fun filterActivatedMeta(
        tokenMetaMap: Map<String, TokenMetaData>,
        actives: List<LocalActiveNetworks>
    ): List<MatchedTokenMeta> {
        return actives.mapNotNull { active ->
            val meta = tokenMetaMap[active.tokenId.toString()] ?: return@mapNotNull null

            val contract = meta.contractAddress.find {
                it.contractAddress.equals(active.tokenAddress, ignoreCase = true) &&
                        it.platform.coin.slug.equals(active.chain, ignoreCase = true)
            } ?: return@mapNotNull null

            MatchedTokenMeta(
                meta = meta,
                contract = contract,
                chain = active.chain
            )
        }
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
        tokenAddress = meta.contractAddress.firstOrNull()?.contractAddress,
        logo = meta.logo,
        description = meta.description,
        websiteUrl = meta.urls?.website?.firstOrNull(),
        lastUpdated = System.currentTimeMillis()
    )
}

fun getSupportedChains(meta: TokenMetaData): List<AlchemyChain> {
    return meta.contractAddress.mapNotNull { contract ->
        AlchemyChain.entries.find { chain ->
            chain.name.equals(contract.platform.name, ignoreCase = true) ||
                    chain.name.equals(contract.platform.coin.name, ignoreCase = true) ||
                    chain.name.equals(contract.platform.coin.slug, ignoreCase = true)
        }
    }.distinct()

}
