package ibkr

import org.roboquant.brokers.CurrencyConverter
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
     * @param from
     * @param to
     * @param amount The total amount to be converted
     * @return The converted amount
     */
    override fun convert(from: Currency, to: Currency, amount: Double, now: Instant): Double {
        (from === to) && return amount

        if (to === baseCurrency)
            return exchangeRates[from]!! * amount

        if (from === baseCurrency)
            return 1 / exchangeRates[to]!! * amount

        return exchangeRates[from]!! * 1 / exchangeRates[to]!! * amount
    }



}