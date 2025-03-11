package com.repoint.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.repoint.basics.atoms.RepointSearchBar
import com.repoint.dashboard.NetworkViewModel
import com.repoint.database.dao.NetworkDao
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.local.LocalActiveNetworks
import com.repoint.models.sharedmodels.remote.BlockchainNetwork
import com.repoint.models.sharedmodels.remote.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CryptoManageScreen(networkDao: NetworkDao,networkViewModel: NetworkViewModel = hiltViewModel<NetworkViewModel>()) {
    val cryptoNetworks by networkViewModel.networks.collectAsState()


    var searchText by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("All Networks") }
    var searchBarActive by remember { mutableStateOf(false) }

    var cryptoAssets by remember { mutableStateOf(cryptoNetworks?.result) }


    var blockchainNetworks by remember { mutableStateOf(cryptoAssets) }
    val networks = listOf("All Networks") + cryptoAssets?.map { it.name }?.distinct().toString()

    Column(modifier = Modifier.padding(16.dp)) {


        RepointSearchBar(
            query = searchText,
            onQueryChange = { searchText = it },
            active = searchBarActive,
            onActiveChange = { searchBarActive = it }) {
            
        }

        NetworkDropdown(networks,selectedNetwork) { selectedNetwork = it }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            blockchainNetworks?.let {
                items(it.filter { network ->
                    (selectedNetwork == "All Networks" || network.name == selectedNetwork) &&
                            (network.name.contains(searchText, true))
                }) { network ->
                    CryptoAssetItem(network,networkDao) { updateToken ->
                        blockchainNetworks = blockchainNetworks!!.map { blockchain ->
                            if (blockchain.id == network.id){
                                blockchain.copy(tokens = blockchain.tokens.map {
                                    if (it.contractAddress == updateToken.contractAddress) updateToken
                                    else it
                                })
                            } else blockchain
                        }

                    }

                }
            }
        }
    }


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
    networkDao: NetworkDao,
    onToggle :  (Token) -> Unit
) {


    val enabledTokens = remember { mutableStateMapOf<String, Boolean>() }
    val tokens = asset.tokens

    LaunchedEffect(asset) {
        val activeNetworks = networkDao.getActiveNetworks()
        asset.tokens.forEach { token ->
            enabledTokens[token.contractAddress] = activeNetworks.any() { it.id == asset.id }
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Text(
            text = asset.name,
            style = RepointTypography.labelSmall,
            modifier = Modifier.padding(4.dp)
        )

        asset.tokens.forEach { token ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AsyncImage(
                    model = token.logoUrl,
                    contentDescription = token.name,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Token Detailes
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(token.symbol, style = RepointTypography.titleSmall)
                    Text(token.name, style = RepointTypography.titleSmall, color = Color.Gray)
                }

                Switch(
                    checked = enabledTokens[token.contractAddress] ?: false,
                    onCheckedChange = { checked ->

                        enabledTokens[token.contractAddress] = checked
                        if (checked) {
                            onToggle(token)
                            CoroutineScope(Dispatchers.IO).launch {
                                networkDao.insertActiveNetwork(LocalActiveNetworks(token.tokenId))
                            }
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                networkDao.deleteActiveNetwork(token.tokenId)
                            }
                        }

                    }
                )

            }

        }

    }


}