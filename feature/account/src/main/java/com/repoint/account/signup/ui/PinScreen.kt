package com.repoint.account.signup.ui

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

@Composable
fun SetPinCode(walletId : String,digits : Array<String>?,navController: NavController,onConfirm : (ArrayList<String>) -> Unit,isItSet : Boolean,activity : FragmentActivity){
    RepointNumPad(walletId = walletId,digits,navController,onConfirm,isItSet,activity = activity)
}

