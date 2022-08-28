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

package org.roboquant.brokers

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.common.Currency
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PortfolioTest {

   @Test
    fun testUpdatePortfolio() {
        val portfolio = mutableMapOf<Asset, Position>().withDefault { Position.empty(asset = it) }
        val c = TestData.usStock()
        val position = Position(c, 100, 10.0)
        portfolio[c] = position
        assertEquals(100.0, portfolio.getValue(c).size.toDouble())
        assertEquals(10.0, portfolio.getValue(c).avgPrice)

       assertEquals(1000.0, portfolio.exposure.getValue(Currency.USD))

       assertTrue(portfolio.isLong(c))
       assertFalse(portfolio.isShort(c))
    }

}