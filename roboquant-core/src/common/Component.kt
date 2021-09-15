package org.roboquant.common

import org.roboquant.Phase
import org.roboquant.metrics.MetricResults

/**
 * Interface implemented by all components used in a run. The component will be informed that a phase has been
 * started or ended. It provides the component with the opportunity to manage its state so a phase can be started
 * without any state remaining from previous runs.
 *
 * Additionally, it provides components the option to log metrics. So any component can log values if required and this
 * adds to the traceability during testing and live trading.
 *
 * The default implementation of all the methods is to do nothing, so you only have to implement relevant methods to
 * your use-case.
 *
 */
interface Component {

    /**
     * Signal the start of a [Phase]. Default implementation is to take no action.
     *
     * @param phase The phase that is going to start
     */
    fun start(phase: Phase) {}

    /**
     * Signal the end of a [Phase]. Default implementation is to take no action.
     *
     * @param phase The phase that has just ended
     */
    fun end(phase: Phase) {}


    /**
     * Reset any state of a component. Default implementation is to take no action.
     */
    fun reset() {}

    /**
     * Get any recorded metrics from the most recent step in the run. This will be invoked after each step and
     * provides the component with the opportunity to log additional information.
     *
     * @return the map containing the metrics. This map should NOT be mutated by the component afterwards
     */
    fun getMetrics(): MetricResults = mapOf()

}