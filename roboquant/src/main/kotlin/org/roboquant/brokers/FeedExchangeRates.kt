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
import org.roboquant.common.AssetType
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Currency
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.time.Instant
import java.util.*

/**
 * Use a feed with [PriceAction] to determine currency conversion rates.
 *
 * @param feed the feed to use
 * @property priceType the type of price, by default "DEFAULT"
 * @property assetTypes the types of asset to consider, by default CRYPTO and FOREX
 */
class FeedExchangeRates(
    feed: Feed,
    private val priceType: String = "DEFAULT",
    private val assetTypes: Set<AssetType> = setOf(AssetType.CRYPTO, AssetType.FOREX)
) : ExchangeRates {

    private val exchangeRates = mutableMapOf<Pair<Currency, Currency>, NavigableMap<Instant, Double>>()

    val currencies
        get() = exchangeRates.keys.map { listOf(it.first, it.second) }.flatten().toSet()

    init {
        setRates(feed)
    }

    private fun setRates(feed: Feed) {
        val actions = feed.filter<PriceAction> {it.asset.type in assetTypes }
        for ((now, action) in actions) {
            val asset = action.asset
            val rate = action.getPrice(priceType)
            val map = exchangeRates.getOrPut(asset.currencyPair) { TreeMap() }
            map[now] = rate
        }
    }

    private fun find(pair: Pair<Currency, Currency>, time: Instant): Double? {
        val rates = exchangeRates[pair]
        return if (rates !== null) {
            val result = rates.floorEntry(time) ?: rates.firstEntry()
            result.value
        } else {
            null
        }
    }

    override fun getRate(amount: Amount, to: Currency, time: Instant): Double {
        val from = amount.currency
        (from === to || amount.value == 0.0) && return 1.0

        var result = find(Pair(amount.currency, to), time)
        if (result !== null) return result

        result = find(Pair(amount.currency, to), time)
        if (result !== null) return 1.0/result
        throw ConfigurationException("No conversion for $amount to $to")
    }

}
