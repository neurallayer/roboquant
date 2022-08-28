/*
 * Copyright 2022 Neural Layer
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


/**
 * Currency converter that supports fixed exchange rates between currencies, so rates that don't change over the
 * duration of a run. It provides logic to convert between two currencies given this map of
 * exchange rates. It is smart in the sense that is able to convert between currencies even if there is no direct
 * exchange rate defined in the map for a given currency pair.
 *
 * It will throw an exception if a conversion is required for an unknown currency.
 *
 * @property baseCurrency the base currency for which the [exchangeRates] are provided
 *
 * @constructor Create a new  fixed currency converter
 */
class FixedExchangeRates(val baseCurrency: Currency, private val exchangeRates: Map<Currency, Double>) : ExchangeRates {

    constructor(baseCurrency: Currency, vararg rates: Pair<Currency, Double>) : this(baseCurrency, rates.toMap())

    /**
     * Returns the exchange rate
     * @see ExchangeRates.getRate
     *
     */
    override fun getRate(amount: Amount, to: Currency, time: Instant): Double {
        val from = amount.currency
        return when {
            from === to -> 1.0
            to === baseCurrency -> exchangeRates.getValue(from)
            from === baseCurrency -> 1.0 / exchangeRates.getValue(to)
            else -> exchangeRates.getValue(from) * 1.0 / exchangeRates.getValue(to)
        }
    }

}
