package com.repoint.basics.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repoint.basics.R
import com.repoint.dependencies.theme.repointBlue
import com.repoint.dependencies.theme.repointOrange


@Composable
fun AuthBottomSheetContent(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

        Spacer(Modifier.height(16.dp))
        AuthButtonsSheet(onCreateWallet, onImportWallet)

    }


}


@Composable
fun AuthButtonsSheet(onCreateWallet: () -> Unit, onImportWallet: () -> Unit) {

    Column(verticalArrangement = Arrangement.SpaceEvenly) {


        BigPng(com.repoint.dependencies.R.drawable.orglogo,Modifier.padding(2.dp),18f,10f)


        WalletButton(
            "Create new wallet",
            subText = "Secret Phrase or Swift Wallet",
            backgroundColor = repointOrange,
            icon = {
                Icon(
                   imageVector = Icons.Filled.Add,
                    contentDescription = "Add Icon",
                    tint = Color(0xFFFFA726)
                )
            }, onClick = onCreateWallet)

        Spacer(Modifier.height(12.dp))

        WalletButton("Add existing Wallet", subText = "Import, restore or view-only", backgroundColor = repointBlue,
            icon = {
                Icon(imageVector = Icons.Filled.Download, contentDescription = "Download Icon", tint = Color(0xFF1565C0))
            }, onClick = onImportWallet)

    }

}