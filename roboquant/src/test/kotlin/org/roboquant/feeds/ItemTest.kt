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

package org.roboquant.feeds

import org.junit.jupiter.api.Assertions.assertTrue
import org.roboquant.TestData
import org.roboquant.common.CorporateItem
import org.roboquant.common.NewsItems
import org.roboquant.common.Stock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ItemTest {

    @Test
    fun corporateAction() {
        val asset = TestData.euStock()
        val action = CorporateItem(asset, "SPLIT", 2.0)
        assertEquals("SPLIT", action.type)
    }

    @Test
    fun newsAction() {
        val item = NewsItems.NewsItem(
            id = "1",
            content = "Some text",
            assets = listOf(Stock("AAPL")),
            meta = mapOf("source" to "TWITTER"))
        val action = NewsItems(listOf(item))
        assertEquals(1, action.items.size)
        assertEquals("Some text", action.items.first().content)
        assertTrue(item.assets.orEmpty().any { it.symbol == "AAPL" })
        assertEquals(1, action.items[0].meta?.size)
        assertFalse(action.items.isEmpty())
    }
}
