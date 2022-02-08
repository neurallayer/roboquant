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

package org.roboquant.policies


import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OrderSizerTest {

    @Test
    fun basic() {
        val sizer = PercentageOrderSizer()
        val asset = TestData.usStock()
        val account = TestData.usAccount()
        val size = sizer.size(asset, account, 5_000.0, 100.0)
        assertEquals(10.0, size)

        val size2 = sizer.size(asset, account, 500.0, 100.0)
        assertEquals(5.0, size2)
    }

    @Test
    fun fractional() {
        val sizer = PercentageOrderSizer(fractions = 50)
        val asset = TestData.usStock()
        val account = TestData.usAccount()
        val size = sizer.size(asset, account, 255.0, 100.0)
        assertEquals(2.54, size)

        val size2 = sizer.size(asset, account, 256.0, 100.0)
        assertEquals(2.56, size2)
    }

    @Test
    fun minimum() {
        val sizer = PercentageOrderSizer(fractions = 50, minAmount = 500.0)
        val asset = TestData.usStock()
        val account = TestData.usAccount()
        val size = sizer.size(asset, account, 255.0, 100.0)
        assertEquals(0.0, size)

    }
}