package com.repoint.dashboard.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.AuthBottomSheetContent
import com.repoint.basics.atoms.BalanceScreen
import com.repoint.basics.atoms.CircularButtonWithText
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.ViewPagerRobot
import com.repoint.basics.atoms.launchBotTab
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dashboard.TokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.R
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.aliceBlue
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.pureWhite
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokensBalance
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat


@Composable
@Preview(showBackground = true)
fun PreviewActionsRow() {

    //HomeScreen()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    walletViewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    userViewModel: UserViewModel = hiltViewModel<UserViewModel>(),
    tokenViewModel: TokenViewModel = hiltViewModel<TokenViewModel>(),
    networkViewModel: NetworkViewModel = hiltViewModel<NetworkViewModel>(),
    web3ViewModel: Web3ViewModel = hiltViewModel<Web3ViewModel>()
) {

    var masterWallets by remember { mutableStateOf<List<MasterWallet>>(emptyList()) }
    var chainWallets by remember { mutableStateOf<List<ChainWallet>>(emptyList()) }
    var tokenList by remember { mutableStateOf<NativesBalance?>(null) }
    var tokensOf by remember { mutableStateOf<List<TokensBalance?>>(emptyList()) }
    var balance by remember { mutableStateOf<String>(" ") }
    var selectedWallet by remember { mutableStateOf<MasterWallet?>(null) }
    var activeAddress by remember { mutableStateOf<String?>(null) }
    var ether by remember { mutableStateOf<BigDecimal?>(BigDecimal.ZERO) }
    val ethereumWallet = remember(chainWallets) {
        derivedStateOf { chainWallets.firstOrNull { it.coinType == 60 } }
    }
    var searchText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tokens", "NFTs")

    var isLoading by remember { mutableStateOf(true) }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val spManager = SpManager(context)

    //val selectedWalletName = selectedWallet?.name ?: masterWallets.firstOrNull()?.name ?: "No Wallet"
    val walletNames = masterWallets.mapIndexed { index, wallet ->
        wallet.name.ifBlank { "Wallet ${index + 1}" }
    }
    var selectedWalletId by remember { mutableStateOf("") }

    //val listState = rememberLazyListState()

    val activeWalletId by spManager.activeWalletIdFlow.collectAsState()
    val allTokens by tokenViewModel.allTokens.collectAsState()
    val activeTokens by networkViewModel.activeTokens.collectAsState()

    val sheetTokenState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTokenBottomSheet by remember { mutableStateOf(false) }


    LaunchedEffect(selectedWallet?.masterWalletId, activeWalletId, masterWallets) {


        tokenList = null
        tokensOf = emptyList()
        ether = BigDecimal.ZERO


        val user = userViewModel.fetchUser()
        val userId = user?.userId

        Log.d("token", "user id is $userId & user model is :$user")
        if (userId != null) {

            masterWallets = walletViewModel.getAllMasterWallets(userId)
            Log.d("token", "Master Wallets: $masterWallets")

            if (masterWallets.isNotEmpty() && !activeWalletId.isNullOrEmpty()) {

                selectedWallet = masterWallets.find { it.masterWalletId == activeWalletId }
                    ?: masterWallets.firstOrNull()
                Log.d("main", "selected wallet is : ${selectedWallet?.name}")

                selectedWalletId = selectedWallet?.masterWalletId.toString()
                Log.d("main", "selected Wallet name is : $selectedWalletId")

                if (selectedWallet != null && selectedWallet!!.masterWalletId != activeWalletId) {
                    spManager.setActiveWallet(selectedWallet!!.masterWalletId)
                }
                tokenViewModel.getAllTokens(masterWalletId = selectedWallet?.masterWalletId)

                //TODO generic the wallet
                chainWallets = walletViewModel.getAllChainWallets(activeWalletId!!)
                //   spManager.setActiveWallet(masterWallets[0].masterWalletId)

                Log.d("token", "selected wallet is currently : $selectedWallet")

                //Default to Polygon chain for balances
                val address = chainWallets.firstOrNull { it.coinType == 60 }?.address
                if (address != null) {
                    activeAddress = address
                    //todo
                    //  web3ViewModel.testConnectionToWeb3(chainId = )
                    Log.d(
                        "token",
                        "active adress is : $activeAddress , selected wallet is : $selectedWallet , masterId is : ${selectedWallet?.masterWalletId}"
                    )
                    // tokenList = tokenViewModel.getTokenBalance(address = address, chain = "polygon")

                    tokenList = tokenViewModel.getMergedActivatedTokenBalances(
                        activeAddress!!, "polygon",
                        selectedWallet!!.masterWalletId
                    )

                    selectedWallet?.let { networkViewModel.initializeWithWallet(it.masterWalletId) }

                    // tokensOf = tokenViewModel.getAllActivatedTokenBalances(activeAddress, "polygon")
                    //tokensOf = tokenViewModel.getMergedActivatedTokenBalances(address,"polygon")
                    //ether = web3ViewModel.fetchNativeWalletBalance(activeAddress!!)

                    Log.d("token", "Chain Wallets: $chainWallets")
                    Log.d("assets", "active Address used: $activeAddress")
                    Log.d("token", "Token list: $tokenList")
                    Log.d("token", "activated tokens are : $tokensOf")
                    Log.d("token", "Ether: $ether")
                }

            }

        }



        Log.d("token", "tokens of is $tokensOf")
        //  ether = web3ViewModel.fetchNativeWalletBalance(wallets[0].address)
        Log.d("token", "token list are : $tokenList")
        Log.d("token", "ether is : $ether")

        isLoading = false
    }



    RepointAppBar("", exp = { isSearchActive, searchQuery, onSearchQueryChange ->

        if (isLoading) {
            // 🔥 Show Loading Animation Centered
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoaderAnimation()
            }
        } else {

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {

                    val amount = netWorthSection(tokenList?.result)
                    val formattedBalanceAmount = DecimalFormat("#0.00").format(amount)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, richBlack),
                            colors = CardDefaults.cardColors(containerColor = pureWhite)
                        ) {

                            Spacer(Modifier.padding(top = 8.dp))

                            BalanceScreen(
                                masterWallets,
                                balance = "$$formattedBalanceAmount",
                                onAddWallet = {
                                    showBottomSheet = true
                                },
                                onWalletSelected = { selectedWallet ->
                                    coroutineScope.launch {
                                        spManager.setActiveWallet(selectedWallet.masterWalletId)
                                        networkViewModel.initializeWithWallet(selectedWallet.masterWalletId)
                                        Log.d(
                                            "token",
                                            "selected wallet changed : ${selectedWallet.masterWalletId}"
                                        )
                                        Log.d("token", "master wallet changed : $masterWallets")
                                    }
                                    //TODO ezafe kardane safe add wallet va sakht wallet jadid
                                    //walletViewModel.createUserWallet()

                                    selectedWalletId =
                                        selectedWallet.masterWalletId
                                },
                                selectedWalletName = selectedWalletId
                            )
                            if (masterWallets.isNotEmpty() && !activeAddress.isNullOrEmpty()) {
                                ActionsRow(
                                    navController,
                                    wallet = selectedWallet,
                                    tokenList,
                                    activeAddress!!
                                )
                            }
                        }
                        Spacer(Modifier.padding(bottom = 24.dp))
                    }

                    item {
                        ViewPagerRobot()
                    }
                    // ─── sticky TabRow ─────────────────────────────────────
                    // ① sticky header for your tab
                    // s
                    stickyHeader {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp)) {
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ghostWhite)
                                ) {
                                    //
                                    Text( text = "Portfolio",modifier = Modifier.padding(2.dp), style = RepointTypography.labelMedium)

                                }
                        }

                        listOf("Tokens", "NFTs").forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = {
                                    Text(
                                        title,
                                        style = RepointTypography.titleSmall
                                    )
                                }
                            )
                        }
                    }
                    // ② under that header, show either your token rows or NFT placeholder
                    if (selectedTab == 0) {
                        items(tokenList?.result.orEmpty()) { token ->
                            TokenRow(token)
                        }
                    } else {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Coming Soon", style = RepointTypography.headlineSmall)
                            }
                        }
                    }
                }


            }
            //BasicTabLayout(actions = )
        }


    }, navController = navController, isSettings = true, onSettingsClick = {
        navController.navigate("settings")
    }, showEndIcon = true, onEndIconClick = {
        //navController.navigate("networks")
        searchActive = true
    }, isSearchActive = searchActive, setSearchActive = { searchActive = it })

    val filtered = remember(searchText, allTokens) {
        allTokens.filter {
            it.name.contains(searchText, ignoreCase = true) ||
                    it.symbol.contains(searchText, ignoreCase = true)
        }
    }
    // after your normal LazyColumn or Column…
    /*   if (searchActive) {
           Box(
               modifier = Modifier
                   .fillMaxSize()
                   .heightIn(max = 600.dp)
                   .background(ghostWhite.copy(alpha = 0.97f)) // optional
                   .animateContentSize() // 👈 Smooths layout size change

           ) {

               LazyColumn(
                   state = listState,
                   modifier = Modifier
                       .fillMaxSize()
                       .padding(top = 72.dp) // 🔥 LIMIT height properly
               ) {
                   items(filtered) { token ->
                       SearchTokenItem(
                           token = token,
                           isActive = networkViewModel.activeTokens.collectAsState().value
                               .any { it.tokenId == token.tokenId },
                           onToggle = { checked ->
                               networkViewModel.toggleActiveNetwork(
                                   tokenId = token.tokenId,
                                   isActive = checked,
                                   masterWalletId = selectedWallet!!.masterWalletId
                               )
                           }
                       )
                   }
               }

           }

       }*/

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {

            AuthBottomSheetContent(
                onCreateWallet = {
                    showBottomSheet = false
                    navController.navigate("walletConfirm")
                },
                onImportWallet = {
                    showBottomSheet = false
                    navController.navigate("login")
                }
            )

        }
    }

    if (showTokenBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetTokenState,
            onDismissRequest = { showTokenBottomSheet = false }
        ) {
            TokenToggleSheet(
                allTokens = allTokens,
                activeTokens = activeTokens,
                onToggle = { token, isActive ->
                    networkViewModel.toggleActiveNetwork(
                        tokenId = token.tokenId,
                        isActive = isActive,
                        masterWalletId = selectedWallet?.masterWalletId ?: ""
                    )
                }
            )
        }
    }
}


