package com.repoint.basics.atoms

import android.app.Activity
import android.view.Surface
import android.widget.Toolbar
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun BackButton(navController: NavController, modifier: Modifier = Modifier) {
    IconButton(onClick = { navController.popBackStack() }, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBackButton(
    navController: NavController,
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = { BackButton(navController) },
        actions = actions,
        modifier = modifier
    )
}

@Composable
fun RepointAppBar(title : String,navController: NavController, exp: @Composable () -> Unit) {
    Scaffold(
        topBar = { TopAppBarWithBackButton(navController = navController,title = title) }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)){
            exp()
        }
    }
}

