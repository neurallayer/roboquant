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

package org.roboquant.samples


import org.roboquant.Roboquant
import org.roboquant.binance.BinanceHistoricFeed
import org.roboquant.binance.BinanceLiveFeed
import org.roboquant.binance.Interval
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.toList
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.loggers.InfoLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Ignore
import kotlin.test.Test


internal class BinanceSamples {

    @Test
    @Ignore
    fun binanceLiveFeed() {
        val feed = BinanceLiveFeed()
        feed.subscribePriceBar("BTCBUSD", "ETHBUSD", interval = Interval.ONE_MINUTE)
        val events = feed.toList(Timeframe.next(10.minutes)).filter {
            println(it.actions)
            it.actions.isNotEmpty()
        }
        println(events.size)
    }

    @Test
    @Ignore
    fun binanceForwardTest() {
        val feed = BinanceLiveFeed()

        // We ony trade Bitcoin
        feed.subscribePriceBar("BTCBUSD")
        val strategy = EMAStrategy.PERIODS_5_15
        val initialDeposit = Amount("BUSD", 10_000).toWallet()
        val broker = SimBroker(initialDeposit)
        val policy = FlexPolicy.singleAsset()
        val rq = Roboquant(strategy, broker = broker, policy = policy, logger = ConsoleLogger())

        // We'll run the forward test for thirty minutes
        val tf = Timeframe.next(30.minutes)
        rq.run(feed, tf)
    }

    @Test
    @Ignore
    fun binanceTestBack() {
        val strategy = EMAStrategy()
        val initialDeposit = Amount("BUSD", 100_000).toWallet()
        val roboquant = Roboquant(strategy, ScorecardMetric(), broker = SimBroker(initialDeposit, retention = 10.years))

        val feed = BinanceHistoricFeed()
        val threeYears = Timeframe.parse("2020-01-01", "2023-01-01")
        feed.retrieve("BTCBUSD", "ETHBUSD", timeframe = threeYears, interval = Interval.DAILY)

        roboquant.run(feed)
        println(roboquant.broker.account.summary())
    }

    @Test
    @Ignore
    fun multiplier() {
        val feed = BinanceLiveFeed()

        // We ony trade Bitcoin
        feed.subscribePriceBar("BTCBUSD")
        val tf = Timeframe.next(5.minutes)

        val jobs = ParallelJobs()

        repeat(3) {
            jobs.add {
                val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = InfoLogger())
                rq.run(feed, tf, name = "run-$it")
            }
        }

        jobs.joinAllBlocking()
        println("runs are done")
        feed.close()
    }

}