@Composable
fun TokenToggleSheet(
    allTokens: List<TokenEntity>,
    activeTokens: List<TokenEntity>,
    onToggle: (TokenEntity, Boolean) -> Unit
) {
    val activeTokenIds = remember(activeTokens) { activeTokens.map { it.tokenId }.toSet() }

    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxHeight(0.9f)) {
        items(allTokens) { token ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TokenWithNetworkDb(token, allTokens[0].logoUrl,
                    onTokenImageLoaded = { tokenImageLoaded = true },
                    onNetworkImageLoaded = { networkImageLoaded = true })
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(token.symbol, style = RepointTypography.titleSmall)
                    Text(token.name, style = RepointTypography.labelSmall, color = Color.Gray)
                }
                Switch(
                    checked = activeTokenIds.contains(token.tokenId),
                    onCheckedChange = { checked ->
                        onToggle(token, checked)
                    }
                )
            }
        }
    }
}


@Composable
fun TokenRow(item: TokensBalance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.logo,
            contentDescription = item.symbol,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.symbol.uppercase(), style = RepointTypography.titleMedium)
            Text(item.name, style = RepointTypography.labelSmall, color = richBlack)
        }
        Column(horizontalAlignment = Alignment.End) {
            val amt = item.balanceFormatted.toDoubleOrNull() ?: 0.0
            Text(DecimalFormat("#0.00").format(amt), style = RepointTypography.titleMedium)
            Text(
                "$${DecimalFormat("#0.00").format(item.usdPrice)}",
                style = RepointTypography.titleSmall
            )
        }
    }
}

