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

//choose one from three words!
@Composable
fun RepointThreeTextSelectable(
    shuffled: List<String>,
    correctAnswers: List<Pair<Int, String>>,
    isSelectedCorrectly: (Boolean) -> Unit
) {


    // Chunk the phrases into rows
    val rows = remember { shuffled.chunked(3) }
    // State for selected indices and answers
    val selectedIndices = remember { mutableStateListOf(*Array(rows.size) { -1 }) }
    val selectedAnswers = remember { mutableStateListOf(*Array(rows.size) { -1 }) }


    // Calculate correctness only when selections are updated
    val results = remember(selectedAnswers) {
        correctAnswers.mapIndexed { rowIndex, (correctIndex, correctPhrase) ->
            val userSelectedPhrase = selectedAnswers.getOrNull(rowIndex)?.let { index ->
                shuffled.getOrNull(index)
            }
            Triple(
                correctIndex,
                correctPhrase,
                userSelectedPhrase == correctPhrase
            )
        }
    }

    // Notify correctness
    LaunchedEffect(results) {
        snapshotFlow { selectedAnswers.toList() }
            .collect { answers ->
                val answer = correctAnswers.mapIndexed { rowIndex, (correctIndex, correctPhrase) ->
                    val userSelectedPhrase = answers.getOrNull(rowIndex)?.let { index ->
                        shuffled.getOrNull(index)
                    }
                    userSelectedPhrase == correctPhrase
                }
                val isAllCorrect = answer.all { it }
                isSelectedCorrectly(isAllCorrect)
                Log.d("isCoorect", "is correcttt =>>>> $isAllCorrect")
                /* val isAllCorrect = results.all { it.third } // Check if all are correct
                 isSelectedCorrectly(isAllCorrect)*/
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->

            // Correct answer information
            Text(
                text = "Correct Word of ${correctAnswers[rowIndex].first} : ",
                style = RepointTypography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            // Render each row
            SelectableRow(
                rowIndex = rowIndex,
                row = row,
                selectedIndex = selectedIndices[rowIndex],
                onItemSelected = { columnIndex ->
                    selectedIndices[rowIndex] = columnIndex
                    selectedAnswers[rowIndex] = rowIndex * 3 + columnIndex
                }
            )


        }
    }
}

@Composable
fun SelectableRow(
    rowIndex: Int,
    row: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        row.forEachIndexed { columnIndex, phrase ->
            Spacer(Modifier.padding(horizontal = 2.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(pureWhite, RoundedCornerShape(22.dp))
                    .border(
                        0.4.dp,
                        if (selectedIndex == columnIndex) repointOrange else lightGray,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickableWithRipple { onItemSelected(columnIndex)  }
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phrase,
                    style = RepointTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (selectedIndex == columnIndex) repointOrange else richBlack,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}