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

package org.roboquant.orders

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CancellationTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 100)
        val oc = Cancellation(order.id)
        assertEquals(order.id, oc.id)
    }


    @Test
    fun testFailure() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 100)
        assertDoesNotThrow {
            Cancellation(order.id)
        }
    }
}
