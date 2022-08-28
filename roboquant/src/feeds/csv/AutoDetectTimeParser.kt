/*
 * Copyright 2022 Neural Layer
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
        if (!this::parser.isInitialized) detect(text)
        return parser.parse(text, exchange)
    }


    private companion object Patterns {

        @Suppress("RegExpRepeatedSpace")
        private val patterns = mapOf(
            """\d{8}""".toRegex() to LocalDateParser("yyyyMMdd"),
            """\d{4}-\d{2}-\d{2}""".toRegex() to LocalDateParser("yyyy-MM-dd"),
            """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""".toRegex() to TimeParser { str, _ -> Instant.parse(str) },
            """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd HH:mm:ss"),
            """\d{8} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss"),
            """\d{8}  \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss"),
            """\d{13}""".toRegex() to TimeParser { str, _ -> Instant.ofEpochMilli(str.toLong()) }
        )
    }


    /**
     * Detect on the first sample received what the time format is.
     *
     * @param sample
     */
    private fun detect(sample: String) {
        synchronized(this) {
            if (!this::parser.isInitialized) {
                parser = patterns.entries.first { it.key.matches(sample) }.value
            }
        }
    }
}

