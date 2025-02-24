package com.repoint.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.repoint.app.navigation.RepointNavigation


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    //MainScreen()
}


@Composable
fun MainScreen(activity: FragmentActivity) {
    RepointNavigation(activity)
}