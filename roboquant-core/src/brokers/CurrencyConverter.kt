package org.roboquant.brokers

import org.roboquant.common.Currency
import java.time.Instant

/**
 * Interface for all types of currency converters that will convert an amount from one currency to another currency.
 * The interface allows currency conversions to take the following aspects into account:
 *
 * - the time of the conversion
 * - the amount to be converted
 *
 */
interface CurrencyConverter {

    /**
     * Convert a monetary amount from one currency to another currency at a specific moment in time.
     *
     * It depends on the implementation all parameters are also actually used by the underlying algorithm. If a
     * conversion cannot be processed due to incorrect or missing configuration, it is expected to throw an exception.
     *
     * @param from The originating currency that needs to be converted
     * @param to The currency that the money needs to be converted to
     * @param amount The amount (in from currency denoted) that needs to be converted
     * @param now The time of the conversion
     * @return The converted amount
     */
    fun convert(from: Currency, to: Currency, amount: Double, now: Instant): Double

}
