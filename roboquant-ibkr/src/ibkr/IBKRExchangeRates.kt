package ibkr

import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Currency convertor that is filled by exchange rates provided by IBKR during the retrieval of the account values
 */
internal class IBKRExchangeRates : ExchangeRates {

    lateinit var baseCurrency: Currency
    val exchangeRates = mutableMapOf<Currency, Double>()

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



}