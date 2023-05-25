/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import kotlin.random.Random

/**
 * Different types of Signal Resolutions available
 */
enum class SignalResolution {

    /**
     * Don't resolve any potential signal conflicts
     */
    NONE,

    /**
     * Select first signal found for an asset
     */
    FIRST,

    /**
     * Select last signal found for an asset
     */
    LAST,

    /**
     * Select first signal for an asset when there are no conflicting signals for that asset, see
     * also [Signal.conflicts]
     */
    NO_CONFLICTS,

    /**
     * Select a signal for an asset when there are no duplicate signals, even if the duplicate signals are not
     * conflicting
     */
    NO_DUPLICATES,
}

/**
 * Resolve potential conflicting signals. For many strategies, this might not be necessary since there is always only 1
 * signal per asset, but as strategies are combined, this issue might pop up. You can specify the resolution [rule]
 * to apply when solving conflicts.
 *
 * It returns the list of signals without any conflicts according to the configured rule.
 */
fun List<Signal>.resolve(rule: SignalResolution): List<Signal> {
    if (size < 2) return this

    return when (rule) {
        SignalResolution.NONE -> this
        SignalResolution.FIRST -> distinctBy { it.asset }
        SignalResolution.LAST -> asReversed().distinctBy { it.asset }.asReversed()
        SignalResolution.NO_CONFLICTS -> filter { none { f -> f.conflicts(it) } }.distinctBy { it.asset }
        SignalResolution.NO_DUPLICATES -> {
            val assets = this.groupBy { it.asset }
            filter { assets.getValue(it.asset).size == 1 }
        }
    }
}

/**
 * Signal resolver policy wraps other policies and makes sure there are no conflicting signals
 *
 * @property policy
 * @property resolution
 * @constructor Create empty Signal resolver
 */
private class SignalResolverPolicy(private val policy: Policy, private val resolution: SignalResolution) :
    Policy by policy {

    private val logger = Logging.getLogger(this::class)

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val resolvedSignals = signals.resolve(resolution)
        logger.debug { "signals in=${signals.size} out=${resolvedSignals.size}" }
        return policy.act(resolvedSignals, account, event)
    }

}

/**
 * Resolve conflicting signals using the provided [resolution] before invoking the policy
 */
fun Policy.resolve(resolution: SignalResolution): Policy = SignalResolverPolicy(this, resolution)


/**
 * Shuffle signals before processing them in the policy, avoiding favoring assets that appears always first in the
 * list of actions.
 *
 * @property policy
 * @constructor Create empty Signal resolver
 */
private class SignalShufflePolicy(private val policy: Policy, private val random: Random) : Policy by policy {


    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        return policy.act(signals.shuffled(random), account, event)
    }

}

/**
 * Shuffle signals using the provided [random] before invoking the policy
 */
fun Policy.shuffleSignals(random: Random = Config.random): Policy = SignalShufflePolicy(this, random)


/**
 * Shuffle signals before processing them in the policy, avoiding favoring assets that appear always first in the
 * actions of an event.
 *
 * @property policy
 * @constructor Create empty Signal resolver
 */
private class SkipSymbolsPolicy(private val policy: Policy, private val symbols: List<String>) : Policy by policy {

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val newSignals = signals.filter { it.asset.symbol !in symbols }
        return policy.act(newSignals, account, event)
    }
}

/**
 * Skip [symbols] that should not be converted into orders by removing them from the signals
 */
fun Policy.skipSymbols(vararg symbols: String): Policy = SkipSymbolsPolicy(this, symbols.asList())



