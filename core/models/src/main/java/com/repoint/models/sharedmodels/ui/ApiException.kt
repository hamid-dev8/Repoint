package com.repoint.models.sharedmodels.ui

sealed class ApiException(message : String) : Exception(message)
{
    class BadRequest : ApiException("Invalid request parameters (400)")
    class Unauthorized : ApiException("Invalid API key (401)")
    class PaymentRequired : ApiException("Payment required (402)")
    class Forbidden : ApiException("Access forbidden (403)")
    class TooManyRequests : ApiException("Rate limit exceeded (429)")
    class ServerError : ApiException("Internal server error (500)")
    class Unknown(message: String) : ApiException(message)
}