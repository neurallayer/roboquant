package org.roboquant.brokers

import org.roboquant.common.Currency
import java.time.Instant


/**
 * Currency converter that supports fixed exchange rates between currencies, so rates that don't change over the
 * duration of a run. It provides logic to convert between two currencies given this map of
 * exchange rates. It is smart in the sense that is able to convert between currencies even if there is no direct
 * exchange rate defined in the map for a given currency pair.
 *
 * It will throw an exception if a conversion is required for an unknown currency.
 *
 * @constructor Create a new  fixed currency converter
 */
class FixedExchangeRates(val baseCurrency: Currency, val exchangeRates: Map<Currency, Double>) : CurrencyConverter {

    constructor(baseCurrency: Currency, vararg rates: Pair<Currency, Double>) : this(baseCurrency, rates.toMap())


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
