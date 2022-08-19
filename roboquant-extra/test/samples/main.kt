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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.brokers.summary
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.common.minutes
import org.roboquant.iex.Range
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.metrics.ProgressMetric
import org.roboquant.polygon.PolygonHistoricFeed
import org.roboquant.strategies.EMACrossover
import org.roboquant.yahoo.YahooHistoricFeed

fun feedIEX() {
    val feed = org.roboquant.iex.IEXHistoricFeed()
    feed.retrieve("AAPL", "GOOGL", "FB", range = Range.TWO_YEARS)

    val strategy = EMACrossover(10, 30)
    val roboquant = Roboquant(strategy, AccountSummary())
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
}

fun feedIEXLive() {
    val feed = org.roboquant.iex.IEXLiveFeed()
    val apple = Asset("AAPL")
    val google = Asset("GOOG")
    feed.subscribeTrades(apple, google)
    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    roboquant.run(feed, Timeframe.next(5.minutes))
    roboquant.broker.account.summary().log()
}

fun feedYahoo() {
    val feed = YahooHistoricFeed()
    val apple = Asset("AAPL")
    val google = Asset("GOOG")
    val last300Days = Timeframe.past(300.days)
    feed.retrieve(listOf(apple, google), timeframe = last300Days)
    val strategy = EMACrossover()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = logger)
    roboquant.run(feed)
    logger.summary(10)
}


fun feedPolygon() {
    val feed = PolygonHistoricFeed()
    val tf = Timeframe.fromYears(2021, 2022)
    feed.retrieve("IBM", tf)
    feed.retrieve("AAPL", tf)
    println(feed.assets)
    println(feed.timeline.size)

    // Lets us the feed
    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy)
    roboquant.run(feed)
    val account = roboquant.broker.account
    account.summary().log()
    account.portfolio.summary().log()
}


fun main() {
    when ("POLYGON") {
        "IEX" -> feedIEX()
        "IEX_LIVE" -> feedIEXLive()
        "YAHOO" -> feedYahoo()
        "POLYGON" -> feedPolygon()
    }
}