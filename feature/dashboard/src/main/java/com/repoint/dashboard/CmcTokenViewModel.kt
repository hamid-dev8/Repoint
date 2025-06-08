package com.repoint.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repoint.models.sharedmodels.local.CmcTokenEntity
import com.repoint.sources.datarepo.datasource.CmcDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CmcTokenViewModel @Inject constructor(private val repository : CmcDataSource) : ViewModel()
{

    private val _tokens = MutableStateFlow<List<CmcTokenEntity>>(emptyList())
    val tokens : StateFlow<List<CmcTokenEntity>> = _tokens

    fun loadTokens() {
        viewModelScope.launch {
            _tokens.value = repository.getAllTokens()
        }
    }


}