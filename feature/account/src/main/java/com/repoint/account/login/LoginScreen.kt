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
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.SimpleEditText


@Preview
@Composable
fun PreviewLogin() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginScreen(navController = navController)

}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel()
) {
    var walletNameInput by remember { mutableStateOf("") } // State to hold the user input
    var secretInput by remember { mutableStateOf("") } // State to hold the user input
    val clipboardManager = LocalClipboardManager.current
    val clipboardText = clipboardManager.getText()?.text
    val context = LocalContext.current



    RepointAppBar("MultiCoinWallet", exp = {
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
                    height = 256,
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
                    fontSize = 14.sp
                )


            }

            RepointCommonButton(
                "Restore Wallet", onClick = {

                   val walletId =  viewModel.importWallet(secretInput, walletNameInput)

                    Log.d("import","wallet is set and its id is = $walletId")
                },
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }, navController = navController)

}