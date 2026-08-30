package com.repoint.models.sharedmodels.remote

data class ActiveSession(
    val dAppName : String,
    val dAppUrl : String,
    val connectedAddress : String,
    val chainIds : List<String>
)
