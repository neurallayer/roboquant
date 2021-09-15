package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.metrics.MetricResults

/**
 * Recording strategy allows subclasses to record metrics during their processing.
 * With the recording flag you can enable or disable the actual recording of the metrics.
 *
 * @property prefix
 */
abstract class RecordingStrategy(private val prefix: String = "strategy.", enabled: Boolean = false) : Strategy {

    /**
     * Should metrics be recorded or not. Some strategies can record a lot of metrics, so this disables that recording
     * and as a consequence might be faster and/or use less memory.
     */
    var recording = enabled

    private val metrics = mutableMapOf<String, Number>()

    /**
     * Get the recorded metrics. After this method has been invoked, the metrics are also cleared, so calling this method
     * twice in a row won't return the same result.
     *
     * @return
     */
    override fun getMetrics(): MetricResults {
        val result = metrics.toMap()
        metrics.clear()
        return result
    }

    /**
     * Record a new metric. If there is already a metric recorded with the same key, it will be overridden.
     *
     * @param key
     * @param value
     */
    protected fun record(key: String, value: Number) {
        if (!recording) return
        metrics["$prefix$key"] = value
    }

    /**
     * At the start of a phase, any recorded metrics will be cleared.
     */
    override fun start(phase: Phase) {
        metrics.clear()
    }
}