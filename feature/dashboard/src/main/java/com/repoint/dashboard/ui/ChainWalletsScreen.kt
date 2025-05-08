package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.local.MasterWallet
import com.repoint.models.sharedmodels.local.User
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
    val reactiveMasterWallets by walletViewModel.masterWallets.collectAsState()
    //val userId = remember(user) { user?.userId }

    LaunchedEffect(Unit) {
        user = userViewModel.fetchUser()

        val userId = user?.userId

        Log.d("wallets","user is $user")
        Log.d("wallets","user id is $userId")
        if (!userId.isNullOrEmpty()) {
            //chainWallets = walletViewModel.getAllMasterWallets(masterWalletId!!)
              walletViewModel.loadMasterWallets(userId)

        }
    }

    RepointAppBar("Your Wallets", exp = {

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(reactiveMasterWallets) { wallet ->
                WalletItem(wallet,
                    onRename = {newName ->
                        coroutineScope.launch {
                            user?.userId.let { userId ->
                                Log.d("wallets","user is $userId")
                                if (userId != null) {
                                    walletViewModel.renameMasterWallet(wallet.masterWalletId,newName,userId)
                                }
                                //masterWallet = walletViewModel.getAllMasterWallets(userId = userId!!)
                            }
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            user?.userId.let { userId ->
                                Log.d("wallets","user is $userId")
                                if (userId != null) {
                                    walletViewModel.deleteMasterWallet(wallet.masterWalletId,userId)
                                    val remainingWallets = walletViewModel.getAllMasterWallets(userId)
                                    if (remainingWallets.isEmpty()){
                                        spManager.clearAllSessionData()
                                        navController.navigate("splash"){
                                            popUpTo(0){inclusive = true}
                                        }
                                    }
                                }

                              //  masterWallet = walletViewModel.getAllMasterWallets(userId!!)
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
    var showDeleteDialog by remember { mutableStateOf(false) } // ✅ delete dialog state


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
                IconButton(onClick = {
                    showDeleteDialog = true // ✅ Show dialog when click delete
                }) {
                    Icon(Icons.Default.Delete , contentDescription = "Delete")
                }
            }
        }
    }

    if (showDeleteDialog){
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Wallet") },
            text = { Text(text = "Are you sure you want to delete this wallet? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(text = "Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}