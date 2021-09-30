package org.roboquant.logging

import org.roboquant.RunInfo

/**
 * Use plain println to output metric results to the console. This works with Notebooks and standalone applications.
 * By default, it will log metrics in a single line, but by setting [splitMetrics] to true, every metric will be logged
 * to separate line.
 */
class ConsoleLogger(private val splitMetrics: Boolean = false) : MetricsLogger {

    override fun log(results: Map<String, Number>, info: RunInfo) {
        if (results.isEmpty()) return

        if (splitMetrics) {
            results.forEach {
                println("$info ${it.key}=${it.value}")
            }
        } else {
            println("$info $results")
        }
    }

}
