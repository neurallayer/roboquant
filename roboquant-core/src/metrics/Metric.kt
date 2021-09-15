package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Component
import org.roboquant.feeds.Event

typealias MetricResults = Map<String, Number>

/**
 * Metric represents a piece of information you want to capture during a run. Common metrics are
 * account value, profit and loss, alpha and beta.
 *
 * This is the interface that any metric will have to implement, so it can be invoked during a run. Notice that a metric
 * takes care of the calculations, the storing of the results is normally done by a MetricsLogger.
 *
 * When metrics rely on state, it is important they override the [Component] methods to ensure they reset their
 * state when appropriate.
 *
 * For convenience there is the [SimpleMetric] abstract class that makes it easy to implement a new metric without
 * too much boilerplate code.
 *
 */
interface Metric : Component {

    /**
     * Calculate the metric given the account and step. This method will be invoked at the end of each step in a
     * run. After this the [getMetrics] method is invoked to retrieve the just calculated values.
     *
     * @param account
     * @param event
     * @return
     */
    fun calculate(account: Account, event: Event)

}


