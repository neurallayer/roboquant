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

package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import org.roboquant.brokers.ExchangeRates
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import java.time.Instant

/**
 * Currency convertor that will be populated by exchange rates provided by IBKR during
 * the retrieval of the account values.
 */
internal class IBKRExchangeRates(
    configure: IBKRConfig.() -> Unit = {}
) : ExchangeRates {

    private val config = IBKRConfig()
    lateinit var baseCurrency: Currency
    val exchangeRates = mutableMapOf<Currency, Double>()
    private var lock = Object()

    init {
        config.configure()
        refresh()
    }

    fun refresh() {
        val wrapper = Wrapper()
        val client = IBKR.connect(wrapper, config)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, config.account.ifBlank { null })
        waitTillSynced()
        IBKR.disconnect(client)
    }

    /**
     * Wait till IBKR account is synchronized, so roboquant has the correct assets and cash balance available.
     */
    private fun waitTillSynced() {
        synchronized(lock) {
            lock.wait(IBKR.MAX_RESPONSE_TIME)
        }
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
            (to === baseCurrency) -> exchangeRates.getValue(from)
            (from === baseCurrency) -> 1 / exchangeRates.getValue(to)
            else -> exchangeRates.getValue(from) * 1 / exchangeRates.getValue(to)
        }

    }

    /**
     * Overwrite the default wrapper
     */
    inner class Wrapper : DefaultEWrapper() {

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            if (currency != null && "BASE" != currency) {
                when (key) {
                    "BuyingPower" -> baseCurrency = Currency.getInstance(currency)
                    "ExchangeRate" -> {
                        val c = Currency.getInstance(currency)
                        exchangeRates[c] = value.toDouble()
                    }
                }
            }
        }

        override fun accountDownloadEnd(p0: String?) {
            synchronized(lock) {
                lock.notify()
            }
        }

    }
}



