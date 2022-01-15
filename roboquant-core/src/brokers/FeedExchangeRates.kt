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

import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.toCurrencyPair
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.util.*

/**
 * use a feed as the basis for currency conversion rates
 */
class FeedExchangeRates(feed: HistoricFeed, baseCurrency: Currency = Currency.USD, private val priceType: String = "DEFAULT") : TimeExchangeRates(baseCurrency) {

    init {
        setRates(feed)
    }

    private fun setRates(feed: HistoricFeed) {
        val actions = feed.filter<PriceAction> {
            it.asset.type == AssetType.FOREX
        }
        for ((now, action) in actions) {
            val asset = action.asset
            val rate = action.getPrice(priceType)
            val codes = asset.symbol.toCurrencyPair()
            if (codes != null) {
                val (from, to) = codes
                if (to == baseCurrency) {
                    val map = exchangeRates.getOrPut(from) { TreeMap() }
                    map[now] = rate
                } else if (from == baseCurrency) {
                    val map = exchangeRates.getOrPut(to) { TreeMap() }
                    map[now] = 1.0 / rate
                }

            }
        }

    }

}