@Composable
fun SearchTokenItem(
    token: TokenEntity,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { /* optional: navigate or copy address */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = token.logoUrl,
            contentDescription = token.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(token.symbol.uppercase(), style = RepointTypography.titleMedium)
            Text(token.name, style = RepointTypography.labelSmall, color = Color.Gray)
        }
        Switch(
            checked = isActive,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun ActionsRow(
    navController: NavController,
    wallet: MasterWallet?,
    tokenList: NativesBalance?,
    activeAddress: String
) {

    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        //arrowUp
        CircularButtonWithText(
            icon = painterResource(R.drawable.wallet_send),
            "Send",
            onClick = {
                //todo handle which is native token to do gas fees
                //send choose token // Todo modify send

                //navController.navigate("sendToken/${wallet?.address}/${tokenList?.result?.get(0)?.balanceFormatted}")
                val gson = Gson()
                val jsonString = gson.toJson(tokenList?.result)
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "tokenBalances",
                    jsonString
                )
                navController.navigate("chooseToken/${true}")
            },
            Modifier.padding(12.dp),
            buttonSize = 28.dp
        )
        CircularButtonWithText(
            icon = painterResource(R.drawable.wallet_receive),
            "Receive",
            onClick = {
                //receive choose token // Todo modify receive
                val encodedAddress = Uri.encode(wallet?.masterWalletId)
                //navController.navigate("qrCode/$encodedAddress")
                navController.navigate("chooseToken/${false}")
            },
            Modifier.padding(12.dp),
            buttonSize = 28.dp
        )
        CircularButtonWithText(
            icon = painterResource(R.drawable.swap),
            "Swap",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 28.dp
        )
        CircularButtonWithText(
            painterResource(R.drawable.bot),
            "To Bot",
            onClick = {
                /* val encodedUrl = Uri.encode("https://app.re-point.net") // or your actual bot URL
                 navController.navigate("bot/$encodedUrl")*/
                launchBotTab(context = context, "https://app.re-point.net")
            },
            Modifier.padding(12.dp),
            buttonSize = 28.dp
        )
        CircularButtonWithText(
            icon = painterResource(R.drawable.history),
            "History",
            onClick = {
                val encodedAddress = Uri.encode(activeAddress)
                val balance = tokenList?.result?.firstOrNull()?.usdPrice ?: 0.0
                val formattedBalance = DecimalFormat("#0.00").format(balance)

                navController.navigate("history/$formattedBalance/$encodedAddress")
            },
            Modifier.padding(12.dp),
            buttonSize = 28.dp
        )

    }
}

