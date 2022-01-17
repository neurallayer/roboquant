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
 * Exchange rates implementation that retrieves the latest rates from OANDA. This implementation uses different
 * rates for BUY and SELL.
 */
class OANDAExchangeRates(
    assets: Iterable<Asset>,
    token: String? = null,
    demoAccount: Boolean = true,
    accountID: String? = null,
) : ExchangeRates {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    private val accountID = OANDA.getAccountID(accountID, ctx)
    private val logger = Logging.getLogger(OANDAExchangeRates::class)
    private val symbols = assets.map { it.symbol }

    // Contains per currency pair the buy and sell rates
    private val exchangeRates = mutableMapOf<Pair<Currency, Currency>, Pair<Double, Double>>()


    companion object {

        /**
         * Get the exchange rates for all available assets
         */
        fun allAvailableAssets(
            token: String? = null,
            demoAccount: Boolean = true,
            accountID: String? = null,
        ): OANDAExchangeRates {
            val ctx: Context = OANDA.getContext(token, demoAccount)
            val accountID2 = OANDA.getAccountID(accountID, ctx)
            val assets = OANDA.getAvailableAssets(ctx, accountID2).values
            return OANDAExchangeRates(assets, token, demoAccount, accountID)
        }
    }

    init {
        refresh()
    }

    /**
     * Refresh the exchange rates
     */
    fun refresh() {
        val request = PricingGetRequest(this.accountID, symbols)
        val resp = ctx.pricing[request]
        for (price in resp.prices) {
            val symbol = price.instrument.toString()
            val pair = symbol.toCurrencyPair()!!
            val quote1 = price.quoteHomeConversionFactors.positiveUnits.doubleValue()
            val quote2 = price.quoteHomeConversionFactors.negativeUnits.doubleValue()
            exchangeRates[pair] = Pair(quote1, quote2)
            logger.finer { "Added $pair BUY=$quote1 SELL=$quote2" }
        }
        logger.info { "Added ${exchangeRates.size} exchange rates" }
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
            (pair1 in exchangeRates) -> if (amount.isPositive) 1.0 / exchangeRates[pair1]!!.first else 1.0 / exchangeRates[pair1]!!.second
            (pair2 in exchangeRates) -> if (amount.isPositive) exchangeRates[pair2]!!.first else exchangeRates[pair2]!!.second
            else -> throw ConfigurationException("Cannot convert $amount to $to")
        }
    }
}