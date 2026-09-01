package com.repoint.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.repoint.account.signup.ui.VerifyPinScreen
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
    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
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
        composable("hedgehog",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) {
            Web3WalletScreen(onConfirm = {
                navController.navigate("auth")
            })
        }
        composable("auth",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { AuthScreen(navController) }
        composable("walletConfirm") {
            WalletConfirmSurface(navController, onConfirm = { phraseList ->
                navController.navigate("phrase/$phraseList")
            })
        }

        composable("phrase/{phrases}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStackEntry ->
            val phrase = backStackEntry.arguments?.getString("phrases") ?: ""
            ShowPhrase(phrases = phrase, navController, onConfirm = { phrasesList ->
                val encodedPhrases = android.net.Uri.encode(phrasesList)
                navController.navigate("confirmPhrases/$encodedPhrases")
            })
        }
        //$walletId

        //login
        composable("login",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStackEntry ->
            LoginScreen(navController, onConfirm = { walletId ->
                navController.navigate("setPin/$walletId")
            })
        }

        composable("confirmPhrases/{phraseList}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStackEntry ->
            val encodedPhrases = backStackEntry.arguments?.getString("phraseList") ?: ""
            //val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            ConfirmPhrases(phrases = encodedPhrases, navController, onConfirm = { walletId ->
                navController.navigate("setPin/$walletId")
            })
        }

        composable("setPin/{walletId}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            SetPinCode(walletId, digits = arrayOf(), navController, onConfirm = { digitStates ->
                navController.navigate("confirmPin/$walletId/$digitStates")
            }, true, activity = activity)
        }

        composable("confirmPin/{walletId}/{digitStates}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStateEntry ->
            val walletId = backStateEntry.arguments?.getString("walletId") ?: ""
            val digitStatesJson = backStateEntry.arguments?.getString("digitStates") ?: "[]"
            val digitStates: Array<String> =
                gson.fromJson(digitStatesJson, Array<String>::class.java)
            SetPinCode(walletId, digits = digitStates, navController, onConfirm = {
                //TODO Handle confirmation here
            }, false, activity = activity)
        }


        composable("verifyPin") {
            VerifyPinScreen(
                navController = navController,
                onVerified = {
                    navController.navigate("home") {
                        popUpTo("verifyPin") { inclusive = true }
                    }
                }
            )
        }

        composable("home") { //navigating from home with a button to qrCode screen
            HomeScreen(navController)
        }

        composable("bot/{url}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("url") ?: ""
            val decode = android.net.Uri.decode(raw)
            BotScreen(url = decode)
        }


        composable(
            "chooseToken/{isSend}",
            arguments = listOf(navArgument("isSend") { type = NavType.BoolType }),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }
        ) { backStackEntry ->
            val isSend = backStackEntry.arguments?.getBoolean("isSend") ?: false
            ChooseTokenScreen(navController, isSend = isSend)
        }

        //afterhome
        composable(
            "qrCode/{walletAddress}/{masterWalletId}/{tokenId}/{networkName}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType },
                navArgument("masterWalletId") { type = NavType.StringType },
                navArgument("tokenId") { type = NavType.IntType },
                navArgument("networkName") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            val masterWalletId = backStackEntry.arguments?.getString("masterWalletId") ?: ""
            val tokenId = backStackEntry.arguments?.getInt("tokenId")
            val networkName = backStackEntry.arguments?.getString("networkName") ?: ""
            WalletQrCodeScreen(
                navController,
                walletAddress = walletAddress,
                masterWalletId = masterWalletId,
                tokenId = tokenId,
                networkName = networkName
            )
        }

        composable(
            "sendToken/{walletAddress}/{balance}/{coinType}/{contractAddress}/{chainId}/{tokenName}/{tokenId}/{masterWalletId}",
            arguments = listOf(navArgument("walletAddress") { type = NavType.StringType },
                navArgument("balance") { type = NavType.StringType },
                navArgument("coinType"){type = NavType.IntType},
                navArgument("contractAddress"){type = NavType.StringType},
                navArgument("chainId"){type = NavType.IntType},
                navArgument("tokenName"){type = NavType.StringType},
                navArgument("tokenId"){type = NavType.IntType},
                navArgument("masterWalletId"){type = NavType.StringType}
            ), enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }
        ) { backStackEntry ->
            val walletAddress = backStackEntry.arguments?.getString("walletAddress") ?: ""
            val balance = backStackEntry.arguments?.getString("balance") ?: ""
            val coinType = backStackEntry.arguments?.getInt("coinType") ?: -1
            val contractAddress = backStackEntry.arguments?.getString("contractAddress") ?: ""
            val chainId = backStackEntry.arguments?.getInt("chainId") ?: -1
            val tokenName = backStackEntry.arguments?.getString("tokenName") ?: "0"
            val tokenId = backStackEntry.arguments?.getInt("tokenId") ?: -1
            val masterWalletId = backStackEntry.arguments?.getString("masterWalletId") ?: ""


            SendTokenScreen(
                walletAddress,
                balance,
                coinType,
                contractAddress,
                chainId,
                tokenName,
                tokenId,
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
            "history/{walletAddress}",
            arguments = listOf(
                navArgument("walletAddress") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) { backstackEntry ->
            val walletAddress = backstackEntry.arguments?.getString("walletAddress") ?: ""
            TransactionHistoryScreen(
                navController,
                walletAddress = walletAddress
            )
        }

        composable(
            "networks",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Up
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Down
                )
            }
        )
        { navBackStackEntry ->
            CryptoManageScreen(navController)
        }

        composable("settings",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }) { navBackStackEntry ->
            SettingsScreen(navController)
        }

        composable("wallets",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }) {
            ChainWalletsScreen(navController = navController)
        }

        composable("security",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            SecurityScreen(navController)
        }

    }


}