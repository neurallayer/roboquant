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

package org.roboquant

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Background
import org.roboquant.common.Logging
import org.roboquant.common.Summary
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.MetricsLogger
import org.roboquant.metrics.Metric
import org.roboquant.orders.Order
import org.roboquant.policies.NeverShortPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.Strategy
import java.time.Duration
import java.time.Instant

/**
 * Roboquant is the engine of the framework that ties [strategy], [policy] and [broker] together and caters to a wide
 * variety of testing and live trading scenarios. Through [metrics] and a [logger] it provides insights into the
 * performance of a [run].
 *
 * Every instance has it own [name] that is also used when logging details.
 *
 */
class Roboquant<L : MetricsLogger>(
    val strategy: Strategy,
    vararg val metrics: Metric,
    val policy: Policy = NeverShortPolicy(),
    val broker: Broker = SimBroker(),
    val logger: L,
    val name: String = "Roboquant-${instanceCounter++}",
    private val channelCapacity: Int = 100,
) {

    private val kotlinLogger = Logging.getLogger(name)
    private var run = 0
    private var episode = 0
    private var step = 0
    private lateinit var _timeFrame: TimeFrame
    private lateinit var phase: Phase
    private val components = listOf(strategy, policy, broker, *metrics, logger)

    companion object {

        private var instanceCounter = 0

        /**
         * Shortcut to create a new roboquant with a default logger.
         *
         * @param strategy
         * @param metrics
         */
        operator fun invoke(
            strategy: Strategy,
            vararg metrics: Metric,
            policy: Policy = NeverShortPolicy(),
            broker: Broker = SimBroker()
        ) = Roboquant(strategy, *metrics, policy = policy, broker = broker, logger = MemoryLogger())

    }

    init {
        kotlinLogger.fine { "Created new roboquant instance" }
    }

    /**
     * Run and evaluate the underlying performance of the strategy and policy. You don't
     * invoke this method directly but rather use the [run] method instead.
     *
     * Under the hood this method replies on the [step] method to take a single step.
     */
    private suspend fun runPhase(feed: Feed, runTime: TimeFrame = TimeFrame.FULL, runPhase: Phase) {

        if (!feed.timeFrame.overlap(runTime)) return
        _timeFrame = runTime.intersect(feed.timeFrame)

        val channel = EventChannel(channelCapacity, runTime)
        val job = Background.ioJob {
            try {
                feed.play(channel)
            } finally {
                channel.close()
            }
        }

        start(runPhase)
        try {
            var orders = listOf<Order>()
            while (true) {
                step++
                val event = channel.receive()
                orders = step(orders, event)
            }
        } catch (exception: ClosedReceiveChannelException) {
            return
        } finally {
            end(runPhase)
            if (job.isActive) job.cancel()
            channel.close()
        }
    }

    /**
     * Inform components of the start of a phase, this provides them with the opportunity to reset state and
     * re-initialize values if required.
     *
     * @param phase
     */
    private fun start(phase: Phase) {
        this.phase = phase
        for (component in components) component.start(this.phase)
    }

    /**
     * Inform components of the end of a phase, this provides them with the opportunity to release resources
     * if required or process aggregated results.
     *
     * @param phase
     */
    private fun end(phase: Phase) {
        for (component in components) component.end(phase)
    }

    /**
     * Reset all state including that of the used components. This allows to start with a fresh run with the same
     * configuration as the original instance.
     */
    fun reset() {
        for (component in components) component.reset()
        run = 0
        episode = 0
        step = 0
    }

    /**
     * Start a new run using the provided [feed] as data. If no [timeFrame] is provided all the events in the feed
     * will be used. Optionally you can provide a [validation] timeframe that will trigger a separate validation phase. You
     * can also repeat the run for a number of [episodes].
     *
     *  The following provides a schematic overview of the flow of a run:
     *
     * [Feed] -> [Strategy] -> [Policy] -> [Broker] -> [Metric] -> [MetricsLogger]
     *
     * This is the synchronous (blocking) method of run that is convenient to use. However, if you want to execute runs
     * in parallel have also a look at [runAsync]
     */
    fun run(feed: Feed, timeFrame: TimeFrame = TimeFrame.FULL, validation: TimeFrame? = null, episodes: Int = 1) =
        runBlocking {
            runAsync(feed, timeFrame, validation, episodes)
        }

    /**
     * This is exactly the same method as the [run] method but as the name already suggest, asynchronously.
     *
     * @see [run]
     */
    suspend fun runAsync(
        feed: Feed,
        timeFrame: TimeFrame = TimeFrame.FULL,
        validation: TimeFrame? = null,
        episodes: Int = 1
    ) {
        require(episodes > 0) { "episodes need to be greater than zero"}

        run++
        episode = 0
        step = 0
        kotlinLogger.fine { "Starting run $run for $episodes episodes" }

        repeat(episodes) {
            episode++
            runPhase(feed, timeFrame, Phase.MAIN)
            if (validation !== null) runPhase(feed, validation, Phase.VALIDATE)
        }
        kotlinLogger.fine { "Finished run $run" }
    }


    /**
     * Take a single step in the timeline
     */
    private fun step(orders: List<Order>, event: Event): List<Order> {
        val account = broker.place(orders, event)
        runMetrics(account, event)
        val signals = strategy.generate(event)
        return policy.act(signals, account, event)
    }

    /**
     * Run the configured metrics and log the results. This includes any metrics that are recorded by the strategy,
     * policy and broker.
     */
    private fun runMetrics(account: Account, event: Event) {
        val info = RunInfo(name, run, episode, this.step, event.now, _timeFrame, phase)
        for (metric in metrics) metric.calculate(account, event)

        for (component in components) {
            val metrics = component.getMetrics()
            logger.log(metrics, info)
        }

    }

    /**
     * Provide a short summary of the state of this roboquant.
     */
    fun summary(): Summary {
        val s = Summary("Roboquant")
        s.add("name", name)
        s.add("run", run)
        s.add("strategy", strategy::class.simpleName)
        s.add("policy", policy::class.simpleName)
        s.add("logger", logger::class.simpleName)
        s.add("metrics", metrics.size)
        return s
    }

}


/**
 * Run related info provided to loggers together with the metric results.
 *
 * @property name of the roboquant that created this object
 * @property run the run
 * @property episode
 * @property step
 * @property time
 * @constructor Create new RunInfo object
 */
data class RunInfo internal constructor(
    val name: String,
    val run: Int,
    val episode: Int,
    val step: Int,
    val time: Instant,
    val timeFrame: TimeFrame,
    val phase: Phase
) {

    /**
     * What is the duration of the run so far
     */
    val duration: Duration
        get() = Duration.between(timeFrame.start, time)

}

/**
 * The different phases that a run can be in, MAIN and VALIDATE. Especially with self learning
 * strategies, it is important that you evaluate your strategy on yet unseen data, so you don't over-fit.
 *
 * See also [Roboquant.run] how to run your strategy with different phases enabled.
 *
 * @property value
 */
enum class Phase(val value: String) {
    MAIN("MAIN"),
    VALIDATE("VALIDATE"),
}
