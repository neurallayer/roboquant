/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.brokers

import org.roboquant.common.Size
import org.roboquant.common.Stock
import org.roboquant.common.Asset
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.Position
import org.roboquant.common.pnl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PositionTest {

    @Test
    fun direction() {
        val p1 = Position(Size(10), 10.0, 12.0)
        assertTrue(p1.long)
        assertFalse(p1.short)
        assertTrue(p1.open)
    }



    @Test
    fun size() {
        val p1 = Position(Size(10), 10.0, 12.0)
        assertEquals(Size(10), p1.size)
    }

    @Test
    fun maps() {
        val m: Map<Asset, Position> = mapOf(
            Stock("AAA") to Position(Size(100), 100.0, 99.0), // -100 profit
            Stock("BBB") to Position(Size(100), 100.0, 90.0), // -1_000 lost
        )
        assertEquals(-1100.0, m.pnl()[USD])
    }

}
