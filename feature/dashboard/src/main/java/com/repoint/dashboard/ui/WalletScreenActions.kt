package com.repoint.dashboard.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.AuthBottomSheetContent
import com.repoint.basics.atoms.BalanceScreen
import com.repoint.basics.atoms.CircularButtonWithText
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.SearchTextField
import com.repoint.basics.atoms.ViewPagerRobot
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dashboard.TokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.R
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokensBalance
import com.repoint.splash.accountmanager.SpManager
import kotlinx.coroutines.coroutineScope
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


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val spManager = SpManager(context)

    //val selectedWalletName = selectedWallet?.name ?: masterWallets.firstOrNull()?.name ?: "No Wallet"
    val walletNames = masterWallets.mapIndexed { index, wallet ->
        wallet.name.ifBlank { "Wallet ${index + 1}" }
    }
    var selectedWalletName by remember { mutableStateOf("") }


    val activeWalletId by spManager.activeWalletIdFlow.collectAsState()

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

                if (selectedWallet != null && selectedWallet!!.masterWalletId != activeWalletId) {
                    spManager.setActiveWallet(selectedWallet!!.masterWalletId)
                }

                //TODO generic the wallet
                chainWallets = walletViewModel.getAllChainWallets(activeWalletId!!)
                //   spManager.setActiveWallet(masterWallets[0].masterWalletId)

                Log.d("token", "selected wallet is currently : $selectedWallet")

                //Default to Polygon chain for balances
                val address = chainWallets.firstOrNull { it.coinType == 60 }?.address
                if (address != null) {
                    activeAddress = address
                    web3ViewModel.testConnectionToWeb3()
                    Log.d("token","active adress is : $activeAddress , selected wallet is : $selectedWallet , masterId is : ${selectedWallet?.masterWalletId}")
                    // tokenList = tokenViewModel.getTokenBalance(address = address, chain = "polygon")

                    tokenList = tokenViewModel.getMergedActivatedTokenBalances(
                        activeAddress!!, "polygon",
                        selectedWallet!!.masterWalletId
                    )

                    selectedWallet?.let { networkViewModel.initializeWithWallet(it.masterWalletId) }

                    // tokensOf = tokenViewModel.getAllActivatedTokenBalances(activeAddress, "polygon")
                    //tokensOf = tokenViewModel.getMergedActivatedTokenBalances(address,"polygon")
                    ether = web3ViewModel.fetchNativeWalletBalance(activeAddress!!)

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
    }



    RepointAppBar("wallet", exp = {


        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            SearchTextField("", onValueChange = { text ->

            }, modifier = Modifier.padding(8.dp))


            val amount = netWorthSection(tokenList?.result)
            val formattedBalanceAmount = DecimalFormat("#0.00").format(amount)
            BalanceScreen(masterWallets, balance = "$$formattedBalanceAmount", onAddWallet = {
                showBottomSheet = true
            }, onWalletSelected = { selectedWallet ->
                coroutineScope.launch {
                    spManager.setActiveWallet(selectedWallet.masterWalletId)
                    networkViewModel.initializeWithWallet(selectedWallet.masterWalletId)
                    Log.d("token", "selected wallet changed : ${selectedWallet.masterWalletId}")
                    Log.d("token", "master wallet changed : $masterWallets")
                }
                //TODO ezafe kardane safe add wallet va sakht wallet jadid
                //walletViewModel.createUserWallet()

                val index = masterWallets.indexOfFirst { it.masterWalletId == selectedWallet.masterWalletId }
                selectedWalletName = selectedWallet.name.ifBlank { "Wallet + $index" }
            }, selectedWalletName = selectedWalletName)

            if (masterWallets.isNotEmpty() && !activeAddress.isNullOrEmpty()) {
                ActionsRow(navController, wallet = selectedWallet, tokenList, activeAddress!!)
            }
            ViewPagerRobot()

            AssetsTabLayout(tokenList?.result.orEmpty())
            //BasicTabLayout(actions = )
        }

    }, navController = navController, isSettings = true, onSettingsClick = {
        navController.navigate("settings")
    }, showEndIcon = true, onEndIconClick = {
        navController.navigate("networks")
    })

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
}


@Composable
fun ActionsRow(
    navController: NavController,
    wallet: MasterWallet?,
    tokenList: NativesBalance?,
    activeAddress: String
) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        //arrowUp
        CircularButtonWithText(
            icon = Icons.AutoMirrored.Rounded.CallMade,
            "Send",
            onClick = {
                //todo handle which is native token to do gas fees
                //send choose token // Todo modify send

                //navController.navigate("sendToken/${wallet?.address}/${tokenList?.result?.get(0)?.balanceFormatted}")
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "tokenBalances",
                    tokenList?.result
                )
                navController.navigate("chooseToken/${true}")
            },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.AutoMirrored.Rounded.CallReceived,
            "Receive",
            onClick = {
                //receive choose token // Todo modify receive
                val encodedAddress = Uri.encode(wallet?.masterWalletId)
                //navController.navigate("qrCode/$encodedAddress")
                navController.navigate("chooseToken/${false}")
            },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Rounded.SwapVert,
            "Swap",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = ImageVector.vectorResource(R.drawable.ic_robot),
            "To Bot",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Filled.History,
            "History",
            onClick = {
                val encodedAddress = Uri.encode(activeAddress)
                val balance = tokenList?.result?.firstOrNull()?.usdPrice ?: 0.0
                val formattedBalance = DecimalFormat("#0.00").format(balance)

                navController.navigate("history/$formattedBalance/$encodedAddress")
            },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )

    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
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

