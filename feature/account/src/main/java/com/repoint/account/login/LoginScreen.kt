package com.repoint.account.login

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.SimpleEditText
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
import kotlinx.coroutines.launch


@Preview
@Composable
fun PreviewLogin() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginScreen(navController = navController, onConfirm = {})

}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun LoginScreen(
    navController: NavController,
    onConfirm: (String) -> Unit,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("login")
    }
    val walletViewModel: WalletViewModel = hiltViewModel(parentEntry)


    var walletNameInput by remember { mutableStateOf("") } // State to hold the user input
    var secretInput by remember { mutableStateOf("") } // State to hold the user input
    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text
    val context = LocalContext.current
    val spManager = SpManager(context)


    val coroutineScope = rememberCoroutineScope()

    RepointAppBar("Multi-CoinWallet", exp = {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {

                SimpleEditText(
                    "Wallet name :",
                    72,
                    isItpasteNeed = false,
                    userInput = walletNameInput,
                    onInputChange = { walletNameInput = it },
                    onPaste = {

                    })

                SimpleEditText(
                    "Secret phrase :",
                    height = 148,
                    true,
                    userInput = secretInput,
                    onInputChange = { secretInput = it },
                    onPaste = {
                        if (clipboardText != null) {
                            secretInput = clipboardText
                            Toast.makeText(
                                context,
                                "Pasted from clipboard!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })


                Log.d("import", " text is $secretInput")

                Text(
                    "Enter your 12 Security words",
                    Modifier.align(Alignment.CenterHorizontally),
                    style = RepointTypography.labelMedium
                )


            }

            RepointCommonButton(
                "Restore Wallet", onClick = {

                    coroutineScope.launch {

                        val user = userViewModel.fetchUser()

                        if (walletViewModel.tempMasterWallet != null) {
                            val nextIndex = walletViewModel.generateNextWalletIndex(user?.userId)
                            Log.d("confirm","next index is : ${walletViewModel.generateNextWalletIndex(user?.userId)}")
                            Log.d("confirm","user id is : ${user?.userId}")
                            val finalName = if (walletNameInput.trim().isNotBlank()) {
                                walletNameInput.trim()
                            } else {
                                walletViewModel.generateDefaultWalletName(nextIndex)
                            }


                            walletViewModel.tempMasterWallet = walletViewModel.tempMasterWallet!!.copy(
                                name = finalName,
                                walletIndex = nextIndex,
                                userId = user?.userId
                            )
                        }

                        val phrase = walletViewModel.generatedImportedWalletInMemory(
                            secretInput,
                            walletNameInput
                        )
                        val phraseString = phrase.joinToString(" ")
                        //walletViewModel.importWallet(phraseString,walletNameInput)
                        walletViewModel.confirmAndSaveWallet()



                        if (user != null && walletViewModel.tempMasterWallet?.masterWalletId != null) {
                            Log.d("walletcreate", "phraseString with user is : $phraseString")
                            Log.d("walletcreate", "wallet name with user is : $walletNameInput")
                            Log.d("walletcreate", "wallet with user is  : ${walletViewModel.tempMasterWallet}")


                            walletViewModel.linkUserToMasterWallet(
                                masterWalletId = walletViewModel.tempMasterWallet!!.masterWalletId,
                                user.userId
                            )

                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            Log.d("walletcreate", "phraseString is : $phraseString")
                            //walletViewModel.importWallet(phraseString, walletName = walletNameInput)
                            Log.d("walletcreate", "wallet name is : $walletNameInput")
                            Log.d("walletcreate", "wallet is : ${walletViewModel.tempMasterWallet}")
                            walletViewModel.tempMasterWallet?.masterWalletId?.let { onConfirm(it) }
                            Toast.makeText(
                                context,
                                "its not a valid Wallet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        ///val walletId = viewModel.importWallet(secretInput, walletNameInput)
                        // Log.d("import", "wallet is set and its id is = $wallet")
                    }
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                }
            )
        }
    }, navController = navController)
}