package com.repoint.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.repoint.account.ui.AuthScreen
import com.repoint.account.ui.WalletConfirmSurface
import com.repoint.account.ui.ShowPhrase

@Composable
fun RepointNavigation() {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController) }
        composable("walletConfirm") {
            WalletConfirmSurface(navController, onConfirm = { phrases ->
                navController.navigate("phrase/$phrases")
            })
        }
        composable("phrase/{phrases}") { backStackEntry ->
            val phraseStrings = backStackEntry.arguments?.getString("phrases") ?: ""
            ShowPhrase(data = phraseStrings,navController) }
    }


}