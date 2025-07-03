package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointSearchBar
import com.repoint.dashboard.CmcTokenViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.models.sharedmodels.local.ActiveTokenKey
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.models.sharedmodels.local.TokenPerChainUiModel
import com.repoint.models.sharedmodels.rpc.AlchemyChain
import com.repoint.models.sharedmodels.ui.ApiResult
import com.repoint.models.sharedmodels.ui.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun CryptoManageScreen(navController: NavController) {
    val cmcTokenViewModel: CmcTokenViewModel = hiltViewModel()

   // val tokens by cmcTokenViewModel.tokens.collectAsState()
    val activeTokenIds by cmcTokenViewModel.activeTokenIds.collectAsState()
    val perChainTokens by cmcTokenViewModel.perChainTokens.collectAsState()
    val activeTokenKeys by cmcTokenViewModel.activeTokenKeyFlow.collectAsState()



    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    var selectedNetwork by remember { mutableStateOf("All Networks") }
    var searchBarActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val spManager = remember { SpManager(context) }

    val activeWalletId by spManager.getActiveWalletId().collectAsState(initial = null)
    val isPaging by cmcTokenViewModel.isPaging.collectAsState()
    val focusManager = LocalFocusManager.current

    val searchState by cmcTokenViewModel.searchState.collectAsState()
    val query by cmcTokenViewModel.searchQuery.collectAsState()

    val isLoading = searchState is UiState.Loading && query.length >= 2
    val isInitialLoaded by cmcTokenViewModel.isInitialLoaded.collectAsState()


    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var showLoader by remember { mutableStateOf(false) }

    val tokenIds = (searchState as? UiState.Success)?.data?.map { it.id }

    val searchPerChainTokens = produceState<List<TokenPerChainUiModel>>(initialValue = emptyList(), tokenIds) {
        if (!tokenIds.isNullOrEmpty()) {
            val metadataResult = cmcTokenViewModel.fetchMetadata(tokenIds)
            if (metadataResult is ApiResult.Success) {
                value = cmcTokenViewModel.toPerChainUiModels(metadataResult.data.data)
            }
        }
    }

// Whenever the bar becomes active, grab focus & show keyboard
    LaunchedEffect(searchBarActive) {
        if (searchBarActive) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(searchState) {
        if (searchState is UiState.Loading) {
            delay(500) // Delay only if still loading
            if (searchState is UiState.Loading) showLoader = true
        } else {
            showLoader = false
        }
    }

    LaunchedEffect(activeWalletId) {
        if (activeWalletId != null && !isInitialLoaded) {
            cmcTokenViewModel.loadNextDbPage()
            activeWalletId?.let { cmcTokenViewModel.setCurrentWallet(it) }
        }
    }

    LaunchedEffect(query) {
        snapshotFlow { query }
            .debounce(300)
            .filter { it.isNotBlank() && it.length >= 3 }
            .distinctUntilChanged()
            .collect {
                cmcTokenViewModel.performSearch(it)
            }
    }

    // Only trigger pagination when NOT searching
    /*   LaunchedEffect(Unit) {
           snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
               .collectLatest { lastIndex ->
                   if (query.length < 2 && lastIndex != null && lastIndex >= tokens.lastIndex - 2) {
                       cmcTokenViewModel.loadMapPageAndShow()
                   }
               }
       }*/

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val visibleItems = layoutInfo.visibleItemsInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = visibleItems.lastOrNull()?.index ?: 0

                lastVisible >= totalItems - 4  // Trigger near end
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    cmcTokenViewModel.loadNextDbPage()  // 👈 Your manual page loader
                }
            }
    }


    LaunchedEffect(perChainTokens) {
        Log.d("ScreenDebug", "Tokens count: ${perChainTokens.size}")
    }

    val onExpandedChange: (Boolean) -> Unit = { expandedValue ->
        expanded = expandedValue
    }

    RepointAppBar("Manage Crypto", exp = { _, _, _ ->

            Column(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Close search bar when tapping outside
                            if (searchBarActive) {
                                searchBarActive = false
                                focusManager.clearFocus()
                            }
                        }
                    )
                }) {

                RepointSearchBar(
                    query = query,
                    onQueryChange = {
                        cmcTokenViewModel.updateSearchQuery(it)
                    },
                    active = searchBarActive,
                    onActiveChange = {
                        searchBarActive = it
                        if (it) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                    content = {
                        // Search results shown inside the SearchBar dropdown
                        when (searchState) {
                            is UiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(), // This ensures it uses all available space
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            is UiState.Error -> {
                                val message = (searchState as UiState.Error).messages
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Error: $message",
                                        color = Color.Red
                                    )
                                }
                            }

                            is UiState.Success -> {
                                val searchResults =
                                    (searchState as UiState.Success<List<CmcTokenEntity>>).data
                                if (searchResults.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No matching tokens found.",
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize()
                                    ) {



                                        //search section part
                                        items(perChainTokens) { token ->

                                            val isActive = activeTokenKeys.contains(
                                                ActiveTokenKey(tokenId = token.tokenId, chain = token.chainName.name.lowercase())
                                            )

                                            CmcPerChainTokenItem (
                                                token = token,
                                                isActive = isActive,
                                                onToggle = {
                                                    cmcTokenViewModel.toggleToken(
                                                        token.tokenId,
                                                        token.contractAddress,
                                                        chain = token.chainName
                                                    )
                                                    // Optionally close search after selection
                                                    // searchBarActive = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onSearch = {},
                    onClear = {
                        // cmcTokenViewModel.updateSearchQuery("")
                        searchBarActive = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    focusRequester = focusRequester
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Only show paginated tokens when not searching
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    state = listState
                ) {
                    if (perChainTokens.isEmpty() && !isPaging) {
                        item {
                            Text(
                                "No tokens available.",
                                Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    } else {
                        items(perChainTokens) { token ->

                            val isActive = activeTokenKeys.contains(
                                ActiveTokenKey(tokenId = token.tokenId, chain = token.chainName.name.lowercase())
                            )
                            CmcPerChainTokenItem (
                                token = token,
                                isActive = isActive,
                                onToggle = {
                                    cmcTokenViewModel.toggleToken(
                                        token.tokenId,
                                        token.contractAddress,
                                        chain = token.chainName
                                    )
                                }
                            )
                        }
                    }

                    // Show pagination loader at the bottom
                    if (isPaging) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
    }, navController = navController)
}
@Composable
fun CmcPerChainTokenItem(
    token: TokenPerChainUiModel,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, lightGray)
            .background(grayHound)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = token.logo,
            contentDescription = "${token.name} Logo",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("${token.symbol} (${token.chainDisplayName})", style = RepointTypography.titleSmall)
            Text(token.name, style = RepointTypography.labelSmall, color = Color.Gray)
        }

        Switch(
            checked = isActive,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.75f)
        )
    }
}

@Composable
fun CmcTokenItem(
    token: CmcTokenEntity,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, lightGray)
            .background(grayHound)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = token.logo,
            contentDescription = "${token.name} Logo",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(token.symbol, style = RepointTypography.titleSmall)
            Text(token.name, style = RepointTypography.labelSmall, color = Color.Gray)
        }

        Switch(
            checked = isActive,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.75f)
        )
    }
}


/*searchhhh*/

/*if (query.length >= 2) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            state = listState
                        ) {
                            if (displayTokens.isEmpty()) {
                                item {
                                    Text("No matching tokens found.", Modifier.padding(16.dp), color = Color.Gray)
                                }
                            } else {
                                items(displayTokens) { token ->
                                    CmcTokenItem(
                                        token = token,
                                        isActive = activeTokenIds.contains(token.id),
                                        onToggle = { cmcTokenViewModel.toggleToken(token.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Enter at least 2 characters", color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                    */

