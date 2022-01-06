package ibkr

import org.roboquant.brokers.CurrencyConverter
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Currency convertor that is filled by exchange rates provided by IBKR during the retrieval of the account values
 */
internal class IBKRCurrencyConverter : CurrencyConverter {

    lateinit var baseCurrency: Currency
    val exchangeRates = mutableMapOf<Currency, Double>()

    /**
     * Convert between two currencies.
     * @see CurrencyConverter.convert
     *
     * @param to
     * @param amount The total amount to be converted
     * @return The converted amount
     */
    override fun convert(amount: Amount, to: Currency, now: Instant): Amount {
        val from = amount.currency
        (from === to) && return amount

        val corr = when {
            (to === baseCurrency) -> exchangeRates[from]!!
            (from === baseCurrency) -> 1 / exchangeRates[to]!!
            else -> exchangeRates[from]!! * 1 / exchangeRates[to]!!
        }

        return Amount(to, amount.value * corr)
    }



}