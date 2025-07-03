package com.repoint.basics.logic

import com.repoint.models.sharedmodels.remote.ContractAddress
import com.repoint.models.sharedmodels.rpc.AlchemyChain

fun mapPlatformToAlchemyChain(contract: ContractAddress): AlchemyChain? {
    return AlchemyChain.entries.find { chain ->
        chain.name.equals(contract.platform.coin.slug, ignoreCase = true) ||
                chain.name.equals(contract.platform.name, ignoreCase = true) ||
                chain.name.equals(contract.platform.coin.name, ignoreCase = true)
    }
}
