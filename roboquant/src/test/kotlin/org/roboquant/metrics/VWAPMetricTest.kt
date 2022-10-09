/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.metrics

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.days
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VWAPMetricTest {

    @Test
    fun test() {
        val metric = VWAPMetric()
        assertEquals(2, metric.minSize)

        val account = TestData.usAccount()
        var result = mapOf<String, Number>()
        val event = TestData.event2()
        repeat(10) {
            result = metric.calculate(account, event)
        }
        assertTrue(result.isNotEmpty())

        metric.reset()
        result = metric.calculate(account, event)
        assertTrue(result.isEmpty())

        val resetEvent = event.copy(time = event.time + 1.days)
        result = metric.calculate(account, resetEvent)
        assertTrue(result.isEmpty())
    }

}