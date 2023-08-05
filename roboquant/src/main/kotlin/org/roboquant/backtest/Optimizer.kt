package org.roboquant.backtest

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.ParallelJobs
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.loggers.MemoryLogger
import java.util.*

data class RunResult(val params: Params, val score: Double, val timeframe: Timeframe, val name: String)

/**
 * Create a mutable synchronized list
 */
fun <T> mutableSynchronisedListOf(): MutableList<T> = Collections.synchronizedList(mutableListOf<T>())

/**
 * Optimizer that implements different back-test optimization strategies to find a set of optimal parameter
 * values.
 *
 * An optimizing back test has two phases, and each phase has up to two periods.
 * The warmup periods are optional and by default [TimeSpan.ZERO].
 *
 * Training phase:
 * - warmup period; get required data for strategies, policies and metrics loaded
 * - training period; optimize the hyperparameters
 *
 * Validation phase
 * - warmup period; get required data for strategies, policies and metrics loaded
 * - validation period; see how a run is performing, based on unseen data
 *
 *
 * @property space search space
 * @property score scoring function
 * @property getRoboquant function that returns an instance of roboquant based on passed parameters
 *
 */
open class Optimizer(
    private val space: SearchSpace,
    private val score: Score,
    private val getRoboquant: (Params) -> Roboquant
) {

    private var run = 0

    /**
     * Using the default objective to maximize a metric. The default objective will use the last entry of the
     * provided [evalMetric] as the value to optimize.
     */
    constructor(space: SearchSpace, evalMetric: String, getRoboquant: (Params) -> Roboquant) : this(
        space, MetricScore(evalMetric), getRoboquant
    )


    /**
     * Run a walk forward
     */
    fun walkForward(
        feed: Feed,
        period: TimeSpan,
        validation: TimeSpan,
        warmup: TimeSpan = TimeSpan.ZERO,
        anchored: Boolean = false
    ): List<RunResult> {
        val timeframe = feed.timeframe
        require(timeframe.isFinite()) { "feed needs known timeframe" }
        val feedStart = feed.timeframe.start
        val results = mutableListOf<RunResult>()

        val trueValidation = validation + warmup
        timeframe.split(period + validation, period, false).forEach {
            val tf = if (anchored) it.copy(start = feedStart) else it
            val (train, valid) = tf.splitTrainTest(trueValidation, warmup)
            val r = trainAndValidate(feed, train, valid, warmup)
            results.addAll(r)
        }
        return results
    }


    private fun trainAndValidate(
        feed: Feed,
        trainTimeframe: Timeframe,
        validationTimeframe: Timeframe,
        warmup: TimeSpan
    ): List<RunResult> {
        val results = mutableListOf<RunResult>()
        val result = train(feed, trainTimeframe, warmup)
        results.addAll(result)
        val best = result.maxBy { entry -> entry.score }
        val score = validate(feed, validationTimeframe, best.params, warmup)
        results.add(score)
        return results
    }

    /**
     * Run a Monte Carlo simulation
     */
    fun monteCarlo(
        feed: Feed,
        period: TimeSpan,
        validation: TimeSpan,
        samples: Int,
        warmup: TimeSpan = TimeSpan.ZERO
    ): List<RunResult> {
        val results = mutableSynchronisedListOf<RunResult>()
        require(!feed.timeframe.isInfinite()) { "feed needs known timeframe" }
        feed.timeframe.sample(period + validation, samples).forEach {
            val (train, valid) = it.splitTrainTest(validation)
            val r = trainAndValidate(feed, train, valid, warmup)
            results.addAll(r)
        }
        return results
    }

    /**
     * The logger to use for training phase. By default, this logger is discarded after the run and score is
     * calculated
     */
    open fun getTrainLogger() = MemoryLogger(false)

    /**
     * Train the solution in parallel
     */
    fun train(feed: Feed, tf: Timeframe = Timeframe.INFINITE, warmup: TimeSpan = TimeSpan.ZERO): List<RunResult> {
        val jobs = ParallelJobs()
        val results = mutableSynchronisedListOf<RunResult>()
        for (params in space) {
            jobs.add {
                val rq = getRoboquant(params).copy(logger = getTrainLogger())
                require(rq.broker is SimBroker) { "Only a SimBroker can be used for back testing" }
                val name = "train-${run++}"
                rq.runAsync(feed, tf, name, warmup)
                val s = score.calculate(rq, name, tf)
                val result = RunResult(params, s, tf, name)
                results.add(result)
            }

        }
        jobs.joinAllBlocking()
        return results
    }


    private fun validate(feed: Feed, timeframe: Timeframe, params: Params, warmup: TimeSpan): RunResult {
        val rq = getRoboquant(params)
        require(rq.broker is SimBroker) { "Only a SimBroker can be used for back testing" }

        val name = "validate-${run++}"
        rq.run(feed, timeframe, name, warmup)
        val s = score.calculate(rq, name, timeframe)
        return RunResult(params, s, timeframe, name)
    }


}