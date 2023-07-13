package org.roboquant.optim

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.Feed
import java.util.*


/**
 * Create a mutable synchronized list
 */
fun <T> mutableSynchronisedListOf(): MutableList<T> = Collections.synchronizedList(mutableListOf<T>())

/**
 * Default optimizer that implements default back-test optimization strategies to find a set of optimal parameter
 * values.
 *
 * @property space search space
 * @property score scoring function
 * @property getRoboquant function that returns an instance of roboquant based on passed parameters
 *
 */
class Optimizer(
    private val space: SearchSpace,
    private val score: Score,
    private val getRoboquant: (Params) -> Roboquant
) {

    private var run = 0

    data class RunResult(val params: Params, val score: Double, val timeframe: Timeframe, val name: String)

    /**
     * Using the default objective to maximize a metric. The default objective will use the last entry of the
     * provided [evalMetric] as the value to optimize.
     */
    constructor(space: SearchSpace, evalMetric: String, getRoboquant: (Params) -> Roboquant) : this(
        space, MetricScore(evalMetric), getRoboquant
    )

    /**
     * Walk-forward with validation (out of sample)
     */
    fun walkForward(
        feed: Feed,
        period: TimeSpan,
        validation: TimeSpan,
        warmup: TimeSpan = 0.days,
        anchored: Boolean = false
    ): List<RunResult> {
        require(!feed.timeframe.isInfinite()) { "feed needs known timeframe" }
        val feedStart = feed.timeframe.start
        val results = mutableListOf<RunResult>()

        feed.timeframe.split(period + validation, period).forEach {
            val trainEnd = it.end - validation
            val trainStart = if (anchored) feedStart else it.start
            val trainTimeframe = Timeframe(trainStart, trainEnd)
            val result = train(feed, trainTimeframe)
            results.addAll(result)
            val best = result.maxBy { entry -> entry.score }
            // println("phase=training timeframe=$timeframe equity=${best.second} params=${best.first}")
            val validationTimeframe = Timeframe(trainEnd, it.end - warmup, it.inclusive)
            val score = validate(feed, validationTimeframe, best.params)
            results.add(score)
        }
        return results
    }

    /**
     * Walk-forward without validation (out of sample)
     */
    fun walkForward(feed: Feed, period: TimeSpan, anchored: Boolean = false): List<RunResult> {
        require(!feed.timeframe.isInfinite()) { "feed needs known timeframe" }
        val start = feed.timeframe.start
        val results = mutableListOf<RunResult>()
        feed.timeframe.split(period).forEach {
            val timeframe = if (anchored) Timeframe(start, it.end, it.inclusive) else it
            val result = train(feed, timeframe)
            results.addAll(result)
        }
        return results
    }


    fun walkForward2(
        feed: Feed,
        period: TimeSpan,
        validation: TimeSpan,
        warmup: TimeSpan? = null,
        anchored: Boolean = false
    ) {
        val start = feed.timeframe.start
        feed.timeframe.split(period).forEach {

            val endTrain = it.end - validation
            val tf = if (anchored) Timeframe(start, endTrain) else it

            val result = train(feed, tf)
            val best = result.maxBy { r -> r.score }
            println("timeframe=$tf equity=${best.score} params=${best.params}")
            val tf2 = Timeframe(endTrain, it.end, it.inclusive)
            val rq = getRoboquant(best.params)
            if (warmup != null) {
                val warmupTimeframe = Timeframe(endTrain - warmup, endTrain, false)
                rq.warmup(feed, warmupTimeframe)
            }

            rq.run(feed, tf2)
            val validationScore = score.calculate(rq, tf2)
        }

    }

    /**
     * Run a Monte Carlo simulation
     */
    fun monteCarlo(feed: Feed, period: TimeSpan, samples: Int): List<RunResult> {
        val results = mutableSynchronisedListOf<RunResult>()
        require(!feed.timeframe.isInfinite()) { "feed needs known timeframe" }
        feed.timeframe.sample(period, samples).forEach {
            val result = train(feed, it)
            results.addAll(result)
            // val best = result.maxBy { it.score }
            // println("timeframe=$it equity=${best.score} params=${best.params}")
        }
        return results
    }

    /**
     * Train the solution in parallel
     */
    fun train(feed: Feed, tf: Timeframe = Timeframe.INFINITE): List<RunResult> {
        val jobs = ParallelJobs()
        val results = mutableSynchronisedListOf<RunResult>()
        for (params in space.materialize()) {
            jobs.add {
                val rq = getRoboquant(params)
                val name = "train-${run++}"
                rq.runAsync(feed, tf, name = name)
                val s = score.calculate(rq, tf)
                val result = RunResult(params, s, tf, name)
                results.add(result)
            }

        }
        jobs.joinAllBlocking()
        return results
    }

    /**
     * Train the solution in parallel
     */
    fun train2(feed: Feed, tf: Timeframe): List<RunResult> {
        val results = mutableListOf<RunResult>()
        for (params in space.materialize()) {
            // println("running roboquant timeframe=$tf params=$params")
            val name = "train-${run++}"
            val roboquant = getRoboquant(params)
            roboquant.run(feed, tf, name = name)
            val s = score.calculate(roboquant, tf)
            val result = RunResult(params, s, tf, name)
            // println("phase=train result=$result")
            results.add(result)
        }
        return results
    }

    /**
     * Run the validation phase
     */
    private fun validate(feed: Feed, timeframe: Timeframe, params: Params): RunResult {
        val rq = getRoboquant(params)
        val name = "validate-${run++}"
        rq.run(feed, timeframe, name = name)
        val s = score.calculate(rq, timeframe)
        // println("phase=validation result=$result")
        return RunResult(params, s, timeframe, name)
    }


}