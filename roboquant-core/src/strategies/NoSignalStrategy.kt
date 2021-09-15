package org.roboquant.strategies

import org.roboquant.feeds.Event

/**
 * Strategy that doesn't generate any signals. This is especially useful if you develop your Strategy as
 * a Policy (for example you require access to the Account). In that case the NoSignalStrategy just serves as
 * a pass through and all logic is handled by the policy.
 *
 * ## Example
 *      val roboquant =  Roboquant(NoSignalStrategy(), MyCustomPolicy())
 *
 * @constructor Create new NoSignalStrategy
 */
class NoSignalStrategy : Strategy {
    override fun generate(event: Event): List<Signal> {
        return listOf()
    }
}