package com.repoint.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.Uri
import com.google.gson.Gson
import com.repoint.account.login.LoginScreen
import com.repoint.account.signup.ui.AuthScreen
import com.repoint.account.signup.ui.ConfirmPhrases
import com.repoint.account.signup.ui.SetPinCode
import com.repoint.account.signup.ui.ShowPhrase
import com.repoint.account.signup.ui.WalletConfirmSurface
import com.repoint.basics.logic.SendRoutes
import com.repoint.basics.logic.authenticateUser
import com.repoint.dashboard.ui.BotScreen
import com.repoint.dashboard.ui.ChainWalletsScreen
import com.repoint.dashboard.ui.ChooseTokenScreen
import com.repoint.dashboard.ui.CryptoManageScreen
import com.repoint.dashboard.ui.HomeScreen
import com.repoint.dashboard.ui.SecurityScreen
import com.repoint.dashboard.ui.SendErrorScreen
import com.repoint.dashboard.ui.SendLoadingScreen
import com.repoint.dashboard.ui.SendSuccessScreen
import com.repoint.dashboard.ui.SendTokenScreen
import com.repoint.dashboard.ui.SettingsScreen
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
            }, onAuthRequest = { onAuthSuccess ->
                authenticateUser(onAuthSuccess, activity)
            }
            )
        }
        composable("hedgehog") {
            Web3WalletScreen(onConfirm = {
                navController.navigate("auth")
            })
        }
        composable("auth") { AuthScreen(navController) }
        composable("walletConfirm") {
            WalletConfirmSurface(navController, onConfirm = { phraseList ->
                navController.navigate("phrase/$phraseList")
            })
        }

        composable("phrase/{phrases}") { backStackEntry ->
            val phrase = backStackEntry.arguments?.getString("phrases") ?: ""
            ShowPhrase(phrases = phrase, navController, onConfirm = { phrasesList ->
                val encodedPhrases = android.net.Uri.encode(phrasesList)
                navController.navigate("confirmPhrases/$encodedPhrases")
            })
        }
        //$walletId

        //login
        composable("login") { backStackEntry ->
            LoginScreen(navController, onConfirm = { walletId ->
                navController.navigate("setPin/$walletId")
            })
        }

        composable("confirmPhrases/{phraseList}") { backStackEntry ->
            val encodedPhrases = backStackEntry.arguments?.getString("phraseList") ?: ""
            //val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            ConfirmPhrases(phrases = encodedPhrases, navController, onConfirm = { walletId ->
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

        composable("bot/{url}") { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("url") ?: ""
            val decode = android.net.Uri.decode(raw)
            BotScreen(url = decode)
        }


        composable(
            "chooseToken/{isSend}",
            arguments = listOf(navArgument("isSend") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isSend = backStackEntry.arguments?.getBoolean("isSend") ?: false
            ChooseTokenScreen(navController, isSend = isSend)
        }

        //afterhome
        composable(
            "qrCode/{walletAddress}/{masterWalletId}/{tokenId}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType })
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            val masterWalletId = backStackEntry.arguments?.getString("masterWalletId") ?: ""
            val tokenId = backStackEntry.arguments?.getString("tokenId")?.toIntOrNull()
            WalletQrCodeScreen(
                navController,
                walletAddress = walletAddress,
                masterWalletId = masterWalletId,
                tokenId = tokenId
            )
        }

        composable(
            "sendToken/{walletAddress}/{tokenBalance}/{coinType}/{contractAddress}/{chainId}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType },
                navArgument("tokenBalance") { type = NavType.StringType },
                navArgument("coinType") { type = NavType.IntType },
                navArgument("contractAddress") { type = NavType.StringType },
                navArgument("chainId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            val tokenBalance = backStackEntry.arguments?.getString("tokenBalance") ?: "0"
            val coinType = backStackEntry.arguments?.getInt("coinType") ?: -1
            val contractAddress = backStackEntry.arguments?.getString("contractAddress") ?: ""
            val chainId = backStackEntry.arguments?.getInt("chainId") ?: -1
            SendTokenScreen(
                walletAddress,
                tokenBalance,
                coinType,
                contractAddress,
                chainId,
                navController
            )
        }

        composable(SendRoutes.ROUTE_SEND_LOADING) {
            SendLoadingScreen()
        }
        composable(SendRoutes.ROUTE_SEND_SUCCESS) {
            val txHash = navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("txHash")
                ?: "Unknown"
            SendSuccessScreen(txHash, navController)
        }
        composable(SendRoutes.ROUTE_SEND_ERROR) { navBackStackEntry ->
            val errorMessage = navBackStackEntry
                .savedStateHandle
                .get<String>("error")
                ?: "Transaction failed"
            SendErrorScreen(errorMessage, navController)
        }


        composable(
            "history/{balance}/{walletAddress}",
            arguments = listOf(navArgument("balance") { type = NavType.FloatType },
                navArgument("walletAddress") { type = NavType.StringType }
            )
        ) { backstackEntry ->
            val balance = backstackEntry.arguments?.getFloat("balance") ?: 0.0f
            val walletAddress = backstackEntry.arguments?.getString("walletAddress") ?: ""
            // val nativeBalance = gson.fromJson(balance,NativesBalance::class.java) // Convert back to object
            TransactionHistoryScreen(
                navController,
                balance = balance,
                walletAddress = walletAddress
            )
        }

        composable(
            "networks"
        )
        { navBackStackEntry ->
            CryptoManageScreen(navController)
        }

        composable("settings") { navBackStackEntry ->
            SettingsScreen(navController)
        }

        composable("wallets") {
            ChainWalletsScreen(navController = navController)
        }

        composable("security") {
            SecurityScreen(navController)
        }

    }


}