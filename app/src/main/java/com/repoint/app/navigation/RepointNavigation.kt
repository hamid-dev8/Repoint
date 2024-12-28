package com.repoint.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.repoint.account.ui.AuthScreen
import com.repoint.account.ui.Screen1
import com.repoint.account.ui.Screen2

@Composable
fun RepointNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "screen1") {
        composable("screen1") { AuthScreen(navController) }
        composable("screen2") { Screen2(navController) }
    }

}