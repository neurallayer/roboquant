package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Currency convertor that can be filled by exchange rates provided by IBKR during the retrieval of the account values
 */
internal class IBKRExchangeRates(
    configure: IBKRConfig.() -> Unit = {}
) : ExchangeRates {

    private val config = IBKRConfig()
    lateinit var baseCurrency: Currency
    val exchangeRates = mutableMapOf<Currency, Double>()

    init {
        config.configure()
        refresh()
    }

    fun refresh() {
        val wrapper = Wrapper()
        val client = IBKRConnection.connect(wrapper, config)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, config.account.ifBlank { null })
        waitTillSynced()
        IBKRConnection.disconnect(client)
    }

    /**
     * Wait till IBKR account is synchronized so roboquant has the correct assets and cash balance available.
     *
     * @TODO: replace sleep with real check
     */
    private fun waitTillSynced() {
        @Suppress("MagicNumber")
        (Thread.sleep(5_000))
    }

    /**
     * Convert between two currencies.
     * @see ExchangeRates.getRate
     *
     * @param to
     * @param amount The total amount to be converted
     * @return The converted amount
     */
    override fun getRate(amount: Amount, to: Currency, time: Instant): Double {
        val from = amount.currency
        (from === to) && return 1.0

        return when {
            (to === baseCurrency) -> exchangeRates[from]!!
            (from === baseCurrency) -> 1 / exchangeRates[to]!!
            else -> exchangeRates[from]!! * 1 / exchangeRates[to]!!
        }

    }

    /**
     * Overwrite the default wrapper
     */
    inner class Wrapper : DefaultEWrapper() {

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            if (currency != null && "BASE" != currency) {
                when (key) {
                    "BuyingPower" -> baseCurrency = Currency.getInstance(currency)
                    "ExchangeRate" -> {
                        val c = Currency.getInstance(currency)
                        exchangeRates[c] = value.toDouble()
                    }
                }
            }
        }


    }
}



