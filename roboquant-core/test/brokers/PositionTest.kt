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

package org.roboquant.brokers

import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PositionTest {


    @Test
    fun update1() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        val p2 = Position(contract, 10.0, 12.0, 12.0)
        val newPos = p1 + p2
        val pnl = p1.realizedPNL(p2)
        assertEquals(11.0, newPos.avgPrice)
        assertEquals(20.0, newPos.size)
        assertEquals(0.0, pnl)
    }


    @Test
    fun update2() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        val p2 = Position(contract, -5.0, 11.0, 12.0)
        val newPos = p1 + p2
        val pnl = p1.realizedPNL(p2)
        assertEquals(10.0, newPos.avgPrice)
        assertEquals(5.0, newPos.size)
        assertEquals(5.0, pnl)
    }

    @Test
    fun update3() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        val p2 = Position(contract, -15.0, 11.0, 12.0)
        val newPos = p1 + p2
        val pnl = p1.realizedPNL(p2)
        assertEquals(11.0, newPos.avgPrice)
        assertEquals(-5.0, newPos.size)
        assertEquals(10.0, pnl)
    }


    @Test
    fun pnl() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        val pnl = p1.unrealizedPNL.value
        assertEquals(20.0, pnl)
    }


    @Test
    fun direction() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        assertTrue(p1.long)
        assertFalse(p1.short)
        assertTrue(p1.open)
    }


    @Test
    fun cost() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0)
        val cost = p1.totalCost.value
        assertEquals(100.0, cost)
    }

    @Test
    fun value() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        val value = p1.marketValue.value
        assertEquals(120.0, value)
    }


    @Test
    fun size() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 10.0, 10.0, 12.0)
        assertEquals(10.0, p1.totalSize)
    }

    @Test
    fun positionUpdateRounding() {
        val contract = TestData.usStock()
        val p1 = Position(contract, 2.0, 1.25003111, 1.203001)
        val p2 = Position(contract, 3.1, 1.24111, 1.2130044)
        val updatedPosition12 = p1.plus(p2)
        val minusP2 = Position(contract, -3.1, 1.3111, 1.3130044)
        val minusP1 = Position(contract, -2.0, 1.25003111, 1.203001)
        val shouldBeZero = updatedPosition12.plus(minusP2).plus(minusP1)
        assertEquals(shouldBeZero.size, 0.0)
    }

}