package org.roboquant.logging

import org.roboquant.RunInfo
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Uses the [Logger] under the hood to log metric results, It will log everything by default at
 * [Level.INFO] level, but this can be configured
 *
 * @constructor Create new Info logger
 */
class InfoLogger(
    name: String = "MetricsLogger",
    private val splitMetrics: Boolean = false,
    private val level: Level = Level.INFO
) : MetricsLogger {

    private val logger: Logger = Logger.getLogger(name)

    override fun log(results: Map<String, Number>, info: RunInfo) {
        if (results.isEmpty()) return

        if (!splitMetrics)
            logger.log(level) {
                mapOf(
                    "name" to info.name,
                    "run" to info.run,
                    "epoch" to info.episode,
                    "time" to info.time,
                    "step" to info.step,
                    "metrics" to results
                ).toString()
            }
        else
            results.forEach {
                logger.log(level) {
                    mapOf(
                        "name" to info.name,
                        "run" to info.run,
                        "epoch" to info.episode,
                        "time" to info.time,
                        "step" to info.step,
                        "name" to it.key,
                        "value" to it.value
                    ).toString()
                }
            }
    }

}