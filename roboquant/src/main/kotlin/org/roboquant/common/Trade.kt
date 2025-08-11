package org.roboquant.common

import java.time.Instant

data class Trade(
    val asset: Asset,
    val time: Instant,
    val size: Size,
    val price: Double,
    val pnl: Double
)

