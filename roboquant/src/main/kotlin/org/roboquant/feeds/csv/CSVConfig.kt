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

import org.roboquant.common.*
import org.roboquant.common.Currency
import java.io.File
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.io.path.div

/**
 * Define the configuration to use when parsing CSV files. There three levels of configuration:
 *
 * 1) Default config that will be applied if nothing else is provided
 * 2) The config.properties that will be added and override the default config
 * 3) The config provided as a parameter to the Feed constructor that will add/override the previous step
 *
 * @property filePattern file patterns to take into considerations
 * @property fileSkip list of files to skip
 * @property hasHeader do the CSV files have a header, default is true
 * @property separator the field separator character, default is ',' (comma)
 * @property timeParser the parser to use for parsing the time column(s)
 * @property priceParser the parser to use for parsing the price and volume column(s)
 * @property assetBuilder the builder to use to create the assets of the price-actions
 * @constructor Create a new CSV config
 */
data class CSVConfig(
    var filePattern: String = ".*.csv",
    var fileSkip: List<String> = emptyList(),
    var hasHeader: Boolean = true,
    var separator: Char = ',',
    var timeParser: TimeParser = AutoDetectTimeParser(),
    var priceParser: PriceParser = PriceBarParser(),
    var assetBuilder: AssetBuilder = StockBuilder()
) {

    private val pattern by lazy { Pattern.compile(filePattern) }

    private var isInitialized = false

    /**
     * @suppress
     */
    companion object {

        private const val CONFIG_FILE = "config.properties"
        private val logger = Logging.getLogger(CSVConfig::class)

        /**
         * Returns a CSVConfig suited for parsing stooq.pl CSV files
         */
        fun stooq(): CSVConfig {

            return CSVConfig(
                filePattern = ".*.txt",
                timeParser = AutoDetectTimeParser(2),
                assetBuilder = { name: String ->
                    Stock(
                        name.removeSuffix(".us.txt").removeSuffix(".us").replace('-', '.').uppercase(),
                        Currency.USD
                    )
                }
            )
        }

        /**
         * Returns a CSVConfig suited for parsing MT5 CSV files
         *
         * @param priceQuote does the CSV file contain quotes, default is false (meaning price-bars are implied)
         * @param timeSpan the timespan to use when creating price bars, default is null
         */
        fun mt5(
            priceQuote: Boolean = false,
            timeSpan: TimeSpan? = null
        ): CSVConfig {

            fun assetBuilder(name: String): Asset {
                val symbol = name.split('_').first().uppercase()
                return Stock(symbol, Currency.USD)
            }

            val dtf = if (priceQuote)
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS")
            else
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

            val priceParser =
                if (priceQuote) PriceQuoteParser(3, 2) else PriceBarParser(2, 3, 4, 5, 6, timeSpan = timeSpan)

            fun parse(line: List<String>): Instant {
                val text = line[0] + " " + line[1]
                val dt = LocalDateTime.parse(text, dtf)
                return dt.toInstant(ZoneOffset.UTC)
            }

            return CSVConfig(
                assetBuilder = { assetBuilder(it) },
                separator = '\t',
                timeParser = { a -> parse(a) },
                priceParser = priceParser
            )

        }

        /**
         * Returns a CSVConfig suited for parsing HistData.com ASCII CSV files
         */
        fun histData(): CSVConfig {
            val result = CSVConfig(
                priceParser = PriceBarParser(1, 2, 3, 4),
                timeParser = AutoDetectTimeParser(0),
                separator = ';',
                hasHeader = false,
                assetBuilder = { name: String ->
                    val symbol = name.split('_')[2]
                    val currency = symbol.toCurrencyPair()!!
                    Forex(symbol, currency.second)
                }
            )
            return result
        }

        /**
         * Returns a CSVConfig suited for parsing Yahoo Finance ASCII CSV files
         *
         */
        fun yahoo(currency: Currency = Currency.USD): CSVConfig {
            val result = CSVConfig(
                priceParser = PriceBarParser(1, 2, 3, 4, autodetect = false),
                timeParser = { columns -> Instant.parse(columns[0]) },
                separator = ',',
                hasHeader = true,
                assetBuilder = { name: String ->
                    val symbol = name.split(' ')[0].uppercase()
                    Stock(symbol = symbol, currency)
                }
            )
            return result
        }

        /**
         * Returns a CSVConfig suited for parsing Kraken CSV trade files.
         *
         * Be aware that these trades are not aggregated, so a feed based on these files can have multiple events for
         * the same asset at the same time.
         */
        fun kraken(): CSVConfig {
            val result = CSVConfig(
                priceParser = TradePriceParser(1, 2),
                timeParser = { columns -> Instant.ofEpochSecond(columns[0].toLong()) },
                hasHeader = false,
                assetBuilder = { name: String ->
                    val currencyPair = name.toCurrencyPair()!!
                    Crypto(name, currencyPair.second)
                }
            )
            return result
        }

        /**
         * Read a CSV configuration from a [path]. It will use the standard config as base and merge all the
         * additional settings found in the config file (if any).
         */
        fun fromFile(path: String): CSVConfig {
            val result = CSVConfig()
            val cfg = readConfigFile(Path.of(path))
            result.merge(cfg)
            return result
        }

        /**
         * Read properties from config file [path] is it exist.
         */
        private fun readConfigFile(path: Path): Map<String, String> {
            val filePath = path / CONFIG_FILE
            val file = filePath.toFile()
            val prop = Properties()
            if (file.exists()) {
                logger.debug { "Found configuration file $file" }
                prop.load(file.inputStream())
                logger.trace { prop.toString() }
            }
            return prop.map { it.key.toString() to it.value.toString() }.toMap()
        }

    }

    /**
     * Returns true is the provided [file] be included, false otherwise
     */
    fun shouldInclude(file: File): Boolean {
        val name = file.name
        return file.isFile && pattern.matcher(name).matches() && name !in fileSkip
    }

    /**
     * Merge a config map into this CSV config
     *
     * @param config
     */
    private fun merge(config: Map<String, String>) {
        for ((key, value) in config) {
            logger.debug { "Found property key=$key value=$value" }
            when (key) {
                "file.pattern" -> filePattern = value
                "file.skip" -> fileSkip = value.split(",")
            }
        }

    }

    /**
     * Process a single line and return a PriceEntry (if the line could be parsed). Otherwise, an exception will
     * be thrown.
     *
     * @param asset
     * @param line
     * @return
     */
    fun processLine(line: List<String>, asset: Asset): PriceEntry {
        val now = timeParser.parse(line)
        val action = priceParser.parse(line, asset)
        return PriceEntry(now, action)
    }

    /**
     * Configure the time & price parsers based on the provided header
     *
     * @param header the header fields
     */
    @Synchronized
    fun configure(header: List<String>) {
        if (isInitialized) return
        timeParser.init(header)
        priceParser.init(header)
        isInitialized = true
    }
}



