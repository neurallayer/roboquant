package org.roboquant.brokers.sim

import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.common.Wallet

/**
 * Interface for usage calculations. It is used to calculate how much cash is required for holding a set of
 * positions and open orders.
 *
 * Even when using a real broker that does its own (margin) calculations, this might be required for a Policy to
 * determine the sizing of orders before placing them at the broker.
 */
interface UsageCalculator {

    /**
     * Calculate the used cash for the existing [positions] and (possible) new [changes]
     */
    fun calculate(positions: List<Position>, changes: List<Position> = emptyList()) : Wallet
}

/**
 * Basic Margin calculator, long positions usage are against their initial cost and short positions against their
 * last known exposure. So there is no leverage involved. Orders are ignored until they materialize into positions.
 */
class BasicUsageCalculator : UsageCalculator {

    override fun calculate(positions: List<Position>, changes: List<Position>): Wallet {
        val usage = Wallet()
        for (p in positions) {
            if (p.long)
                usage.deposit(p.totalCost)
            else if (p.short)
                usage.deposit(p.exposure)
        }

        // for (p in changes) usage.deposit(p.asset.currency, p.totalCost.absoluteValue)

        return usage
    }

}


/**
 * Usage calculator for T Reg accounts, using 50% for initial margin and 25% for maintance margin.
 */
class RegTCalculator : UsageCalculator {

    override fun calculate(positions: List<Position>, changes: List<Position>): Wallet {
        val margin = Wallet()
        val currentPos = mutableMapOf<Asset, Double>()

        // Maintance margin
        for (p in positions) {
            if (p.long)
                margin.deposit(p.totalCost)
            else if (p.short)
                margin.deposit(p.totalCost)

            currentPos[p.asset] = p.size
        }

        // Initial margin
        for (p in changes) {
            if (p.long)
                margin.deposit(p.totalCost * 0.5)
            else if (p.short)
                margin.deposit(p.exposure * 1.5)
        }
        return margin
    }

}


/**
 * Basic usage calculator that support a fixed laverage
 */
class LeveragedUsageCalculator(val leverage: Double = 1.0) : UsageCalculator {

    override fun calculate(positions: List<Position>, changes: List<Position>): Wallet {
        val margin = Wallet()
        for (p in positions) {
            if (p.long)
                margin.deposit(p.totalCost * (1.0/leverage))
            else if (p.short)
                margin.deposit(p.exposure * (1.0 + 1.0/leverage))
        }
        return margin
    }

}