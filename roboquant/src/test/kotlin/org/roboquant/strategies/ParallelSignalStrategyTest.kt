/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.strategies

import org.roboquant.feeds.random.RandomWalk
import kotlin.test.Test

internal class ParallelSignalStrategyTest {

    private fun getStrategy(r: Int) = RandomWalk.lastDays(10)

    @Test
    fun test() {
        val s1 = getStrategy(1)
        val s2 = getStrategy(2)
        /*val s3 = ParallelStrategy(getStrategy(1), getStrategy(2))

        for (event in TestData.events(20)) {
            val r1 = s1.generate(event)
            val r2 = s2.generate(event)
            val r3 = s3.generate(event)
            assertEquals(r1.size + r2.size, r3.size)
            if (r1.isNotEmpty()) assertEquals(r1.first().asset, r3.first().asset)
            if (r2.isNotEmpty()) assertEquals(r2.last().asset, r3.last().asset)
        }
*/
    }

}
