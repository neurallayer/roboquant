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

package org.roboquant.oanda

import org.roboquant.brokers.CurrencyConverter
import org.roboquant.common.Currency
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Currency converter used by default by OANDA Broker that uses live feed data to update exchange rates.
 */
class OANDACurrencyConverter(private val priceType: String = "DEFAULT") : CurrencyConverter {

    internal lateinit var baseCurrency: Currency
    private val exchangeRates = mutableMapOf<Currency, Double>()


    internal fun setRate(symbol: String, action :PriceAction) {
        val codes = symbol.split("_")
        if (codes.size == 2) {
            val rate = action.getPrice(priceType)
            val from = Currency.getInstance(codes.first())
            val to = Currency.getInstance(codes.last())
            if (to == baseCurrency)
                exchangeRates[from] = rate
            else if (from == baseCurrency)
                exchangeRates[to] = 1.0/ rate
        }
    }

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