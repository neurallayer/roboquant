/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.common


import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class AssetTest {

    @Test
    fun basic() {
        val a = Stock("ABC")
        val c = Stock("ABC")

        assertEquals("ABC", a.symbol)
        assertEquals(a, c)
        assertEquals(a, a)
        assertContains(Asset.registry, "Stock")
    }


    @Test
    fun option() {
        val a = Option("ABC", Currency.USD)
        val s = a.serialize()
        val t = Asset.deserialize(s)
        assertEquals(a, t)
    }



    @Test
    fun contractValue() {
        val a = Stock("ABC")
        assertEquals(250.0.USD, a.value(Size(10), 25.0))

        assertEquals((-250.0).USD, a.value(Size(-10), 25.0))
    }



}
