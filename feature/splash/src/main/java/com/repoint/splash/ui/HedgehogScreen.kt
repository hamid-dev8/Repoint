package com.repoint.splash.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.dependencies.theme.RepointTypography

@Composable
@Preview
fun PreviewHedgehog(){
   /* Web3WalletScreen(onConfirm = {

    })*/
}


@Composable
fun Web3WalletScreen(onConfirm : () -> Unit) {
    Scaffold { paddingValues ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Spacer
        Spacer(modifier = Modifier.height(16.dp))

        // Image Section
        Image(
            painter = painterResource(com.repoint.dependencies.R.drawable.hedgehog_ic), // Replace with your image resource
            contentDescription = "Hedgehog Mascot",
            modifier = Modifier.wrapContentWidth()
        )

        // Text Section
        Text(
            text = "Your all-in-one\n wallet for the Web3 world",
            style = RepointTypography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Button Section
        RepointCommonButton(
            text = "Get Started",
            onClick = { onConfirm() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        // Footer Text
        Text(
            text = "By tapping \"Get Started\" you agree and consent to our\n" +
                    "Terms of Services and Privacy Policy",
            style = RepointTypography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color.Gray
        )
    }
    }
}
