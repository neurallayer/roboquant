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

package org.roboquant

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.RunPhase.MAIN
import org.roboquant.RunPhase.VALIDATE
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Background
import org.roboquant.common.Logging
import org.roboquant.common.Summary
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.MetricsLogger
import org.roboquant.metrics.Metric
import org.roboquant.orders.Order
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.Strategy
import java.time.Duration
import java.time.Instant

/**
 * Roboquant is the engine of the platform that ties [strategy], [policy] and [broker] together and caters to a wide
 * variety of testing and live trading scenarios. Through [metrics] and a [logger] it provides insights into the
 * performance of a [run]. Only a strategy is required, the other components are optional.
 *
 * @property strategy The strategy to use, there is no default
 * @property metrics the various metrics to calculate during the runs, default is none
 * @property policy The policy to use, default is [FlexPolicy]
 * @property broker the broker to use, default is [SimBroker]
 * @property logger the metrics logger to use, default is [MemoryLogger]
 * @param channelCapacity the max capacity of the event channel, more capacity means more buffering
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
        val job = Background.ioJob {
            try {
                feed.play(channel)
            } finally {
                channel.close()
            }
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
     * 1. a [validation] timeframe that will trigger a separate validation phase.
     * 2. the number of [episodes] the run should be repeated.
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
        episodes: Int = 1
    ) =
        runBlocking {
            runAsync(feed, timeframe, validation, name, episodes)
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
        episodes: Int = 1
    ) {
        require(episodes > 0) { "episodes need to be greater than zero" }
        val run = runName ?: "run-${runCounter++}"
        val runInfo = RunInfo(run)
        kotlinLogger.debug { "starting run $runInfo for $episodes episodes" }

        repeat(episodes) {
            runInfo.episode++
            runInfo.phase = MAIN
            runInfo.timeframe = timeframe
            runPhase(feed, runInfo)
            if (validation !== null) {
                runInfo.timeframe = validation
                runInfo.phase = VALIDATE
                runPhase(feed, runInfo)
            }
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
        runInfo.step++
        runInfo.time = event.time

        kotlinLogger.trace { "starting step $runInfo" }

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
        if (metricResult.isNotEmpty()) logger.log(metricResult, runInfo.copy())
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

}

/**
 * Run related info provided to metrics loggers together with the metric results.
 *
 * @property run the name of the run
 * @property episode the episode number
 * @property step the step
 * @property time the time
 * @property timeframe the total timeframe of the run, if not known it will be [Timeframe.INFINITE]
 * @property phase the phase of the run
 * @constructor Create new RunInfo object
 */
data class RunInfo internal constructor(
    val run: String,
    var episode: Int = 0,
    var step: Int = 0,
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
 * Enumeration of the different phases that a run can be in, [MAIN] and [VALIDATE]. Especially with self learning
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
