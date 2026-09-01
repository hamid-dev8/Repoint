package com.repoint.dashboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.repoint.account.signup.ui.VerifyPinScreen
import com.repoint.basics.logic.authenticateUser
import com.repoint.dashboard.LockMethod

@Composable
fun LockChallengeScreen(
    selectedLockMethod: String,
    onUnlock: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val navController = rememberNavController()
    val activity = LocalContext.current as FragmentActivity

    BackHandler {
        onCancel?.invoke()
    }

    LaunchedEffect(selectedLockMethod) {
        if (selectedLockMethod == LockMethod.BIOMETRIC.name) {
            authenticateUser(
                onSuccess = onUnlock,
                onCancel = onCancel,
                activity = activity
            )
        }
    }

    if (selectedLockMethod == LockMethod.PASSCODE.name) {
        VerifyPinScreen(
            navController = navController,
            onVerified = onUnlock
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Authenticating...", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

