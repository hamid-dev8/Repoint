package com.repoint.account.ui

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

@Composable
fun SetPinCode(digits : Array<String>?,navController: NavController,onConfirm : (ArrayList<String>) -> Unit,isItSet : Boolean,activity : FragmentActivity){
    RepointNumPad(digits,navController,onConfirm,isItSet,activity = activity)
}

