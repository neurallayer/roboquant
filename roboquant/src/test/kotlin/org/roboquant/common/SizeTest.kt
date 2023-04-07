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

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SizeTest {

    @Test
    fun test() {
        val size = Size(10)
        assertEquals(Size(5), size / 2)
        assertEquals(Size(20), size * 2)
        assertEquals(Size(0), Size.ZERO)
        assertEquals(Size(1), Size.ONE)
        assertEquals(Size(13), size + Size(3))
        assertEquals(Size(9), size - Size.ONE)
        assertEquals(Size(-10), -size)
        assertEquals("10", size.toString())
        assertEquals("10.00123", Size("10.00123").toString())
    }

    @Test
    fun testOverflow() {
        assertDoesNotThrow {
            Size("12345678.12345")
        }

        assertThrows<ArithmeticException> {
            Size("10000000000000000")
        }

        assertThrows<ArithmeticException> {
            Size("100000.123456789")
        }

    }

    @Test
    fun testFractional() {
        val size = Size("10.10")
        assertTrue(size.isFractional)
        assertEquals(BigDecimal("10.10"), size.toBigDecimal().setScale(2))
        assertEquals(10.10, size.toDouble())

        // Equality test
        assertEquals(size, Size("10.10"))

        // test not fractional
        val size2 = Size(100001)
        assertFalse(size2.isFractional)
    }

}