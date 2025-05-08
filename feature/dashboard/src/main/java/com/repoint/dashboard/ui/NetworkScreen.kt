package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointSearchBar
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.Token

@Composable
fun CryptoManageScreen(navController: NavController) {
    val networkViewModel: NetworkViewModel = hiltViewModel()

    val cryptoNetworks by networkViewModel.networks.collectAsState()


    var searchText by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("All Networks") }
    var searchBarActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val spManager = remember { SpManager(context) }

    val activeWalletId by spManager.getActiveWalletId().collectAsState(initial = null)

    val networks = listOf("All Networks") + cryptoNetworks?.map { it.name }?.distinct().orEmpty()
    val blockchainNetworks = remember(cryptoNetworks) {
        cryptoNetworks?.map { it.copy() } ?: emptyList()
    }

    Log.d("CryptoManageScreen", "block chain network is : $blockchainNetworks")

    var isLoading by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(activeWalletId) {
        if (!activeWalletId.isNullOrEmpty()) {
            networkViewModel.initializeWithWallet(activeWalletId!!)
            isLoading = false
        }
    }



    RepointAppBar("Manage Crypto", exp = {

        if (isLoading) {
            // 🔥 Show Loading Animation Centered
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoaderAnimation()
            }
        } else {
            Column(modifier = Modifier.padding(16.dp).pointerInput(Unit){
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    searchBarActive = false
                })
            }) {
                Log.d("CryptoManageScreen", "Networks shows")


                RepointSearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    active = searchBarActive,
                    onActiveChange = { searchBarActive = it }, content = {
                        val filteredNetworks = cryptoNetworks
                            ?.filter { selectedNetwork == "All Networks" || it.name == selectedNetwork }
                            ?.mapNotNull { network ->
                                val filteredTokens = network.tokens.filter {
                                    it.name.contains(searchText, ignoreCase = true) ||
                                            it.symbol.contains(searchText, ignoreCase = true)
                                }
                                if (filteredTokens.isNotEmpty()) network.copy(tokens = filteredTokens) else null
                            } ?: emptyList()

                        LazyColumn(modifier = Modifier.fillMaxWidth().background(ghostWhite)) {
                            items(filteredNetworks) { network ->
                                CryptoAssetItem(
                                    asset = network,
                                    viewModel = networkViewModel,
                                    activeWalletId = activeWalletId ?: "",
                                    onToggle = { updatedToken ->
                                        networkViewModel.toggleActiveNetwork(
                                            updatedToken.tokenId,
                                            !networkViewModel.activeTokens.value.any { it.tokenId == updatedToken.tokenId },
                                            activeWalletId ?: ""
                                        )
                                    }
                                )
                            }
                        }
                    }, onSearch = {

                    })

                NetworkDropdown(networks, selectedNetwork) { selectedNetwork = it }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    Log.d(
                        "CryptoManageScreen",
                        "LazyColumn triggered with networks: $blockchainNetworks"
                    )

                    if (blockchainNetworks.isEmpty()) {
                        item {
                            Text(
                                "No data available",
                                color = Color.Red
                            )
                        } // ✅ Show error message
                    }

                    cryptoNetworks?.let { it ->
                        Log.d("CryptoManageScreen", "crypto assets : $cryptoNetworks")
                        items(it.filter { network ->
                            (selectedNetwork == "All Networks" || network.name == selectedNetwork) &&
                                    (network.name.contains(searchText, true))
                        }) { network ->
                            CryptoAssetItem(
                                network,
                                networkViewModel,
                                activeWalletId = activeWalletId!!,
                                onToggle = { updateToken ->
                                    blockchainNetworks.map { blockchain ->
                                        if (blockchain.id == network.id) {
                                            blockchain.copy(tokens = blockchain.tokens.map {
                                                if (it.contractAddress == updateToken.contractAddress) updateToken else it
                                            })
                                        } else blockchain
                                    }
                                })
                        }
                    }
                }
            }
        }

    }, navController = navController)

}


