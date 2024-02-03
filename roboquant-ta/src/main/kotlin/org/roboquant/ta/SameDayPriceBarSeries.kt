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

package org.roboquant.ta

import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Special version of [PriceBarSeries] that will update an existing price-bar entry in the buffer is a price-bar is
 * added for the same day, and otherwise add a new entry.
 *
 * @see [PriceBarSeries]
 *
 * @param capacity the initial capacity of buffer
 *
 * @constructor Create a new instance of PriceBarSeries
 */
class SameDayPriceBarSeries(capacity: Int) : PriceBarSeries(capacity) {

    /**
     * Update the existing OHLCV values if [priceBar] is on the same day as the last entry, otherwise add a new entry
     * to the buffer.
     * Return true if the buffer is full.
     */
    override fun add(priceBar: PriceBar, time: Instant): Boolean {
        return when {
            size == 0 -> add(priceBar.ohlcv, time)
            priceBar.asset.exchange.sameDay(timeline.last(), time) -> update(priceBar.ohlcv, time)
            else -> add(priceBar.ohlcv, time)
        }
    }

    /**
     * Update the buffer with a new [ohlcv] values and [time]. Return true if series is full.
     */
    private fun update(ohlcv: DoubleArray, time: Instant): Boolean {
        assert(ohlcv.size == 5)
        if (openBuffer.last().isNaN()) openBuffer.update(ohlcv[0])
        if (ohlcv[1] > highBuffer.last()) highBuffer.update(ohlcv[1])
        if (ohlcv[2] < lowBuffer.last()) lowBuffer.update(ohlcv[2])
        closeBuffer.update(ohlcv[3])
        volumeBuffer.update(volumeBuffer.last() + ohlcv[4])
        timeBuffer[timeBuffer.lastIndex] = time
        return isFull()
    }



}




