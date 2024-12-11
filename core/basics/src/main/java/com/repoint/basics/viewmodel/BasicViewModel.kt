package com.repoint.basics.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BasicViewModel<T> : ViewModel() {

    fun uiState() : LiveData<T> = uiState()
    protected val uiState : MutableLiveData<T> = MutableLiveData()


}