package com.repoint.account.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.AuthViewModel
import com.repoint.basics.logic.EcGen
import com.repoint.basics.logic.EntropyManager


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
            //AuthButton("Enter Wallet",actions)
        }


    }

}


@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel<AuthViewModel>()
) {
    val context = LocalContext.current


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
            AuthButton("Create Wallet"){
                val entropyManager = EcGen()
                entropyManager.getFromMnemonic()
            }
            AuthButton("Enter Wallet"){

            }
        }


    }

}


@Composable
fun AuthButton(
    text: String,
    onClick : () -> Unit
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
fun Screen1(navController: NavController,viewModel : AuthViewModel = hiltViewModel()) {
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
fun Screen2(navController: NavController,viewModel: AuthViewModel = hiltViewModel()) {
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