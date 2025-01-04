package com.repoint.account.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.RepointThreeTextSelectable
import kotlin.random.Random


@Composable
fun ConfirmPhrases(phrases: String, navController: NavController) {

    val phraseList = phrases.split(" ")
    val shuffledList = phraseList.chunked(3).flatMap { it.shuffled() }
    val resultPair = calculateRandomStrings(phraseList)
    var isUserCorrect by remember { mutableStateOf(false) }

    for (index in 0..3) {
        Log.d("confirmsss", " the result pair is : ${resultPair.get(index).first}")
    }
    Log.d("confirmsss", "orginal list : $phraseList")
    Log.d("confirmsss", "shuffled by 3 : $shuffledList")
    Box(Modifier.fillMaxSize()) {

        RepointAppBar("Confirm Secret Phrase", navController = navController, exp = {
            resultPair.fastForEachIndexed{ index , answer ->
                Log.d("confirmsss"," result pair is : ${resultPair.toString()}" )

                RepointThreeTextSelectable(shuffledList,resultPair, isSelectedCorrectly = {
                    isUserCorrect = it
                    Log.d("isSelected","is it ok? : $isUserCorrect")
                })
            }

        })

        RepointCommonButton(
            "Confirm", modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp), onClick = {


            }, enabled = isUserCorrect)
    }

}


fun calculateRandomStrings(phrases: List<String>): List<Pair<Int, String>> {
    val result = mutableListOf<Pair<Int, String>>()

    phrases.chunked(3).forEachIndexed { chunkedIndex, chunk ->

        val randomIndexInChunk = Random.nextInt(chunk.size)
        val actualIndex = chunkedIndex * 3 + randomIndexInChunk+1
        result.add(actualIndex to chunk[randomIndexInChunk])
    }
    return result
}
/*

RepointThreeTextSelectable(chunk, onSelected = {selectedPhrase ->
    selected = selectedPhrase+1
    Log.d("confirmsss","selected phrase : -> $selected")
    listOfAnswers.add(index,selected)
    //       Log.d("confirmsss","list of correct answers is : -> $listOfAnswers")
})*/
/*
Column(
modifier = Modifier
.padding(8.dp)
.padding(top = 20.dp)
) {
    chunked.forEachIndexed { index, chunk ->
        Log.d(
            "confirmsss",
            "shuffled by 3 and chunked : index :  ${index + 1} : $chunk"
        )
        Text(
            "Word of  ${resultPair.get(index).first}",
            modifier = Modifier.padding(start = 16.dp)
        )
        Log.d("confirmsss","correct answer of chunked : answer : ${resultPair.get(index).second}")

    }
}
Log.d("confirmsss", "the choosen by random is : $resultPair")*/
