/*
 * Copyright 2022 Neural Layer
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

import java.time.Instant
import kotlin.test.*

internal class MovingTimedWindowTest {

    @Test
    fun test() {
        val w = MovingTimedWindow(10)
        repeat(30) {
            w.add(1.0, Instant.now())
        }
        assertTrue(w.isAvailable())
        assertEquals(10, w.toDoubleArray().size)
        assertEquals(10, w.windowSize)
        assertEquals(10, w.toLongArray().size)
    }

}