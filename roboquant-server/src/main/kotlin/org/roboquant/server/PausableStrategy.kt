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
import org.roboquant.strategies.Strategy
import java.time.Instant

/**
 * SignalConverter that can be paused and also captures a number of metrics
 */
internal class PausableStrategy(private val trader: Strategy, var pause: Boolean = false) : Strategy  {


    internal var totalInstructions = 0
    internal var totalEvents = 0
    internal var emptyEvents = 0
    internal var totalActions = 0

    internal var lastUpdate: Instant = Instant.MIN

    override fun create(event: Event, account: Account): List<Instruction> {
        // Still invoke the strategy so any state can be updated if required.
        val orders = trader.create(event, account)

        totalEvents++
        if (event.items.isEmpty()) emptyEvents++
        totalActions += event.items.size
        lastUpdate = event.time

        return if (!pause) {
            totalInstructions += orders.size
            orders
        } else {
            emptyList()
        }

    }


}
