package com.repoint.models.sharedmodels.ui

sealed class UiState {
    object Loading : UiState()
    data class Success(val data : String) : UiState() //TODO ADD GENERIC MODEL
    data class Error(val messages : String) : UiState()
}