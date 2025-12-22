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

package org.roboquant.feeds.csv

import org.roboquant.common.ConfigurationException
import org.roboquant.common.Exchange
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
     * Initialize the parser based on the header. Default is to do nothing.
     */
    fun init(header: List<String>) {
        // Default implementation is to do nothing
    }

    /**
     * Return an [Instant] given the provided [line] of strings
     */
    fun parse(line: List<String>): Instant
}


private fun interface AuteDetectParser {

    fun parse(text: String): Instant
}

/**
 * Datetime parser that parses local date-time
 */
private class LocalTimeParser(pattern: String, val exchange: Exchange = Exchange.US) : AuteDetectParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    override fun parse(text: String): Instant {
        val dt = LocalDateTime.parse(text, dtf)
        return exchange.getInstant(dt)
    }

}

/**
 * Parser that parses local dates and uses the exchange closing time to determine the time.
 * @param pattern
 */
private class LocalDateParser(pattern: String, val exchange: Exchange = Exchange.US) : AuteDetectParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    /**
     * @see TimeParser.parse
     */
    override fun parse(text: String): Instant {
        val date = LocalDate.parse(text, dtf)
        return exchange.getClosingTime(date)
    }

}

/**
 * Auto-detect the appropriate time parser to use based on the first sample it receives.
 *
 * @constructor Create new AutoDetect time parser
 * @property exchange the exchange to use for time zone and open/close times
 */
class AutoDetectTimeParser(private var timeColumn: Int = -1, val exchange: Exchange = Exchange.US) : TimeParser {

    private lateinit var parser: AuteDetectParser

    override fun init(header: List<String>) {
        if (timeColumn != -1) return
        val notCapital = Regex("[^A-Z]")
        header.forEachIndexed { index, column ->
            when (column.uppercase().replace(notCapital, "")) {
                "TIME" -> timeColumn = index
                "DATE" -> timeColumn = index
                "DAY" -> timeColumn = index
                "DATETIME" -> timeColumn = index
                "TIMESTAMP" -> timeColumn = index
            }
        }
    }

    /**
     * @see TimeParser.parse
     */
    override fun parse(line: List<String>): Instant {
        // If this is the first time calling, detect the format and parser to use
        val text = line[timeColumn]
        if (!this::parser.isInitialized) detect(text)
        return parser.parse(text)
    }

    /**
     * @suppress
     */
    private companion object Patterns {

        @Suppress("RegExpRepeatedSpace")
        private val patterns = listOf(
            """19\d{6}""".toRegex() to LocalDateParser("yyyyMMdd"),
            """20\d{6}""".toRegex() to LocalDateParser("yyyyMMdd"),
            """\d{8} \d{6}""".toRegex() to LocalTimeParser("yyyyMMdd HHmmss"),
            """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""".toRegex() to AuteDetectParser { text -> Instant.parse(text) },
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm:ss"),
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm"),
            """\d{4}-\d{2}-\d{2}""".toRegex() to LocalDateParser("yyyy-MM-dd"),
            """\d{8} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd HH:mm:ss"),
            """\d{8}  \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss"),
            """-?\d{1,19}""".toRegex() to AuteDetectParser { text -> Instant.ofEpochMilli(text.toLong()) }
        )
    }

    /**
     * Detect on the first [sample] received what the time format is and set the parser accordingly.
     */
    private fun detect(sample: String) {
        synchronized(this) {
            if (!this::parser.isInitialized) {
                val match = patterns.firstOrNull { it.first.matches(sample) }
                    ?: throw ConfigurationException("No suitable time parser found for time=$sample")
                parser = match.second
            }
        }
    }
}

