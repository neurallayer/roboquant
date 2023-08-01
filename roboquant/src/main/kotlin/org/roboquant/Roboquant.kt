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
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.closeSizes
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Logging
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.common.plus
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.TradePrice
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.MetricsLogger
import org.roboquant.metrics.Metric
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.createCancelOrders
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.Strategy
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
 * @param channelCapacity the max capacity of the event channel, more capacity means more buffering. Default is 10.
 */
data class Roboquant(
    val strategy: Strategy,
    val metrics: List<Metric>,
    val policy: Policy = FlexPolicy(),
    val broker: Broker = SimBroker(),
    val logger: MetricsLogger = MemoryLogger(),
    private val channelCapacity: Int = 10,
) {

    /**
     * Convenience constructor that instead on a list of Metrics, accept a vararg of metrics.
     *
     * @see Roboquant
     */
    constructor(
        strategy: Strategy,
        vararg metrics: Metric,
        policy: Policy = FlexPolicy(),
        broker: Broker = SimBroker(),
        logger: MetricsLogger = MemoryLogger(),
        channelCapacity: Int = 10
    ) : this(strategy, metrics.toList(), policy, broker, logger, channelCapacity)

    private val kotlinLogger = Logging.getLogger(Roboquant::class)
    private val components = listOf(strategy, policy, broker, logger) + metrics

    init {
        kotlinLogger.debug { "Created new roboquant instance" }
    }

    /**
     * Inform components of the start of a new [run] with the provided [timeframe].
     */
    private fun start(run: String, timeframe: Timeframe) {
        kotlinLogger.debug { "starting run=$run timeframe=$timeframe" }
        for (component in components) component.start(run, timeframe)
    }

    /**
     * Inform components of the end of a [run].
     */
    private fun end(run: String) {
        kotlinLogger.debug { "Finished run=$run" }
        for (component in components) component.end(run)
    }

    /**
     * Reset the state including that of the used underlying components. This allows starting a fresh run with the same
     * configuration as the original instance.
     *
     * By default, also the [logger] will be reset. If you don't want this, set [includeLogger] to false.
     */
    fun reset(includeLogger: Boolean = true) {
        for (component in components) {
            if (!includeLogger && component is MetricsLogger) continue
            component.reset()
        }
    }

    /**
     * Start a new run using the provided [feed] as data. If no [timeframe] is provided all the events in the feed
     * will be processed. You can provide a custom [name] that will help to later identify this run. If none is
     * provided, the default [name] "run" will be used.
     * Additionally, you can provide a [warmup] period in which no metrics will be logged or orders placed.
     *
     * By default, at the beginning of a run, all components (besides the logger) will be [reset] and typically discard
     * their state. If you don't want this behavior set [reset] to false.
     *
     * This is the synchronous (blocking) method of run that is convenient to use. However, if you want to execute runs
     * in parallel, use the [runAsync] method.
     */
    fun run(
        feed: Feed,
        timeframe: Timeframe = feed.timeframe,
        name: String = "run",
        warmup: TimeSpan = TimeSpan.ZERO,
        reset: Boolean = true
    ) = runBlocking {
        runAsync(feed, timeframe, name, warmup, reset)
    }

    /**
     * This is the same method as the [run] method but as the name already suggests, asynchronously. This makes it
     * better suited for running back-test in parallel. Other than that, it behaves exactly the same as the regular
     * run method.
     *
     * @see [run]
     */
    suspend fun runAsync(
        feed: Feed,
        timeframe: Timeframe = feed.timeframe,
        name: String = "run",
        warmup: TimeSpan = TimeSpan.ZERO,
        reset: Boolean = true
    ) {
        val channel = EventChannel(channelCapacity, timeframe)
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val job = scope.launch {
            channel.use { feed.play(it) }
        }

        start(name, timeframe)
        val warmupEnd = timeframe.start + warmup
        if (reset) reset(false)

        try {
            while (true) {
                val event = channel.receive()
                val time = event.time

                // Sync with broker and run metrics
                broker.sync(event)
                val account = broker.account
                val metricResult = getMetrics(account, event)
                if (time >= warmupEnd) logger.log(metricResult, time, name)

                // Generate signals and place orders
                val signals = strategy.generate(event)
                val orders = policy.act(signals, account, event)
                if (time >= warmupEnd) broker.place(orders, time)

                kotlinLogger.trace {
                    "time=$${event.time} actions=${event.actions.size} signals=${signals.size} orders=${orders.size}"
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // intentionally empty
        } finally {
            end(name)
            if (job.isActive) job.cancel()
            scope.cancel()
            channel.close()
        }

    }

    /**
     * Calculate the configured [metrics] and metrics generated by components and return them
     */
    private fun getMetrics(account: Account, event: Event) = buildMap {
        for (metric in metrics) putAll(metric.calculate(account, event))
        putAll(strategy.getMetrics())
        putAll(policy.getMetrics())
        putAll(broker.getMetrics())
        kotlinLogger.trace { "captured metrics=$size" }
    }

    /**
     * Provide a string representation of this roboquant.
     */
    override fun toString(): String {
        val s = strategy::class.simpleName
        val p = policy::class.simpleName
        val l = logger::class.simpleName
        val b = broker::class.simpleName
        val m = metrics.map { it::class.simpleName }.toString()
        return "strategy=$s policy=$p logger=$l metrics=$m broker=$b"
    }

    /**
     * Close the open positions of the underlying broker account. This comes in handy at the end of a run if you
     * don't want to have open positions left in the portfolio. You can optionally provide the [time] and
     * [runName] to use to close the positions and log the metrics.
     *
     * This method performs the following steps:
     * 1. Cancel existing open orders
     * 2. Close open positions by placing [MarketOrder] for the required opposite sizes
     * 3. Run and log the metrics
     */
    fun closePositions(time: Instant? = null, runName: String = "close") {
        val account = broker.account
        val eventTime = time ?: account.lastUpdate
        val cancelOrders = account.openOrders.createCancelOrders()
        val change = account.positions.closeSizes
        val changeOrders = change.map { MarketOrder(it.key, it.value) }
        val orders = cancelOrders + changeOrders
        val actions = account.positions.map { TradePrice(it.asset, it.mktPrice) }
        val event = Event(actions, eventTime)
        broker.place(orders)
        broker.sync(event)
        val newAccount = broker.account
        val metricResult = getMetrics(newAccount, event)
        logger.log(metricResult, event.time, runName)
    }

}
