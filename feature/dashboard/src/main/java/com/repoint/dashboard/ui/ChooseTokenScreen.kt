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
    web3ViewModel: Web3ViewModel = hiltViewModel(),
    alchemyViewModel: AlchemyViewModel = hiltViewModel(),
    cmcTokenViewModel: CmcTokenViewModel = hiltViewModel(),
    isSend: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val spManager = SpManager(context)

    val scope = rememberCoroutineScope()

    val masterWalletId by spManager.getActiveWalletId().collectAsState(null)

// Suppose cryptoNetworks is your list of all BlockchainNetwork objects:

    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    var searchBarActive by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var chainWallets by remember { mutableStateOf<List<ChainWallet>>(emptyList()) }

    val isLoading = !tokenImageLoaded && !networkImageLoaded

    val tokenBalances = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ArrayList<AlchemyTokenBalance>>("tokenBalances")
    } ?: arrayListOf()


    val balancesState by alchemyViewModel.tokenBalances.collectAsState()
    val alchemyBalances = (balancesState as? UiState.Success)?.data ?: emptyList()

    val tokens by cmcTokenViewModel.filteredMetas.collectAsState()
    // Get first token (or best match later)
    val selectedToken = remember(tokens, searchQuery) {
        tokens.firstOrNull {
            it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true)
        } ?: tokens.firstOrNull()
    }

    val coinType = remember(selectedToken) {
        selectedToken?.chain?.let { coinTypeFromSlug(it) }
    }

    val currentWalletAddress by produceState<String?>(initialValue = null, masterWalletId, coinType) {
        if (!masterWalletId.isNullOrBlank() && coinType != null) {
            value = walletViewModel.getChainWallet(masterWalletId!!, coinType)?.address
        }
    }
    LaunchedEffect(currentWalletAddress) {
        Log.d("ChooseToken", "Resolved wallet address for chain: $currentWalletAddress")
    }

    val activeTokenKeys by cmcTokenViewModel.activeTokenKeyFlow.collectAsState()
    val allMetas: Map<String, TokenMetaData>


    val activeWalletId by spManager.activeWalletIdFlow.collectAsState()

    val chainWalletsState = remember { mutableStateOf<List<ChainWallet>>(emptyList()) }
    val currentSlug = remember(searchQuery) {
        // Optional logic to infer current chain based on top matching token
        tokens.firstOrNull {
            it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true)
        }?.chain?.lowercase()
    }

    // Create a map of chain to wallet address
    val chainToAddress = remember(chainWallets) {
        val ethAddress = chainWallets.firstOrNull { it.coinType == 60 }?.address

        // Map all Ethereum-compatible chains to the same address
        val addressMap = mutableMapOf<String, String>()



        if (ethAddress != null) {
            // Polygon uses Ethereum address
            addressMap["polygon"] = ethAddress
            addressMap["ethereum"] = ethAddress
            addressMap["arbitrum"] = ethAddress
            addressMap["optimism"] = ethAddress

            // Other chains use their own addresses
            chainWallets.forEach { wallet ->
                when (wallet.coinType) {
                    56 -> { // BNB
                        addressMap["bnb"] = wallet.address
                        addressMap["bsc"] = wallet.address
                    }
                    // Add other chains as needed
                }
            }
        }

        addressMap
    }

    Log.d("ChooseToken","the currentWallet chain : ${chainWalletsState.value}")

