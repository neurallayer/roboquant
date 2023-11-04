package org.roboquant.backtest

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.ParallelJobs
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.loggers.MemoryLogger
import java.util.*

/**
 * Contains the result of a run and its score
 * @param params
 * @param score
 * @param timeframe
 * @param name
 */
data class RunResult(val params: Params, val score: Double, val timeframe: Timeframe, val name: String)

/**
 * Create a mutable synchronized list
 */
fun <T> mutableSynchronisedListOf(): MutableList<T> = Collections.synchronizedList(mutableListOf<T>())

/**
 * Optimizer implements different back-test optimization strategies to find a set of optimal parameter
 * values.
 *
 * An optimizing back test has two runs, and each run has up to two periods.
 * The warmup periods are optional and by default not used ([TimeSpan.ZERO]).
 *
 * Training run:
 * - warmup period; get required data for strategies, policies and metrics loaded
 * - training period; optimize the hyperparameters
 *
 * Validation run
 * - warmup period; get required data for strategies, policies and metrics loaded
 * - validation period; see how a run is performing, based on unseen data
 *
 * @property space the search space to sue for determining valid combinaiton of parameters
 * @property score scoring function to use while determining the optimal parameters
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


    private fun getSafeRoboquant(params: Params) : Roboquant {
        val roboquant = getRoboquant(params)
        require(roboquant.broker is SimBroker)
        return roboquant
    }


    /**
     * Run a walk forward
     */
    fun walkForward(
        feed: Feed,
        period: TimeSpan,
        validation: TimeSpan,
        overlap: TimeSpan = TimeSpan.ZERO,
        anchored: Boolean = false,
        timeframe: Timeframe = feed.timeframe
    ): List<RunResult> {
        require(timeframe.isFinite()) { "feed needs known timeframe" }
        if (anchored) require(overlap.isZero) { "Cannot have overlap if anchored"}
        val feedStart = timeframe.start
        val results = mutableListOf<RunResult>()

        val trueValidation = validation + overlap
        timeframe.split(period + validation, period, false).forEach {
            val tf = if (anchored) it.copy(start = feedStart) else it
            val (train, valid) = tf.splitTwoWay(trueValidation, overlap)
            val r = trainAndValidate(feed, train, valid)
            results.addAll(r)
        }
        return results
    }


    private fun trainAndValidate(
        feed: Feed,
        trainTimeframe: Timeframe,
        validationTimeframe: Timeframe,
    ): List<RunResult> {
        val results = mutableListOf<RunResult>()
        val result = train(feed, trainTimeframe)
        results.addAll(result)
        val best = result.maxBy { entry -> entry.score }
        val score = validate(feed, validationTimeframe, best.params)
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
        timeframe: Timeframe = feed.timeframe
    ): List<RunResult> {
        val results = mutableSynchronisedListOf<RunResult>()
        require(timeframe.isFinite()) { "feed needs known timeframe" }
        timeframe.sample(period + validation, samples).forEach {
            val (train, valid) = it.splitTwoWay(validation)
            val r = trainAndValidate(feed, train, valid)
            results.addAll(r)
        }
        return results
    }

    /**
     * The logger to use for training run. By default, this logger is discarded after the run and score is
     * calculated
     */
    open fun getTrainLogger() = MemoryLogger(false)

    /**
     * Train the solution in parallel
     */
    fun train(feed: Feed, tf: Timeframe = Timeframe.INFINITE): List<RunResult> {
        val jobs = ParallelJobs()
        val results = mutableSynchronisedListOf<RunResult>()
        for (params in space) {
            jobs.add {
                val rq = getSafeRoboquant(params).copy(logger = getTrainLogger())
                val name = "train-${run++}"
                rq.runAsync(feed, tf, name)
                val s = score.calculate(rq, name, tf)
                val result = RunResult(params, s, tf, name)
                results.add(result)
            }

        }
        jobs.joinAllBlocking()
        return results
    }


    private fun validate(feed: Feed, timeframe: Timeframe, params: Params): RunResult {
        val rq = getSafeRoboquant(params)
        val name = "validate-${run++}"
        rq.run(feed, timeframe, name)
        val s = score.calculate(rq, name, timeframe)
        return RunResult(params, s, timeframe, name)
    }


}
