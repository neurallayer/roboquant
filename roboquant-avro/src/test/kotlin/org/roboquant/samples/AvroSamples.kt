/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.samples

import org.roboquant.avro.AvroFeed
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.csv.TimeParser
import org.roboquant.strategies.FlexConverter
import org.roboquant.run
import org.roboquant.strategies.EMACrossover
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AvroSamples {

    @Test
    @Ignore
    internal fun generate() {
        val path = Path("/tmp/us")
        val path1 = path / "nasdaq stocks"
        val path2 =  path / "nyse stocks"

        val feed = CSVFeed(path1.toString(), CSVConfig.stooq())
        val tmp = CSVFeed(path2.toString(), CSVConfig.stooq())
        feed.merge(tmp)

        val avroFeed = AvroFeed("/tmp/sp25.avro")

        val s = "MSFT,NVDA,AAPL,AMZN,META,GOOGL,GOOG,BRK.B,LLY,AVGO,JPM,XOM,TSLA,UNH,V,PG,MA,COST,JNJ,HD,MRK,NFLX,WMT,ABBV,CVX"
        val symbols = s.split(',').toTypedArray()
        assertEquals(25, symbols.size)

        val timeframe = Timeframe.fromYears(2020, 2024)
        avroFeed.record(
            feed,
            true,
            timeframe,
            assetFilter = AssetFilter.includeSymbols(*symbols)
        )

    }

    @Test
    @Ignore
    internal fun cfd() {
        val dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

        fun parse(line: List<String>, asset: Asset): Instant {
            val text = line[0] + " " + line[1]
            val dt = LocalDateTime.parse(text, dtf)
            return asset.exchange.getInstant(dt)
        }

        val feed = CSVFeed("/tmp/DE40CASH.csv") {
            template = Asset("TEMPLATE", AssetType.CFD, Currency.EUR, Exchange.DEX)
            separator = '\t'
            timeParser = TimeParser { a, b -> parse(a, b) }
        }

        require(feed.assets.size == 1)

        val strategy = EMACrossover()

        // We don't want the default initial deposit in USD, since then we need a currency convertor
        val initialDeposit = 10_000.EUR.toWallet()

        // We configure a large leverage of 50x, so we 10_000 x 50 = 500_000K buyingpower to start with
        val leverage = 50.0
        val broker = SimBroker(initialDeposit, accountModel = MarginAccount(leverage = leverage))

        // Since we only trade in one asset and have leverage, we allow up to 1000% of our equity (10k) allocated to
        // one order
        val converter = FlexConverter.capitalBased {
            shorting = true
            orderPercentage = 90.percent
            safetyMargin = 10.percent
        }
        strategy.signalConverter = converter

        val account = run(feed, strategy, broker = broker)

        println(account)
    }




}

