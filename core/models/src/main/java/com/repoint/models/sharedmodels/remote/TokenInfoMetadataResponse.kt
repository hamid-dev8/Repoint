package com.repoint.models.sharedmodels.remote

data class TokenInfoMetadataResponse(val status : CmcStatus,val data : Map<String,TokenMetaData>)
