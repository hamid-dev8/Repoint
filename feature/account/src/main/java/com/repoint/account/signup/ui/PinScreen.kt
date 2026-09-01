package com.repoint.account.signup.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.richBlack

@Composable
fun SetPinCode(
    walletId: String,
    digits: Array<String>?,
    navController: NavController,
    onConfirm: (ArrayList<String>) -> Unit,
    isItSet: Boolean,
    activity: FragmentActivity
) {
    RepointNumPad(walletId = walletId, digits, navController, onConfirm, isItSet, activity = activity)
}

@Composable
fun VerifyPinScreen(
    navController: NavController,
    onVerified: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val digitCount = 6
    val digitStates = remember { mutableStateListOf(*Array(digitCount) { "" }) }
    var focusedIndex by remember { mutableIntStateOf(0) }

    val numpad = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "←"),
    )

    ConstraintLayout(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        val (titleRef, digitRef, subtitleRef, padRef) = createRefs()

        Text(
            text = "Enter Passcode",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            textAlign = TextAlign.Center,
            style = RepointTypography.bodyLarge,
            color = colors.onBackground
        )

        SingleDigitRow(
            digitState = digitStates,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .constrainAs(digitRef) {
                    top.linkTo(titleRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            focusedIndex = focusedIndex
        )

        Text(
            text = "Enter your passcode to unlock rePoint",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .constrainAs(subtitleRef) {
                    bottom.linkTo(padRef.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            textAlign = TextAlign.Center,
            style = RepointTypography.labelMedium,
            color = colors.onSurface.copy(alpha = 0.6f)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.constrainAs(padRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        ) {
            numpad.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> {
                                        digitStates.fill("")
                                        focusedIndex = 0
                                    }
                                    "←" -> if (digitStates[focusedIndex].isNotEmpty()) {
                                        digitStates[focusedIndex] = ""
                                    } else if (focusedIndex > 0) {
                                        focusedIndex--
                                        digitStates[focusedIndex] = ""
                                    }
                                    else -> {
                                        if (focusedIndex < digitCount) {
                                            digitStates[focusedIndex] = key
                                            focusedIndex++
                                        }
                                    }
                                }
                                if (focusedIndex == digitCount && digitStates.all { it.isNotEmpty() }) {
                                    val entered = digitStates.joinToString("")
                                    viewModel.verifyPasscode(
                                        input = entered,
                                        onSuccess = { onVerified() },
                                        onFail = {
                                            Toast.makeText(context, "Wrong passcode", Toast.LENGTH_SHORT).show()
                                            digitStates.fill("")
                                            focusedIndex = 0
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = colors.onSurface
                            ),
                            modifier = Modifier
                                .size(64.dp)
                                .weight(1f)
                        ) {
                            Text(text = key, fontSize = 22.sp, style = RepointTypography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}


