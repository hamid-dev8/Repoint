package com.repoint.account.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.repoint.basics.atoms.BigPng
import com.repoint.basics.atoms.WalletButton
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.repointBlue
import com.repoint.dependencies.theme.repointOrange


@Preview
@Composable
fun AuthScreenPreview() {

    AuthScreen(navController = NavController(context = LocalContext.current))

}


@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    ConstraintLayout(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background).fillMaxSize().padding(16.dp),
    ) {

        val (topViews, centerViews, bottomViews) = createRefs()

        Box(modifier = Modifier.constrainAs(topViews) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }) {
            AuthInfo(modifier = Modifier.padding(2.dp))
        }
        BigPng(
            com.repoint.dependencies.R.drawable.wallet,
            modifier = Modifier.constrainAs(centerViews) {
                top.linkTo(topViews.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(bottomViews.top)
            })

        AuthButtons(navController, modifier = Modifier.constrainAs(bottomViews) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        })

    }
}

@Composable
fun AuthButtons(navController: NavController, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WalletButton(
            text = "Create new wallet",
            subText = "Secret phrase or Swift wallet",
            backgroundColor = repointOrange, // Orange
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Icon",
                    tint = Color(0xFFFFA726)
                )
            },
            onClick = { navController.navigate("walletConfirm") }
        )

        WalletButton(
            text = "Add existing wallet",
            subText = "Import, restore or view-only",
            backgroundColor = repointBlue, // Blue
            icon = {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download Icon",
                    tint = Color(0xFF1565C0)
                )
            },
            onClick = { navController.navigate("login") }
        )
    }
}
//
//

@Composable
fun AuthInfo(modifier: Modifier) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {

        /*PngWithText(
            com.repoint.dependencies.R.drawable.orglogo, "repointLogo", "re_Point",
            repointOrange,Modifier.padding(2.dp)
        )*/

        Image(
            painter = painterResource(com.repoint.dependencies.R.drawable.logo),
            contentDescription = "logo",
            modifier = modifier
                .fillMaxWidth().align(Alignment.Start).padding(start = 8.dp, bottom = 8.dp , end = 8.dp, top = 72.dp),
            alignment = Alignment.CenterStart,
            contentScale = ContentScale.Fit
        )

        Text(
            "own and manage\nyour assets",
            style = RepointTypography.displayLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp).padding(2.dp),
        )
        Text(
            "+100 blockchains supported",
            style = RepointTypography.bodySmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp).padding(2.dp),
        )

    }

}
