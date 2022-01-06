/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant
import java.util.*

/**
 * Currency Convertor that supports different rates at different times. Abstract class that is used by
 * FeedCurrencyConverter and ECBCurrencyConverter.
 */
abstract class TimeCurrencyConverter(protected val baseCurrency: Currency) : CurrencyConverter {

    protected val exchangeRates = mutableMapOf<Currency, NavigableMap<Instant,Double>>()

    val currencies: Collection<Currency>
        get() = exchangeRates.keys

    private fun find(currency: Currency, time: Instant): Double {
        val rates = exchangeRates[currency]!!
        val result = rates.floorEntry(time) ?: rates.firstEntry()
        return result.value
    }

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

        val rate = when {
            (to === baseCurrency) -> find(from, now)
            (from === baseCurrency) -> 1.0 / find(to, now)
            else -> find(from, now) * (1.0 / find(to, now))
        }

        return Amount(to, amount.value * rate)
    }
}