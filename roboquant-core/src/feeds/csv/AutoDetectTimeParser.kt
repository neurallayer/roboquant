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

/**
 * Auto-detect the appropriate time parser based on the first
 * sample it receives. This is the default time parser if no config was found
 *
 * @constructor Create empty Auto c s v data time parser
 */
class AutoDetectTimeParser(val exchangeCode: String = "NASDAQ") : TimeParser {

    private lateinit var parser: TimeParser

    override fun parse(s: String): Instant {
        if (!this::parser.isInitialized) detect(s)
        return parser.parse(s)
    }


    /**
     * Detect on the first sample received what the time format is.
     *
     * @param sample
     */
    private fun detect(sample: String) {
        synchronized(this) {
            if (!this::parser.isInitialized) {

                val exchange = Exchange.getInstance(exchangeCode)

                // Map of regex and the corresponding parser for date/time string parsing
                @Suppress("RegExpRepeatedSpace")
                val matches = mutableMapOf(
                    """\d{8}""".toRegex() to LocalTimeParser("yyyyMMdd", true, exchange),
                    """\d{4}-\d{2}-\d{2}""".toRegex() to LocalTimeParser("yyyy-MM-dd", true, exchange),
                    """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z""".toRegex() to TimeParser { Instant.parse(it) },
                    """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser(
                        "yyyy-MM-dd HH:mm:ss",
                        false,
                        exchange
                    ),
                    """\d{8} \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss", false, exchange),
                    """\d{8}  \d{2}:\d{2}:\d{2}""".toRegex() to LocalTimeParser("yyyyMMdd  HH:mm:ss", false, exchange),
                    """\d{13}""".toRegex() to TimeParser { Instant.ofEpochMilli(it.toLong()) }
                )


                for (entry in matches.entries) {
                    if (entry.key.matches(sample)) {
                        parser = entry.value
                        return
                    }
                }
                throw java.lang.Exception("Unknown datetime format $sample. Please provide config.properties with time parsing instructions")
            }
        }
    }


}