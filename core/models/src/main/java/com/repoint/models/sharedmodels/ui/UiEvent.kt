package com.repoint.models.sharedmodels.ui

sealed class UiEvent {

    data class ItemClicked(val itemId : Int) : UiEvent()
    data object ButtonClicked : UiEvent()

    //events as need
}