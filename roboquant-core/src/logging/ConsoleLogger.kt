package org.roboquant.logging

import org.roboquant.RunInfo

/**
 * Use plain println to output metric results to the console. This works with Notebooks and standalone applications.
 *
 * @constructor Create new console logger
 */
class ConsoleLogger(private val splitMetrics: Boolean = false) : MetricsLogger {

    override fun log(metrics: Map<String, Number>, info: RunInfo) {
        if (metrics.isEmpty()) return

        if (splitMetrics) {
            metrics.forEach {
                println("$info ${it.key}=${it.value}")
            }
        } else {
            println("$info $metrics")
        }
    }

}
