/*
 * Copyright 2020-2023 Neural Layer
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

import org.roboquant.common.*
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
 * @property template The template to use in the default [assetBuilder]
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
    var template: Asset = Asset("TEMPLATE"),
    var hasHeader: Boolean = true,
    var separator: Char = ',',
    var timeParser: TimeParser = AutoDetectTimeParser(),
    var priceParser: PriceParser = PriceBarParser(),
    var assetBuilder: AssetBuilder = DefaultAssetBuilder(template)
) {

    private val pattern by lazy { Pattern.compile(filePattern) }

    private var isInitialized = false

    /**
     * @suppress
     */
    companion object {

        private const val configFileName = "config.properties"
        private val logger = Logging.getLogger(CSVConfig::class)

        /**
         * Returns a CSVConfig suited for parsing stooq.com CSV files
         */
        fun forStooq(template: Asset = Asset("TEMPLATE")): CSVConfig {
            fun file2Symbol(file: File): String {
                return file.name.removeSuffix(".us.txt").replace('-', '.').uppercase()
            }

            return CSVConfig(
                filePattern = ".*.txt",
                timeParser = AutoDetectTimeParser(2),
                assetBuilder = { file: File -> template.copy(symbol = file2Symbol(file)) }
            )
        }


        /**
         * Returns a CSVConfig suited for parsing MT5 CSV files
         */
        fun forMT5(template: Asset = Asset("TEMPLATE"), priceQuote: Boolean = false) : CSVConfig {

            fun assetBuilder(file: File): Asset {
                val symbol = file.name.split('_').first().uppercase()
                return template.copy(symbol = symbol)
            }

            val dtf = if (priceQuote)
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS")
            else
                DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

            val priceParser = if (priceQuote) PriceQuoteParser(3,2) else PriceBarParser(2,3,4,5,6)

            fun parse(line: List<String>): Instant {
                val text = line[0] + " " + line[1]
                val dt = LocalDateTime.parse(text, dtf)
                return dt.toInstant(ZoneOffset.UTC)
            }

            return CSVConfig (
                assetBuilder = { assetBuilder(it) } ,
                separator = '\t',
                timeParser = { a, _ -> parse(a) },
                priceParser = priceParser
            )

        }

        /**
         * Returns a CSVConfig suited for parsing HistData.com ASCII CSV files
         */
        fun forHistData(): CSVConfig {
            val result = CSVConfig (
                priceParser = PriceBarParser(1,2,3,4),
                timeParser = AutoDetectTimeParser(0),
                separator = ';',
                hasHeader = false,
                assetBuilder = { file: File ->
                    val currencyPair = file.name.split('_')[2]
                    Asset.forexPair(currencyPair)
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
        fun forKraken(): CSVConfig {
            val exchange = Exchange.CRYPTO
            val result = CSVConfig (
                priceParser = TradePriceParser(1,2),
                timeParser =  { columns, _ -> Instant.ofEpochSecond(columns[0].toLong()) },
                hasHeader = false,
                assetBuilder = { file: File ->
                    val currencyPair = file.nameWithoutExtension.toCurrencyPair()!!
                    Asset.crypto(
                        currencyPair.first.currencyCode,
                        currencyPair.second.currencyCode,
                        exchange.exchangeCode
                    )
                }
            )
            return result
        }


        /**
         * Read a CSV configuration from a [path]. It will use the standard config as base and merge all the
         * additional settings found in the config file (if any).
         */
        internal fun fromFile(path: Path): CSVConfig {
            val result = CSVConfig()
            val cfg = readConfigFile(path)
            result.merge(cfg)
            return result
        }


        /**
         * Read properties from config file [path] is it exist.
         */
        private fun readConfigFile(path: Path): Map<String, String> {
            val filePath = path / configFileName
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
     * Should the provided [file] be parsed or skipped all together, true is parsed
     */
    internal fun shouldParse(file: File): Boolean {
        val name = file.name
        return file.isFile && pattern.matcher(name).matches() && name !in fileSkip
    }

    private fun getAssetTemplate(config: Map<String, String>): Asset {
        return Asset(
            symbol = config.getOrDefault("symbol", "TEMPLATE"),
            type = AssetType.valueOf(config.getOrDefault("type", "STOCK")),
            currencyCode = config.getOrDefault("currency", "USD"),
            exchangeCode = config.getOrDefault("exchange", ""),
            multiplier = config.getOrDefault("multiplier", "1.0").toDouble()
        )
    }

    /**
     * Merge a config map into this CSV config
     *
     * @param config
     */
    private fun merge(config: Map<String, String>) {
        val assetConfig = config.filter { it.key.startsWith("asset.") }.mapKeys { it.key.substring(6) }
        template = getAssetTemplate(assetConfig)
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
    internal fun processLine(line: List<String>, asset: Asset): PriceEntry {
        val now = timeParser.parse(line, asset)
        val action = priceParser.parse(line, asset)
        return PriceEntry(now, action)
    }

    /**
     * Configure the time & price parsers based on the provided header
     *
     * @param header the header fields
     */
    @Synchronized
    internal fun configure(header: List<String>) {
        if (isInitialized) return
        timeParser.init(header)
        priceParser.init(header)
        isInitialized = true
    }
}



