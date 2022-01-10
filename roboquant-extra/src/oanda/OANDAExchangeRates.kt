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

import com.oanda.v20.Context
import com.oanda.v20.pricing.PricingGetRequest
import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.*
import java.time.Instant
import javax.naming.ConfigurationException

/**
 * Currency converter used by default by OANDA Broker that uses feed data to update exchange rates.
 */
class OANDAExchangeRates(
    assets: Iterable<Asset>,
    token: String? = null,
    demoAccount: Boolean = true,
    accountID: String? = null,
) : ExchangeRates {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    private val accountID = OANDA.getAccountID(accountID, ctx)

    internal lateinit var baseCurrency: Currency
    private val exchangeRates = mutableMapOf<Pair<Currency, Currency>, Double>()
    private val logger = Logging.getLogger(OANDAExchangeRates::class)

    init {
        val symbols = assets.map { it.symbol }
        val request = PricingGetRequest(this.accountID, symbols)
        val resp = ctx.pricing[request]
        for (price in resp.prices) {
            val symbol = price.instrument.toString()
            val pair1 = symbol.toCurrencyPair()!!
            val pair2 = Pair(pair1.second, pair1.first)
            val quote1 = price.quoteHomeConversionFactors.positiveUnits.doubleValue()
            val quote2 = 1.0 / price.quoteHomeConversionFactors.negativeUnits.doubleValue()
            exchangeRates[pair1] = quote1
            exchangeRates[pair2] = quote2
            logger.finer {"Added $pair1 : $quote1 and $pair2 : $quote2 to the exchange rates"}
        }
        logger.info {"Added ${exchangeRates.size} exchange rates"}
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
        (from === to || amount.value == 0.0) && return 1.0

        val pair1 = Pair(from, to)
        val pair2 = Pair(to, from)
        return when {
            (pair1 in exchangeRates) -> exchangeRates[pair1]!!
            (pair2 in exchangeRates) -> 1.0 / exchangeRates[pair2]!!
            else -> throw ConfigurationException("Cannot convert $amount to $to")
        }

    }
}