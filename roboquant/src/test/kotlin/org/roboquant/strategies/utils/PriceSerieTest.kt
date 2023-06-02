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

package org.roboquant.strategies.utils

import org.roboquant.common.PriceSerie
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceSerieTest {

    @Test
    fun test() {
        val series = PriceSerie(10)
        assertTrue { series.toDoubleArray().all { it.isNaN() } }

        repeat(5) { series.add(1.0) }
        assertFalse(series.isFull())
        assertEquals(5, series.size)
        assertEquals(5, series.toDoubleArray().size)

        repeat(5) { series.add(1.0) }
        assertTrue(series.isFull())
        assertEquals(10, series.size)
        assertFalse(series.toDoubleArray().toList().contains(Double.NaN))

        repeat(5) { series.add(1.0) }
        assertTrue(series.isFull())
        assertEquals(10, series.size)

        assertEquals(10, series.toDoubleArray().size)

        series.clear()
        assertTrue { series.toDoubleArray().all { it.isNaN() } }
        assertEquals(0, series.size)
    }

    @Test
    fun capacity() {
        val series = PriceSerie(10)
        repeat(5) { series.add(1.0) }

        series.increaeseCapacity(20)
        assertFalse(series.isFull())
        assertEquals(5, series.size)
        assertEquals(5, series.toDoubleArray().size)

        repeat(20) { series.add(1.0) }
        assertTrue(series.isFull())
        assertEquals(20, series.size)
        assertEquals(20, series.toDoubleArray().size)
    }

}