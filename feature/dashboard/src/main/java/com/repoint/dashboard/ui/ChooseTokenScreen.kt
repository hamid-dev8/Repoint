package com.repoint.dashboard.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.local.TokenEntity
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

    var tokenImageLoaded by remember { mutableStateOf(false) }
    var networkImageLoaded by remember { mutableStateOf(false) }

    val isLoading = !tokenImageLoaded && !networkImageLoaded
    val networkOfToken by viewModel.specificNetworkByToken.collectAsState()

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
                Column(Modifier.fillMaxSize()) {


                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Rounded.ManageSearch,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    val scope = rememberCoroutineScope()

                    LazyColumn {

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
                                        val jsonString = navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.get<String>("tokenBalances")
                                        val gson = Gson()
                                        val tokenBalances: List<TokensBalance>? = try {
                                            val listType = object : TypeToken<List<TokensBalance>>() {}.type
                                            gson.fromJson(jsonString, listType)
                                        } catch (e: Exception) {
                                            Log.e("chooseToken", "Failed to parse tokenBalances", e)
                                            null
                                        }

                                        Log.d("chooseToken", "wallet address is : $walletAddress")
                                        //navigate to the right screen
                                        if (isSend) {
                                            //TODO add token balance

                                            //val balance = walletViewModel.gettok
                                            val balance = tokenBalances?.find { it.tokenAddress.lowercase() == token.contractAddress.lowercase() }?.balanceFormatted
                                                    ?: "0"
                                            Log.d("chooseToken", "token is : $tokenBalances")
                                            Log.d("chooseToken", "token is : ${networkOfToken?.nativeToken}")
                                            navController.navigate("sendToken/$walletAddress/$balance/$coinType/${token.contractAddress}/${chainId}")
                                        } else {
                                            navController.navigate("qrCode/$walletAddress/$masterWalletId/$tokenId")
                                        }
                                    }
                                },
                                networkUrl = activeTokens[0].logoUrl, onTokenImageLoaded = {tokenImageLoaded = true}, onNetworkImageLoaded = {networkImageLoaded = true}
                            )
                        }
                    }
                }
            }

        }, navController = navController
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
            .clickable { onClick() }
            .padding(vertical = 12.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        Text(token.symbol, style = RepointTypography.titleMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            token.name, style = RepointTypography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )


    }

}