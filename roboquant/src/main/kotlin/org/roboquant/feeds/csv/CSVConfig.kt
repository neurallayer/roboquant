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

import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import java.io.File
import java.nio.file.Path
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
 * @property fileExtension file extensions to parse, default is '.csv'
 * @property filePattern file patterns to take into considerations
 * @property fileSkip list of files to skip
 * @property parsePattern what do the columns present, if empty columns will be determined based on their name
 * @property priceAdjust should the price be adjusted using a volume adjusted column
 * @property template The template to use in the default [assetBuilder]
 * @constructor Create new CSV config
 */
data class CSVConfig(
    var fileExtension: String = ".csv",
    var filePattern: String = ".*",
    var fileSkip: List<String> = emptyList(),
    var parsePattern: String = "",
    var priceAdjust: Boolean = false,
    var template: Asset = Asset("TEMPLATE")
) {

    /**
     * Asset builder allows to create assets based on more than just the symbol name. The input is the file that will
     * be parsed and the returned value is a valid Asset.
     *
     * The default implementation will process the following steps:
     *
     * 1. Take the file name part of the file as the symbol name
     * 2  Remove the [fileExtension] part
     * 3. Convert to symbol name to uppercase
     * 4. Use the [template] to create the actual asset, with only the symbol name variable
     */
    var assetBuilder = this::defaultBuilder

    private val timeParser: TimeParser = AutoDetectTimeParser()
    private val info = ColumnInfo()
    private val pattern by lazy { Pattern.compile(filePattern) }
    private var hasColumnsDefined = false

    /**
     * default builder takes the file name, removes the file extension and uses that the symbol name
     */
    private fun defaultBuilder(file: File): Asset {
        val symbol = file.name.substringBefore(fileExtension).uppercase()
        return template.copy(symbol = symbol)
    }

    init {
        require(parsePattern.isEmpty() || parsePattern.length > 5)
    }

    /**
     * @suppress
     */
    internal companion object {

        private const val configFileName = "config.properties"
        private val logger = Logging.getLogger(CSVConfig::class)

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
        return file.isFile && name.endsWith(fileExtension) && pattern.matcher(name).matches() && name !in fileSkip
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
    fun merge(config: Map<String, String>) {
        val assetConfig = config.filter { it.key.startsWith("asset.") }.mapKeys { it.key.substring(6) }
        template = getAssetTemplate(assetConfig)
        for ((key, value) in config) {
            logger.debug { "Found property key=$key value=$value" }
            when (key) {
                "file.extension" -> fileExtension = value
                "file.pattern" -> filePattern = value
                "file.skip" -> fileSkip = value.split(",")
                "price.adjust" -> priceAdjust = value.toBoolean()
                "parse.pattern" -> parsePattern = value

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
    internal fun processLine(asset: Asset, line: List<String>): PriceEntry {

        val now = timeParser.parse(line[info.time], asset.exchange)
        val volume = if (info.hasVolume) line[info.volume].toDouble() else Double.NaN
        val action = PriceBar(
            asset,
            line[info.open].toDouble(),
            line[info.high].toDouble(),
            line[info.low].toDouble(),
            line[info.close].toDouble(),
            volume
        )
        if (priceAdjust) action.adjustClose(line[info.adjustedClose].toDouble())
        return PriceEntry(now, action)
    }

    /**
     * Detect columns in a CSV file. Either by a pre-defined parsePattern or else automatically based on the header
     * names provided
     *
     * @param headers
     */
    @Synchronized
    fun detectColumns(headers: List<String>) {
        if (hasColumnsDefined) return
        if (parsePattern.isNotEmpty()) info.define(parsePattern)
        else info.detectColumns(headers)
        hasColumnsDefined = true
        if (priceAdjust) require(info.adjustedClose != -1) { "No adjusted close prices found" }

    }
}



