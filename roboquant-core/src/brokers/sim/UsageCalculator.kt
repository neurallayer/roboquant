package org.roboquant.brokers.sim

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.sum

/**
 * Interface for usage calculations. It is used to calculate how much cash is required for holding a set of
 * positions and open orders.
 *
 * Even when using a real broker that does its own (margin) calculations, this might be required for a Policy to
 * determine the sizing of orders before placing them at the broker.
 */
interface UsageCalculator {

    /**
     * Calculate the reamining buying power for an account
     */
    fun calculate(account: Account) : Amount
}

/**
 * Basic calculator that calculates: cash balance - open orders. So no equity or leverage is involved.
 */
class BasicUsageCalculator(private val minimum: Double = 0.0) : UsageCalculator {

    override fun calculate(account: Account): Amount {
        val cash = account.cash
        val openOrders = account.orders.open.map { it.getValueAmount() }.sum()
        val total = cash - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))
        return account.convert(total)
    }

}


/**
 * Usage calculator for Regulation T accounts. Formula used is
 *
 *      free = equity - initialMarginOrders - maintanceMarginLong - maintanceMarginShort
 *      buyingPower = free / initialMargin
 *
 */
class RegTCalculator(
    private val initialMargin: Double= 0.5,
    private val longMaintanceMargin: Double = 0.25,
    private val shortMaintanceMargin: Double = 0.30,
    private val minimumAmount : Double = 0.0
) : UsageCalculator {

    override fun calculate(account: Account): Amount {
        val initialMarginOrders = account.orders.open.map { it.getValueAmount().absoluteValue * initialMargin }.sum()
        val maintanceMarginLong = account.portfolio.longPositions.map { it.exposure * longMaintanceMargin }.sum()
        val maintanceMarginShort = account.portfolio.longPositions.map { it.exposure * shortMaintanceMargin }.sum()
        val free = account.equity - initialMarginOrders - maintanceMarginLong - maintanceMarginShort
        val result = account.convert(free) - minimumAmount
        return if (result.value < 0.0)
            Amount(account.baseCurrency, result.value)
        else
            result * ( 1.0 / initialMargin)
    }

}

