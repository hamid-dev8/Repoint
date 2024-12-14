package com.repoint.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.repoint.basics.atoms.BasicTabLayout


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}


@Composable
fun MainScreen() {
    var tabIndex = 0

    Surface(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { tabIndex = BasicTabLayout() }
        ) { innerpadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerpadding),
                contentAlignment = Alignment.Center
            ){
                when (tabIndex){
                    0 -> Text("Home Content")
                    1 -> Text("Profile Content")
                    2 -> Text("Settings Content")
                }
            }
        }

    }

}