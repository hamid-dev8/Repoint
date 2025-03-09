package com.repoint.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.repoint.account.login.LoginScreen
import com.repoint.account.signup.ui.AuthScreen
import com.repoint.account.signup.ui.ConfirmPhrases
import com.repoint.account.signup.ui.SetPinCode
import com.repoint.account.signup.ui.ShowPhrase
import com.repoint.account.signup.ui.WalletConfirmSurface
import com.repoint.dashboard.ui.HomeScreen
import com.repoint.dashboard.ui.SendTokenScreen
import com.repoint.dashboard.ui.TransactionHistoryScreen
import com.repoint.dashboard.ui.WalletQrCodeScreen
import com.repoint.models.sharedmodels.remote.NativesBalance
import com.repoint.splash.ui.SplashScreenRepoint
import com.repoint.splash.ui.Web3WalletScreen


val gson = Gson()

@Composable
@Preview
fun PreViewNav() {

    //RepointNavigation()

}

@Composable
fun RepointNavigation(activity: FragmentActivity) {
    val navController = rememberNavController()


    //auth and signup
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreenRepoint(onStay = {
                navController.navigate("hedgehog") {
                    popUpTo("splash") { inclusive = true }
                }
            }, onProceed = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("hedgehog") {
            Web3WalletScreen(onConfirm = {
                navController.navigate("auth")
            })
        }
        composable("auth") { AuthScreen(navController) }
        composable("walletConfirm") {
            WalletConfirmSurface(navController, onConfirm = { walletId ->
                navController.navigate("phrase/$walletId")
            })
        }

        composable("phrase/{phrases}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("phrases") ?: ""
            ShowPhrase(walletId = walletId, navController, onConfirm = { phrasesList ->
                navController.navigate("confirmPhrases/$walletId/$phrasesList")
            })
        }

        //login
        composable("login") { backStackEntry ->
            LoginScreen(navController, onConfirm = { walletId ->
                navController.navigate("setPin/$walletId")
            })
        }

        composable("confirmPhrases/{walletId}/{phraseList}") { backStackEntry ->
            val phrases = backStackEntry.arguments?.getString("phraseList") ?: ""
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            ConfirmPhrases(phrases = phrases, navController, onConfirm = {
                navController.navigate("setPin/$walletId")
            })
        }

        composable("setPin/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            SetPinCode(walletId, digits = arrayOf(), navController, onConfirm = { digitStates ->
                navController.navigate("confirmPin/$walletId/$digitStates")
            }, true, activity = activity)
        }

        composable("confirmPin/{walletId}/{digitStates}") { backStateEntry ->
            val walletId = backStateEntry.arguments?.getString("walletId") ?: ""
            val digitStatesJson = backStateEntry.arguments?.getString("digitStates") ?: "[]"
            val digitStates: Array<String> =
                gson.fromJson(digitStatesJson, Array<String>::class.java)
            SetPinCode(walletId, digits = digitStates, navController, onConfirm = {
                //TODO Handle confirmation here
            }, false, activity = activity)
        }


        composable("home") { //navigating from home with a button to qrCode screen
            HomeScreen(navController)
        }


        //afterhome
        composable(
            "qrCode/{walletAddress}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType })
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            WalletQrCodeScreen(navController, walletAddress = walletAddress)
        }

        composable(
            "sendToken/{walletAddress}/{tokenBalance}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType },
                navArgument("tokenBalance") { type = NavType.StringType })
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            val tokenBalance = backStackEntry.arguments?.getString("tokenBalance") ?: "0"
            SendTokenScreen(walletAddress, tokenBalance, navController)
        }

        composable(
            "history/{balance}",
            arguments = listOf(navArgument("balance"){type = NavType.FloatType})
        ) { backstackEntry ->
            val balance = backstackEntry.arguments?.getFloat("balance") ?: 0.0f
           // val nativeBalance = gson.fromJson(balance,NativesBalance::class.java) // Convert back to object
            TransactionHistoryScreen(navController,balance = balance)
        }

    }


}