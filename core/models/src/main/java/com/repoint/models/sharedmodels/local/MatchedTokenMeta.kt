package com.repoint.models.sharedmodels.local

import com.repoint.models.sharedmodels.remote.ContractAddress
import com.repoint.models.sharedmodels.remote.TokenMetaData

data class MatchedTokenMeta(val meta: TokenMetaData,
                            val contract: ContractAddress,
                            val chain: String)
