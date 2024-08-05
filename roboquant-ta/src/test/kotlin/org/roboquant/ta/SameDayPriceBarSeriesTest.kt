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

import org.roboquant.common.USStock
import org.roboquant.common.days
import org.roboquant.common.millis
import org.roboquant.common.plus
import org.roboquant.feeds.PriceBar
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SameDayPriceBarSeriesTest {

    @Test
    fun sameDay() {

        val asset = USStock("DEMO")
        val pb = PriceBar(asset, 10, 11, 9, 10, 100)

        val pbs = SameDayPriceBarSeries(50)
        val now =  Instant.parse("2022-01-01T18:00:00Z")

        // Create same day events
        repeat(20) { pbs.add(pb, now + it.millis) }
        assertEquals(1, pbs.size)
        assertEquals(20*100.0, pbs.volume.last())

        // Create ne wday events
        repeat(20) {pbs.add(pb, now + (it + 1).days) }
        assertEquals(21, pbs.size)
    }


}
