package com.repoint.account.signup.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.PngWithText
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.VectorWithText
import com.repoint.basics.atoms.WarningBanner
import com.repoint.dependencies.theme.Purple40
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.models.sharedmodels.RepointWallet


@Composable
fun ShowPhrase(
    walletId: String,
    navController: NavController,
    viewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    onConfirm: (String?) -> Unit
) {
    val context = LocalContext.current
    var wallet by remember { mutableStateOf<RepointWallet?>(null) }
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

//val phraseList = viewModel.showPhrase(id)
    RepointAppBar("Secret phrase", navController, exp = {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            LaunchedEffect(walletId) {
                wallet = viewModel.showPhrase(walletId)
            }
            val phraseList = wallet?.phrase?.split(" ")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display phrases in two columns
                BoxWithConstraints {
                    val isWide = maxWidth > 500.dp
                    if (phraseList != null) {
                        TwoColumnGridModified(phraseList)
                    }
                }

                if (phraseList != null) {
                    VectorWithText(Icons.Default.CopyAll,"copy","Copy to Clipboard", Purple40,Modifier, onClick = {
                        val clip = ClipData.newPlainText("phrases" , phraseList.joinToString(" "))
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context,"Copied To Clipboard!",Toast.LENGTH_SHORT).show()
                    })
                }

                Spacer(Modifier.padding(32.dp))

            }
            Column(Modifier.align(Alignment.BottomCenter)) {

                WarningBanner("Never share your secret phrase with anyone,and store it securely!",Modifier)
                RepointCommonButton(
                    "I saved And Confirmed",
                    onClick = {
                        //TODO ADD CONFIRM TO CONFIRM
                        if (wallet != null) onConfirm(wallet?.phrase)
                        Log.d("saved", "halaloua")

                    }, modifier = Modifier.padding(top = 32.dp))
            }
        }
    })
}

@Composable
fun OneColumnList(phrases: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(phrases.size) { index ->
            PhraseItem(index + 1, phrases[index])
        }
    }
}

@Composable
fun TwoColumnGridModified(phrases: List<String>){

    // Split the phrases into two columns
    val midIndex = (phrases.size + 1) / 2 // Calculate the mid-point
    val firstColumn = phrases.subList(0, midIndex) // First half
    val secondColumn = phrases.subList(midIndex, phrases.size) // Second half

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // First column
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            firstColumn.forEachIndexed { index, phrase ->
                PhraseItem(index + 1, phrase)
            }
        }

        // Second column
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            secondColumn.forEachIndexed { index, phrase ->
                PhraseItem(midIndex + index + 1, phrase)
            }
        }
    }

}

@Composable
fun TwoColumnGrid(phrases: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(phrases.size) { index ->
            PhraseItem(index + 1, phrases[index])
        }
    }
}

//val styleSeedGrid = GridStyle.Text.text(Col+phra!).modifiert*
@Composable
fun PhraseItem(index: Int, phrase: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$index. ",
            style = RepointTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = phrase,
            style = RepointTypography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSeedPhraseScreen() {
    val samplePhrases = listOf(
        "apple", "banana", "cherry", "date",
        "elderberry", "fig", "grape", "honeydew",
        "kiwi", "lemon", "mango", "nectarine"
    )
    //ShowPhrase(data = samplePhrases)
}




