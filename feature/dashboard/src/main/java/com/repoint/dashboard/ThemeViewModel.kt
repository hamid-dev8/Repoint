package com.repoint.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.dependencies.accountmanager.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val spManager: SpManager
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme : StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            spManager.getTheme().collect{ savedTheme ->
                _isDarkTheme.value = savedTheme
            }
        }
    }


    fun toggleTheme(isDark : Boolean){
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue

        viewModelScope.launch {
            spManager.saveTheme(newValue)
        }
    }

}