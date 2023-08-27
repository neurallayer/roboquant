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

package org.roboquant.common

import de.siegmar.fastcsv.reader.NamedCsvReader
import org.roboquant.common.Currency.Companion.USD
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate


/**
 * A `Universe` represents a collection of assets. Where it differs from a regular collection, is that the assets that
 * belong to the collection can change over time. So the assets in the collection at time `t` can be different from
 * the assets in the collection at time `t+1`.
 */
interface Universe {

    /**
     * Return the list of assets in this universe at the given point in [time]
     */
    fun getAssets(time: Instant): List<Asset>

    /**
     * Set of standard universes like the assets in the S&P 500 index
     */
    companion object Factory {

        /**
         * Return the universe of all the S&P 500 stocks.
         */
        val sp500: Universe by lazy { SP500() }

    }
}

/**
 * SP500 asset collection
 */
private class SP500 : Universe {

    private val startSP500 = LocalDate.parse("1960-01-01")
    private val assets: List<Pair<Asset, Timeframe>>
    private val fileName = "/sp500.csv"

    init {
        val stream =
            SP500::class.java.getResourceAsStream(fileName) ?: throw RoboquantException("Couldn't find file $fileName")
        stream.use { inputStream ->
            val content = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            val builder = NamedCsvReader.builder().fieldSeparator(';').build(content)
            val us = Exchange.getInstance("US")
            assets = builder.map {
                val symbol = it.getField("Symbol")
                val asset = Asset(symbol, currency = USD, exchange = us)
                val date = it.getField("Date")
                val startDate = if (date.isNotEmpty()) LocalDate.parse(it.getField("Date")) else startSP500
                val start = startDate.atTime(0, 0).atZone(us.zoneId).toInstant()
                val timeframe = Timeframe(start, Timeframe.MAX)
                Pair(asset, timeframe)
            }
        }
    }

    /**
     * Return the SP500 stocks (for now, ignoring mostly the time).
     */
    override fun getAssets(time: Instant): List<Asset> {
        return assets.filter { it.second.contains(time) }.map { it.first }
    }

}
