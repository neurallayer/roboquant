/*
 * Copyright 2020-2022 Neural Layer
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Datetime parser that parses local date-time
 *
 */
internal class LocalTimeParser(pattern: String) : TimeParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    override fun parse(text: String, exchange: Exchange): Instant {
        val dt = LocalDateTime.parse(text, dtf)
        return exchange.getInstant(dt)
    }

}

/**
 * Parser that parses local dates
 *
 * @constructor
 *
 * @param pattern
 */
internal class LocalDateParser(pattern: String) : TimeParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    /**
     * @see TimeParser.parse
     */
    override fun parse(text: String, exchange: Exchange): Instant {
        val date = LocalDate.parse(text, dtf)
        return exchange.getClosingTime(date)
    }

}