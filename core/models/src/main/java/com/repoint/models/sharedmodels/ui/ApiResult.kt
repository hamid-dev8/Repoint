package com.repoint.models.sharedmodels.ui


sealed class ApiResult<out T>
{
    data class Success<T>(val data : T) : ApiResult<T>()
    data class Error(val exception : ApiException) : ApiResult<Nothing>()
}