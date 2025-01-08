package com.repoint.app.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.repoint.account.ui.AuthScreen
import com.repoint.account.ui.ConfirmPhrases
import com.repoint.account.ui.SetPinCode
import com.repoint.account.ui.WalletConfirmSurface
import com.repoint.account.ui.ShowPhrase


val gson = Gson()

@Composable
fun RepointNavigation(activity: FragmentActivity) {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController) }
        composable("walletConfirm") {
            WalletConfirmSurface(navController, onConfirm = { walletId ->
                navController.navigate("phrase/$walletId")
            })
        }
        composable("phrase/{phrases}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("phrases") ?: ""
            ShowPhrase(walletId = walletId, navController, onConfirm = { phrasesList ->
                navController.navigate("confirmPhrases/$phrasesList")
            })
        }
        composable("confirmPhrases/{phraseList}") { backStackEntry ->
            val phrases = backStackEntry.arguments?.getString("phraseList") ?: ""
            ConfirmPhrases(phrases = phrases, navController, onConfirm = {
                navController.navigate("setPin")
            })
        }

        composable("setPin") { backStackEntry ->
            SetPinCode(digits = arrayOf(), navController, onConfirm = { digitStates ->
                navController.navigate("confirmPin/$digitStates")
            }, true, activity = activity)
        }
        composable("confirmPin/{digitStates}") { backStateEntry ->
            val digitStatesJson = backStateEntry.arguments?.getString("digitStates") ?: "[]"
            val digitStates: Array<String> =
                gson.fromJson(digitStatesJson, Array<String>::class.java)
            SetPinCode(digits = digitStates, navController, onConfirm = {
            }, false, activity = activity)
        }
    }


}