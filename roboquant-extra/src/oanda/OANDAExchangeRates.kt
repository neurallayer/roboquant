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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import com.oanda.v20.pricing.PricingGetRequest
import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.*
import java.time.Instant
import javax.naming.ConfigurationException

/**
 * Exchange rates implementation that retrieves the latest rates from OANDA. This implementation uses different
 * rates for BUY and SELL. If no assets are provided, all available assets will be used.
 */
class OANDAExchangeRates(
    assets: Collection<Asset> = emptyList(),
    configure: OANDAConfig.() -> Unit = {}
) : ExchangeRates {

    val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID
    private val logger = Logging.getLogger(OANDAExchangeRates::class)
    private val symbols: List<String>

    // Contains per currency pair the buy and sell rates
    private val exchangeRates = mutableMapOf<Pair<Currency, Currency>, Pair<Double, Double>>()

    init {
        config.configure()
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
        symbols = if (assets.isNotEmpty()) {
            assets.map { it.symbol }
        } else {
            OANDA.getAvailableAssets(ctx, accountID)
                .filter { it.value.type in setOf(AssetType.FOREX, AssetType.CRYPTO) }.map { it.value.symbol }
        }
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
            (pair1 in exchangeRates) -> if (amount.isPositive) 1.0 / exchangeRates.getValue(pair1).first else
                1.0 / exchangeRates.getValue(pair1).second
            (pair2 in exchangeRates) -> if (amount.isPositive) exchangeRates.getValue(pair2).first else
                exchangeRates.getValue(pair2).second
            else -> throw ConfigurationException("Cannot convert $amount to $to")
        }
    }
}