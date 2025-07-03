package com.repoint.models.sharedmodels.rpc

import java.math.BigDecimal

data class AlchemyChainNativeBalance(val chainId : Int,val chainName : String,val balance : BigDecimal)