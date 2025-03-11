package com.repoint.models.sharedmodels.remote

data class Token(
    val tokenId : Int,
    val name : String,
    val symbol : String,
    val contractAddress : String,
    val decimals : Int,
    val logoUrl : String,
)
