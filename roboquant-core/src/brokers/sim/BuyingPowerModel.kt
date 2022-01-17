package org.roboquant.brokers.sim

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.sum

/**
 * Interface for different types of buying power calculations. These are used by the [SimBroker] to simulate the
 * different types of account that exist, like a Cash Account and Margin Account.
 *
 * At the end of each step, the buying power is re-calculaated and made available in [Account.buyingPower]
 */
interface BuyingPowerModel {

    /**
     * Calculate the total buying power for an account. The returned amount should be expressed in the base currency
     * of the account.
     */
    fun calculate(account: Account) : Amount

}

/**
 * Basic calculator that calculates: cash balance - open orders. So no leverage or margin is avaialble for trading.
 * This is the default BuyingPower and can be used to model a plain Cash Account.
 *
 * It is recommended to not to allow for shorting when using the CashBuyingPower since that is almost never
 * allowed in the real world.
 */
class CashBuyingPower(private val minimum: Double = 0.0) : BuyingPowerModel {

    override fun calculate(account: Account): Amount {
        val cash = account.cash
        val openOrders = account.orders.accepted.map { it.getValueAmount() }.sum()
        val total = cash - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))
        return account.convert(total)
    }

}


/**
 * BuyingPower that is based on a fixed leverage as often found at Forex brokers.
 */
class ForexBuyingPower(leverage: Double = 20.0) : BuyingPowerModel {

    private val margin = 1.0 / leverage

    override fun calculate(account: Account): Amount {

        // How much do we need to reserve to meet portfolio margin requirements
        val portfolioMargin = account.portfolio.positions.map { it.marketValue.absoluteValue * margin }.sum()

        // How much extra margin we might need for the open orders
        val orderMargin = account.orders.accepted.map { it.getValueAmount().absoluteValue * margin }.sum()

        // What is left over that we can use
        val total = account.equity - portfolioMargin - orderMargin
        return account.convert(total) / margin
    }

}



/**
 * BuyingPower that allows for a fixed margin percentage.
 */
class MarginBuyingPower(private val margin: Double = 0.50, private val minimum: Double = 0.0) : BuyingPowerModel {

    init {
        require(margin in 0.0..1.0) {"Margin between 0.0 and 1.0"}
    }

    override fun calculate(account: Account): Amount {
        val cash = account.cash
        val loanValue = account.portfolio.positions.map { it.totalCost.absoluteValue * (1.0 - margin) }.sum()
        val openOrders = account.orders.accepted.map { it.getValueAmount().absoluteValue }.sum() * margin
        val total = cash + loanValue - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))
        return account.convert(total) / margin
    }

}

/**
 * Usage calculator for Regulation T accounts. Formula used is
 *
 *    free = cash + loan value - initial margin
 *    buying power = free / initial margin
 *
 * Right now maintenance margin is not calculated.
 */
class RegTCalculator(
    private val initialMargin: Double= 0.5,
    // private val longMaintanceMargin: Double = 0.25,
    // private val shortMaintanceMargin: Double = 0.30,
) : BuyingPowerModel {

    override fun calculate(account: Account): Amount {
        val loanValue = account.portfolio.positions.map { it.totalCost.absoluteValue * (1.0 - initialMargin) }.sum()
        val initialMarginOrders = account.orders.open.map { it.getValueAmount().absoluteValue * initialMargin }.sum()
        val availableCash = account.cash + loanValue - initialMarginOrders
        return account.convert(availableCash) / initialMargin
    }

}

