package com.repoint.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.basics.logic.ScreenActions


@Composable
@Preview
fun AuthScreenPreview() {

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .padding(4.dp)
                .align(Alignment.BottomCenter)
        ) {
            // AuthButton("Create Wallet",actions)
            // AuthButton("Enter Wallet",actions)
        }


    }

}


@Composable
fun AuthScreen(actions: ScreenActions) {

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .padding(4.dp)
                .align(Alignment.BottomCenter)
        ) {
            AuthButton("Create Wallet", actions)
            AuthButton("Enter Wallet", actions)
        }


    }

}


@Composable
fun AuthButton(text: String, actions: ScreenActions) {


    Button(
        onClick = {
            actions.onButtonClick()
        },
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .height(48.dp), shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onTertiary)
    }
}
