package org.roboquant.common

import org.junit.Test
import kotlin.test.*

internal class CurrenciesTest {

    @Test
    fun test() {
        assertTrue {
            Currency.EUR
            Currency.USD
            Currency.USD
            Currency.CAD
            Currency.CHF
            Currency.JPY
            Currency.HKD
            Currency.GBP
            Currency.AUD
            Currency.CNY
            Currency.NZD
            true
        }
    }

}