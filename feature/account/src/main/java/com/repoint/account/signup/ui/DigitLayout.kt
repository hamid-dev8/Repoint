package com.repoint.account.signup.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.BioViewModel
import com.repoint.account.R
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.aliceBlue
import com.repoint.dependencies.theme.repointBlue
import com.repoint.dependencies.theme.repointOrange
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.User
import kotlinx.coroutines.launch


@Composable
@Preview
fun SingleDigitRowPreview() {

    //RepointNumPad()

}

@Composable
fun SingleDigitRow(digitState: SnapshotStateList<String>, modifier: Modifier, focusedIndex: Int) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Digit Input Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        ) {
            digitState.forEachIndexed { index, digit ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(48.dp)
                        .height(56.dp)
                ) {
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { input ->
                            if (input.length <= 1 && input.all { it.isDigit() }) {
                                //TODO
                                //digit = input
                            }
                        }, shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier
                            .width(48.dp)
                            .height(56.dp)
                            .border(
                                width = 2.dp,
                                color = if (index == focusedIndex) repointOrange else aliceBlue,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            color = repointBlue
                        ),
                        readOnly = true,
                        enabled = true
                    )
                }
            }
        }

    }
}


@Composable
fun RepointNumPad(
    walletId: String,
    digits: Array<String>?,
    navController: NavController,
    onConfirm: (ArrayList<String>) -> Unit,
    isItSet: Boolean,
    activity: FragmentActivity,
    viewModel: UserViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val digitCount = 6
    val digitState = remember {
        List(digitCount) { mutableStateOf("") }
    }
    val digitStates = remember { mutableStateListOf(*Array(digitCount) { "" }) }
    var focusedIndex by remember { mutableIntStateOf(0) }

    var showDialog = remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Log.d(
        "focus",
        "digits that passed is : ${digits.contentToString()} & ${digits?.joinToString("")}"
    )

    Log.d("focus", "wallet id is : $walletId")



    RepointAppBar("", navController, exp = {


        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {


            val (topView, after, constrainedView, bottomView) = createRefs()

            Text(
                if (isItSet) "Create Passcode" else "Confirm Passcode",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp)
                    .constrainAs(topView) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                textAlign = TextAlign.Center,
                style = RepointTypography.bodyLarge
            )

            SingleDigitRow(
                digitStates,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .constrainAs(after) {
                        top.linkTo(topView.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                focusedIndex = focusedIndex
            )

            Text(
                "Passcode adds an extra layer of security when using rePoint",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .constrainAs(constrainedView) {
                        bottom.linkTo(bottomView.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                textAlign = TextAlign.Center,
                style = RepointTypography.labelSmall,
                color = Color.Gray
            )


            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.constrainAs(bottomView) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            )
            {
                Log.d("focus", "$focusedIndex + is it set : $isItSet ")


                val numpad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "←"),
                )

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
                                            digitStates[focusedIndex] = key
                                            if (focusedIndex < digitCount) focusedIndex++
                                        }
                                    }

                                    Log.d("focus", "$focusedIndex + is it set : $isItSet ")


                                    Log.d(
                                        "focus",
                                        "${digits.contentToString()} + digit states  : $digitStates "
                                    )

                                    if (isItSet) {
                                        toConfirmPin(digitStates, onConfirm)
                                    } else if (!isItSet && focusedIndex == 6) {
                                        if (digits.contentToString() == digitStates.toList()
                                                .toString()
                                        ) {
                                            Log.d(
                                                "focus",
                                                "${digits.contentToString()} + digit states  : $digitStates "
                                            )
                                            showDialog.value = true
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "its not correct",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            digitStates.fill("")
                                            focusedIndex = 0
                                        }
                                    }


                                }, colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = richBlack
                                ),
                                modifier = Modifier
                                    .size(64.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 22.sp,
                                    style = RepointTypography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showDialog.value) {


            CustomBasicAlertDialog(showDialog = showDialog.value, onDismiss = {
                showDialog.value = false
                digitStates.fill("")
                focusedIndex = 0
            }, onConfirm = {
                showBiometricDialog = true
                showDialog.value = false
                digits?.joinToString("")?.let { viewModel.createUser(it) }
                coroutineScope.launch {
                    val user = viewModel.fetchUser()
                    Log.d("focus", "the user id  is somehow : ${user?.userId}")
                    user?.userId?.let { walletViewModel.linkUserToMasterWallet(walletId, it) }
                }
                //user?.let { walletViewModel.linkUserToWallet(walletId, userId = it.userId) }
                //Log.d("focus", " user is $user ")
            })
        }
    })

    if (showBiometricDialog) {
        LaunchedEffect(walletId) {
            val user = viewModel.fetchUser()
            Log.d("focus" , "user isss : $user")
            walletViewModel.linkUserToMasterWallet(walletId, userId = user?.userId ?: "")
        }
        BiometricalDialog(
            onDenyClick = {
                showBiometricDialog = false
                navController.navigate("home")
            },
            onConfirmClick = {
                showBiometricDialog = false
                navController.navigate("home"){
                    popUpTo(0){inclusive = true}
                }
                Log.d("focus", "biometric Confirmed")
            },
            fingerprintIcon = painterResource(com.repoint.dependencies.R.drawable.biometric_ic),
            activity = activity,
            onDismissRequest = {
                showBiometricDialog = false
                digitStates.fill("")
                focusedIndex = 0
            }
        )
    }
}


fun toConfirmPin(digitStates: SnapshotStateList<String>, onConfirm: (ArrayList<String>) -> Unit) {
    if (digitStates.all { it.isNotEmpty() } && digitStates.size >= 6) {
        val digitStatesArrayList: ArrayList<String> = ArrayList(digitStates)
        onConfirm(digitStatesArrayList)
        Log.d("pincod", "all fields are filled ${digitStates.size} && $digitStates")
    } else Log.d("pincod", "all fields are not filled!!! $digitStates")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBasicAlertDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { onDismiss() },
            Modifier.background(Color.White),
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Are you Sure you want to proceed further ?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "you can change your passcode later...")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { onDismiss() }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = { onConfirm() }) {
                            Text("Confirm")
                        }
                    }
                }
            })
    }
}


@Composable
fun BiometricScreen(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    viewModel: BioViewModel = hiltViewModel(),

    ) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkBiometricAvailability()
    }

    val isBiometricAvailable by viewModel.isBiometricAvailable.collectAsState()

    if (isBiometricAvailable) {
        Button(onClick = {
            Log.d("Biometric", "biometric activity is :$activity")
            activity.let {
                viewModel.authenticate(it, onSuccess, onFailure)
            }
        }) {
            Text("Authenticate")
        }
    } else {
        Text("Biometric authentication is not available")
    }
}