package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.metrics.MetricResults

/**
 * This logger wraps another MetricsLogger and allows to configure a number of steps at the start of a run at which
 * metrics will not be logged. The most common use case is that there is a warm-up period for a strategy that you
 * don't want to capture metrics at that period.
 *
 * @constructor Create empty Skip warmup logger
 */
class SkipWarmupLogger(private val logger: MetricsLogger, private val minSteps: Int = -1) : MetricsLogger by logger {

    override fun log(results: MetricResults, info: RunInfo) {
        if (info.step < minSteps) return
        logger.log(results, info)
    }

}

fun MetricsLogger.skipFirst(steps: Int) = SkipWarmupLogger(this, steps)
