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

@file:Suppress("SimplifiableCallChain")

package org.roboquant.feeds.csv

import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.feeds.AssetBuilderFactory
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern
import kotlin.io.path.div

/**
 * Takes care of the configuration of the CSV parsing. There three steps:
 *
 * 1) Default config that will be applied if nothing else is provided
 * 2) The config.properties that can add and override the default config
 * 3) The config provided as a parameter that can override the previous step
 *
 * @constructor
 *
 */
data class CSVConfig(
    var fileExtension: String = ".csv",
    var filePattern: String = ".*",
    var fileSkip: List<String> = emptyList(),
    var parsePattern: String = "",
    var parseTime: String = "",
    var parseIsDate: Boolean = true,
    var priceValidate: Boolean = false,
    var priceThreshold: Double = 0.5,
    var priceAdjust: Boolean = false,
    var skipZeroPrice: Boolean = true,
    var assetCurrency: String = "",
    var template: Asset = Asset("TEMPLATE")
) {

    private val timeParser: TimeParser by lazy {
        when {
            parseTime.isEmpty() -> AutoDetectTimeParser()
            parseTime.length < 11 -> LocalDateParser(parseTime)
            else -> LocalTimeParser(parseTime)
        }

    }

    private val info = ColumnInfo()
    private val pattern by lazy { Pattern.compile(filePattern) }
    private var hasColumnsDefined = false

    val exchange
        get() = template.exchange

    val currency: Currency
        get() = if (assetCurrency.isNotEmpty())
            Currency.getInstance(assetCurrency)
        else
            exchange.currency


    init {
        require(parsePattern.isEmpty() || parsePattern.length > 5)
    }

    companion object {

        private const val configFileName = "config.properties"
        private val logger = Logging.getLogger(CSVConfig::class)

        fun fromFile(path: Path): CSVConfig {
            val result = CSVConfig()
            val cfg = readConfigFile(path)
            result.merge(cfg)
            return result
        }


        /**
         * Read properties from config file is it exist.
         *
         * @param path
         * @return
         */
        private fun readConfigFile(path: Path): Map<String, String> {
            val filePath = path / configFileName
            val file = filePath.toFile()
            val prop = Properties()
            if (file.exists()) {
                logger.fine { "Found configuration file $file" }
                prop.load(file.inputStream())
                logger.finer { prop.toString() }
            }
            return prop.map { it.key.toString() to it.value.toString() }.toMap()
        }

    }

    fun getAsset(fileName: String): Asset {
        val name = fileName.substringBefore(fileExtension).uppercase()
        return AssetBuilderFactory.build(name, template)
    }


    /**
     * Should the provided [file] be parsed or skipped all together, true is parsed
     */
    fun shouldParse(file: File): Boolean {
        val name = file.name
        return file.isFile && name.endsWith(fileExtension) && pattern.matcher(name).matches() && name !in fileSkip
    }


    private fun getTemplateAsset(config: Map<String, String>): Asset {
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
        val newAssetConfig = config.filter { it.key.startsWith("asset.") }.mapKeys { it.key.substring(6) }
        template = getTemplateAsset(newAssetConfig)
        for ((key, value) in config) {
            when (key) {
                "file.extension" -> fileExtension = value
                "file.pattern" -> filePattern = value
                "file.skip" -> fileSkip = value.split(",")
                "price.adjust" -> priceAdjust = value.toBoolean()
                "price.validate" -> priceValidate = value.toBoolean()
                "price.threshold" -> priceThreshold = value.toDouble()
                else -> {
                    logger.finer { "Found property $key with value $value" }
                }
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
        val action: PriceAction = when {
            priceAdjust -> PriceBar.fromAdjustedClose(
                asset,
                line[info.open].toDouble(),
                line[info.high].toDouble(),
                line[info.low].toDouble(),
                line[info.adjustedClose].toDouble(),
                line[info.volume].toDouble()
            )
            else -> PriceBar(
                asset,
                line[info.open].toDouble(),
                line[info.high].toDouble(),
                line[info.low].toDouble(),
                line[info.close].toDouble(),
                volume
            )
        }
        // Skip the price action if the open price is zero. This is an often occurring data issue
        if (skipZeroPrice && action.getPrice("OPEN") == 0.0) throw ValidationException("Found zero price")

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
        if (parsePattern.isNotEmpty())
            info.define(parsePattern)
        else
            info.detectColumns(headers)
        hasColumnsDefined = true
        if (priceAdjust) require(info.adjustedClose != -1) { "No adjusted close prices found" }

    }
}



