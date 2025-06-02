package com.repoint.basics.atoms

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.basics.logic.clickableWithRipple
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite
import com.repoint.dependencies.theme.repointOrange
import com.repoint.dependencies.theme.richBlack


@Preview
@Composable
fun PreviewGangOfThree() {
    val sampleList = listOf("salam", " noch ", " hey cash")
    //RepointThreeTextSelectable(sampleList)
}
@Composable
fun RepointThreeTextSelectable(
    rows: List<PhraseChallengeRow>,
    isSelectedCorrectly: (Boolean) -> Unit
) {
    val selectedAnswers = remember { mutableStateListOf(*Array(rows.size) { "" }) }

    // Notify correctness
    LaunchedEffect(selectedAnswers) {
        snapshotFlow { selectedAnswers.toList() }
            .collect { answers ->
                val isAllCorrect = answers.zip(rows).all { (selected, row) ->
                    selected == row.correctWord
                }
                isSelectedCorrectly(isAllCorrect)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        rows.forEachIndexed { rowIndex, rowData ->
            Text(
                text = "Correct Word of ${rowData.wordPosition} : ",
                style = RepointTypography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            SelectableRow(
                rowIndex = rowIndex,
                row = rowData.options,
                selectedValue = selectedAnswers[rowIndex],
                onItemSelected = { selected ->
                    selectedAnswers[rowIndex] = selected
                }
            )
        }
    }
}

@Composable
fun SelectableRow(
    rowIndex: Int,
    row: List<String>,
    selectedValue: String,
    onItemSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        row.forEach { phrase ->
            Spacer(Modifier.padding(horizontal = 2.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(pureWhite, RoundedCornerShape(22.dp))
                    .border(
                        0.4.dp,
                        if (selectedValue == phrase) repointOrange else lightGray,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickableWithRipple {
                        onItemSelected(phrase)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phrase,
                    style = RepointTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (selectedValue == phrase) repointOrange else richBlack,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}



@Composable
fun SelectableRowStrings(
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        options.forEach { option ->
            Box(Modifier
                .padding(4.dp)
                .background(if (option == selectedValue) repointOrange else lightGray)
                .clickable { onSelected(option) }
            ) {
                Text(option, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

data class PhraseChallengeRow(
    val wordPosition: Int,
    val correctWord: String,
    val options: List<String>
)