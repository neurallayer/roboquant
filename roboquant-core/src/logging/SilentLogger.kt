package org.roboquant.logging

import org.roboquant.RunInfo

/**
 * Silent logger ignores all metrics results and only counts the number of invocations.
 * Used mainly during unit tests to suppress the logging of metrics.
 *
 * @constructor Create new Silent logger
 */
class SilentLogger : MetricsLogger {

    var events = 0L

    override fun log(metrics: Map<String, Number>, info: RunInfo) {
        events += 1
    }

    override fun reset() {
        events = 0L
    }

}
