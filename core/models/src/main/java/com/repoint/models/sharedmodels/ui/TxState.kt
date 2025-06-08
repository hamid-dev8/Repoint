package com.repoint.models.sharedmodels.ui

sealed class TxState  {
    object Idle : TxState()
    object Loading : TxState()
    data class Success(val txHash : String) : TxState()
    data class Error(val message : String , val exception : Throwable? = null) : TxState()
}