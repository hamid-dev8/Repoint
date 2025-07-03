package com.repoint.models.sharedmodels.rpc

import com.repoint.models.sharedmodels.remote.TokenMetaData
import java.math.BigDecimal

data class TokenBalanceInfo(
    val meta : TokenMetaData,
    val balance : BigDecimal
)
