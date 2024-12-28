package com.repoint.basics.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.repoint.models.sharedmodels.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


@HiltViewModel
open class BasicViewModel<T , G> : ViewModel() {

    fun uiState() : LiveData<T> = uiState()
    protected val uiState : MutableLiveData<T> = MutableLiveData()
    private val _uiEvents = MutableSharedFlow<UiEvent>()
    protected val uiEvent : SharedFlow<UiEvent> = _uiEvents


    // StateFlow for managing UI state
    private val _state = MutableStateFlow("Initial State")
    val state: StateFlow<String> = _state

/*    // Function to handle events
    suspend fun handleEvent(event: UiEvent) {
        withContext(Dispatchers.Main){
        when (event) {
            is UiEvent.ItemClicked -> {
                _state.update { "Item ${event.itemId} clicked ahh" }
            }
            is UiEvent.ButtonClicked -> {
                _state.update { "Button clicked ahh" }
            }
        }
        _uiEvents.emit(event) // Emit event if needed
        }
    }*/
}