package com.repoint.dashboard.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointSearchBar
import com.repoint.basics.logic.chainIdFromSlug
import com.repoint.basics.logic.coinTypeFromSlug
import com.repoint.basics.logic.getAddressForChain
import com.repoint.dashboard.AlchemyViewModel
import com.repoint.dashboard.CmcTokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.ResolvedTokenInstance
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.remote.TokensBalance
import com.repoint.models.sharedmodels.remote.normalizeSlug
import com.repoint.models.sharedmodels.rpc.AlchemyTokenBalance
import com.repoint.models.sharedmodels.ui.UiState
import kotlinx.coroutines.launch

@Composable
fun ChooseTokenScreen(
    navController: NavController,
    walletViewModel: WalletViewModel = hiltViewModel(),
    alchemyViewModel: AlchemyViewModel = hiltViewModel(),
    cmcTokenViewModel: CmcTokenViewModel = hiltViewModel(),
    isSend: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val spManager = SpManager(context)

    val masterWalletId by spManager.getActiveWalletId().collectAsState(null)

// Suppose cryptoNetworks is your list of all BlockchainNetwork objects:

    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    var searchBarActive by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var chainWallets by remember { mutableStateOf<List<ChainWallet>>(emptyList()) }


    val balancesState by alchemyViewModel.tokenBalances.collectAsState()
    val metaState by cmcTokenViewModel.tokenMetasFlow.collectAsState()
    val priceState by alchemyViewModel.nativeUsdPrice.collectAsState() // if used

    val isUiLoading = balancesState is UiState.Loading || priceState is UiState.Loading

    val alchemyBalances = (balancesState as? UiState.Success)?.data ?: emptyList()

    val tokens by cmcTokenViewModel.filteredMetas.collectAsState()
    val filteredTokens = remember(tokens, searchQuery) {
        tokens
            .filter { it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true) }
            .distinctBy { token ->
                if (token.isNative) "native:${token.chain.lowercase()}"
                else "${token.tokenId}:${token.chain.lowercase()}"
            }
            .sortedWith(
                compareBy<ResolvedTokenInstance>(
                    { it.chain.lowercase() },
                    { it.symbol.lowercase() }
                )
            )
    }
    // Get first token (or best match later)
    val selectedToken = remember(tokens, searchQuery) {
        tokens.firstOrNull {
            it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true)
        } ?: tokens.firstOrNull()
    }

    // 60 is for fallback if user has no selected tokens (0 tokens) so loading ends
    val coinType = remember(selectedToken) {
        selectedToken?.chain?.let { coinTypeFromSlug(it) } ?: 60
    }

    val currentWalletAddress by produceState<String?>(initialValue = null, masterWalletId, coinType) {
        if (!masterWalletId.isNullOrBlank() && coinType != null) {
            value = walletViewModel.getChainWallet(masterWalletId!!, coinType)?.address
        }
    }
    LaunchedEffect(currentWalletAddress) {
        Log.d("ChooseToken", "Resolved wallet address for chain: $currentWalletAddress")
    }


    val chainWalletsState = remember { mutableStateOf<List<ChainWallet>>(emptyList()) }

    // FIX: Provide a fallback slug so the chainId logic resolves to a valid number
    val currentSlug = remember(searchQuery) {
        // Optional logic to infer current chain based on top matching token
        tokens.firstOrNull {
            it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true)
        }?.chain?.lowercase() ?: "ethereum"
    }

    Log.d("ChooseToken","the currentWallet chain : ${chainWalletsState.value}")

    Log.d("ChooseToken","the currentWallet Address : $currentWalletAddress")
    LaunchedEffect(currentWalletAddress, masterWalletId) {
        if (!currentWalletAddress.isNullOrBlank() && !masterWalletId.isNullOrBlank()) {
            Log.d("ChooseToken", "🔄 Fetching balances for wallet=$currentWalletAddress")
            cmcTokenViewModel.loadInitialTokens()


            // Chain ID logic (replace with actual variable or logic if dynamic)
            val chainId = when (currentSlug?.lowercase()) {
                "ethereum" -> 1
                "polygon" -> 137
                "bnb", "bsc" -> 56
                else -> 1 // fallback to Ethereum
            }
            alchemyViewModel.loadNativeUsdPrice(chainId)
            alchemyViewModel.loadTokenBalances(masterWalletId!!, currentWalletAddress!!)
        }
    }



    LaunchedEffect(masterWalletId) {

        tokenImageLoaded = true
        networkImageLoaded = true
        if (!masterWalletId.isNullOrEmpty()) {
            chainWallets = walletViewModel.getAllChainWallets(masterWalletId!!)

            val wallets = walletViewModel.getAllChainWallets(masterWalletId!!)
            chainWalletsState.value = wallets


            Log.d("LoadingDebug", "tokenBalances: $balancesState")
            Log.d("LoadingDebug", "tokenMetas: ${metaState.size}")
            Log.d("LoadingDebug", "nativeUsdPrice: $priceState")


            cmcTokenViewModel.setCurrentWallet(masterWalletId!!)

            Log.d("chooseToken", "Fetching active tokens for wallet : $masterWalletId")
        }

    }

    RepointAppBar(
        title = if (isSend) "Send" else "Receive", exp = { _, _, _ ->

            val addressBySlug = remember(chainWalletsState.value) {
                chainWalletsState.value.associateBy { normalizeSlug(it.networkName) }
            }

            if (isUiLoading) {
                // 🔥 Show Loading Animation Centered
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoaderAnimation()
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                searchBarActive = false
                            })
                        }) {


                    RepointSearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                        },
                        active = searchBarActive,
                        onActiveChange = { searchBarActive = it },
                        content = {

                            if (searchBarActive){


                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(pureWhite)
                            ) {
                                if (filteredTokens.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (searchQuery.isBlank()) {
                                                    "You haven't added any tokens yet."
                                                } else {
                                                    "No tokens found for \"$searchQuery\""
                                                },
                                                style = RepointTypography.bodyMedium,
                                                color = Color.Gray,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    items(filteredTokens) { token ->
                                       val walletAddress = getAddressForChain(token.chain, chainWallets)

                                       if (walletAddress != null && masterWalletId != null) {
                                           val perTokenCoinType = coinTypeFromSlug(token.chain)
                                           Log.d("ChooseToken", "▶ coinType for ${token.chain} = $perTokenCoinType")

                                           ResolvedTokenRow(
                                               token = token,
                                               masterWalletId = masterWalletId!!,
                                               isSend = isSend,
                                               balances = alchemyBalances,
                                               walletAddress = walletAddress,
                                               navController = navController,
                                               coinType = perTokenCoinType
                                           )
                                       } else {
                                           Log.w("ChooseToken", "⚠️ Missing wallet for chain=${token.chain}")
                                       }
                                    }
                                }
                                }
                            }
                        },
                        onSearch = {
                            focusManager.clearFocus() // ✅ good UX
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        focusRequester = focusRequester
                    )


                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        Log.d("ResolvedTokenRow","the tokens are : $tokens")
                        Log.d("ResolvedTokenRow","the filtered tokens are : $filteredTokens")

                        if (filteredTokens.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isBlank()) {
                                            "You haven't added any tokens yet."
                                        } else {
                                            "No tokens found for \"$searchQuery\""
                                        },
                                        style = RepointTypography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }else {
                            items(filteredTokens) { token ->
                                val perTokenCoinType = coinTypeFromSlug(token.chain)
                                val walletAddressie = getAddressForChain(token.chain, chainWallets)

                                Log.d("TokenDebug", "MasterWalletId: $masterWalletId")
                                Log.d("TokenDebug", "CoinType for ${token.chain}: $perTokenCoinType")
                                Log.d("TokenDebug", "Resolved wallet address: $walletAddressie")
                                masterWalletId?.let { walletId ->
                                    if (walletAddressie != null) {
                                        ResolvedTokenRow(
                                            token = token,
                                            masterWalletId = walletId,
                                            isSend = isSend,
                                            balances = alchemyBalances,
                                            walletAddress = walletAddressie,
                                            navController = navController,
                                            coinType = perTokenCoinType
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }, navController = navController
    )
}


@Composable
fun ResolvedTokenRow(
    token: ResolvedTokenInstance,
    masterWalletId: String,
    isSend: Boolean,
    balances: List<AlchemyTokenBalance>,
    walletAddress : String, // 🔥 now dynamic
    navController: NavController,
    coinType : Int = 60,
) {

    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                scope.launch {
                    Log.d("ResolvedTokenRow", "🔍 Debug info for token: $token")

                    Log.d("ResolvedTokenRow", "🔘 Clicked token: ${token.symbol} on ${token.chain}")
                    Log.d("ResolvedTokenRow", "Token ID: ${token.tokenId}, Contract: ${token.contractAddress}")
                    Log.d("ResolvedTokenRow", "tokenName = ${token.name}")


                    val balance = if (token.isNative) {
                        "0"
                    } else {
                        token.tokenMeta?.let { meta ->
                            matchBalance(meta, balances, token.chain)?.toPlainString()
                        } ?: "0"
                    }

                    Log.d("BalanceMatch", "Trying to match token: ${token.name} on chain=${token.chain}")
                    Log.d("BalanceMatch", "→ Meta contracts: ${token.tokenMeta?.contractAddress.orEmpty().map { it.contractAddress }}")
                    Log.d("BalanceMatch", "→ Alchemy balances: ${balances.map { it.contractAddress to it.chainSlug }}")

                    Log.d("ResolvedTokenRow","token meta is : ${token.tokenMeta}")
                    Log.d("ResolvedTokenRow","balances  is : $balances")
                    Log.d("ResolvedTokenRow","chain  is : ${token.chain}")
                    Log.d("ResolvedTokenRow", "Balance: $balance")


                    Log.d("ResolvedTokenRow", "Wallet Address (injected): $walletAddress")

                  //  coinType = coinTypeFromSlug(token.chain)
                    val chainId = chainIdFromSlug(token.chain)
                    Log.d("ResolvedTokenRow", "CoinType: $coinType, ChainId: $chainId")
                    Log.d("ResolvedTokenRow", "coinType = $coinType for slug=${token.chain}")

                   if (chainId == null) {
                       Log.e("ResolvedTokenRow", "❌ chainId is null for slug=${token.chain}")
                        return@launch
                    }

                    Log.d("ResolvedTokenRow", "Wallet Address: $walletAddress")

                    if (isSend) {
                        val routeTokenId = if (token.isNative) -1 else token.tokenId
                        // Navigation routes cannot reliably preserve an empty path segment.
                        val routeContractAddress = if (token.isNative) "native" else token.contractAddress
                        val route = "sendToken/$walletAddress/$balance/$coinType/$routeContractAddress/$chainId/${Uri.encode(token.name)}/$routeTokenId/$masterWalletId"
                        Log.d("ResolvedTokenRow", "Navigating to: $route")
                        navController.navigate(
                            route
                        )
                    } else {
                        val routeTokenId = if (token.isNative) -1 else token.tokenId
                        val route = "qrCode/$walletAddress/$masterWalletId/$routeTokenId/${token.chain}"
                        Log.d("ResolvedTokenRow", "Navigating to: $route")
                        navController.navigate("qrCode/$walletAddress/$masterWalletId/${token.tokenId}/${token.chain}")
                    }
                }
            }
            .background(grayHound)
            .border(0.5.dp, lightGray, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(token.symbol, style = RepointTypography.titleSmall)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            token.name, style = RepointTypography.labelSmall, color = Color.Gray,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            token.chain.uppercase(),
            style = RepointTypography.labelSmall,
            color = Color.DarkGray
        )
    }
}
