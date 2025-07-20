package com.repoint.models.sharedmodels.rpc

import java.math.BigDecimal

data class GasPriceTier(val slow : BigDecimal , val average : BigDecimal , val fast : BigDecimal)
