package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.common.Component
import org.roboquant.metrics.MetricResults

/**
 * Interface that any metrics logger will need to implement.
 *
 */
interface MetricsLogger : Component {

    /**
     * Log the results of metrics. It should be noted that the provided metrics can be empty.
     *
     * @param metrics The metric results to be logged
     * @param info info about the metrics and run, like when they were captured
     */
    fun log(metrics: MetricResults, info: RunInfo)
}


