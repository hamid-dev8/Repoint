package com.repoint.account.ui

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointCommonButton


@Preview
@Composable
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
            //AuthButton("Create Wallet",actions)
            RepointCommonButton("salam", {

            })
            //AuthButton("Enter Wallet",actions)
        }


    }

}


@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .padding(bottom = 36.dp)
                .align(Alignment.BottomCenter)
        ) {
            RepointCommonButton("Create Wallet", onClick = {
                navController.navigate("walletConfirm")
            })
            RepointCommonButton("Enter Wallet", onClick = {
                Toast.makeText(context, " NOT YET! " , Toast.LENGTH_SHORT).show()
            })
        }


    }

}


@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            onClick()
        },
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .height(48.dp), shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onTertiary)
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen1(navController: NavController, viewModel: WalletViewModel = hiltViewModel()) {
    Scaffold(
        content = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { navController.navigate("screen2") }) {
                    Text("Go to Screen 2")
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen2(navController: NavController, viewModel: WalletViewModel = hiltViewModel()) {
    Scaffold(
        content = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back to Screen 1")
                }
            }
        }
    )
}