/*    val currentWalletAddress = remember(currentSlug, chainWalletsState.value) {
        chainWalletsState.value.firstOrNull {
            normalizeSlug(it.networkName) == currentSlug
        }?.address
    }*/

    Log.d("ChooseToken","the currentWallet Address : $currentWalletAddress")
    LaunchedEffect(currentWalletAddress, masterWalletId) {
        if (!currentWalletAddress.isNullOrBlank() && !masterWalletId.isNullOrBlank()) {
            val forcedAddress = "0x49977501faf5f4aa3b38241bea622acda017e867"

            alchemyViewModel.loadTokenBalances(masterWalletId!!, forcedAddress!!)
            Log.d("ChooseToken","the currentWalletAddress is something : $currentWalletAddress")
        }
    }



    LaunchedEffect(masterWalletId) {

        tokenImageLoaded = true
        networkImageLoaded = true
        if (!masterWalletId.isNullOrEmpty()) {
            chainWallets = walletViewModel.getAllChainWallets(masterWalletId!!)

            val wallets = walletViewModel.getAllChainWallets(masterWalletId!!)
            chainWalletsState.value = wallets



            cmcTokenViewModel.setCurrentWallet(masterWalletId!!)
            Log.d("chooseToken", "Fetching active tokens for wallet : $masterWalletId")
            //viewModel.fetchActiveTokens(masterWalletId!!)
        }

    }

    RepointAppBar(
        title = if (isSend) "Send" else "Receive", exp = { _, _, _ ->

            val addressBySlug = remember(chainWalletsState.value) {
                chainWalletsState.value.associateBy { normalizeSlug(it.networkName) }
            }

            if (isLoading) {
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
                                val filtered = tokens
                                    .filter { it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true) }
                                    .distinctBy { it.tokenMeta.id to it.chain.lowercase() }


                                if (filtered.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No tokens found",
                                            modifier = Modifier.padding(16.dp),
                                            style = RepointTypography.bodyMedium
                                        )
                                    }
                                } else {
                                    items(filtered) { token ->
                                        val normalizedChain = normalizeSlug(token.chain)
                                        val walletAddress = getAddressForChain(token.chain, chainWallets)

                                        //  val walletAddress = chainToAddress[normalizedChain]

                                     //   val walletAddress = addressBySlug[normalizeSlug(token.chain)]?.address

                                        if (walletAddress != null && masterWalletId != null) {
      /*                                      // ✅ Load balances dynamically
                                            LaunchedEffect(walletAddress) {
                                                alchemyViewModel.loadTokenBalances(masterWalletId!!, walletAddress)
                                                Log.d("ChooseToken","the balance state is : $balancesState")
                                            }*/
                                            val perTokenCoinType = coinTypeFromSlug(token.chain)
                                            Log.d("ChooseToken", "▶ coinType for ${token.chain} = $perTokenCoinType")

                                            val walletAddressState = produceState<String?>(initialValue = null, masterWalletId, token.chain) {
                                                value = if (masterWalletId != null && perTokenCoinType != null) {
                                                    walletViewModel.getChainWallet(masterWalletId!!, perTokenCoinType)?.address
                                                } else null
                                            }

                                            // ✅ Pass the correct wallet address
                                            val selectedWalletAddress = walletAddressState.value
                                            val slug = token.chain.lowercase()
                                            val selectedContract = token.tokenMeta.contractAddress.firstOrNull {
                                                it.platform.coin.slug.equals(slug, ignoreCase = true)
                                            }
                                                ResolvedTokenRow(
                                                    token = token,
                                                    masterWalletId = masterWalletId!!,
                                                    isSend = isSend,
                                                    balances = alchemyBalances,
                                                    walletAddress = walletAddress,
                                                    navController = navController,
                                                    walletViewModel = walletViewModel,
                                                    coinType = perTokenCoinType
                                                )
                                        } else {
                                            Log.w("ChooseToken", "⚠️ Missing wallet for chain=${token.chain}")
                                        }
                                    }
                                }
                                }
                         /*       items(tokenMetas.filter {
                                    it.symbol.contains(searchQuery, true) || it.name.contains(
                                        searchQuery,
                                        true
                                    )
                                }) { meta ->
                                    masterWalletId?.let { walletId ->
                                        scope.launch {
                                            sendTokenMetaItem(
                                                meta,
                                                alchemyBalances,
                                                walletViewModel,
                                                walletId,
                                                isSend,
                                                navController
                                            )
                                        }
                                    }
                                }*/
                            }
                        },
                        onSearch = {
                            focusManager.clearFocus() // ✅ good UX
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        focusRequester = focusRequester
                    )


                    LazyColumn(modifier = Modifier.padding(16.dp)) {

                        /*  if (activeTokens.isEmpty()) {
                              item {
                                  Text(
                                      text = "No activated tokens found. Go to the Network screen and enable some.",
                                      modifier = Modifier.padding(16.dp),
                                      color = Color.Gray
                                  )
                              }
                          }*/

//                     /*   items(activeTokens.filter {
//                            it.symbol.contains(searchQuery, ignoreCase = true) ||
//                                    it.name.contains(searchQuery, ignoreCase = true)
//                        }) { token ->
//                            val networkForToken = cryptoNetworks?.find { it.id == token.networkId }
//                            val defaultNetworkLogoUrl = "" // empty string or null, and in your AsyncImage you provide a placeholder drawable resource
//                            val networkLogoUrl = networkForToken?.tokens?.firstOrNull()?.logoUrl ?: defaultNetworkLogoUrl
//
//
//                            masterWalletId?.let {
//                                sendTokenItem(token,viewModel,web3ViewModel,walletViewModel,
//                                    it,isSend,networkLogoUrl,navController)
//                            }
//                        }*/
           /*             items(tokenMetas.filter {
                            it.symbol.contains(searchQuery, true) || it.name.contains(
                                searchQuery,
                                true
                            )
                        }) { meta ->
                            masterWalletId?.let { walletId ->
                                scope.launch {
                                    sendTokenMetaItem(
                                        meta,
                                        alchemyBalances,
                                        walletViewModel,
                                        walletId,
                                        isSend,
                                        navController
                                    )
                                }
                            }
                        }*/

                        val filtered = tokens
                            .filter { it.symbol.contains(searchQuery, true) || it.name.contains(searchQuery, true) }
                            .distinctBy { it.tokenMeta.id to it.chain.lowercase() }


                        val resolvedTokens = filtered.mapNotNull { token ->
                            val slug = token.chain.lowercase()

                            // گرفتن آدرس قرارداد مرتبط با شبکه انتخاب شده
                            val selectedContract = token.tokenMeta.contractAddress.firstOrNull {
                                it.platform.coin.slug.equals(slug, ignoreCase = true)
                            }

                            if (selectedContract == null) {
                                Log.w("ChooseToken", "❌ No contract found for ${token.tokenMeta.symbol} on chain $slug")
                                return@mapNotNull null
                            }

                            // گرفتن موجودی از لیست Alchemy
                            val matchedBalance = alchemyBalances.firstOrNull {
                                it.contractAddress.equals(selectedContract.contractAddress, ignoreCase = true) &&
                                        it.chainSlug.equals(slug, ignoreCase = true)
                            }?.tokenBalance?.toBigDecimalOrNull()

                            Log.d("ChooseToken", "✅ Matched ${token.tokenMeta.symbol} on $slug → contract=${selectedContract.contractAddress}, balance=$matchedBalance")

                            ResolvedTokenInstance(
                                tokenId = token.tokenMeta.id,
                                symbol = token.tokenMeta.symbol,
                                name = token.tokenMeta.name,
                                logo = token.tokenMeta.logo,
                                contractAddress = selectedContract.contractAddress,
                                chain = slug,
                                decimals = token.decimals,
                                tokenMeta = token.tokenMeta
                            ) to matchedBalance
                        }


                        Log.d("ResolvedTokenRow","the tokens are : $tokens")
                        Log.d("ResolvedTokenRow","the filtered tokens are : $filtered")

                        if (filtered.isEmpty()) {
                            item {
                                Text(
                                    text = "No tokens found",
                                    modifier = Modifier.padding(16.dp),
                                    style = RepointTypography.bodyMedium
                                )
                            }
                        } else {
                            items(resolvedTokens) { token ->
                                val walletAddress = addressBySlug[normalizeSlug(token.first.chain)]?.address
                              /*  masterWalletId?.let {
                                    if (walletAddress != null) {
                                        alchemyViewModel.loadTokenBalances(
                                            masterWalletId = it,
                                            walletAddress = walletAddress
                                        )
                                    }
                                }*/
                                val perTokenCoinType = coinTypeFromSlug(token.first.chain)
                                val addressBySluge = chainWallets.associateBy {
                                    normalizeSlug(it.networkName)
                                }
                                val walletAddressie = addressBySluge[normalizeSlug(token.first.chain)]?.address

                                // ✅ Pass the correct wallet address
                              //  val selectedWalletAddress = walletAddressState.value

                                Log.d("TokenDebug", "MasterWalletId: $masterWalletId")
                                Log.d("TokenDebug", "CoinType for ${token.first.chain}: $perTokenCoinType")
                                Log.d("TokenDebug", "Resolved wallet address: $walletAddressie")
                                masterWalletId?.let { walletId ->
                                    if (walletAddressie != null) {
                                        if (perTokenCoinType != null) {
                                            ResolvedTokenRow(
                                                token = token.first,
                                                masterWalletId = walletId,
                                                isSend = isSend,
                                                balances = alchemyBalances,
                                                walletAddress = walletAddressie,
                                                navController = navController,
                                                walletViewModel = walletViewModel,
                                                coinType = perTokenCoinType
                                            )
                                        }
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
    walletViewModel: WalletViewModel
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


                    val balance = matchBalance(token.tokenMeta, balances, token.chain)?.toPlainString() ?: "0"

                    Log.d("BalanceMatch", "Trying to match token: ${token.name} on chain=${token.chain}")
                    Log.d("BalanceMatch", "→ Meta contracts: ${token.tokenMeta.contractAddress.map { it.contractAddress }}")
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

                    if (coinType == null || chainId == null) {
                        Log.e("ResolvedTokenRow", "❌ coinType or chainId is null for slug=${token.chain}")
                        return@launch
                    }

                    Log.d("ResolvedTokenRow", "Wallet Address: $walletAddress")

                    if (isSend) {
                        val route = "sendToken/$walletAddress/$balance/$coinType/${token.contractAddress}/$chainId/${Uri.encode(token.name)}/${token.tokenId}/$masterWalletId"
                        Log.d("ResolvedTokenRow", "Navigating to: $route")
                        navController.navigate(
                            route
                        )
                    } else {
                        val route = "qrCode/$walletAddress/$masterWalletId/${token.tokenId}/${token.chain}"
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

@Composable
fun sendTokenItem(
    token: TokenEntity,
    web3ViewModel: Web3ViewModel,
    cmcViewModel: CmcTokenViewModel,
    masterWalletId: String,
    isSend: Boolean,
    networkUrl: String?,
    navController: NavController
) {

    val tokenId = token.tokenId
    val matchedTokenMeta = cmcViewModel.tokenMetasFlow

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /*  val networkByToken by viewModel.specificNetworkByToken.collectAsState()
      val networkForToken by viewModel.specificNetworkForToken.collectAsState()*/

    //val activeTokens by viewModel.activeTokens.collectAsState()
    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    val tokenBalances = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ArrayList<TokensBalance>>("tokenBalances")
    } ?: arrayListOf()


    TokenItem(
        token = token,
        onClick = {
            val tokenId = token.tokenId
            // viewModel.fetchTokensWithNetwork(token.tokenId) // fetch TokenWithNetwork
            Log.d("chooseToken", "tokenId is : $tokenId")
            Log.d("chooseToken", "tokenEntity is : $token")

            scope.launch {
                /*   val network = viewModel.getNetworkById(token.networkId)
                   viewModel.getNetworkForToken(tokenId)*/

                /*    val chainId = network?.chainId
                    val coinType = network?.coinType*/

                /*  if (chainId == null){
                      Log.e("chooseToken", "❌ Chain ID not found for tokenId: ${token.tokenId}")
                      Toast.makeText(context, "Network info missing", Toast.LENGTH_SHORT).show()
                      return@launch
                  }else
                  {
                      web3ViewModel.fetchGasPrice(chainId.toLong())
                  }*/


                // val coinType = viewModel.tokenWithNetworks.value?.network?.coinType
                //  Log.d("chooseToken", "coinType is : $coinType")

                /*  if (coinType == null) {
                      Log.e(
                          "chooseToken",
                          "Coin type is null for networkId: ${token.networkId}"
                      )
                      Toast.makeText(
                          context,
                          "Invalid token network",
                          Toast.LENGTH_SHORT
                      ).show()
                      return@launch
                  }*/

                /*   val chainWallet = walletViewModel.getChainWallet(
                       masterWalletId.toString(),
                       coinType!!
                   )*/
                //      Log.d("chooseToken", "the chainWAllet is : $chainWallet")
                Log.d(
                    "chooseToken",
                    "the masterWAllet id is : ${masterWalletId}"
                )
                //     val walletAddress = chainWallet?.address ?: ""
                /*    val jsonString = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>("tokenBalances")
                    val gson = Gson()*/
                if (tokenBalances.isEmpty()) {
                    Log.e("ChooseToken", "tokenBalances is empty or missing!")
                }

                //         Log.d("chooseToken", "wallet address is : $walletAddress")
                //navigate to the right screen
                /*      if (isSend) {
                          //TODO add token balance

                          //val balance = walletViewModel.gettok
                          val balance = tokenBalances?.find { it.tokenAddress.lowercase() == token.contractAddress.lowercase() }?.balanceFormatted  ?: "0"
                          Log.d("chooseToken", "token is : $tokenBalances")
                          Log.d("chooseToken", "token is : ${networkByToken?.nativeToken}")
                          navController.navigate(
                              "sendToken/${Uri.encode(walletAddress)}/${Uri.encode(balance)}/$coinType/${Uri.encode(token.contractAddress)}/$chainId/${Uri.encode(token.name)}"
                          )                } else {
                          Log.d("chooseToken", "network name is : ${networkByToken?.nativeToken}")
                          Log.d("chooseToken", "wallet address is : $walletAddress")
                          Log.d("chooseToken", "masterwallet iD is : $masterWalletId")
                          Log.d("chooseToken", "token id is : $tokenId")
                          navController.navigate("qrCode/$walletAddress/$masterWalletId/$tokenId/${networkByToken?.nativeToken}")
                      }*/
            }
        },
        networkUrl = networkUrl.toString(),
        onTokenImageLoaded = { tokenImageLoaded = true },
        onNetworkImageLoaded = { networkImageLoaded = true }
    )
}

@Composable
fun TokenItem(
    token: TokenEntity, onClick: () -> Unit, networkUrl: String, onTokenImageLoaded: () -> Unit,
    onNetworkImageLoaded: () -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .border(0.5.dp, lightGray)
            .background(grayHound, RoundedCornerShape(22.dp))
            .padding(8.dp)
    ) {
/*        AsyncImage(
            model = token.logoUrl,
            contentDescription = token.name,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit
        )*/

        TokenWithNetworkDb(
            token,
            networkUrl,
            onTokenImageLoaded = { onTokenImageLoaded() },
            onNetworkImageLoaded = { onNetworkImageLoaded() })

        Spacer(modifier = Modifier.width(8.dp))

        Text(token.symbol, style = RepointTypography.titleSmall)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            token.name, style = RepointTypography.labelSmall,
            color = Color.Gray,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )


    }

}

suspend fun sendTokenMetaItem(
    meta: TokenMetaData,
    balances: List<AlchemyTokenBalance>,
    walletViewModel: WalletViewModel,
    masterWalletId: String,
    isSend: Boolean,
    navController: NavController
) {
    val contract = meta.contractAddress.firstOrNull() ?: return
    val slug = contract.platform.coin.slug.lowercase()
    val chainId = chainIdFromSlug(slug) ?: return
    val coinType = coinTypeFromSlug(slug)
    val walletAddress = walletViewModel.getChainWallet(masterWalletId, coinType)?.address ?: ""
    val balance = matchBalance(meta, balances, slug)?.toPlainString() ?: "0"


    if (isSend) {
        navController.navigate(
            "sendToken/${Uri.encode(walletAddress)}/${Uri.encode(balance)}/$coinType/${
                Uri.encode(
                    contract.contractAddress
                )
            }/$chainId/${Uri.encode(meta.name)}"
        )
    } else {
        navController.navigate("qrCode/$walletAddress/$masterWalletId/${meta.id}/$slug")
    }
}
