package com.repoint.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.repoint.app.ui.MainScreen
import com.repoint.dependencies.theme.RepointTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    @SuppressLint("WrongConstant", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setTheme(R.style.Theme_Repoint)
        setContent {

                RepointTheme {
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