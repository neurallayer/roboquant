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


import kotlin.test.*
import kotlin.random.Random

internal class RSITest {


    @Test
    fun test() {
        val r = Random(42)
        repeat(10) {
            val rsi = RSI(100.0)
            assertFalse(rsi.isReady())
            repeat(50) {
                rsi.add(100.0 + r.nextDouble() - 0.5)
            }
            assertTrue(rsi.isReady())
            val v = rsi.calculate()
            assertTrue( v >= 0.0)
            assertTrue( v <= 100.0)
        }

    }
}