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

package org.roboquant.loggers

import org.junit.jupiter.api.assertThrows
import org.roboquant.common.TimeSeries
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TimeSeriesTest {

    @Test
    fun test() {
        val data = DoubleArray(100) { 10.0 }
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(100)
        val d = TimeSeries(t, data)

        assertEquals(10.0, d.average())
        assertEquals(1_000.0, d.sum())

        assertEquals(100, d.toList().size)
        assertEquals(99, d.returns().size)
        assertEquals(1.0, d.growthRates().average())
    }

    @Test
    fun testRainy() {
        val data = DoubleArray(100) { 10.0 }
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(101)
        assertThrows<IllegalArgumentException> {
            TimeSeries(t, data)
        }
    }

    @Test
    fun methods() {
        val data = doubleArrayOf(100.0, 150.0, 200.0)
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(3)
        val ts = TimeSeries(t, data)
        val ts2 = ts.shuffle()
        assertEquals(ts.size, ts2.size)
        assertEquals(ts.average(), ts2.average())

        val ts3 = ts.index()
        assertEquals(1.0, ts3.toDoubleArray().first())
        assertEquals(2.0, ts3.toDoubleArray().last())
    }

    @Test
    fun testClean() {
        val data = doubleArrayOf(100.0, Double.NaN, 200.0)
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(3)
        val ts = TimeSeries(t, data)
        assertEquals(3, ts.size)
        val ts2 = ts.clean()
        assertEquals(2, ts2.size)
    }

    @Test
    fun testTime() {
        val data = doubleArrayOf(100.0, Double.NaN, 200.0)
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(3)
        val ts = TimeSeries(t, data)
        assertEquals(Timeframe.parse("2020-01-01", "2020-01-03", true), ts.timeframe)
    }

    @Test
    fun testOperators() {
        val data = doubleArrayOf(100.0, Double.NaN, 200.0)
        val t = Timeframe.fromYears(2020, 2021).toTimeline(1.days).take(3)
        val ts = TimeSeries(t, data)

        val ts2 = (((ts + 1.0) * 2.0) - 2.0) / 2.0
        assertEquals(100.0, ts2.values[0])
    }


}
