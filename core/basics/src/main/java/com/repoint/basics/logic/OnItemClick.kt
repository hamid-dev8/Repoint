package com.repoint.basics.logic

interface ScreenActions {
    fun onButtonClick()
    fun onItemSelected(itemId: Int)
    fun onTabSelected(index: Int, title: String)
}