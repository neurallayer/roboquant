/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.questdb

import io.questdb.cairo.CairoEngine
import io.questdb.cairo.TableWriter
import io.questdb.cairo.sql.Record
import org.roboquant.common.Asset
import org.roboquant.common.ConfigurationException
import org.roboquant.common.TimeSpan
import org.roboquant.common.UnsupportedException
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import kotlin.reflect.KClass


/**
 * Interface for various price action handlers
 */
internal interface PriceActionHandler<T : PriceAction> {

    fun updateRecord(row: TableWriter.Row, action: PriceAction)

    fun getPriceAction(asset: Asset, record: Record): T

    fun createTable(name: String, partition: String, engine: CairoEngine)

    /**
     * @suppress
     */
    companion object {
        /**
         * Detect and return the handler to use for the table.
         */
        internal fun detectHandler(engine: CairoEngine, tableName: String): PriceActionHandler<*> {
            val columns = engine.tableColumns(tableName)
            return when {
                columns.contains("open") -> PriceBarHandler()
                columns.contains("askSize") -> PriceQuoteHandler()
                columns.contains("price") -> TradePriceHandler()
                else -> throw ConfigurationException("unknown table format table=$tableName")
            }
        }


        fun getHandler(type: KClass<*>): PriceActionHandler<*> {
            return when (type) {
                PriceBar::class -> PriceBarHandler()
                PriceQuote::class -> PriceQuoteHandler()
                TradePrice::class -> TradePriceHandler()
                else -> throw UnsupportedException("PriceAction ${type.simpleName} not supported")
            }
        }

    }
}


private class PriceBarHandler : PriceActionHandler<PriceBar> {

    /**
     * Cache time-spans
     */
    private val timeSpans = mutableMapOf<String, TimeSpan>()

    override fun createTable(name: String, partition: String, engine: CairoEngine) {
        // Let's drop the table first if it already exists
        engine.update(
            """CREATE TABLE IF NOT EXISTS $name (
                |asset SYMBOL,
                |time TIMESTAMP,
                |open DOUBLE,  
                |high DOUBLE, 
                |low DOUBLE, 
                |close DOUBLE, 
                |volume DOUBLE, 
                |span SYMBOL
                |) timestamp(time) PARTITION BY $partition""".trimMargin(),
        )
    }

    override fun updateRecord(row: TableWriter.Row, action: PriceAction) {
        if (action !is PriceBar) return
        with(action) {
            row.putDouble(2, open)
            row.putDouble(3, high)
            row.putDouble(4, low)
            row.putDouble(5, close)
            row.putDouble(6, volume)
            row.putSym(7, timeSpan?.toString())
        }
    }

    override fun getPriceAction(asset: Asset, record: Record): PriceBar {
        val agg = record.getSym(7)?.toString()
        val timeSpan = if (agg == null) null else timeSpans.getOrPut(agg) { TimeSpan.parse(agg) }
        return PriceBar(
            asset,
            record.getDouble(2),
            record.getDouble(3),
            record.getDouble(4),
            record.getDouble(5),
            record.getDouble(6),
            timeSpan
        )
    }


}


private class TradePriceHandler : PriceActionHandler<TradePrice> {

    override fun createTable(name: String, partition: String, engine: CairoEngine) {
        // Let's drop the table first if it already exists
        engine.update(
            """CREATE TABLE IF NOT EXISTS $name (
                |asset SYMBOL,
                |time TIMESTAMP,
                |price DOUBLE,  
                |volume DOUBLE 
                |) timestamp(time) PARTITION BY $partition""".trimMargin(),
        )
    }


    override fun updateRecord(row: TableWriter.Row, action: PriceAction) {
        if (action !is TradePrice) return
        with(action) {
            row.putDouble(2, price)
            row.putDouble(3, volume)
        }

    }

    override fun getPriceAction(asset: Asset, record: Record): TradePrice {
        return TradePrice(
            asset,
            record.getDouble(2),
            record.getDouble(3),
        )
    }

}


private class PriceQuoteHandler : PriceActionHandler<PriceQuote> {

    override fun createTable(name: String, partition: String, engine: CairoEngine) {
        // Let's drop the table first if it already exists
        engine.update(
            """CREATE TABLE IF NOT EXISTS $name (
                |asset SYMBOL,
                |time TIMESTAMP,
                |askPrice DOUBLE,  
                |askSize DOUBLE, 
                |bidPrice DOUBLE, 
                |bidSize DOUBLE
                |) timestamp(time) PARTITION BY $partition""".trimMargin(),
        )
    }


    override fun updateRecord(row: TableWriter.Row, action: PriceAction) {
        if (action !is PriceQuote) return
        with(action) {
            row.putDouble(2, askPrice)
            row.putDouble(3, askSize)
            row.putDouble(4, bidPrice)
            row.putDouble(5, bidSize)
        }
    }

    override fun getPriceAction(asset: Asset, record: Record): PriceQuote {
        return PriceQuote(
            asset,
            record.getDouble(2),
            record.getDouble(3),
            record.getDouble(4),
            record.getDouble(5)
        )
    }


}
