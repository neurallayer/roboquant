/*
 * Copyright 2020-2022 Neural Layer
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
 * Exchange Rates implementation that supports different rates at different times. This abstract class is used
 * by other exchange rates implementations like FeedCurrencyConverter and ECBCurrencyConverter.
 */
abstract class TimedExchangeRates(protected val baseCurrency: Currency) : ExchangeRates {

    protected val exchangeRates = mutableMapOf<Currency, NavigableMap<Instant, Double>>()

    val currencies: Collection<Currency>
        get() = exchangeRates.keys + setOf(baseCurrency)

    private fun find(currency: Currency, time: Instant): Double {
        val rates = exchangeRates.getValue(currency)
        val result = rates.floorEntry(time) ?: rates.firstEntry()
        return result.value
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
            (to === baseCurrency) -> find(from, time)
            (from === baseCurrency) -> 1.0 / find(to, time)
            else -> find(from, time) * (1.0 / find(to, time))
        }
    }
}