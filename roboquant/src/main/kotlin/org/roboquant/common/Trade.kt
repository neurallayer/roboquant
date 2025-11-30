package org.roboquant.common

import java.time.Instant

/**
 * Represents a trade that happened as a consequence of the (partial) execution of an order.
 *
 * @property asset the underluying asset
 * @property time the time of the trade
 * @property size the size of the trade
 * @property price the average price paid
 * @property pnl the realized pnl
 */
data class Trade(
    val asset: Asset,
    val time: Instant,
    val size: Size,
    val price: Double,
    val pnl: Double
)

