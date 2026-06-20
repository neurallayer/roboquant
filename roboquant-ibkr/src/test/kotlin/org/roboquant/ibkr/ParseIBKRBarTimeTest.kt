/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.ibkr

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseIBKRBarTimeTest {

    @Test
    fun `intraday with timezone suffix converts correctly`() {
        // 09:30 US/Eastern (EDT, UTC-4) = 13:30 UTC
        val result = parseIBKRBarTime("20260320 09:30:00 US/Eastern")
        assertEquals(Instant.parse("2026-03-20T13:30:00Z"), result)
    }

    @Test
    fun `intraday without timezone suffix treated as UTC`() {
        val result = parseIBKRBarTime("20260320 14:30:00")
        assertEquals(Instant.parse("2026-03-20T14:30:00Z"), result)
    }

    @Test
    fun `intraday with standard timezone offset suffix`() {
        // 09:30 US/Eastern in winter (EST, UTC-5) = 14:30 UTC
        val result = parseIBKRBarTime("20260120 09:30:00 US/Eastern")
        assertEquals(Instant.parse("2026-01-20T14:30:00Z"), result)
    }

}
