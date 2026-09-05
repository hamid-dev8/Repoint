package com.repoint.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.repoint.app.ui.MainScreen
import com.repoint.dashboard.SecurityViewModel
import com.repoint.dashboard.ui.LockChallengeScreen
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dependencies.theme.RepointTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var lastInteractionAt = System.currentTimeMillis()
    private val appLocked = mutableStateOf(false)
    private val hasResolvedLaunchLock = mutableStateOf(false)
    private var autoLockDelayMs = 0L
    private var autoLockJob: kotlinx.coroutines.Job? = null
    @Volatile
    private var isOnboardedForLock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setTheme(R.style.Theme_Repoint)

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val selectedLockMethod by securityViewModel.lockMethod.collectAsState()
            val autoLockMinutes by securityViewModel.autoLockMinutes.collectAsState()
            val securitySettingsLoaded by securityViewModel.isLoaded.collectAsState()
            val locked by appLocked
            val launchLockResolved by hasResolvedLaunchLock

            val spManager = remember { com.repoint.dependencies.accountmanager.SpManager(this@MainActivity) }
            val userId by spManager.getUserIdFlow().collectAsState(initial = null)
            val activeWalletId by spManager.getActiveWalletId().collectAsState(initial = null)
            // Lock is only relevant once the user has finished onboarding
            // (has an account and at least one wallet). New users going
            // through signup should never be challenged.
            val isOnboarded = !userId.isNullOrEmpty() && !activeWalletId.isNullOrEmpty()
            isOnboardedForLock = isOnboarded

            LaunchedEffect(autoLockMinutes) {
                autoLockDelayMs = (autoLockMinutes * 60L * 1000L)
                if (autoLockDelayMs <= 0L) {
                    appLocked.value = false
                    cancelAutoLockTimer()
                    return@LaunchedEffect
                }
                restartAutoLockTimer()
            }

            // Require the configured lock method (passcode OR biometric) on
            // every app launch, regardless of which method is selected.
            // Wait for securitySettingsLoaded so we don't act on the
            // transient default lockMethod (BIOMETRIC) before the real
            // saved value has loaded from DataStore - otherwise a biometric
            // prompt can flash even when the user chose Passcode.
            val shouldLockForLaunch =
                isOnboarded && securitySettingsLoaded && !launchLockResolved && !locked
            val awaitingSecuritySettings = isOnboarded && !securitySettingsLoaded && !launchLockResolved

            DisposableEffect(this@MainActivity) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (isOnboardedForLock && autoLockDelayMs > 0L && !appLocked.value) {
                            val elapsed = System.currentTimeMillis() - lastInteractionAt
                            if (elapsed >= autoLockDelayMs) {
                                appLocked.value = true
                            }
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            RepointTheme(darkTheme = isDarkMode) {
                when {
                    locked || shouldLockForLaunch -> {
                        LockChallengeScreen(
                            selectedLockMethod = selectedLockMethod,
                            onUnlock = {
                                appLocked.value = false
                                hasResolvedLaunchLock.value = true
                                lastInteractionAt = System.currentTimeMillis()
                            }
                        )
                    }
                    awaitingSecuritySettings -> {
                        // Blank holding screen: avoids revealing app content
                        // (or triggering the wrong authenticator) while the
                        // saved lock method is still loading from DataStore.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                    else -> {
                        FullScreenContent(this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (appLocked.value) return
        lastInteractionAt = System.currentTimeMillis()
        if (isOnboardedForLock && autoLockDelayMs > 0L) {
            restartAutoLockTimer()
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val wcUri = intent?.data?.let(::extractWalletConnectUri)
            ?: intent?.getStringExtra("wc_uri")
            ?: intent?.dataString?.let { uriString ->
                extractWalletConnectUri(Uri.parse(uriString))
            }

        com.repoint.basics.WalletConnectDeepLinkBridge.setPendingUri(wcUri)
    }

    private fun extractWalletConnectUri(uri: Uri): String? {
        val queryUri = uri.getQueryParameter("uri")
        if (!queryUri.isNullOrBlank()) return queryUri

        val decoded = Uri.decode(uri.toString())
        if (decoded.startsWith("wc:")) return decoded

        val pathValue = uri.encodedPath.orEmpty()
        if (pathValue.contains("wc")) {
            val fallback = uri.toString().substringAfter("wc", missingDelimiterValue = "")
            if (fallback.startsWith(":") || fallback.startsWith("/")) {
                return fallback.trimStart(':', '/')
            }
        }

        return null
    }

    private fun restartAutoLockTimer() {
        cancelAutoLockTimer()
        if (autoLockDelayMs <= 0L) return
        autoLockJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000L)
                if (appLocked.value || !isOnboardedForLock) continue
                val elapsed = System.currentTimeMillis() - lastInteractionAt
                if (elapsed >= autoLockDelayMs) {
                    appLocked.value = true
                    break
                }
            }
        }
    }

    private fun cancelAutoLockTimer() {
        autoLockJob?.cancel()
        autoLockJob = null
    }
}

@Composable
fun FullScreenContent(activity: FragmentActivity) {
    Box(Modifier.fillMaxSize()) {
        MainScreen(activity)
    }
}