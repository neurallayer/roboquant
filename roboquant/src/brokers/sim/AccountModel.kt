package org.roboquant.brokers.sim

import org.roboquant.brokers.*
import org.roboquant.common.Amount
import org.roboquant.common.Logging

/**
 * Interface for modelling different types Accounts in the [SimBroker], like a [CashAccount] or [MarginAccount]
 *
 * The main functionality is that at the end of each step the buying power is re-calculated and made
 * available in [Account.buyingPower].
 */
interface AccountModel {

    /**
     * Returns the total amount of remaining buying power for a given [account]. The returned amount should be
     * in the base currency of the account.
     */
    fun getBuyingPower(account: InternalAccount): Amount

}

/**
 * Basic calculator that calculates: cash balance - open orders. So no leverage or margin is available for trading.
 * This is the default BuyingPower and can be used to model a plain Cash Account.
 *
 * You should not short positions when using the CashModel since that is almost never allowed in the real world
 * and also not supported. It will generate warning messages.
 *
 * Note: currently open orders are not taken into consideration when calculating the total buying power
 *
 * @property minimum the minimum amount of cash balance required to maintain
 */
class CashAccount(private val minimum: Double = 0.0) : AccountModel {

    private val logger = Logging.getLogger(CashAccount::class)

    override fun getBuyingPower(account: InternalAccount): Amount {
        if (account.portfolio.values.any { it.short }) {
            logger.warning("Having short positions while using cash account is not supported")
        }

        return account.cash.convert(account.baseCurrency, account.lastUpdate) - minimum
    }

}


/**
 * Account model that supports trading with margin. The buying power calculation uses the following formula:
 *
 *      1. long value = long positions * maintenance margin long
 *      2. short value = short positions * maintenance margin short
 *      3. excess margin = equity - long value - short value - minimum equity
 *      4. buying power = excess margin * ( 1 / initial margin)
 *
 * Note: currently open orders are not taken into consideration when calculating the total buying power
 */
class MarginAccount(
    private val initialMargin: Double = 0.50,
    private val maintenanceMarginLong: Double = 0.3,
    private val maintenanceMarginShort: Double = maintenanceMarginLong,
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
        require(maintenanceMarginLong in 0.0..1.0) { "maintenanceMarginLong between 0.0 and 1.0" }
        require(maintenanceMarginShort in 0.0..1.0) { "maintenanceMarginShort between 0.0 and 1.0" }
    }

    override fun getBuyingPower(account: InternalAccount): Amount {
        val excessMargin = account.cash + account.portfolio.marketValue
        
        val positions = account.portfolio.values
        excessMargin.withdraw(positions.long.exposure * maintenanceMarginLong)
        excessMargin.withdraw(positions.short.exposure * maintenanceMarginShort)
        excessMargin.withdraw(Amount(account.baseCurrency, minimumEquity))
        val buyingPower = excessMargin * (1.0 / initialMargin)
        return buyingPower.convert(account.baseCurrency, account.lastUpdate)
    }

}

