package com.repoint.app

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.repoint.app.ui.MainScreen
import com.repoint.dashboard.LockMethod
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setTheme(R.style.Theme_Repoint)

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkTheme.collectAsState()
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val usePasscode by securityViewModel.isPasscodeEnabled.collectAsState()
            val selectedLockMethod by securityViewModel.lockMethod.collectAsState()
            val autoLockMinutes by securityViewModel.autoLockMinutes.collectAsState()
            val locked by appLocked
            val launchLockResolved by hasResolvedLaunchLock

            LaunchedEffect(autoLockMinutes) {
                autoLockDelayMs = (autoLockMinutes * 60L * 1000L)
                if (autoLockDelayMs <= 0L) {
                    appLocked.value = false
                    cancelAutoLockTimer()
                    return@LaunchedEffect
                }
                restartAutoLockTimer()
            }

            val shouldUsePasscodeOnLaunch = usePasscode && selectedLockMethod == LockMethod.PASSCODE.name
            val shouldLockForLaunch = shouldUsePasscodeOnLaunch && !launchLockResolved && !locked

            DisposableEffect(this@MainActivity) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (autoLockDelayMs > 0L && !appLocked.value) {
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
                if (locked || shouldLockForLaunch) {
                    LockChallengeScreen(
                        selectedLockMethod = selectedLockMethod,
                        onUnlock = {
                            appLocked.value = false
                            hasResolvedLaunchLock.value = true
                            lastInteractionAt = System.currentTimeMillis()
                        }
                    )
                } else {
                    FullScreenContent(this@MainActivity)
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (appLocked.value) return
        lastInteractionAt = System.currentTimeMillis()
        if (autoLockDelayMs > 0L) {
            restartAutoLockTimer()
        }
    }

    private fun restartAutoLockTimer() {
        cancelAutoLockTimer()
        if (autoLockDelayMs <= 0L) return
        autoLockJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000L)
                if (appLocked.value) continue
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