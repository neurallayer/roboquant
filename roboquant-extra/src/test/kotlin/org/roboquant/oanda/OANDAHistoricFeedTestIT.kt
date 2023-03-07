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

package org.roboquant.oanda

import com.oanda.v20.RequestException
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Timeframe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OANDAHistoricFeedTestIT {


    private val symbols = arrayListOf("EUR_USD", "USD_JPY", "GBP_USD").toTypedArray()

    @Test
    fun historicTest() {
        System.getenv("FULL_COVERAGE") ?: return
        val feed = OANDAHistoricFeed()
        val tf = Timeframe.parse("2020-03-05", "2020-03-06")
        feed.retrieve(*symbols, timeframe = tf)
        assertEquals(3, feed.assets.size)

        val tf2 = Timeframe.parse("2020-03-04", "2020-03-07")
        assertTrue(tf2.contains(feed.timeline.first()))
        assertTrue(tf2.contains(feed.timeline.last()))
        feed.close()
    }

    @Test
    fun correctException() {

        assertThrows<RequestException> {
            OANDAHistoricFeed {
                key = "wrong_key"
            }
        }

    }

}