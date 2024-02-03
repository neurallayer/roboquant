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

package org.roboquant.feeds.csv

import org.roboquant.common.Asset
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals


internal class TimeParserTest {

    private fun parse(time: String): Instant {
        val tp = AutoDetectTimeParser(0)
        val asset = Asset("TEST")
        return tp.parse(listOf(time), asset)
    }

    @Test
    fun autoDetectCurrent() {
        val result = Instant.parse("2022-01-04T21:00:00Z")
        assertEquals(result, parse("20220104"))
        assertEquals(result, parse("2022-01-04"))
        assertEquals(result, parse("2022-01-04T21:00:00Z"))
        assertEquals(result, parse("2022-01-04 16:00:00"))
        assertEquals(result, parse("20220104 16:00:00"))
        assertEquals(result, parse("20220104  16:00:00"))
        assertEquals(result, parse(result.toEpochMilli().toString()))
    }

    @Test
    fun autoDetectOld() {
        val result = Instant.parse("1900-01-01T21:00:00Z")
        assertEquals(result, parse("19000101"))
        assertEquals(result, parse("1900-01-01"))
        assertEquals(result, parse("1900-01-01T21:00:00Z"))
        assertEquals(result, parse("1900-01-01 16:00:00"))
        assertEquals(result, parse("19000101 16:00:00"))
        assertEquals(result, parse("19000101  16:00:00"))
        assertEquals(result, parse(result.toEpochMilli().toString()))
    }

    @Test
    fun autoDetectNew() {
        val result = Instant.parse("2099-01-01T21:00:00Z")
        assertEquals(result, parse("20990101"))
        assertEquals(result, parse("2099-01-01"))
        assertEquals(result, parse("2099-01-01T21:00:00Z"))
        assertEquals(result, parse("2099-01-01 16:00:00"))
        assertEquals(result, parse("20990101 16:00:00"))
        assertEquals(result, parse("20990101  16:00:00"))
        assertEquals(result, parse(result.toEpochMilli().toString()))
    }


}
