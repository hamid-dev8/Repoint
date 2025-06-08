package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.repoint.dashboard.CmcTokenViewModel
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoManageScreen(navController: NavController) {
    val networkViewModel: NetworkViewModel = hiltViewModel()
    val cmcTokenViewModel : CmcTokenViewModel = hiltViewModel()

    val cryptoNetworks by networkViewModel.networks.collectAsState()
    val allTokens by cmcTokenViewModel.tokens.collectAsState()

    var expanded by remember { mutableStateOf(false) }  // or come from your state


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

    LaunchedEffect(activeWalletId, expanded) {
        if (!activeWalletId.isNullOrEmpty()) {
            networkViewModel.initializeWithWallet(activeWalletId!!)
            cmcTokenViewModel.loadTokens()
            isLoading = false
            expanded = false
        }
    }


    val onExpandedChange: (Boolean) -> Unit = { expandedValue ->
        expanded = expandedValue
    }

    val searchBarState = rememberSearchBarState()
    val coroutineScope = rememberCoroutineScope()


    RepointAppBar("Manage Crypto", exp = { _, _, _ ->

        if (isLoading) {
            // 🔥 Show Loading Animation Centered
            Box(
                Modifier
                    .fillMaxSize()
                    .background(grayHound),
                contentAlignment = Alignment.Center
            ) {
                LoaderAnimation()
            }
        } else {
            Column(modifier = Modifier
                .pointerInput(Unit) {
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

                        LazyColumn(modifier = Modifier.fillMaxWidth().background(pureWhite)) {
                            items(filteredNetworks) { network ->
                                CryptoAssetItem(
                                    asset = network,
                                    viewModel = networkViewModel,
                                    activeWalletId = activeWalletId ?: "",
                                    onToggle = { updatedToken , networkId ->
                                        networkViewModel.toggleActiveNetwork(
                                            updatedToken.tokenId,
                                            !networkViewModel.activeTokens.value.any { it.tokenId == updatedToken.tokenId },
                                            activeWalletId ?: "",
                                            networkId = networkId,
                                            updatedToken = updatedToken
                                        )
                                    }
                                )
                            }
                        }
                    }, onSearch = {

                    }
                , modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )


            NetworkDropdown(networks, selectedNetwork, onNetworkSelected = {selectedNetwork = it}, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

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
                            onToggle = { updatedToken, networkId ->
                                networkViewModel.toggleActiveNetwork(
                                    tokenId = updatedToken.tokenId,
                                    !networkViewModel.activeTokens.value.any { it.tokenId == updatedToken.tokenId },
                                    activeWalletId!!, updatedToken, networkId
                                )
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
    onNetworkSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentWidth(Alignment.Start)) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(0.5.dp, lightGray)
                .height(48.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0), // light gray background
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                selectedNetwork,
                style = RepointTypography.labelLarge
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp)
        ) {
            networks.forEach { network ->
                DropdownMenuItem(
                    text = {
                        Text(
                            network,
                            style = RepointTypography.labelMedium
                        )
                    },
                    onClick = {
                        onNetworkSelected(network)
                        expanded = false
                    },
                    modifier = Modifier.padding(horizontal = 8.dp).wrapContentWidth()
                )
            }
        }
    }
}



@Composable
fun CryptoAssetItem(
    asset: BlockchainNetwork,
    viewModel: NetworkViewModel,
    onToggle: (Token, Int) -> Unit,
    activeWalletId: String
) {
    val activeTokens by viewModel.activeTokens.collectAsState()
    val activeTokenIds = remember(activeTokens) { activeTokens.map { it.tokenId }.toSet() }


    Log.d("CryptoAssetItem", "Rendering ${asset.name} tokens: ${asset.tokens}")

    val tokens = asset.tokens
    Log.d("CryptoManageScreen", "shows")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(0.5.dp, lightGray)
                    .background(grayHound, RoundedCornerShape(22.dp))
                    .padding(8.dp)
                ,
                verticalAlignment = Alignment.CenterVertically
            ) {

                TokenWithNetworkBadge(token, tokens[0].logoUrl)
                Spacer(modifier = Modifier.width(8.dp))

                // Token Detailes
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(token.symbol, style = RepointTypography.titleSmall)
                    Text(token.name, style = RepointTypography.labelSmall, color = Color.Gray)
                }
                Switch(
                    checked = activeTokenIds.contains(token.tokenId),
                    onCheckedChange = { checked ->
                        onToggle(token, asset.id)
                        // enabledTokens[token.contractAddress] = checked
                        Log.d("CryptoAssetItem", "Toggling token: $token (checked: $checked)")
                        // viewModel.toggleActiveNetwork(token.tokenId, checked, activeWalletId, updatedToken = token, networkId = ???)
                    },
                    modifier = Modifier.scale(0.75f)
                )
            }
        }
    }
}