@Composable
fun NetworkDropdown(
    networks: List<String>,
    selectedNetwork: String,
    onNetworkSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedNetwork)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            networks.forEach { network ->
                DropdownMenuItem(
                    text = { Text(network) },
                    onClick = {
                        onNetworkSelected(network)
                        expanded = false
                    }
                )

            }

        }
    }


}


@Composable
fun CryptoAssetItem(
    asset: BlockchainNetwork,
    viewModel: NetworkViewModel,
    onToggle: (Token) -> Unit,
    activeWalletId: String
) {


    val activeNetworks by viewModel.activeNetworks.collectAsState()
    val activeTokens by viewModel.activeTokens.collectAsState()
    val activeTokenIds = remember(activeTokens) { activeTokens.map { it.tokenId }.toSet() }
    val networks by viewModel.networks.collectAsState()

    //val networks by viewModel.networks.collectAsState() // ✅ Observe the StateFlow
    //  val error by viewModel.apiError.collectAsState() // ✅ Observe errors
    Log.d("CryptoAssetItem", "Rendering ${asset.name} tokens: ${asset.tokens}")

    val enabledTokens = remember { mutableStateMapOf<String, Boolean>() }
    val tokens = asset.tokens
    Log.d("CryptoManageScreen", "shows")
    /*  LaunchedEffect(activeNetworks) {
          Log.d("CryptoManageScreen", "Calling fetchNetworks()")


          Log.d("repointnetwork", "network is : $asset")


          if (asset.tokens.isNotEmpty()) { // ✅ Ensure network has tokens
              asset.tokens.forEach { token ->
                  enabledTokens[token.contractAddress] = activeTokens.any() { it.tokenId == token.tokenId} // ✅ Set it as enabled
              }
              Log.d("Network", "network tokens: ${asset.tokens}")
          }
      }*/

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text(
            text = asset.name,
            style = RepointTypography.labelSmall,
            modifier = Modifier.padding(4.dp)
        )

        asset.tokens.forEach { token ->
            Log.d("CryptoAssetItem", "Token: ${token.name}, Logo URL: ${token.logoUrl}")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /*AsyncImage(
                    model = token.logoUrl,
                    contentDescription = token.name,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )*/
                TokenWithNetworkBadge(token, tokens[0].logoUrl)
                /*  AsyncImage(
                      model = ImageRequest.Builder(LocalContext.current)
                          .data(token.logoUrl.trim())
                          .crossfade(true)
                          .diskCacheKey(token.logoUrl) // helps prevent cache miss
                          .memoryCacheKey(token.logoUrl).listener(
                              onError = { request, throwable ->
                                  Log.e(
                                      "COIL_IMAGE",
                                      "Image Load failed : ${token.logoUrl}",
                                      throwable.throwable
                                  )
                              },
                              onSuccess = { _, _ ->
                                  Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                              }
                          )
                          .build(),
                      contentDescription = token.name,
                      modifier = Modifier.size(48.dp),
                      contentScale = ContentScale.Fit,
                      placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
                      error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
                  )
  */

                Spacer(modifier = Modifier.width(12.dp))

                // Token Detailes
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(token.symbol, style = RepointTypography.titleSmall)
                    Text(token.name, style = RepointTypography.titleSmall, color = Color.Gray)
                }
//enabledTokens[token.contractAddress] ?: false
                Switch(
                    checked = activeTokenIds.contains(token.tokenId),
                    onCheckedChange = { checked ->
                        onToggle(token)
                        // enabledTokens[token.contractAddress] = checked
                        Log.d("CryptoAssetItem", "Toggling token: $token (checked: $checked)")
                        viewModel.toggleActiveNetwork(token.tokenId, checked, activeWalletId)
                    }
                )
            }
        }
    }
}

/*
*  CoroutineScope(Dispatchers.IO).launch {
                                viewModel.deleteActiveNetwork(token.tokenId)
                            }*/
