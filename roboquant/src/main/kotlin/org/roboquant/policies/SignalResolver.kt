/*
 * Copyright 2020-2022 Neural Layer
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
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

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
     * Select the first signal for an asset assuming there are no conflicting signals, see also [Signal.conflicts]
     */
    NO_CONFLICTS,

    /**
     * Select a signal for an asset is there are no duplicate signals, even if the duplicate signals are not
     * conflicting
     */
    NO_DUPLICATES,
}

/**
 * Resolve potential conflicting signals. For many strategies this might not be necessary since there is always only 1
 * signal per asset, but as strategies are combined, this issue might pop up. You can specify the resolution [rule]
 * to apply when solving conflicts, the default being [SignalResolution.NONE].
 *
 * It returns the list of signals without any conflicts according to the configured rule.
 */
fun List<Signal>.resolve(rule: SignalResolution = SignalResolution.NONE): List<Signal> {
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
class SignalResolver(val policy: Policy, private val resolution: SignalResolution) : Policy by policy {

    private val logger = Logging.getLogger(this::class)

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val resolvedSignals = signals.resolve(resolution)
        logger.fine { "signals in=${signals.size} out=${resolvedSignals.size}" }
        return policy.act(resolvedSignals, account, event)
    }

}

/**
 * Resolve signals
 */
fun Policy.resolve(resolution: SignalResolution) = SignalResolver(this, resolution)
