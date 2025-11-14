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

package org.roboquant.ta

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.Stock
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.util.HistoricTestFeed
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class TaLibIndicatorTest {

    private fun feed(): HistoricFeed {
        return HistoricTestFeed(90..110, 110 downTo 80, 80..125, priceBar = true, asset = Stock("TEST"))
    }

    @Test
    fun basic() {
        val ind = TaLibIndicator {
            mapOf(
                "rsi" to rsi(it),
                "max" to max(it, timePeriod = 10)
            )
        }
        val feed = feed()
        val result = feed.apply(ind, feed.assets.first())
        assertEquals(2, result.size)
        assertContains(result, "rsi.test")
        assertContains(result, "max.test")
        assertEquals(feed.timeline.size - 14, result.values.first().size)
    }

    @Test
    fun predefined() {
        val feed = feed()
        val asset = feed.assets.first()
        assertDoesNotThrow {
            val map = feed.apply(TaLibIndicator.rsi(), asset)
            assertEquals(88, map.values.first().size)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.bbands(), asset)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.ema(), asset)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.sma(), asset)
        }

        assertDoesNotThrow {
            feed.apply(TaLibIndicator.mfi(), asset)
        }

        assertDoesNotThrow {
            feed.apply(TaLibIndicator.stochastic(), asset)
        }

    }


}
