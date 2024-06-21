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

package org.roboquant.brokers.sim.execution

import org.roboquant.TestData
import org.roboquant.common.Size
import org.roboquant.common.days
import org.roboquant.common.plus
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TIFTest {

    val now: Instant = Instant.now()

    private fun getOrderCommand(tif: TimeInForce, fill: Double = 0.0): SingleOrderExecutor<MarketOrder> {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, Size(50), tif)
        order.openedAt = now
        order.status = OrderStatus.ACCEPTED
        val result = MarketOrderExecutor(order)
        result.fill = order.size * fill
        return result
    }

    @Test
    fun testDAY() {
        val cmd = getOrderCommand(DAY())
        assertFalse(cmd.expired(now))
        assertTrue(cmd.expired(now.plusSeconds(3600*24)))
    }

    @Test
    fun testGTC() {
        val cmd = getOrderCommand(GTC(maxDays = 5))
        assertFalse(cmd.expired(now))
        assertFalse(cmd.expired(now.plusSeconds(3600*24)))
        assertTrue(cmd.expired(now.plusSeconds(3600*24*6)))
    }

    @Test
    fun testFOK() {
        val cmd = getOrderCommand(FOK())
        assertTrue(cmd.expired(now))


    }

    @Test
    fun testGTD() {
        val date = now + 2.days
        val cmd = getOrderCommand(GTD(date), 1.0)
        assertFalse(cmd.expired(now))
        assertFalse(cmd.expired(now + 1.days))
        assertTrue(cmd.expired(now + 3.days))
    }

    @Test
    fun testIOC() {
        val cmd = getOrderCommand(IOC(), 0.5)
        assertFalse(cmd.expired(now))
        assertTrue(cmd.expired(now + 1.days))
    }

}
