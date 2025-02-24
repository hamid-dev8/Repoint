package com.repoint.basics.atoms

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.repoint.dependencies.theme.RepointTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepointBar(navController: NavController) {

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = currentDestination
                    ?: context.getString(com.repoint.dependencies.R.string.app_name)
            )
        }, navigationIcon = {
            IconButton(onClick = {
                if (!navController.popBackStack()) { // If no more items on backstack, close activity
                    (context as? Activity)?.finish()
                }
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is the details screen")
        }
    }
}

@Composable
fun NavigationButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    isSettings: Boolean,
    onSettingsClick: () -> Unit = {}
) {
    IconButton(
        onClick = { if (isSettings) onSettingsClick() else navController.popBackStack() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isSettings) Icons.Rounded.Settings else Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = if (isSettings) "Settings" else "Back"
        )
    }
}

@Composable
fun EndIconButton(showEndIcon: Boolean, onEndIconClick: () -> Unit) {

    if (showEndIcon) {
        IconButton(onClick = onEndIconClick) {
            Icon(imageVector = Icons.Rounded.AddCircleOutline, contentDescription = "Add Tokens")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithButton(
    navController: NavController,
    title: String,
    isSettings: Boolean = false,
    showEndIcon: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onEndIconClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = RepointTypography.bodyMedium,
            )
        },
        navigationIcon = {
            NavigationButton(
                navController,
                isSettings = isSettings,
                onSettingsClick = onSettingsClick
            )
        },
        actions = {
            EndIconButton(showEndIcon, onEndIconClick = onEndIconClick)
            actions()
        })
}

@Composable
fun RepointAppBar(
    title: String,
    navController: NavController,
    isSettings: Boolean = false,
    showEndIcon: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onEndIconClick: () -> Unit = {},
    exp: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBarWithButton(
                navController = navController,
                title = title,
                isSettings = isSettings,
                showEndIcon = showEndIcon,
                onSettingsClick = onSettingsClick,
                onEndIconClick = onEndIconClick
            )
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            exp()
        }
    }
}

