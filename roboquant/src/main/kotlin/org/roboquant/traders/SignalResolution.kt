/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.traders

import org.roboquant.brokers.Account
import org.roboquant.common.Config
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import org.roboquant.strategies.Signal
import kotlin.random.Random

/**
 * Shuffle signals before processing them in the trader, avoiding favouring assets that appear always first in the
 * list of actions.
 *
 * @property trader
 * @constructor Create empty Signal resolver
 */
private class SignalShuffleTrader(private val trader: Trader, private val random: Random) : Trader by trader {

    override fun create(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
        return trader.create(signals.shuffled(random), account, event)
    }

}

/**
 * Shuffle signals using the provided [random] before invoking the trader
 */
fun Trader.shuffleSignals(random: Random = Config.random): Trader = SignalShuffleTrader(this, random)

/**
 * Shuffle signals before processing them in the trader, avoiding favoring assets that appear always first in the
 * actions of an event.
 *
 * @property trader
 * @constructor Create empty Signal resolver
 */
private class SkipSymbolsTrader(private val trader: Trader, private val symbols: List<String>) : Trader by trader {

    override fun create(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
        val newSignals = signals.filter { it.asset.symbol !in symbols }
        return trader.create(newSignals, account, event)
    }
}

/**
 * Skip [symbols] that should not be converted into orders by removing them from the signals
 */
fun Trader.skipSymbols(vararg symbols: String): Trader = SkipSymbolsTrader(this, symbols.asList())



