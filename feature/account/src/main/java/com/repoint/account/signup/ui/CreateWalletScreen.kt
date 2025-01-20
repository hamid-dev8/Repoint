package com.repoint.account.signup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.BigPng
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCheckbox
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.repointBlue


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
            style = RepointTypography.titleLarge, textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp, top = 14.dp)
        )
        Text(
            "Tap on all checkboxes to confirm you understand " +
                    "the importance of your secret phrase",
            style = RepointTypography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp, top = 16.dp, bottom = 16.dp),
            color = Color.Gray
        )

    }
}


enum class CheckboxItemTexts(val displayText: String) {
    ITEM1("rePoint does NOT keep a copy of your secret phrase"),
    ITEM2(
        "Saving this digitally in plain text is NOT recommended." +
                "Examples include screenshots , text files , or emailing yourself"
    ),
    ITEM3("Write down your secret phrase , and store it in a secure online location!")
}

@Composable
fun WalletConfirmSurface(
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    onConfirm: (String) -> Unit
) {
    val checkboxesState = remember {
        mutableStateMapOf(
            CheckboxItemTexts.ITEM1 to false,
            CheckboxItemTexts.ITEM2 to false,
            CheckboxItemTexts.ITEM3 to false
        )
    }

    val isButtonEnabled = checkboxesState.values.all { it }

    RepointAppBar("", navController, exp = {
        Box(
            Modifier.fillMaxSize().padding(16.dp)) {
            Column(Modifier.padding(16.dp).padding(bottom = 16.dp))
            {

                BigPng(
                    com.repoint.dependencies.R.drawable.trusted,
                    Modifier
                        .align(Alignment.CenterHorizontally),
                    aspectRatioWidth = 16f,
                    aspectRatioHeight = 10f
                )

                Spacer(Modifier.padding(bottom = 16.dp))

                CreateWalletTitle()

                Column() {
                    checkboxesState.forEach { (label, state) ->

                        Row(
                            Modifier.padding(16.dp,top = 28.dp).clickable { checkboxesState[label] = !state  },
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RepointCheckbox(
                                isChecked = state,
                                onCheckedChange = { checkboxesState[label] = it },
                                size = 32.dp,
                                borderColor = Color.Black,
                                checkedColor = repointBlue,
                                uncheckedColor = Color.LightGray,
                            )
                            Text(
                                text = label.displayText,
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp),
                                textAlign = TextAlign.Start,

                                style = RepointTypography.labelMedium
                            )
                            Spacer(Modifier.padding(bottom = 8.dp))
                        }

                    }

                }
            }
            RepointCommonButton(
                "Confirm",
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = {
                    val walletId = viewModel.createUserWallet()
                    onConfirm(walletId)
                    //navController.navigate("phrase")
                }, enabled = isButtonEnabled,
            )
        }
    })
}
