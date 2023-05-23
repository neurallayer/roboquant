/*
 * Copyright 2020-2023 Neural Layer
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

    fun init(header: List<String>, config: CSVConfig) {}

    /**
     * Return an [Instant] given the provided [line] of strings and [asset]
     */
    fun parse(line: List<String>, asset: Asset): Instant
}


private fun interface AuteDetectParser {

    fun parse(text: String, exchange: Exchange): Instant
}

/**
 * Datetime parser that parses local date-time
 */
private class LocalTimeParser(pattern: String) : AuteDetectParser {

    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

    override fun parse(text: String, exchange: Exchange): Instant {
        val dt = LocalDateTime.parse(text, dtf)
        return exchange.getInstant(dt)
    }

}

/**
 * Parser that parses local dates and uses the exchange closing time to determine the time.
 * @param pattern
 */
private class LocalDateParser(pattern: String) : AuteDetectParser {

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
 * Auto-detect the appropriate time parser to use based on the first sample it receives.
 *
 * @constructor Create new AutoDetect time parser
 */
class AutoDetectTimeParser : TimeParser {

    private lateinit var parser: AuteDetectParser
    private var time = 0

    override fun init(header: List<String>, config: CSVConfig) {
        val notCapital = Regex("[^A-Z]")
        header.forEachIndexed { index, column ->
            when (column.uppercase().replace(notCapital, "")) {
                "TIME" -> time = index
                "DATE" -> time = index
                "DAY" -> time = index
                "DATETIME" -> time = index
                "TIMESTAMP" -> time = index
            }
        }
    }

    /**
     * @see TimeParser.parse
     */
    override fun parse(line: List<String>, asset: Asset): Instant {
        // If this is the first time calling, detect the format and parser to use
        val text = line[time]
        if (!this::parser.isInitialized) detect(text)
        return parser.parse(text, asset.exchange)
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
            """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""".toRegex() to AuteDetectParser { text, _ -> Instant.parse(text) },
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm:ss"),
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm"),
            """\d{4}-\d{2}-\d{2}""".toRegex() to LocalDateParser("yyyy-MM-dd"),
            """\d{8} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd HH:mm:ss"),
            """\d{8}  \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss"),
            """-?\d{1,19}""".toRegex() to AuteDetectParser { text, _ -> Instant.ofEpochMilli(text.toLong()) }
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

