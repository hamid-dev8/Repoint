package com.repoint.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.CreateWalletViewModel
import com.repoint.basics.atoms.RepointAppBar


@Composable
fun ShowPhrase(
    data: String,
    navController: NavController,
    viewModel: CreateWalletViewModel = hiltViewModel<CreateWalletViewModel>()
) {

//val phraseList = viewModel.showPhrase(id)
    RepointAppBar("Alert!", navController, exp = {

        val phraseList = data.split(" ")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Recovery Phrases",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display phrases in two columns
            BoxWithConstraints {
                val isWide = maxWidth > 500.dp
                if (isWide) {
                    TwoColumnGrid(phraseList)
                } else {
                    OneColumnList(phraseList)
                }
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
fun TwoColumnGrid(phrases: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index. ",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = phrase,
            style = MaterialTheme.typography.bodyMedium
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




