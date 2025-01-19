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
    val selectedIndices = remember { mutableStateListOf(*Array(rows.size) {-1}) }
    val selectedAnswers = remember { mutableStateListOf(*Array(rows.size) {-1}) }



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
                val results = correctAnswers.mapIndexed { rowIndex, (correctIndex, correctPhrase) ->
                    val userSelectedPhrase = answers.getOrNull(rowIndex)?.let { index ->
                        shuffled.getOrNull(index)
                    }
                    userSelectedPhrase == correctPhrase
                }
                val isAllCorrect = results.all { it }
                isSelectedCorrectly(isAllCorrect)
                Log.d("isCoorect", "is correcttt =>>>> $isAllCorrect")
                /* val isAllCorrect = results.all { it.third } // Check if all are correct
                 isSelectedCorrectly(isAllCorrect)*/
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->

            // Correct answer information
            Text(
                text = "Correct Word of ${correctAnswers[rowIndex].first} : ",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selectedIndex == columnIndex) Color.Gray else Color.White
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (selectedIndex == columnIndex) Color.Black else Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onItemSelected(columnIndex) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phrase,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/*
var selectedIndices = remember { mutableStateListOf<Int>(-1, -1, -1, -1) }
    //var selectedIndices =  mutableListOf<Int>(-1, -1, -1, -1)
    var selectedAnswers = remember { mutableStateListOf<Int>(0, 0, 0, 0) }
    //var selectedAnswers = mutableListOf(0,0,0,0)
    var isCorrect by remember { mutableStateOf(false) }
    val rows = shuffled.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        rows.forEachIndexed { rowIndex, row ->
            // text for correct answer index!!!
            Text(text = "the Word ${correctAnswers.get(rowIndex).first} is :")

            Log.d("rowww", "chunked is : $rows")
            Log.d("rowww", "row is : $row")
            //Text(text = row.get(index))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                row.forEachIndexed { columnIndex, phrase ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .height(50.dp)
                            .clickable {
                                selectedIndices[rowIndex] = columnIndex
                                Log.d(
                                    "rowww",
                                    "indices is : ${(selectedIndices[rowIndex] + 1) * columnIndex}"
                                )
                                Log.d("rowww", "row index is : $rowIndex")
                                Log.d("rowww", "column index is : $columnIndex")
                                val formula = (((rowIndex + 1) * 2)) + columnIndex
                                when (rowIndex) {
                                    0 -> selectedAnswers[rowIndex] = formula - 2
                                    1 -> selectedAnswers[rowIndex] = formula - 1
                                    2 -> selectedAnswers[rowIndex] = formula
                                    3 -> selectedAnswers[rowIndex] = formula + 1
                                }

                                Log.d("indicesss", "$selectedAnswers")
                            }
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedIndices[rowIndex] == columnIndex) Sepia else ghostWhite
                            )
                            .border(
                                BorderStroke(
                                    0.5.dp,
                                    if (selectedIndices[rowIndex] == columnIndex) Color.Black else richBlack
                                ), shape = RoundedCornerShape(12.dp)
                            ), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedIndices[rowIndex] == columnIndex) ghostWhite else richBlack,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }


    }
    // Compare the user's selected answers with the correct answers
    val results = correctAnswers.map { (correctIndex, correctPhrase) ->
        val userSelectedIndex =
            selectedAnswers.getOrNull(correctAnswers.indexOf(Pair(correctIndex, correctPhrase)))
        val userSelectedPhrase = userSelectedIndex?.let {
            shuffled[it]
        } ?: "None"
        isCorrect = userSelectedPhrase == correctPhrase

        Log.d(
            "answersss",
            "userSelectedIndex = $userSelectedIndex and userSelecedPhrase = $userSelectedPhrase"
        )
        Triple(correctIndex, correctPhrase, isCorrect)
    }

    // Print the results
    results.forEach { (index, correctPhrase, isCorrect) ->
        Log.d(
            "answersss",
            "Index $index - Correct Phrase: $correctPhrase - Result: ${if (isCorrect) "Correct" else "Wrong"}"
        )
        isSelectedCorrectly(isCorrect)
    }
*/
