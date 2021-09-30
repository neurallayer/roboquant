package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.common.Component
import org.roboquant.metrics.MetricResults

/**
 * Interface that any metrics logger will need to implement. It is called by roboquant after metrics have been calculated
 * to store or log them.
 */
interface MetricsLogger : Component {

    /**
     * Log the [results of metrics]. It should be noted that the provided results can be empty. Also [info] is provided
     * about when these results where captured.
     */
    fun log(results: MetricResults, info: RunInfo)
}


