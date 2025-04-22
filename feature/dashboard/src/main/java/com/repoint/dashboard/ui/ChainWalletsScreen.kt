package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.local.ChainWallet
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.User
import com.repoint.splash.accountmanager.SpManager
import kotlinx.coroutines.launch


@Composable
fun ChainWalletsScreen(
    walletViewModel: WalletViewModel = hiltViewModel(),
    userViewModel : UserViewModel = hiltViewModel(),
    spManager: SpManager = SpManager(LocalContext.current),
    navController : NavController
) {

    val coroutineScope = rememberCoroutineScope()
    var masterWallet by remember { mutableStateOf<List<MasterWallet>>(emptyList()) }
    var user by remember { mutableStateOf<User?>(null) }
    //val userId = remember(user) { user?.userId }

    LaunchedEffect(Unit) {
        user = userViewModel.fetchUser()

        val userId = user?.userId

        Log.d("wallets","user is $user")
        Log.d("wallets","user id is $userId")
        if (!userId.isNullOrEmpty()) {
            //chainWallets = walletViewModel.getAllMasterWallets(masterWalletId!!)
            masterWallet = walletViewModel.getAllMasterWallets(userId)
        }
    }

    RepointAppBar("Your Wallets", exp = {

        LazyColumn {
            items(masterWallet) { wallet ->
                WalletItem(wallet,
                    onRename = {newName ->
                        coroutineScope.launch {
                            user?.userId.let { userId ->
                                Log.d("wallets","user is $userId")
                                walletViewModel.renameChainWallet(wallet.masterWalletId,newName)
                                masterWallet = walletViewModel.getAllMasterWallets(userId = userId!!)
                            }
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            user?.userId.let { userId ->
                                Log.d("wallets","user is $userId")
                                walletViewModel.deleteChainWallet(wallet.masterWalletId)
                                masterWallet = walletViewModel.getAllMasterWallets(userId!!)
                            }
                        }
                    })
            }
        }


    }, navController = navController)

}

@Composable
fun WalletItem(wallet : MasterWallet,onRename : (String) -> Unit,onDelete : () -> Unit){

    var isRenaming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(wallet.name) }


    Log.d("wallets","wallets is $wallet")
    Card(modifier = Modifier.padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {

        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

            if (isRenaming) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    isRenaming = false
                    onRename(name)
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = wallet.name, style = RepointTypography.titleSmall)
                    //Text(text = formatAddress(wallet.userId), color = Color.Gray)
                }
                IconButton(onClick = {isRenaming = true}) {
                    Icon(Icons.Default.Edit , contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete , contentDescription = "")
                }
            }
        }


    }

}