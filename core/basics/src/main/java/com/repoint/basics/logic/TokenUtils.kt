package com.repoint.basics.logic

import java.math.BigDecimal
import java.math.BigInteger

fun decodeTokenBalance(hexBalance: String?, decimals: Int = 18): BigDecimal {
    if (hexBalance.isNullOrBlank() || hexBalance == "0x0") return BigDecimal.ZERO
    return try {
        val raw = BigInteger(hexBalance.removePrefix("0x"), 16)
        raw.toBigDecimal().movePointLeft(decimals)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}