package org.roboquant.strategies

import org.roboquant.common.Component
import org.roboquant.feeds.Event

/**
 * The Strategy is the interface that most type of trading strategy will need to implement. A strategy receives a
 * [Event] and can generate zero or more [Signal], where each signal provides a [Rating] for a certain asset.
 *
 * Roboquant makes no assumptions on the type of strategy. It can range from a technical indicator all the way
 * to sentiment analysis using machine learning.
 *
 * Strategy only has access to the event. In case a strategy is required to also have access to the Account or Portfolio,
 * it can be implemented as a Policy instead.
 */
interface Strategy : Component {

    /**
     * Based on a [Event], generate zero or more [Signal]s. Typically, the signals are a result of the actions in the event,
     * but this is not a strict requirement.
     *
     * @param event
     * @return List of signals, can be empty.
     */
    fun generate(event: Event): List<Signal>

}

