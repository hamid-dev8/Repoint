package com.repoint.sources.datarepo.datasource

import com.repoint.models.sharedmodels.local.TxDirection
import com.repoint.models.sharedmodels.remote.ContractAddress
import com.repoint.models.sharedmodels.rpc.TxHistoryItem
import com.repoint.models.sharedmodels.ui.ApiResult

interface AlchemyTxDataSource
{

    suspend fun getTransactionHistory(
        walletAddress: String,
        fromBlock : String? = null,
        toBlock : String? = "latest",
        pageKey : String? = null,
        maxCount : Int = 25,
        category : List<String> = listOf("external" , "erc20" , "erc721","erc1155"),
        chainId : Int,
        direction: TxDirection
    ) : ApiResult<Pair<List<TxHistoryItem>,String?>>


}