package com.repoint.app

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import com.repoint.app.ui.MainScreen
import com.repoint.dependencies.theme.RepointTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    val view = LocalView.current
    val context = LocalContext.current

    // Set the system UI visibility flags
    DisposableEffect(Lifecycle.Event.ON_RESUME) {
        val decorView = view.rootView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        onDispose { /* Clean up if necessary */ }
    }

    // Your UI content
    Box(Modifier.fillMaxSize()) {
        MainScreen(activity)
    }
}