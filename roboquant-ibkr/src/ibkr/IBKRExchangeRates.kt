package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import com.ib.client.EClientSocket
import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Currency convertor that can be filled by exchange rates provided by IBKR during the retrieval of the account values
 */
internal class IBKRExchangeRates(
    host: String = "127.0.0.1",
    port: Int = 4002,
    clientId: Int = 3,
    accountId: String? = null,
) : ExchangeRates {

    private var client: EClientSocket
    lateinit var baseCurrency: Currency
    val exchangeRates = mutableMapOf<Currency, Double>()


    init {
        val wrapper = Wrapper()
        client = IBKRConnection.connect(wrapper, host, port, clientId)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, accountId)
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



