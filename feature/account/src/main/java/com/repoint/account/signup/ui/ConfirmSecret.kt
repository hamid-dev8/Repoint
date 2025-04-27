package com.repoint.account.signup.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.RepointThreeTextSelectable
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.splash.accountmanager.SpManager
import kotlinx.coroutines.launch
import kotlin.random.Random


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun ConfirmPhrases(
    phrases: String,
    navController: NavController,
    onConfirm: (walletId: String) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
) {

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("walletConfirm")
    }
    val walletViewModel: WalletViewModel = hiltViewModel(parentEntry)

    val decodedPhrases = Uri.decode(phrases)
    val phraseList = decodedPhrases.split(" ")
    val shuffledList = phraseList.chunked(3).flatMap { it.shuffled() }
    val resultPair = calculateRandomStrings(phraseList)
    var isUserCorrect by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val spManager = remember { SpManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val walletCreated = walletViewModel.walletCreated.value

    val wallet = walletViewModel.getTempWallet()


    for (index in 0..3) {
        Log.d("confirmsss", " the result pair is : ${resultPair.get(index).first}")
    }
    Log.d("confirmsss", "orginal list : $phraseList")
    Log.d("confirmsss", "shuffled by 3 : $shuffledList")


    RepointAppBar("Confirm Secret Phrase", navController = navController, exp = {

        Box(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            resultPair.fastForEachIndexed { index, answer ->
                Log.d("confirmsss", " result pair is : ${resultPair.toString()}")


                Column {

                    Text(
                        "please tap on correct answer of the below seed phrases",
                        style = RepointTypography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(8.dp)
                            .padding(bottom = 8.dp)
                    )

                    RepointThreeTextSelectable(shuffledList, resultPair, isSelectedCorrectly = {
                        isUserCorrect = it
                        Log.d("isSelected", "is it ok? : $isUserCorrect")
                    })
                }
            }

            Log.d("confirmsss", "wallet is   sss : $wallet")


            RepointCommonButton(
                "Confirm", modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp), onClick = {

                    coroutineScope.launch {

                        val user = userViewModel.fetchUser()


                        Log.d("confirm", "wallet id is : ${wallet?.masterWalletId}")
                        if (user != null && wallet?.masterWalletId != null) {
                            //user Already exist
                            walletViewModel.confirmAndSaveWallet()
                            walletViewModel.linkUserToMasterWallet(
                                masterWalletId = wallet.masterWalletId,
                                userId = user.userId
                            )
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            Log.d("confirm","master wallet id is : ${wallet?.masterWalletId}")
                            walletViewModel.confirmAndSaveWallet()
                            wallet?.masterWalletId?.let { onConfirm(it) }
                        }
                    }
                }, enabled = isUserCorrect
            )
        }
    })

}


fun calculateRandomStrings(phrases: List<String>): List<Pair<Int, String>> {
    val result = mutableListOf<Pair<Int, String>>()

    phrases.chunked(3).forEachIndexed { chunkedIndex, chunk ->

        val randomIndexInChunk = Random.nextInt(chunk.size)
        val actualIndex = chunkedIndex * 3 + randomIndexInChunk + 1
        result.add(actualIndex to chunk[randomIndexInChunk])
    }
    return result
}