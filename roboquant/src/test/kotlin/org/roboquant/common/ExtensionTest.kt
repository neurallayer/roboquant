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

package org.roboquant.common

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExtensionTest {

    @Test
    fun returns() {
        val a = doubleArrayOf(10.0, 20.0, 10.0)
        assertEquals(2, a.returns().size)
        assertEquals(1.0, a.returns()[0])
        assertEquals(-0.5, a.returns()[1])
    }

    @Test
    fun growthRates() {
        val a = doubleArrayOf(10.0, 20.0, 10.0)
        assertEquals(2, a.growthRates().size)
        assertEquals(2.0, a.growthRates()[0])
        assertEquals(0.5, a.growthRates()[1])
    }

    @Test
    fun paths() {
        val p1 = "tmp" / "dummy" / "test.txt"
        assertEquals(Path("tmp", "dummy", "test.txt"), Path(p1))

        val p2 = "tmp" / "dummy" / ".." / "test.txt"
        assertEquals(Path("tmp", "test.txt"), Path(p2))
    }

    @Test
    fun testRounding() {
        val small = 0.00000000000001
        assertTrue(small.iszero)
        assertFalse(small.nonzero)
        val big = 0.01
        assertFalse(big.iszero)
        assertTrue(big.nonzero)
    }

    @Test
    fun doubleArrayTests() {
        val a = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val b = doubleArrayOf(1.0, 1.0, 2.0, 2.0)
        assertEquals(6.0, (a + b).last())
        assertEquals(2.0, (a - b).last())
        assertEquals(2.0, (a / b).last())
        assertEquals(8.0, (a * b).last())

        assertEquals(20.0, (a * 5.0).last())
        assertEquals(9.0, (a + 5.0).last())
        assertEquals(2.0, (a / 2.0).last())
        assertEquals(0.0, (a - 4.0).last())
    }

    @Test
    fun testAccess() {
        val l = listOf("1", 1, true, "more", 2, 3)
        val sub = l[1..2]
        assertEquals(1, sub[0])
        assertEquals(2, sub.size)
    }

    @Test
    fun testCurrencyPairs() {
        val s = "ABCDEF"
        val t = "ABC_DEF"
        assertEquals(s.toCurrencyPair(), t.toCurrencyPair())
        assertNull("dummy".toCurrencyPair())
    }


    @Test
    fun other() {
        val i = 1..10..2
        assertEquals(5, i.toList().size)

        val s = listOf("a", "b", "c")
        assertTrue(s.summary().content.contains('a'))

    }

    @Test
    fun instant() {
        val t = Instant.now()
        assertTrue(t > Timeframe(t, t))
        assertFalse(t < Timeframe(t, t))

        assertFalse(t > Timeframe(t, t, true))
        assertFalse(t < Timeframe(t, t, true))

        assertTrue(t in Timeframe(t, t, true))
        assertFalse(t in Timeframe(t, t))
    }
}