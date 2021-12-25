package org.roboquant.brokers

import org.roboquant.common.Cash

/**
 * Interface for usage calculations. It is used to calculate how much cash is required for holding a set of
 * positions and open orders.
 *
 * Even when using a real broker that does its own (margin) calculations, this might be required for a Policy to
 * determine the sizing of orders before placing them at the broker.
 */
interface UsageCalculator {

    /**
     * Calculate the used cash for the provided account and possible changes
     */
    fun calculate(positions: List<Position>, changes: List<Position> = emptyList()) : Cash
}

/**
 * Basic Margin calculator, long positions usage are against their initial cost and short positions against their
 * last known exposure. So there is no leverage involved. Orders are ignored until they materialize into positions.
 */
class BasicUsageCalculator : UsageCalculator {

    override fun calculate(positions: List<Position>, changes: List<Position>): Cash {
        val usage = Cash()
        for (p in positions) {
            if (p.long)
                usage.deposit(p.asset.currency, p.totalCost)
            else if (p.short)
                usage.deposit(p.asset.currency, p.exposure)
        }
        return usage
    }

}

/**
 * Basic usage calculator that support a fixed laverage
 */
class LeveragedUsageCalculator(val leverage: Double = 1.0) : UsageCalculator {

    override fun calculate(positions: List<Position>, changes: List<Position>): Cash {
        val margin = Cash()
        for (p in positions) {
            if (p.long)
                margin.deposit(p.asset.currency,p.totalCost * 1.0/leverage)
            else if (p.short)
                margin.deposit(p.asset.currency,p.exposure * (1.0 + 1.0/leverage))
        }
        return margin
    }

}