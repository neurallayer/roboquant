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
import org.roboquant.common.Timeframe
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
class Roboquant(
    val strategy: Strategy,
    vararg val metrics: Metric,
    val policy: Policy = FlexPolicy(),
    val broker: Broker = SimBroker(),
    val logger: MetricsLogger = MemoryLogger(),
    private val channelCapacity: Int = 10,
) {

    private var runCounter = -1
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
    private suspend fun runPhase(feed: Feed, run: String, timeframe: Timeframe) {
        val channel = EventChannel(channelCapacity, timeframe)
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val job = scope.launch {
            channel.use { feed.play(it) }
        }

        start(run, timeframe)
        try {
            while (true) {
                val event = channel.receive()
                val time = event.time

                // Sync with broker and run metrics
                broker.sync(event)
                val account = broker.account
                val metricResult = getMetrics(account, event)
                logger.log(metricResult, time, run)

                // Generate signals and place orders
                val signals = strategy.generate(event)
                val orders = policy.act(signals, account, event)
                broker.place(orders, time)

                kotlinLogger.trace {
                    "time=$${event.time} actions=${event.actions.size} signals=${signals.size} orders=${orders.size}"
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // intentionally empty
        } finally {
            end(run)
            if (job.isActive) job.cancel()
            scope.cancel()
            channel.close()
        }
    }

    /**
     * Run a warm-up. It is similar to a normal [run] with the following two exceptions:
     * - metrics are not logged
     * - orders are not placed at the broker
     *
     * Typically used to allow strategies to capture enough historic data required to execute its logic.
     */
    fun warmup(feed: Feed, timeframe: Timeframe, run: String = "warmup") = runBlocking {
        warmupAsync(feed, timeframe, run)
    }

    /**
     * Identical to [warmup], but now running asynchronously.
     */
    suspend fun warmupAsync(feed: Feed, timeframe: Timeframe, run: String = "warmup") {
        val channel = EventChannel(channelCapacity, timeframe)
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val job = scope.launch {
            channel.use { feed.play(it) }
        }

        start(run, timeframe)
        try {
            while (true) {
                val event = channel.receive()

                // Sync with broker and run metrics
                broker.sync(event)
                val account = broker.account
                getMetrics(account, event)

                // Generate signals and place orders
                val signals = strategy.generate(event)
                val orders = policy.act(signals, account, event)

                kotlinLogger.trace {
                    "time=$${event.time} actions=${event.actions.size} signals=${signals.size} orders=${orders.size}"
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // intentionally empty
        } finally {
            end(run)
            if (job.isActive) job.cancel()
            scope.cancel()
            channel.close()
        }
    }

    /**
     * Inform components of the start of a [runPhase], this provides them with the opportunity to clear state and
     * re-initialize values if required.
     */
    private fun start(run: String, timeframe: Timeframe) {
        for (component in components) component.start(run, timeframe)
    }

    /**
     * Inform components of the end of a [run], this provides them with the opportunity to release resources
     * if required or process aggregated results.
     */
    private fun end(run: String) {
        for (component in components) component.end(run)
    }

    /**
     * Reset the state including that of the used underlying components. This allows starting a fresh run with the same
     * configuration as the original instance. This will also reset the run counter in this roboquant instance.
     */
    fun reset(includeLogger: Boolean = true) {
        for (component in components) {
            if (! includeLogger && component is MetricsLogger) continue
            component.reset()
        }
        runCounter = -1
    }

    /**
     * Start a new run using the provided [feed] as data. If no [timeframe] is provided all the events in the feed
     * will be processed. You can provide a custom [name] that will help to later identify this run. If none is
     * provided, a name will be generated with the format "run-<counter>"
     *
     * Optionally you can provide:
     * 1. a [timeframe] timeframe that will limit duration of the main phase.
     * 2. a [validation] timeframe that will trigger a separate validation phase.
     *
     * These last two options come into play when you want to run machine-learning based strategies. This is the
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
     * This is the same method as the [run] method but as the name already suggests, asynchronously. This makes it
     * better suited for running back-test in parallel. Other than that, it behaves exactly the same as the regular
     * run method.
     *
     * @see [run]
     */
    suspend fun runAsync(
        feed: Feed,
        timeframe: Timeframe = feed.timeframe,
        validation: Timeframe? = null,
        name: String? = null,
    ) {
        require(validation == null || validation.start >= timeframe.end) {
            "validation should start after main timeframe"
        }
        val run = name ?: "run-${++runCounter}"
        kotlinLogger.debug { "starting run=$run timeframe=$timeframe" }
        runPhase(feed, run, timeframe)

        if (validation !== null) runPhase(feed, run, validation)
        kotlinLogger.debug { "Finished run=$run" }
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
    fun closePositions(time: Instant? = null, runName: String? = null) {
        val account = broker.account
        val eventTime = time ?: account.lastUpdate
        val cancelOrders = account.openOrders.createCancelOrders()
        val change = account.positions.closeSizes
        val changeOrders = change.map { MarketOrder(it.key, it.value) }
        val orders = cancelOrders + changeOrders
        val actions = account.positions.map { TradePrice(it.asset, it.mktPrice) }
        val event = Event(actions, eventTime)
        val run = runName ?: "run-${runCounter}"
        broker.place(orders)
        broker.sync(event)
        val newAccount = broker.account
        val metricResult = getMetrics(newAccount, event)
        logger.log(metricResult, event.time, run)
    }

}

/**
 * Enumeration of the two different phases that a run can be in, [MAIN] and [VALIDATE]. Especially with self-learning
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
