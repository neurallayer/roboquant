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
import org.roboquant.common.Asset
import org.roboquant.common.Currency

internal class PortfolioTest {

    @Test
    fun testUpdatePortfolio() {
        val portfolio = Portfolio()
        val c = TestData.usStock()
        val position = Position(c, 100.0, 10.0)
        portfolio.updatePosition(position)
        assertEquals(100.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)


        val position2 = Position(c, 100.0, 10.0)
        portfolio.updatePosition(position2)
        assertEquals(200.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)

        val position3 = Position(c, -100.0, 10.0)
        portfolio.updatePosition(position3)
        assertEquals(100.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)


        assertEquals(portfolio.positions.size, portfolio.longPositions.size + portfolio.shortPositions.size)

        val s = portfolio.summary()
        assertTrue(s.toString().isNotEmpty())

    }


    @Test
    fun derivedValues() {
        val portfolio = Portfolio()
        portfolio.updatePosition(Position(Asset("A"), 100.0, 10.0))
        portfolio.updatePosition(Position(Asset("B"), 100.0, 10.0))
        portfolio.updatePosition(Position(Asset("C"), -100.0, 10.0))
        portfolio.updatePosition(Position(Asset("D"), -100.0, 10.0))
        assertEquals(2000.0, portfolio.longValue.getValue(Currency.USD))
        assertEquals(-2000.0, portfolio.shortValue.getValue(Currency.USD))
        assertEquals(0.0, portfolio.value.getValue(Currency.USD))
    }

    /*
    @Test
    fun testUpdatePortfolio2() {
        val portfolio = Portfolio()
        val c = TestData.usStock()
        val position = Position(c, 100.0, 10.0)
        portfolio.updatePosition2(position)
        assertEquals(100.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)


        val position2 = Position(c, 100.0, 10.0)
        portfolio.updatePosition2(position2)
        assertEquals(200.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)

        val position3 = Position(c, -100.0, 10.0)
        portfolio.updatePosition2(position3)
        assertEquals(100.0, portfolio.getPosition(c).size)
        assertEquals(10.0, portfolio.getPosition(c).avgPrice)

        assertEquals(portfolio.positions.size, portfolio.longPositions.size + portfolio.shortPositions.size)

        val s = portfolio.summary()
        assertTrue(s.toString().isNotEmpty())

    }
     */


}