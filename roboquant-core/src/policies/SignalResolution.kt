/*
 * Copyright 2021 Neural Layer
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
 * Resolve conflicting signals. For many strategies this might not be necessary since there is only 1 signal per
 * asset, but as strategies are combined, this issue might pop up. You can specify the resolution [rule] to apply
 * when solving conflicts, the default being [SignalResolution.NO_CONFLICTS].
 *
 * It returns the list of signals without any conflicts.
 */
fun List<Signal>.resolve(rule: SignalResolution = SignalResolution.NO_CONFLICTS): List<Signal> {
    if (size < 2) return this

    return when (rule) {
        SignalResolution.NONE -> this
        SignalResolution.FIRST -> distinctBy { it.asset }
        SignalResolution.LAST -> asReversed().distinctBy { it.asset }.asReversed()
        SignalResolution.NO_CONFLICTS -> filter { none { f -> f.conflicts(it) } }.distinctBy { it.asset }
        SignalResolution.NO_DUPLICATES -> {
            val assets = this.groupBy { it.asset }
            filter { assets[it.asset]!!.size == 1 }
        }
    }
}