//@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AssetsTabLayout(tokenList: List<TokensBalance>) {
    val tabs = listOf("Token", "NFTs")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val selectedTabIndex by remember { mutableStateOf(0) }

    Log.d("assets", "the tokenList in assetsTabLayout : $tokenList")
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (tabRowRef, pagerRef) = createRefs()

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .constrainAs(tabRowRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            tabs.forEachIndexed { index, title ->

                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    text = {
                        Text(
                            text = title,
                            style = RepointTypography.titleSmall,
                            color = richBlack,
                            textAlign = TextAlign.Center
                        )
                    },
                )
            }

        }
        VerticalPager(
            state = pagerState,
            userScrollEnabled = false, // 🚨 Prevents swipe gestures
            modifier = Modifier
                .constrainAs(pagerRef) {
                    top.linkTo(tabRowRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .padding(32.dp)
        ) { page ->

            when (page) {

                0 -> {
                    ListScreen(items = tokenList)
                }

                1 -> {
                    // NFT tab: apply blur only to background, keeping text clear
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(12.dp)
                                .background(color = ghostWhite)
                                .blur(64.dp) // Blur applied to the background only
                        )

                        Text(
                            text = "Coming Soon",
                            style = RepointTypography.headlineSmall,
                            color = Color.Black,
                        )
                    }
                }

            }
        }

    }

}

@Composable
fun ListScreen(items: List<TokensBalance>?) {

    // val items = List(10) { "item ${it + 1} in tab ${page + 1}" }

    LazyColumn(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .fillMaxSize()
    ) {
        items(items.orEmpty()) { item ->


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                AsyncImage(
                    model = item.logo,
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = item.symbol.uppercase(), modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    Text(
                        text = item.name,
                        style = RepointTypography.labelSmall,
                        color = richBlack,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .align(Alignment.Start)
                    )

                }

                val amountToken = item.balanceFormatted.toDouble() ?: 0.0
                val formattedTokenAmount = DecimalFormat("#0.00").format(amountToken)

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {

                    Text(
                        text = formattedTokenAmount,
                        style = RepointTypography.titleMedium
                    )

                    val amountUsd = item.usdPrice
                    val formattedAmount = DecimalFormat("#0.00").format(amountUsd)
                    //USD
                    Text(
                        text = "$$formattedAmount",
                        style = RepointTypography.titleSmall
                    )


                }


            }
        }
    }

}


fun netWorthSection(tokenList: List<TokensBalance>?): Float {
    return tokenList?.sumOf { it.balanceFormatted.toDouble() * it.usdPrice.toDouble() }?.toFloat()
        ?: 0f
}

/*fun generateFakeData(tabIndex: Int): List<Tokens> {
    return List(20) { index ->
        Tokens(
            id = index,
            content = "Item ${index + 1} in Tab ${tabIndex + 1}"
        )
    }
}*/
