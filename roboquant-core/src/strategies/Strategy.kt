package org.roboquant.strategies

import org.roboquant.common.Component
import org.roboquant.feeds.Event

/**
 * The Strategy is the interface that a trading strategy will need to implement. A strategy receives a
 * [Event] and can generate zero or more [Signal], where each signal provides a [Rating] for a certain asset.
 *
 * Roboquant makes no assumptions on the type of strategy. It can range from a technical indicator all the way
 * to sentiment analysis using machine learning.
 *
 * A strategy only has access to an event. In case a strategy requires to also have access to the Account or Portfolio,
 * it can be implemented as a Policy instead.
 */
interface Strategy : Component {

    /**
     * Based on received [event], generate zero or more [Signal]s. Typically, the signals are a result of the actions in the
     * event, but this is not a strict requirement.
     *
     * If there are no signals detected, this method should return an empty list.
     */
    fun generate(event: Event): List<Signal>

}

