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

package org.roboquant.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Interface for time parsers that can use an [Exchange] to support parsing logic like the time zone or opening and
 * closing times.
 */
fun interface TimeParser {

    /**
     * Return an [Instant] given the provided [text] string and [exchange]
     */
    fun parse(text: String, exchange: Exchange): Instant
}


/**
 * Datetime parser that parses local date-time
 */
private class LocalTimeParser(pattern: String) : TimeParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    override fun parse(text: String, exchange: Exchange): Instant {
        val dt = LocalDateTime.parse(text, dtf)
        return exchange.getInstant(dt)
    }

}

/**
 * Parser that parses local dates
 * @param pattern
 */
private class LocalDateParser(pattern: String) : TimeParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    /**
     * @see TimeParser.parse
     */
    override fun parse(text: String, exchange: Exchange): Instant {
        val date = LocalDate.parse(text, dtf)
        return exchange.getClosingTime(date)
    }

}


/**
 * Auto-detect the appropriate time parser based on the first sample it receives. This is the default time parser
 * if no configuration was found for the pattern to use
 *
 * @constructor Create new AutoDetect time parser
 */
class AutoDetectTimeParser : TimeParser {

    private lateinit var parser: TimeParser

    /**
     * @see TimeParser.parse
     */
    override fun parse(text: String, exchange: Exchange): Instant {
        // If this is the first time calling, lets detect the format
        if (!this::parser.isInitialized) detect(text)
        return parser.parse(text, exchange)
    }

    /**
     * @suppress
     */
    private companion object Patterns {

        @Suppress("RegExpRepeatedSpace")
        private val patterns = listOf(
            """19\d{6}""".toRegex() to LocalDateParser("yyyyMMdd"),
            """20\d{6}""".toRegex() to LocalDateParser("yyyyMMdd"),
            """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""".toRegex() to TimeParser { str, _ -> Instant.parse(str) },
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm:ss"),
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm"),
            """\d{4}-\d{2}-\d{2}""".toRegex() to LocalDateParser("yyyy-MM-dd"),
            """\d{8} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd HH:mm:ss"),
            """\d{8}  \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss"),
            """-?\d{1,19}""".toRegex() to TimeParser { str, _ -> Instant.ofEpochMilli(str.toLong()) }
        )
    }

    /**
     * Detect on the first [sample] received what the time format is.
     */
    private fun detect(sample: String) {
        synchronized(this) {
            if (!this::parser.isInitialized) {
                val match = patterns.first { it.first.matches(sample) }
                parser = match.second

            }
        }
    }
}

