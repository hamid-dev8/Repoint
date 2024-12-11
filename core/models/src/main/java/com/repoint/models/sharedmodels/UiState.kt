package com.repoint.models.sharedmodels

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data : T) : UiState<T>() //TODO ADD GENERIC MODEL
    data class Error(val messages : String) : UiState<Nothing>()
}