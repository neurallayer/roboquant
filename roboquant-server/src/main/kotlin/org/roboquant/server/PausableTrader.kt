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

package org.roboquant.server

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import org.roboquant.traders.Trader
import org.roboquant.strategies.Signal
import java.time.Instant

/**
 * Trader that can be paused and also captures a number of metrics
 */
internal class PausableTrader(private val trader: Trader, var pause: Boolean = false) : Trader by trader {

    internal var sellSignals = 0
    internal var holdSignals = 0
    internal var buySignals = 0
    internal var totalOrders = 0
    internal var totalEvents = 0
    internal var emptyEvents = 0
    internal var totalActions = 0

    internal var lastUpdate: Instant = Instant.MIN

    override fun create(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
        // Still invoke the trader so any state can be updated if required.
        val orders = trader.create(signals, account, event)

        buySignals += signals.filter { it.isBuy }.size
        sellSignals += signals.filter { it.isSell }.size
        holdSignals += signals.filter { it.rating == 0.0 }.size

        totalEvents++
        if (event.items.isEmpty()) emptyEvents++
        totalActions += event.items.size
        lastUpdate = event.time

        return if (!pause) {
            totalOrders += orders.size
            orders
        } else {
            emptyList()
        }

    }


}
