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
 * Besides the name of the framework, Roboquant is also the engine that ties all components together.
 *
 * It gets the data from the feed, runs the [Strategy] and [Policy], evaluates the performance through metrics and
 * finally logs the results. The following provides a schematic overview of this flow:
 *
 * [Feed] -> [Strategy] -> [Policy] -> [Broker] -> [Metric] -> [MetricsLogger]
 *
 * The Roboquant engine is used for all the different stages of developing a new strategy, from back-testing to
 * live trading.
 *
 * @property strategy the strategy to use
 * @property metrics the metrics to calculate during the runs
 * @property policy the policy to use, default is [NeverShortPolicy]
 * @property broker the broker to use, default is [SimBroker]
 * @property logger the metrics logger to use, default is [MemoryLogger]
 * @property name the identifying name for this roboquant, available in logs
 * @constructor Create a new Roboquant
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
        kotlinLogger.info { "Created new roboquant instance" }
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
     * Reset all state including that of the used components. This allows to start with a fres run with the same
     * configuration as the original instance.
     */
    fun reset() {
        for (component in components) component.reset()
        run = 0
        episode = 0
        step = 0
    }

    /**
     * Start a new run using the provided feed as data.
     *
     * You can optionally specify timeframes for the main phase and a validation phase.
     * - If no timeframe is specified for the main phase, all the available data will be used.
     * - If no timeframe is specified for the validation period, this phase will be skipped all together.
     *
     * Algorithms that are none-learning, typically only require a single (first) phase. But mor advanced algorithms,
     * like machine learning ones, might require an out-of-sample validation period to avoid over-fitting.
     *
     * Finally, the episodes parameter indicates how often to repeat this sequence of main/validate.  Using a value of
     * higher than 1 is most common in machine learning based strategies.
     *
     * This is the synchronous (blocking) method of run that is convenient to use. However, if you want to execute runs
     * in parallel have also a look at [runAsync]
     *
     * @param feed Specify the feed to use
     * @param timeFrame Specify a timeframe for the main phase, if none is specified all available events will be used.
     * @param validation Optional specify a timeframe for the validation phase, if none is specified this will be skipped
     * @param episodes How many repeats of the run
     */
    fun run(feed: Feed, timeFrame: TimeFrame = TimeFrame.FULL, validation: TimeFrame? = null, episodes: Int = 1) =
        runBlocking {
            runAsync(feed, timeFrame, validation, episodes)
        }

    /**
     * This is exactly the same as the [run] method but as the name already suggest, this time asynchronously.
     *
     * @see [run]
     *
     * @param feed Specify the feed to use
     * @param timeFrame Specify a timeframe for the training, is none all available events will be used.
     * @param validation Optional specify a timeframe for the validation
     * @param episodes For how many episodes to run this training
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
     *
     * @param event the event to use for this step
     * @return new orders (created by the policy), to be used in the next step
     */
    private fun step(orders: List<Order>, event: Event): List<Order> {
        val account = broker.place(orders, event)
        runMetrics(account, event)
        val signals = strategy.generate(event)
        return policy.act(signals, account, event)
    }

    /**
     * Run the configured metrics and log the results.
     * This includes any metrics that are recorded by the strategy, policy and broker.
     *
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
     * Provide a short summary
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
data class RunInfo constructor(
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
 * See also [Roboquant.run] how to run your strategy with different phases turned on.
 *
 * @property value
 */
enum class Phase(val value: String) {
    MAIN("MAIN"),
    VALIDATE("VALIDATE"),
}
