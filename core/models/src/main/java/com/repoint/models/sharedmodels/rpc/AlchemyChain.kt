package com.repoint.models.sharedmodels.rpc

enum class AlchemyChain(val baseUrl: String) {
    ETHEREUM("https://eth-mainnet.g.alchemy.com/v2/"),
    POLYGON("https://polygon-mainnet.g.alchemy.com/v2/"),
    BNB("https://bnb-mainnet.g.alchemy.com/v2/"),
    ARBITRUM("https://arb-mainnet.g.alchemy.com/v2/"),
    OPTIMISM("https://opt-mainnet.g.alchemy.com/v2/"),
    BASE("https://base-mainnet.g.alchemy.com/v2/")
}
