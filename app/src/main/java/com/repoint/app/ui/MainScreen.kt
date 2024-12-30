package com.repoint.app.ui

import android.annotation.SuppressLint
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
import androidx.navigation.NavController
import com.repoint.app.navigation.RepointNavigation
import com.repoint.basics.atoms.BasicTabLayout
import com.repoint.basics.atoms.RepointBar
import com.repoint.basics.atoms.TopAppBarWithBackButton
import com.repoint.basics.logic.ScreenActions


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    //MainScreen()
}


@Composable
fun MainScreen() {
    RepointNavigation()
}