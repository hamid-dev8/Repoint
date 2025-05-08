package com.repoint.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoint.account.UserViewModel
import com.repoint.app.ui.MainScreen
import com.repoint.dashboard.SecurityViewModel
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dependencies.theme.RepointTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var isAuthenticated = false

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
            setTheme(R.style.Theme_Repoint)
            setContent {
                val themeViewModel : ThemeViewModel = hiltViewModel()
                val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
                val  securityViewModel : SecurityViewModel = hiltViewModel()
                val usePasscode by securityViewModel.isPasscodeEnabled.collectAsState()

                if (usePasscode){


                }

                RepointTheme(darkTheme = isDarkMode) {
                    FullScreenContent(this)
                }
            }

    }
}


@Composable
fun FullScreenContent(activity: MainActivity) {

    //val systemUiController = rememberSys

        Box(Modifier.fillMaxSize()) {
            MainScreen(activity)

    }
}