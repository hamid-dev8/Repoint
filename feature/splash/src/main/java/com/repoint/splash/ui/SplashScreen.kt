package com.repoint.splash.ui

import android.util.Log
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
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.splash.accountmanager.SpManager
import kotlinx.coroutines.delay


@Preview(showBackground = true)
@Composable
private fun PreviewSplashScreen() {
    /*SplashScreenRepoint(onTimeout = {

    } , onStay = {

    })*/
}

@Composable
fun SplashScreenRepoint(onStay: () -> Unit, onProceed: () -> Unit) {

    val splashTimeout = 3000L

    val context = LocalContext.current
    val spManager = SpManager(context)
    val userIdFlow = spManager.getUserIdFlow().collectAsState(initial = null)

    // MutableState to track if navigation has already occurred
    var isSplashFinished by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        delay(splashTimeout)
        isSplashFinished = true

        Log.d("userId", " User id is : ${userIdFlow.value}")
        if (!userIdFlow.value.isNullOrEmpty()) {
            onProceed()
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
