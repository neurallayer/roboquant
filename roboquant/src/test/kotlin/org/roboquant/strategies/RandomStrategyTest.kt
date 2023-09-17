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
import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import kotlin.test.assertTrue

internal class RandomStrategyTest {

    @Test
    fun randomStrategy() {
        val s1 = RandomStrategy(1.0)
        for (event in TestData.events(20)) {
            val r1 = s1.generate(event)
            assertTrue(r1.isNotEmpty())
        }

        val s2 = RandomStrategy(0.0)
        for (event in TestData.events(20)) {
            val r1 = s2.generate(event)
            assertTrue(r1.isEmpty())
        }

        assertThrows<IllegalArgumentException> {
            RandomStrategy(1.2)
        }
    }

}
