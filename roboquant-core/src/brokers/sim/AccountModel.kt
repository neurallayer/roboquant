package org.roboquant.brokers.sim

import org.roboquant.brokers.*
import org.roboquant.common.Amount
import org.roboquant.common.Logging

/**
 * Interface for modelling types Accounts in the [SimBroker], like a [CashAccount] or [MarginAccount]
 *
 * The main functionality is that at the end of each step the buying power is re-calculaated and made
 * available in [Account.buyingPower].
 */
interface AccountModel {

    /**
     * Calculate the total buying power for a given [account]. The returned amount should be expressed in the
     * base currency of the account.
     */
    fun calculate(account: InternalAccount): Amount

}

/**
 * Basic calculator that calculates: cash balance - open orders. So no leverage or margin is avaialble for trading.
 * This is the default BuyingPower and can be used to model a plain Cash Account.
 *
 * You should not using shorting when using the CashBuyingPower since that is almost never allowed in the real world
 * and also not supported. It will generate warning messages.
 */
class CashAccount(private val minimum: Double = 0.0) : AccountModel {

    val logger = Logging.getLogger(CashAccount::class)

    override fun calculate(account: InternalAccount): Amount {
        val total = account.cash

        // Only accepted orders are taken into consideration
        // val openOrders = account.orders.accepted.map { it.value().absoluteValue }.sum()
        // val total = cash // - openOrders
        total.withdraw(Amount(account.baseCurrency, minimum))

        if (account.portfolio.values.any { it.short }) {
            logger.warning("Having short positions while using cash account is not supported")
        }

        return account.convert(total)
    }

}


/**
 * BuyingPower that supports initial and maintanace margin
 *
 * Formula
 *
 *      long value = long positions * maintance margin long
 *      short value = short positions * maintance margin short
 *      excess margin = equity - long value - short value - minimum equity
 *      buying power = excess margin * ( 1 / initial margin)
 *
 */
class MarginAccount(
    private val initialMargin: Double = 0.50,
    private val maintanceMarginLong: Double = 0.3,
    private val maintanceMarginShort: Double = maintanceMarginLong,
    private val minimumEquity: Double = 0.0
    // private val includeOpenOrders: Boolean = false
) : AccountModel {

    /**
     * Create a margin based on a leverage. Effectively all margins will be set to 1/leverage
     */
    constructor(leverage: Double, minimumEquity: Double = 0.0) : this(
        1.0 / leverage,
        1.0 / leverage,
        1.0 / leverage,
        minimumEquity
    )

    init {
        require(initialMargin in 0.0..1.0) { "initialMargin between 0.0 and 1.0" }
        require(maintanceMarginLong in 0.0..1.0) { "maintanceMarginLong between 0.0 and 1.0" }
        require(maintanceMarginShort in 0.0..1.0) { "maintanceMarginShort between 0.0 and 1.0" }
    }

    override fun calculate(account: InternalAccount): Amount {
        val longValue = account.portfolio.values.long.exposure * maintanceMarginLong
        val shortValue = account.portfolio.values.short.exposure * maintanceMarginShort
        val excessMargin = account.equity - longValue - shortValue - Amount(account.baseCurrency, minimumEquity)
        val buyingPower = excessMargin * (1.0 / initialMargin)
        return account.convert(buyingPower)
    }

}

