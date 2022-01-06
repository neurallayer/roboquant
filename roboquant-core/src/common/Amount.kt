package org.roboquant.common

import java.math.BigDecimal

data class Amount(val currency: Currency, val value: Double) {
    operator fun times(d: Number): Amount = Amount(currency, value * d.toDouble())
    operator fun plus(d: Number): Amount = Amount(currency, value + d.toDouble())

    operator fun plus(other: Amount): Cash = Cash(this, other)


    /**
     * Format the value hold in this amount based on the currency. For example USD would have two fraction digits
     * by default while JPY would have none.
     */
    fun formatValue(fractionDigits: Int = currency.defaultFractionDigits) = toBigDecimal(fractionDigits).toString()


    /**
     * Convert the value to BigDecimal using the number of digits defined for the currency. Internally roboquant
     * doesn't use BigDecimals, but this method is used to enable a nicer display of currency amounts.
     */
    fun toBigDecimal(fractionDigits: Int = currency.defaultFractionDigits): BigDecimal =
        BigDecimal(value).setScale(fractionDigits, Currency.roundingMode)


    override fun toString(): String = "${currency.currencyCode} ${formatValue()}"


}