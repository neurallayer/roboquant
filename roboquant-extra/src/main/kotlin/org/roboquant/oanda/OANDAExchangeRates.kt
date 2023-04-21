/*
 * Copyright 2020-2023 Neural Layer
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
 * Exchange rates that retrieves the latest rates from OANDA. This implementation uses different
 * rates for BUY and SELL. If no symbols are provided, all available FOREX & CRYPTO symbols will be used.
 *
 * Please note these are only the current exchange rates, so there is are no historic rates provided.
 *
 * @param symbols a list of symbols to restrict the currencies loaded
 * @param configure additional configuration settings to connect to OANDA
 */
class OANDAExchangeRates(
    symbols: Collection<String> = emptyList(),
    configure: OANDAConfig.() -> Unit = {}
) : ExchangeRates {

    private val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID
    private val logger = Logging.getLogger(OANDAExchangeRates::class)
    private val symbols: Collection<String>

    // Contains per currency pair the buy and sell exchange rates
    private val exchangeRates = mutableMapOf<Pair<Currency, Currency>, Pair<Double, Double>>()

    init {
        config.configure()
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
        this.symbols = symbols.ifEmpty {
            OANDA.getAvailableAssets(ctx, accountID)
                .filter { it.value.type in setOf(AssetType.FOREX, AssetType.CRYPTO) }.map { it.value.symbol }
        }
        logger.debug {  OANDA.getAvailableAssets(ctx, accountID).toList() }
        require(this.symbols.isNotEmpty()) { "no available currency pairs found for account=$accountID"}
        refresh()
    }

    /**
     * Refresh the exchange rates from OANDA
     */
    fun refresh() {
        val request = PricingGetRequest(accountID, symbols)
        val resp = ctx.pricing[request]
        for (price in resp.prices) {
            val symbol = price.instrument.toString()
            val pair = symbol.toCurrencyPair()!!
            val quote1 = price.quoteHomeConversionFactors.positiveUnits.doubleValue()
            val quote2 = price.quoteHomeConversionFactors.negativeUnits.doubleValue()
            exchangeRates[pair] = Pair(quote1, quote2)
            logger.trace { "Added $pair BUY=$quote1 SELL=$quote2" }
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