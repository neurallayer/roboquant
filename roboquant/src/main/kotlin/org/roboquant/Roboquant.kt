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

package org.roboquant

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.RunPhase.MAIN
import org.roboquant.RunPhase.VALIDATE
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.closeSizes
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Logging
import org.roboquant.common.Summary
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.TradePrice
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.MetricsLogger
import org.roboquant.metrics.Metric
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.createCancelOrders
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.Strategy
import java.time.Duration
import java.time.Instant

/**
 * Roboquant is the engine of the platform that ties [strategy], [policy] and [broker] together and caters to a wide
 * variety of testing and live trading scenarios. Through [metrics] and a [logger] it provides insights into the
 * performance of a [run]. Only a strategy is required when instantiating a Roboquant, the other parameters are
 * optional.
 *
 * @property strategy The strategy to use, there is no default
 * @property metrics the various metrics to calculate during the runs, default is none
 * @property policy The policy to use, default is [FlexPolicy]
 * @property broker the broker to use, default is [SimBroker]
 * @property logger the metrics logger to use, default is [MemoryLogger]
 * @param channelCapacity the max capacity of the event channel, more capacity means more buffering. Default is 100.
 */
class Roboquant(
    val strategy: Strategy,
    vararg val metrics: Metric,
    val policy: Policy = FlexPolicy(),
    val broker: Broker = SimBroker(),
    val logger: MetricsLogger = MemoryLogger(),
    private val channelCapacity: Int = 100,
) {

    private var runCounter = 0
    private val kotlinLogger = Logging.getLogger(Roboquant::class)
    private val components = listOf(strategy, policy, broker, logger) + metrics

    init {
        kotlinLogger.debug { "Created new roboquant instance" }
    }

    /**
     * Run and evaluate the underlying performance of the strategy and policy. You don't invoke this method directly
     * but rather use the [run] method instead. Under the hood this method replies on the [step] method to take a
     * single step.
     */
    private suspend fun runPhase(feed: Feed, runInfo: RunInfo) {
        val channel = EventChannel(channelCapacity, runInfo.timeframe)
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val job = scope.launch {
            channel.use { feed.play(it) }
        }

        start(runInfo.phase)
        try {
            var orders = listOf<Order>()
            while (true) {
                val event = channel.receive()
                orders = step(orders, event, runInfo)
            }
        } catch (_: ClosedReceiveChannelException) {
            // intentionally empty
        } finally {
            end(runInfo.phase)
            if (job.isActive) job.cancel()
            scope.cancel()
            channel.close()
        }
    }

    /**
     * Inform components of the start of a [runPhase], this provides them with the opportunity to clear state and
     * re-initialize values if required.
     */
    private fun start(runPhase: RunPhase) {
        for (component in components) component.start(runPhase)
    }

    /**
     * Inform components of the end of a [runPhase], this provides them with the opportunity to release resources
     * if required or process aggregated results.
     */
    private fun end(runPhase: RunPhase) {
        for (component in components) component.end(runPhase)
    }

    /**
     * Reset all state including that of the used underlying components. This allows to start a fresh run with the same
     * configuration as the original instance. This will also reset the run counter in this roboquant instance.
     */
    fun reset() {
        for (component in components) component.reset()
        runCounter = 0
    }

    /**
     * Start a new run using the provided [feed] as data. If no [timeframe] is provided all the events in the feed
     * will be processed. You can provide a custom [name] that will help to later identify this run. If none is
     * provided, a name will be generated with the format "run-<counter>"
     *
     * Optionally you can provide:
     * 1. a [timeframe] timeframe that will limit duration of main phase.
     * 2. a [validation] timeframe that will trigger a separate validation phase.
     *
     * These last two options come into play when you want to run machine learning based strategies. This is the
     * synchronous (blocking) method of run that is convenient to use. However, if you want to execute runs
     * in parallel have a look at [runAsync]
     */
    fun run(
        feed: Feed,
        timeframe: Timeframe = feed.timeframe,
        validation: Timeframe? = null,
        name: String? = null,
    ) =
        runBlocking {
            runAsync(feed, timeframe, validation, name)
        }

    /**
     * This is the same method as the [run] method but as the name already suggest, asynchronously. This makes it better
     * suited for running back-test in parallel. Other than that, it behaves exactly the same as the regular run method.
     *
     * @see [run]
     */
    suspend fun runAsync(
        feed: Feed,
        timeframe: Timeframe = feed.timeframe,
        validation: Timeframe? = null,
        runName: String? = null,
    ) {
        require(validation == null || validation.start >= timeframe.end) {
            "validation should start after main timeframe"
        }
        val run = runName ?: "run-${runCounter++}"
        val runInfo = RunInfo(run)
        kotlinLogger.debug { "starting run=$runInfo" }

        runInfo.phase = MAIN
        runInfo.timeframe = timeframe
        runPhase(feed, runInfo)
        if (validation !== null) {
            runInfo.timeframe = validation
            runInfo.phase = VALIDATE
            runPhase(feed, runInfo)
        }

        kotlinLogger.debug { "Finished run $runInfo" }
    }

    /**
     * Take a single step in the timeline. The broker is always invoked before the strategy and policy to ensure it is
     * impossible to look ahead in the future. So the loop really is:
     *
     *  feed --|event|--> broker --|account|--> metrics -> strategy --|signals|--> policy --|orders|-->
     *
     */
    private fun step(orders: List<Order>, event: Event, runInfo: RunInfo): List<Order> {
        runInfo.time = event.time
        kotlinLogger.trace { "starting step info=$runInfo orders=${orders.size} actions=${event.actions.size}" }

        val account = broker.place(orders, event)
        runMetrics(account, event, runInfo)
        val signals = strategy.generate(event)
        return policy.act(signals, account, event)
    }

    /**
     * Calculate the configured [metrics] and log the results. This includes also metrics that are recorded
     * by the [strategy], [policy] and [broker].
     */
    private fun runMetrics(account: Account, event: Event, runInfo: RunInfo) {
        val metricResult = mutableMapOf<String, Double>()
        for (metric in metrics) metricResult.putAll(metric.calculate(account, event))
        metricResult.putAll(strategy.getMetrics())
        metricResult.putAll(policy.getMetrics())
        metricResult.putAll(broker.getMetrics())
        kotlinLogger.trace { "captured metrics=${metricResult.size}" }

        // Always call the logger, so things like progress bar can be updated
        logger.log(metricResult, runInfo.copy())
    }

    /**
     * Provide a summary of this roboquant.
     */
    fun summary(): Summary {
        val s = Summary("roboquant")
        s.add("strategy", strategy::class.simpleName)
        s.add("policy", policy::class.simpleName)
        s.add("logger", logger::class.simpleName)
        val metricNames = metrics.map { it::class.simpleName }.joinToString()
        s.add("metrics", metricNames)
        return s
    }

    /**
     * Close the open positions of the underlying broker account. This comes in handy at the end of a run if you
     * don't want to have open positions left in the portfolio. You can optionally provide the [time], [phase] and
     * [runName] used to close the positions and log the metrics.
     *
     * This method performs the following two steps:
     * 1. cancel open orders
     * 2. close open positions by creating and processing [MarketOrder] for the required quantities, using the
     * last known market price for an asset as the price action
     * 3. run all metrics
     */
    fun closePositions(time: Instant? = null, phase: RunPhase = MAIN, runName: String? = null) {
        val account = broker.account
        val orderTime = time ?: account.lastUpdate
        val cancelOrders = account.openOrders.createCancelOrders()
        val change = account.positions.closeSizes
        val changeOrders = change.map { MarketOrder(it.key, it.value) }
        val orders = cancelOrders + changeOrders
        val actions = account.positions.map { TradePrice(it.asset, it.mktPrice) }
        val event = Event(actions, orderTime)
        val run = runName ?: "run-${runCounter}"
        val runInfo = RunInfo(run, orderTime, phase = phase)
        val newAccount = broker.place(orders, event)
        runMetrics(newAccount, event, runInfo)
    }

}

/**
 * Run related info provided to metrics loggers together with the metric results.
 *
 * @property run the name of the run
 * @property time the time
 * @property timeframe the total timeframe of the run, if not known it will be [Timeframe.INFINITE]
 * @property phase the phase of the run
 * @constructor Create a new RunInfo object
 */
data class RunInfo internal constructor(
    val run: String,
    var time: Instant = Instant.MIN,
    var timeframe: Timeframe = Timeframe.INFINITE,
    var phase: RunPhase = MAIN
) {

    /**
     * Return the duration of the run so far
     */
    val duration: Duration
        get() = Duration.between(timeframe.start, time)

}

/**
 * Enumeration of the two different phases that a run can be in, [MAIN] and [VALIDATE]. Especially with self learning
 * strategies, it is important that you evaluate your strategy on yet unseen data, so you don't over-fit.
 *
 * See also [Roboquant.run] how to run your strategy with different phases enabled.
 *
 * @property value String value of the run phase
 */
enum class RunPhase(val value: String) {

    /**
     * Main run phase
     */
    MAIN("MAIN"),

    /**
     * Validation run phase
     */
    VALIDATE("VALIDATE"),
}
