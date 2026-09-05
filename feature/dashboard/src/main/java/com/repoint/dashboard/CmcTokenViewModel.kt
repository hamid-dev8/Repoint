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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState =
        MutableStateFlow<UiState<List<CmcTokenEntity>>>(UiState.Success(emptyList()))
    val searchState: StateFlow<UiState<List<CmcTokenEntity>>> = _searchState.asStateFlow()
    private val _isPaging = MutableStateFlow(false)
    val isPaging: StateFlow<Boolean> = _isPaging

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _activeNetworks = MutableStateFlow<LocalActiveNetworks?>(null)


    private val _activeTokenEntities = MutableStateFlow<List<LocalActiveNetworks>>(emptyList())
    val activeTokenEntities: StateFlow<List<LocalActiveNetworks>> =
        _activeTokenEntities.asStateFlow()


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

    // 1️⃣ Which master-wallet are we looking at?
    private val _currentWalletId = MutableStateFlow<String?>(null)
    fun setCurrentWallet(walletId: String) {
        _currentWalletId.value = walletId
        Log.d(
            "wallety",
            "the current wallet id : $_currentWalletId and the actual wallet id is setting : $walletId"
        )
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


    val activeTokenKeyFlow: StateFlow<Set<ActiveTokenKey>> =
        _currentWalletId
            .filterNotNull()
            .flatMapLatest { wid ->
                repository.getActiveTokenKeys(wid)
            }
            .map {
                Log.d("activeToken", "active token key is : $it")
                it.toSet()
            }
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
        Log.d("flowDebug", "metas and keys are $metas & $keys")
        val erc20Assets = getFilteredMetas(metas, keys)
        val nativeAssets = getNativeAssetEntries(
            activeChains = keys.map { it.chain }.toSet()
        )

        val nativeSymbolsByChain = mapOf(
            "ethereum" to setOf("ETH"),
            "polygon" to setOf("MATIC", "POL"),
            "bnb" to setOf("BNB"),
            "arbitrum" to setOf("ETH"),
            "optimism" to setOf("ETH"),
            "avalanche" to setOf("AVAX"),
            "fantom" to setOf("FTM")
        )

        // NetworkScreen can activate CMC's native-coin metadata directly. Prefer that
        // record because it includes the canonical logo and quote; add a synthetic
        // fallback only when no corresponding native record is active.
        val chainsWithNativeMetadata = erc20Assets
            .filter { asset ->
                asset.symbol.uppercase() in nativeSymbolsByChain[asset.chain].orEmpty()
            }
            .map { it.chain }
            .toSet()

        erc20Assets + nativeAssets.filterNot { it.chain in chainsWithNativeMetadata }
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
            .onEach {
                Log.d(
                    "SlugMatch",
                    "🔍 Searching tokenId=$tokenId in ${it.map { t -> t.id }}"
                )
            }
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
            meta.contractAddress.orEmpty().mapNotNull { contract ->
                val platform = contract.platform ?: return@mapNotNull null
                val slug = normalizeSlug(platform.coin?.slug ?: platform.name)
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
                        tokenMeta = meta,
                        isNative = isNativeAsset(meta.symbol, slug)
                    )
                } else null
            }
        }
    }

    private fun isNativeAsset(symbol: String, chain: String): Boolean {
        val nativeSymbolsByChain = mapOf(
            "ethereum" to setOf("ETH"),
            "polygon" to setOf("MATIC", "POL"),
            "bnb" to setOf("BNB"),
            "arbitrum" to setOf("ETH"),
            "optimism" to setOf("ETH"),
            "avalanche" to setOf("AVAX"),
            "fantom" to setOf("FTM")
        )

        return symbol.uppercase() in nativeSymbolsByChain[normalizeSlug(chain)].orEmpty()
    }

    fun getNativeAssetEntries(activeChains : Set<String>) : List<ResolvedTokenInstance>{
        val nativeMap = mapOf(
            "ethereum" to ("ETH" to "Ethereum"),
            "polygon" to ("POL" to "Polygon"),
            "bnb" to ("BNB" to "BNB Smart Chain"),
            "bsc" to ("BNB" to "BNB Smart Chain"),
            "arbitrum" to ("ETH" to "Arbitrum"),
            "optimism" to ("ETH" to "Optimism"),
            "avalanche" to ("AVAX" to "Avalanche"),
            "fantom" to ("FTM" to "Fantom")
        )

        return activeChains.mapNotNull { chainSlug ->
            val normalized = normalizeSlug(chainSlug)
            val pair = nativeMap[normalized] ?: return@mapNotNull null
            val (symbol, displayName) = pair

            ResolvedTokenInstance(
                tokenId = -1,
                symbol = symbol,
                name = displayName,
                logo = null,
                contractAddress = "",
                chain = normalized,
                decimals = 18,
                tokenMeta = null,
                isNative = true
            )
        }
    }

    fun loadNextDbPage() {
        viewModelScope.launch {
            val page = currentPage++
            val pageSize = 12

            val tokens = withContext(Dispatchers.IO) {
                repository.getDbTokensPaged(
                    page * pageSize,
                    pageSize
                ) // You must implement this in DAO
            }

            val ids = tokens.map { it.id }

            val metaDataResult = repository.fetchTokenMetadata(ids)
            if (metaDataResult !is ApiResult.Success) {
                Log.e("PerChainTokens", "Meta data failed")
                return@launch
            }

            val metaMap = metaDataResult.data.data
            val perChain = toPerChainUiModels(metaMap)
            _perChainTokens.update { it + perChain }

            Log.d("PerChainTokens", "Loaded page with ${perChain.size} per-chain tokens")
        }
    }

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

            Log.d(
                "SearchDebug",
                "Final matches: ${sorted.map { it.symbol + " on " + it.chainDisplayName }}"
            )
        }
    }


    fun toPerChainUiModels(metaMap: Map<String, TokenMetaData>): List<TokenPerChainUiModel> {
        return metaMap.values.flatMap { meta ->
            meta.contractAddress.orEmpty().mapNotNull { contract ->
                val platform = contract.platform ?: return@mapNotNull null
                val platformSlug = platform.coin?.slug ?: platform.name
                val chain = resolveChainFromPlatform(platform.name)
                    ?: resolveChainFromPlatform(platformSlug)
                    ?: AlchemyChain.entries.find {
                        it.name.equals(platformSlug, ignoreCase = true)
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
                    chainDisplayName = platform.name
                )
            }
        }
    }


    fun toggleToken(tokenId: Int, tokenAddress: String, chain: AlchemyChain) =
        viewModelScope.launch {
            val wid = _currentWalletId.value ?: return@launch
            val chainName = chain.name.lowercase(Locale.US)
            val key = ActiveTokenKey(tokenId, chainName)

            Log.d("ToggleToken", "Current active keys: ${activeTokenKeyFlow.value}")
            Log.d("ToggleToken", "Toggling key: $key")

            if (key in activeTokenKeyFlow.value) {
                repository.deleteActiveNetworks(tokenId, wid, chainName)
                Log.d("ToggleToken", "deleted the token in $tokenId & $wid")
            } else {
                repository.insertActiveToken(
                    LocalActiveNetworks(
                        tokenId,
                        tokenAddress,
                        wid,
                        chainName
                    )
                )
                Log.d(
                    "ToggleToken",
                    "adding token: id=$tokenId, address=$tokenAddress, wid=$wid , chain name : $chainName"
                )
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
                    data = emptyMap()
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
        Log.d(
            "PriceFetch",
            "Fetched prices: ${originalData.mapValues { it.value.quote["USD"]?.price }}"
        )

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

                Log.d(
                    "PriceFallback",
                    "fall back by slug is : $fallbackBySlug and fallback by symbol is : $fallbackBySymbol and fallback is :$fallback"
                )
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
                    Log.w(
                        "PriceFallback",
                        "No fallback found for token ID $id (${quoteData.symbol})"
                    )
                    quoteData
                }
            }
        }

        Log.d(
            "PriceFetch",
            "Final patched prices: ${patchedData.mapValues { it.value.quote["USD"]?.price }}"
        )

        return ApiResult.Success(TokenQuotesResponse(result.data.status, patchedData))
    }

    private fun mergeMapAndMetadata(
        map: CmcAllTokens,
        meta: TokenMetaData
    ): CmcTokenEntity {
        val firstContract = meta.contractAddress?.firstOrNull()

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
            tokenAddress = firstContract?.contractAddress,
            logo = meta.logo,
            description = meta.description,
            websiteUrl = meta.urls?.website?.firstOrNull(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}
