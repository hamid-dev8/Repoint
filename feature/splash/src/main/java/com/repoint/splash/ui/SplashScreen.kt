package com.repoint.splash.ui

import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoint.account.WalletViewModel
import com.repoint.dashboard.CmcTokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.accountmanager.SpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
private fun PreviewSplashScreen() {
    /*SplashScreenRepoint(onTimeout = {

    } , onStay = {

    })*/
}

@Composable
fun SplashScreenRepoint(onStay: () -> Unit, onProceed: () -> Unit, onAuthRequest : (onSuccess : () -> Unit) -> Unit, web3ViewModel : Web3ViewModel = hiltViewModel(), walletViewModel : WalletViewModel = hiltViewModel(),cmcTokenViewModel: CmcTokenViewModel = hiltViewModel()) {

    val splashTimeout = 3000L

    val context = LocalContext.current
    val spManager = SpManager(context)
    val userIdFlow = spManager.getUserIdFlow().collectAsState(initial = null)

    // MutableState to track if navigation has already occurred
    var isSplashFinished by remember { mutableStateOf(false) }

    val isConnectedToWeb3 by web3ViewModel.connectionStatus.collectAsState()

    LaunchedEffect(userIdFlow.value) {
        delay(splashTimeout)
        isSplashFinished = true


        web3ViewModel.testConnectionToWeb3(chainId = 1)

        CoroutineScope(Dispatchers.IO).launch {
            cmcTokenViewModel.syncTopTokensToDb()
        }

         Log.d("Test","connection to web3 status : $isConnectedToWeb3")



        val actual = web3ViewModel.getChainId(1)
        Log.d("Test", "Chain ID returned = $actual")
        val userId = userIdFlow.value
        Log.d("userId", " User id is : ${userIdFlow.value}")
        if (!userId.isNullOrEmpty()) {
            val wallets = walletViewModel.getAllMasterWallets(userId)
            if (wallets.isNotEmpty()){
                onAuthRequest{
                    onProceed()
                }
            }
            else{
                spManager.clearAllSessionData()
                onStay()
            }
        } else {
            onStay()
        }
    }

    if (!isSplashFinished) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ghostWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = com.repoint.dependencies.R.drawable.org_logo),
                contentDescription = "central Image",
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center)
                    .size(128.dp),
                tint = Color.Unspecified
            )
        }
    }
}