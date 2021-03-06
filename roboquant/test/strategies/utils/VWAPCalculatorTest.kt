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

package org.roboquant.strategies.utils

import org.roboquant.TestData.priceBar
import java.time.Instant
import kotlin.test.*

internal class VWAPCalculatorTest {

    @Test
    fun test() {
        val c = VWAPCalculator(20)
        assertFalse(c.isReady())

        repeat(30) { c.add(priceBar()) }
        assertTrue(c.isReady())

        assertEquals(priceBar().getPrice("TYPICAL"), c.calc())
    }

    @Test
    fun testDaily() {
        val c = VWAPDaily(5)
        assertFalse(c.isReady())
        val now = Instant.now()
        repeat(5) {
            c.add(priceBar(), now)
        }
        assertTrue(c.isReady())

        assertEquals(priceBar().getPrice("TYPICAL"), c.calc())
    }
}
