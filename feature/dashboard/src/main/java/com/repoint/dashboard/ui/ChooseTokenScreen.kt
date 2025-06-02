package com.repoint.dashboard.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointSearchBar
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.models.sharedmodels.remote.TokensBalance
import kotlinx.coroutines.launch

@Composable
fun ChooseTokenScreen(
    navController: NavController,
    viewModel: NetworkViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    web3ViewModel : Web3ViewModel = hiltViewModel(),
    isSend: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }

    val activeTokens by viewModel.activeTokens.collectAsState()
    val context = LocalContext.current
    val spManager = SpManager(context)
    val masterWalletId by spManager.getActiveWalletId().collectAsState(null)

    val cryptoNetworks by viewModel.networks.collectAsState()
// Suppose cryptoNetworks is your list of all BlockchainNetwork objects:

    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    var searchBarActive by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current

    val isLoading = !tokenImageLoaded && !networkImageLoaded

    LaunchedEffect(masterWalletId) {

        tokenImageLoaded = true
        networkImageLoaded = true
        if (!masterWalletId.isNullOrEmpty()) {

            Log.d("chooseToken", "Fetching active tokens for wallet : $masterWalletId")
            viewModel.fetchActiveTokens(masterWalletId!!)
        }

    }

    RepointAppBar(
        title = if (isSend) "Send" else "Receive", exp = {_,_,_ ->


            if (isLoading) {
                // 🔥 Show Loading Animation Centered
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoaderAnimation()
                }
            }else {
                Column(Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            searchBarActive = false
                        })
                    }){


                   RepointSearchBar(
                       query = searchQuery,
                       onQueryChange =  {searchQuery = it},
                       active = searchBarActive,
                       onActiveChange = {searchBarActive = it},
                       content = {
                           val filteredTokens = activeTokens.filter {
                               it.symbol.contains(searchQuery.lowercase(), ignoreCase = true) ||
                                       it.name.contains(searchQuery.lowercase(), ignoreCase = true)
                           }


                           LazyColumn(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .background(pureWhite)
                           ) {
                               if (filteredTokens.isEmpty()) {
                                   item {
                                       Text(
                                           text = "No tokens found",
                                           modifier = Modifier.padding(16.dp),
                                           color = Color.Gray
                                       )
                                   }
                               } else {
                                   items(filteredTokens) { token ->
                                       val networkForToken = cryptoNetworks?.find { it.id == token.networkId }
                                       val defaultNetworkLogoUrl = "" // empty string or null, and in your AsyncImage you provide a placeholder drawable resource
                                       val networkLogoUrl = networkForToken?.tokens?.firstOrNull()?.logoUrl ?: defaultNetworkLogoUrl

                                       masterWalletId?.let { walletId ->
                                           sendTokenItem(
                                               token,
                                               viewModel,
                                               web3ViewModel,
                                               walletViewModel,
                                               walletId,
                                               isSend,
                                               networkLogoUrl,
                                               navController
                                           )
                                       }
                                   }
                               }
                           }
                       },
                       onSearch = {

                       },
                       modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                   )


                    LazyColumn(modifier = Modifier.padding(16.dp)) {

                        if (activeTokens.isEmpty()) {
                            item {
                                Text(
                                    text = "No activated tokens found. Go to the Network screen and enable some.",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        }

                        items(activeTokens.filter {
                            it.symbol.contains(searchQuery, ignoreCase = true) ||
                                    it.name.contains(searchQuery, ignoreCase = true)
                        }) { token ->
                            val networkForToken = cryptoNetworks?.find { it.id == token.networkId }
                            val defaultNetworkLogoUrl = "" // empty string or null, and in your AsyncImage you provide a placeholder drawable resource
                            val networkLogoUrl = networkForToken?.tokens?.firstOrNull()?.logoUrl ?: defaultNetworkLogoUrl


                            masterWalletId?.let {
                                sendTokenItem(token,viewModel,web3ViewModel,walletViewModel,
                                    it,isSend,networkLogoUrl,navController)
                            }
                        }
                    }
                }
            }

        }, navController = navController
    )
}

@Composable
fun sendTokenItem(token : TokenEntity,viewModel: NetworkViewModel,web3ViewModel: Web3ViewModel,walletViewModel: WalletViewModel,masterWalletId : String,isSend: Boolean,networkUrl : String?,navController: NavController){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val networkByToken by viewModel.specificNetworkByToken.collectAsState()
    val networkForToken by viewModel.specificNetworkForToken.collectAsState()

    val activeTokens by viewModel.activeTokens.collectAsState()
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
                val network = viewModel.getNetworkById(token.networkId)
                viewModel.getNetworkForToken(tokenId)

                val chainId = network?.chainId
                val coinType = network?.coinType

                if (chainId == null){
                    Log.e("chooseToken", "❌ Chain ID not found for tokenId: ${token.tokenId}")
                    Toast.makeText(context, "Network info missing", Toast.LENGTH_SHORT).show()
                    return@launch
                }else
                {
                    web3ViewModel.fetchGasPrice(chainId.toLong())
                }



                // val coinType = viewModel.tokenWithNetworks.value?.network?.coinType
                Log.d("chooseToken", "coinType is : $coinType")

                if (coinType == null) {
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
                }

                val chainWallet = walletViewModel.getChainWallet(
                    masterWalletId.toString(),
                    coinType!!
                )
                Log.d("chooseToken", "the chainWAllet is : $chainWallet")
                Log.d(
                    "chooseToken",
                    "the masterWAllet id is : ${masterWalletId}"
                )
                val walletAddress = chainWallet?.address ?: ""
            /*    val jsonString = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("tokenBalances")
                val gson = Gson()*/
                if (tokenBalances.isEmpty()) {
                    Log.e("ChooseToken", "tokenBalances is empty or missing!")
                }

                Log.d("chooseToken", "wallet address is : $walletAddress")
                //navigate to the right screen
                if (isSend) {
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
                }
            }
        },
        networkUrl = networkUrl.toString(), onTokenImageLoaded = {tokenImageLoaded = true}, onNetworkImageLoaded = {networkImageLoaded = true}
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
            onTokenImageLoaded = { onTokenImageLoaded()},
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