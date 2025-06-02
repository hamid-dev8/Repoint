package com.repoint.account.signup.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.PhraseChallengeRow
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.RepointThreeTextSelectable
import com.repoint.basics.atoms.WalletCreationStepProgress
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.RepointTypography
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
    val context = LocalContext.current
    val spManager = remember { SpManager(context) }

    val userId = spManager.getUserIdFlow().collectAsState(initial = null)
    val userExists = userId.value != null


    val decodedPhrases = Uri.decode(phrases)
    val phraseList = Uri.decode(phrases).split(" ")
    val shuffledList = phraseList.chunked(3).flatMap { it.shuffled() }
    val challengeRows = remember {
        phraseList
            .let { generateChallengeRows(it) } // see next step
    }
    var isUserCorrect by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val walletCreated = walletViewModel.walletCreated.value

    val wallet = walletViewModel.getTempWallet()

    val defaultName = remember { mutableStateOf(wallet?.name.orEmpty()) }


    Log.d("confirmsss", "orginal list : $phraseList")
    Log.d("confirmsss", "shuffled by 3 : $shuffledList")

    RepointAppBar("Confirm Secret Phrase", navController = navController, exp = { _,_,_ ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

                Column(Modifier.padding(16.dp)) {

                    WalletCreationStepProgress(currentStep = 2, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 12.dp), userExists = false)


                    Text(
                        "please tap on correct answer of the below seed phrases",
                        style = RepointTypography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(16.dp)
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = defaultName.value,
                        onValueChange = { defaultName.value = it },
                        label = { Text("Wallet Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )
                    RepointThreeTextSelectable(
                        rows = challengeRows,
                        isSelectedCorrectly = { isCorrect ->
                            isUserCorrect = isCorrect
                        }
                    )
                }


            Log.d("confirmsss", "wallet is   sss : $wallet")


            RepointCommonButton(
                "Confirm", modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp), onClick = {

                    coroutineScope.launch {

                        val user = userViewModel.fetchUser()

                        if (walletViewModel.tempMasterWallet != null) {
                            val nextIndex = walletViewModel.generateNextWalletIndex(user?.userId)
                            Log.d("confirm","next index is : ${walletViewModel.generateNextWalletIndex(user?.userId)}")
                            Log.d("confirm","user id is : ${user?.userId}")
                            /*val finalName = walletViewModel.tempMasterWallet!!.name.ifBlank {
                                walletViewModel.generateDefaultWalletName(nextIndex)
                            }
*/
                            val enteredName = defaultName.value.trim()
                            val finalName = if (enteredName.isNotBlank()) {
                                enteredName // custom name, no index
                            } else {
                                walletViewModel.generateDefaultWalletName(nextIndex) // default auto-named
                            }
                            walletViewModel.tempMasterWallet = walletViewModel.tempMasterWallet!!.copy(
                                name = finalName,
                                walletIndex = nextIndex,
                                userId = user?.userId
                            )
                        }

                        Log.d("confirm", "wallet id is : ${wallet?.masterWalletId}")
                        if (user != null && walletViewModel.tempMasterWallet?.masterWalletId != null) {
                            //user Already exist
                            walletViewModel.confirmAndSaveWallet()
                            walletViewModel.tempMasterWallet?.masterWalletId?.let {
                                walletViewModel.linkUserToMasterWallet(
                                    masterWalletId = it,
                                    userId = user.userId
                                )
                            }
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            Log.d("confirm","master wallet id is : ${wallet?.masterWalletId}")
                            walletViewModel.confirmAndSaveWallet()
                            walletViewModel.tempMasterWallet?.masterWalletId?.let { onConfirm(it) }
                        }
                    }
                }, enabled = isUserCorrect
            )
        }
    })

}
fun generateChallengeRows(phrases: List<String>): List<PhraseChallengeRow> {
    // Select 4 unique random indexes from the phrase list (0–11)
    val indexes = (phrases.indices).shuffled().take(4)

    return indexes.map { index ->
        val correctWord = phrases[index]
        val distractors = phrases.filterNot { it == correctWord }.shuffled().take(2)
        val options = (distractors + correctWord).shuffled()

        PhraseChallengeRow(
            wordPosition = index + 1, // display 1-based index
            correctWord = correctWord,
            options = options
        )
    }
}