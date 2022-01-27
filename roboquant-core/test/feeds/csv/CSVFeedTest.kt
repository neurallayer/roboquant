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

package org.roboquant.feeds.csv

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class CSVFeedTest {

    @Test
    fun getContracts() {
        val feed = CSVFeed(TestData.dataDir() + "US")
        assertEquals(3, feed.assets.size)
        val c2 = feed.assets.find { it.symbol == "AAPL" }
        assertTrue(c2 !== null)
    }

    @Test
    fun noAssets() {
        assertFailsWith<Exception> {
            CSVFeed(TestData.dataDir() + "NON_Existing_DIR")
        }

        val config = CSVConfig(fileExtension = "non_existing_ext")
        val feed1 = CSVFeed(TestData.dataDir() + "US", config)
        assertTrue(feed1.assets.isEmpty())
    }


    @Test
    fun customBuilder() {
        val asset = Asset("TEMPLATE", exchangeCode = "TEST123")
        val config = CSVConfig(template = asset)
        val feed = CSVFeed(TestData.dataDir() + "US", config)
        assertEquals("TEST123", feed.assets.first().exchangeCode)
    }


    @Test
    fun config() {
        val asset = Asset("TEMPLATE", exchangeCode = "TEST123")
        val config = CSVConfig(template = asset)
        assertEquals(asset.currency, config.currency)
    }

    @Test
    fun columnInfoTest() {
        val a = ColumnInfo()
        a.define("XOHXLC")
        assertEquals(1, a.open)
        assertEquals(5, a.close)

        assertFails {
            a.define("QQQ")
        }
    }


}