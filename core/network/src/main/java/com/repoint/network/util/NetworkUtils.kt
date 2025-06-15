package com.repoint.network.util

import com.repoint.models.sharedmodels.ui.ApiException
import com.repoint.models.sharedmodels.ui.ApiResult
import retrofit2.HttpException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        val exception = when (e.code()) {
            400 -> ApiException.BadRequest()
            401 -> ApiException.Unauthorized()
            402 -> ApiException.PaymentRequired()
            403 -> ApiException.Forbidden()
            429 -> ApiException.TooManyRequests()
            500 -> ApiException.ServerError()
            else -> ApiException.Unknown("Unexpected error: ${e.code()}")
        }
        ApiResult.Error(exception)
    } catch (e: Exception) {
        ApiResult.Error(ApiException.Unknown(e.localizedMessage ?: "Unknown Error"))
    }
}
