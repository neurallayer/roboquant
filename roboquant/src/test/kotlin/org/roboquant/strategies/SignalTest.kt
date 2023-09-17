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

package org.roboquant.strategies

import kotlin.test.Test
import org.roboquant.common.Asset
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class SignalTest {

    @Test
    fun basic() {
        val c = Asset("AAPL")
        val s = Signal(c, Rating.BUY)

        assertEquals(s.asset, c)
        assertTrue(s.takeProfit.isNaN())
        assertTrue(s.takeProfit.isNaN())
        assertTrue(s.takeProfitAmount.value.isNaN())

        assertTrue(s.stopLoss.isNaN())
        assertTrue(s.stopLossAmount.value.isNaN())

        assertTrue(s.probability.isNaN())
        assertEquals(s.rating, Rating.BUY)

        val s2 = Signal(c, Rating.SELL, takeProfit = 110.0)
        assertTrue(s2.conflicts(s))
        assertTrue(!s2.conflicts(s2))

        val s3 = Signal(c, Rating.SELL, SignalType.ENTRY)
        assertNotEquals(s3.type, SignalType.EXIT)

    }

}
