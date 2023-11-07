package org.roboquant.backtest

import org.hipparchus.stat.correlation.PearsonsCorrelation
import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.ParallelJobs
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.loggers.SilentLogger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Contains the result of a run and its score
 * @param params the parameters sued
 * @param score the score
 * @param timeframe teh timeframe of the run
 * @param run the id of the run
 * @param validation is this a validation run
 */
class RunResult internal constructor(
    val params: Params,
    val score: Double,
    val timeframe: Timeframe,
    val run: Int,
    val validation: Boolean
) {

    val training
        get() = ! validation


}


private fun mutableResults() = Collections.synchronizedList(mutableListOf<RunResult>())

/**
 * Optimizer implements different back-test optimization strategies to find a set of optimal parameter
 * values.
 *
 * An optimizing back test run has two steps:
 * Training run - optimize the hyperparameters
 * Validation run - see how a run is performing, based on unseen data and optimized set of parameters from the training
 * run
 *
 * @property space the search space to sue for determining valid combinaiton of parameters
 * @property score scoring function to use while determining the optimal parameters
 * @property getRoboquant function that returns an instance of roboquant based on passed parameters
 *
 */
open class Optimizer(
    private val space: SearchSpace,
    private val score: Score,
    private val warmup: Boolean = true,
    private val getRoboquant: (Params) -> Roboquant,
) {

    private var run = AtomicInteger()

    /**
     * Using the default objective to maximize a metric. The default objective will use the last entry of the
     * provided [evalMetric] as the value to optimize.
     */
    constructor(
        space: SearchSpace,
        evalMetric: String,
        warmup: Boolean = true,
        getRoboquant: (Params) -> Roboquant
    ) : this(
        space, MetricScore(evalMetric), warmup, getRoboquant
    )


    private fun getSafeRoboquant(params: Params): Roboquant {
        val roboquant = getRoboquant(params)
        require(roboquant.broker is SimBroker)
        return roboquant
    }

    /**
     * Run a walk forward
     */
    fun walkForward(
        feed: Feed,
        training: TimeSpan,
        validation: TimeSpan,
        overlap: TimeSpan = TimeSpan.ZERO,
        anchored: Boolean = false,
        timeframe: Timeframe = feed.timeframe
    ): List<RunResult> {
        require(timeframe.isFinite()) { "feed needs known timeframe" }
        if (anchored) require(overlap.isZero) { "Cannot have overlap if anchored" }
        val result = mutableResults()
        timeframe.split(training + validation, overlap, false).forEach {
            val tf = if (anchored) it.copy(start = timeframe.start) else it
            val (train, valid) = tf.splitTwoWay(training)
            val entry = trainAndValidate(feed, train, valid)
            result.addAll(entry)
        }
        return result
    }


    private fun trainAndValidate(
        feed: Feed,
        trainTimeframe: Timeframe,
        validationTimeframe: Timeframe,
    ): List<RunResult> {
        val results = mutableResults()
        val t = train(feed, trainTimeframe)
        results.addAll(t)
        val best = t.maxBy { it.score }
        val v = validate(feed, best, validationTimeframe)
        results.add(v)
        return results
    }

    /**
     * Run a Monte Carlo simulation
     */
    fun monteCarlo(
        feed: Feed,
        training: TimeSpan,
        validation: TimeSpan,
        samples: Int,
        timeframe: Timeframe = feed.timeframe
    ): List<RunResult> {
        require(timeframe.isFinite()) { "need a finite timeframe" }
        val result = mutableResults()
        val period = training + validation
        timeframe.sample(period, samples).forEach {
            val (train, valid) = it.splitTwoWay(training)
            val entry = trainAndValidate(feed, train, valid)
            result.addAll(entry)
        }
        return result
    }

    /**
     * Train the solution in parallel and return the results
     */
    fun train(feed: Feed, tf: Timeframe = Timeframe.INFINITE): List<RunResult> {
        val jobs = ParallelJobs()
        val results = mutableResults()
        for (params in space) {
            jobs.add {
                val rq = getSafeRoboquant(params)
                val run = run.incrementAndGet()
                val name = "train-${run}"
                rq.runAsync(feed, tf, name)
                val s = score.calculate(rq, name, tf)
                val result = RunResult(params, s, tf, run, false)
                results.add(result)
            }

        }
        jobs.joinAllBlocking()
        return results
    }

    /**
     * Warmup the strategy and policy, so they can build up any history required.
     */
    private fun warmup(feed: Feed, timeframe: Timeframe, params: Params): Roboquant {
        val warmup = getRoboquant(params).copy(logger = SilentLogger(), metrics = emptyList())
        warmup.run(feed, timeframe)
        return getRoboquant(params).copy(strategy = warmup.strategy, policy = warmup.policy)
    }

    /**
     * Validate the solution and return the result
     */
    private fun validate(feed: Feed, best: RunResult, validation: Timeframe): RunResult {
        val rq = if (warmup)
            warmup(feed, best.timeframe, best.params)
        else
            getRoboquant(best.params)

        val name = "valid-${best.run}"
        rq.run(feed, validation, name, reset = false)
        val s = score.calculate(rq, name, validation)
        return RunResult(best.params, s, validation, best.run, true)
    }


}

/**
 * Calculate the maximum of all training and validation scores.
 */
fun Collection<RunResult>.max(): Map<String, Double> {
    return mapOf(
        "training" to filter { it.training }.maxOf { it.score },
        "validation" to filter { it.validation }.maxOf { it.score },
    )
}

/**
 * Calculate the minimum of all training and validation scores.
 */
fun Collection<RunResult>.min(): Map<String, Double> {
    return mapOf(
        "training" to filter { it.training }.minOf { it.score },
        "validation" to filter { it.validation }.minOf { it.score },
    )
}

/**
 * Calculate the average of all training and validation scores.
 */
fun Collection<RunResult>.average(): Map<String, Double> {
    return mapOf(
        "training" to filter { it.training }.map { it.score }.average(),
        "validation" to filter { it.validation }.map { it.score }.average(),
    )
}


/**
 * Calculate the correlation between validation and training score for the various runs
 */
fun Collection<RunResult>.correlation(): Double {
    val training = mutableListOf<Double>()
    val validation = mutableListOf<Double>()
    for (v in groupBy { it.run }.values) {
        if (v.size == 2 && v[0].training && v[1].validation) {
            training.add(v[0].score)
            validation.add(v[1].score)
        }
    }

    val c = PearsonsCorrelation()
    return c.correlation(training.toDoubleArray(), validation.toDoubleArray())
}

