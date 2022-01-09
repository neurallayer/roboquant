package org.roboquant.brokers.sim

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.sum

/**
 * Interface for different types of buying power calculations. These are used by the SimBroker to simulate different
 * types of account.
 *
 */
interface BuyingPower {

    /**
     * Calculate the remaining buying power for an account. The returned amount should be expressed in the base currency
     * of the account.
     */
    fun calculate(account: Account) : Amount
}

/**
 * Basic calculator that calculates: cash balance - open orders. So no equity or leverage calculations are involved.
 * This is the default BuyingPower and can be used to model a plain Cash Account, and in that case it is recommended to not allow shorting in the used
 * Policy.
 */
class CashBuyingPower(private val minimum: Double = 0.0) : BuyingPower {

    override fun calculate(account: Account): Amount {
        val cash = account.cash
        val openOrders = account.orders.open.map { it.getValueAmount() }.sum()
        val total = cash - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))
        return account.convert(total)
    }

}


/**
 * BuyingPower that allows for a fixed margin percentage.
 */
class MarginBuyingPower(private val margin: Double = 0.50, private val minimum: Double = 0.0) : BuyingPower {

    init {
        require(margin in 0.0..1.0) {"Margin between 0.0 and 1.0"}
    }

    override fun calculate(account: Account): Amount {
        val cash = account.cash
        val loanValue = account.portfolio.positions.map { it.totalCost.absoluteValue * (1.0 - margin) }.sum()
        val openOrders = account.orders.open.map { it.getValueAmount().absoluteValue }.sum() * margin
        val total = cash + loanValue - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))
        return account.convert(total) / margin
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
) : BuyingPower {

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

