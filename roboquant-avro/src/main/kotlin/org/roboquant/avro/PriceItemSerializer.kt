/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.avro

import org.roboquant.common.Asset
import org.roboquant.common.TimeSpan
import org.roboquant.common.UnsupportedException
import org.roboquant.feeds.*

/**
 * Used by AvroFeed to serialize and deserialize [PriceItem] to a DoubleArray, so it can be stored in an Avro file.
 */
internal class PriceItemSerializer {

    internal class Serialization(val type: PriceItemType, val values: List<Double>, val meta: String? = null)

    private val timeSpans = mutableMapOf<String, TimeSpan>()

    fun serialize(item: PriceItem): Serialization {
        return when (item) {
            is PriceBar -> Serialization(PriceItemType.BAR, item.ohlcv.toList(), item.timeSpan?.toString())
            is TradePrice -> Serialization(PriceItemType.TRADE, listOf(item.price, item.volume))
            is PriceQuote -> Serialization(
                PriceItemType.QUOTE,
                listOf(item.askPrice, item.askSize, item.bidPrice, item.bidSize)
            )

            is OrderBook -> Serialization(PriceItemType.BOOK, orderBookToValues(item))
            else -> throw UnsupportedException("cannot serialize action=$item")
        }
    }


    private fun getPriceBar(asset: Asset, values: DoubleArray, other: String?): PriceBar {
        val timeSpan = if (other != null) {
            timeSpans.getOrPut(other) { TimeSpan.parse(other) }
        } else {
            null
        }
        return PriceBar(asset, values, timeSpan)
    }

    fun deserialize(asset: Asset, type: PriceItemType, values: List<Double>, other: String?): PriceItem {
        return when (type) {
            PriceItemType.BAR -> getPriceBar(asset, values.toDoubleArray(), other)
            PriceItemType.TRADE -> TradePrice(asset, values[0], values[1])
            PriceItemType.QUOTE -> PriceQuote(asset, values[0], values[1], values[2], values[3])
            PriceItemType.BOOK -> getOrderBook(asset, values)
            else -> throw UnsupportedException("cannot deserialize asset=$asset type=$type")
        }
    }

    private fun orderBookToValues(action: OrderBook): List<Double> {
        return listOf(action.asks.size.toDouble()) +
                action.asks.map { listOf(it.size, it.limit) }.flatten() +
                action.bids.map { listOf(it.size, it.limit) }.flatten()
    }


    private fun getOrderBook(asset: Asset, values: List<Double>): OrderBook {
        val asks = mutableListOf<OrderBook.OrderBookEntry>()
        val bids = mutableListOf<OrderBook.OrderBookEntry>()
        val endAsks = 1 + 2 * values[0].toInt()
        for (i in 1 until endAsks step 2) {
            val entry = OrderBook.OrderBookEntry(values[i], values[i + 1])
            asks.add(entry)
        }

        for (i in endAsks until values.lastIndex step 2) {
            val entry = OrderBook.OrderBookEntry(values[i], values[i + 1])
            bids.add(entry)
        }
        return OrderBook(asset, asks, bids)
    }


}
