package com.repoint.account.signup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCheckbox
import com.repoint.basics.atoms.RepointCommonButton


@Preview
@Composable
fun CreateWalletPreview() {
    Surface(Modifier.fillMaxSize()) {
        //WalletConfirmSurface()
    }

}

@Composable
fun CreateWalletTitle() {
    Column {

        Text(
            "This Secret phrase is the master key to your wallet",
            style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            "Tap on all checkboxes to confirm you understand " +
                    "the importance of your secret phrase",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )

    }
}


enum class CheckboxItemTexts(val displayText: String) {
    ITEM1("rePoint does NOT keep a copy of your secret phrase"),
    ITEM2(
        "Saving this digitally in plain text is NOT recommended. \n" +
                "Examples include screenshots , text files , or emailing yourself"
    ),
    ITEM3("Write down your secret phrase , and store it in a secure online location!")
}

@Composable
fun WalletConfirmSurface(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    onConfirm : (String) -> Unit)
 {
    var isChecked by remember { mutableStateOf(false) }
    RepointAppBar("Alert!", navController, exp = {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(bottom = 25.dp)
            )
            {

                Spacer(Modifier.padding(bottom = 16.dp))

                CreateWalletTitle()

                Column() {
                    for (item in CheckboxItemTexts.entries) {

                        Row(Modifier.padding(12.dp)) {

                            RepointCheckbox(
                                isChecked,
                                onCheckedChange = { isChecked = it },
                                size = 32.dp,
                                borderColor = Color.Black,
                                checkedColor = Color.Green,
                                uncheckedColor = Color.LightGray
                            )
                            Text(
                                item.displayText,
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.padding(bottom = 8.dp))
                        }

                    }

                }
            }
            RepointCommonButton(
                "Confirm",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                onClick = {
                    val walletId = viewModel.createUserWallet()
                    onConfirm(walletId)
                    //navController.navigate("phrase")
                })
        }
    })
}
