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

package org.roboquant.orders

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.Size
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class TrailLimitOrderTest {

    @Test
    fun testTrailOrder() {
        val asset = TestData.usStock()
        val order = TrailOrder(asset, Size(-100), 0.01)
        assertEquals(0.01, order.trailPercentage)
        assertTrue(order.toString().isNotBlank())

        assertFails {
            TrailOrder(asset, Size(-100), -0.01)
        }

    }

    @Test
    fun testTrailLimitOrder() {
        val asset = TestData.usStock()
        val order = TrailLimitOrder(asset, Size(-100), 0.01, -1.0)
        assertEquals(0.01, order.trailPercentage)
        assertEquals(-1.0, order.limitOffset)
        assertTrue(order.toString().isNotBlank())
    }

}