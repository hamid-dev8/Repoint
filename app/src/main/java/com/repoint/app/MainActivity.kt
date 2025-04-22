package com.repoint.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoint.app.ui.MainScreen
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dependencies.theme.RepointTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setTheme(R.style.Theme_Repoint)
        setContent {
            val themeViewModel : ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkTheme.collectAsState()

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