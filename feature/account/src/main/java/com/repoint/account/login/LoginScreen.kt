package com.repoint.account.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.SimpleEditText
import com.repoint.dependencies.theme.RepointTypography


@Preview
@Composable
fun PreviewLogin() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginScreen(navController = navController, onConfirm = {})

}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel(),
    userViewModel : UserViewModel = hiltViewModel(),
    onConfirm: (String) -> Unit
) {
    var walletNameInput by remember { mutableStateOf("") } // State to hold the user input
    var secretInput by remember { mutableStateOf("") } // State to hold the user input
    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text
    val context = LocalContext.current



    RepointAppBar("Multi-CoinWallet", exp = {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {

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

                    val walletId = viewModel.importWallet(secretInput, walletNameInput)

                    Log.d("import", "wallet is set and its id is = $walletId")

                    if (walletId != null) onConfirm(walletId) else Toast.makeText(
                        context,
                        "its not a valid Wallet",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }, navController = navController)

}