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
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.PriceBar
import org.roboquant.feeds.apply
import org.roboquant.feeds.util.HistoricTestFeed
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TaLibTest {

    private fun getSeries(size: Int): PriceBarSeries {
        val feed = HistoricTestFeed(100 until 200, priceBar = true)
        val series = PriceBarSeries(size)
        feed.apply<PriceBar> { pb, _ ->
            series.add(pb, Instant.now())
        }
        return series
    }

    @Test
    fun test() {
        val lib = TaLib()
        val series = getSeries(10)
        assertDoesNotThrow {
            lib.sma(series, 8)
        }

        assertThrows<InsufficientData> {
            lib.sma(series, 12)
        }

        try {
            lib.sma(series, 12)
        } catch (ex: InsufficientData) {
            assertEquals(12, ex.minSize)
        }

    }

    @Test
    fun test2() {
        val lib = TaLib()
        val series = getSeries(10)
        try {
            lib.sma(series, 20)
        } catch (ex: InsufficientData) {

            series.increaseCapacity(ex.minSize - 1)
            assertThrows<InsufficientData> {
                lib.sma(series, 20)
            }

            series.increaseCapacity(ex.minSize)
            assertDoesNotThrow {
                lib.sma(series, 20)
            }

            assertThrows<InsufficientData> {
                lib.sma(series, 21)
            }
        }
    }

    @Test
    fun test3() {
        val lib = TaLib()
        val series = getSeries(10)
        try {
            lib.sma(series, 20, 4)
        } catch (ex: InsufficientData) {

            series.increaseCapacity(ex.minSize - 1)
            assertThrows<InsufficientData> {
                lib.sma(series, 20, 4)
            }

            series.increaseCapacity(ex.minSize)
            assertDoesNotThrow {
                lib.sma(series, 20, 4)
            }

            assertThrows<InsufficientData> {
                lib.sma(series, 21, 4)
            }
        }
    }

}
