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

import org.roboquant.common.Exchange
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Datetime parser that parses local time or dates
 *
 * @constructor
 *
 */
internal class LocalTimeParser(
    pattern: String,
    private val dateFormat: Boolean = pattern.length < 11,
    private val exchange: Exchange = Exchange.getInstance("")
) : TimeParser {

    private val dtf: DateTimeFormatter

    init {
        val zoneId = exchange.zoneId
        dtf = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    }

    override fun parse(s: String): Instant {
        return if (dateFormat) {
            val date = LocalDate.parse(s, dtf)
            exchange.getClosingTime(date)
        } else {
            ZonedDateTime.parse(s, dtf).toInstant()
        }

    }


}