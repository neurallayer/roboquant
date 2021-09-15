@file:Suppress("SimplifiableCallChain")

package org.roboquant.feeds.csv

import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

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
    var fileSkip: List<String> = listOf(),

    var assetExchange: String = "",

    var parsePattern: String = "",
    var parseTime: String = "",
    var parseIsDate: Boolean = true,
    var priceValidate: Boolean = false,
    var priceThreshold: Double = 0.5,
    var priceAdjust: Boolean = false,
    var skipZeroPrice: Boolean = true,
    var assetBuilder: AssetBuilder = AssetBuilder.getInstance(AssetType.STOCK),
    var assetCurrency: String = ""
) {

    private val dateTimeParser: TimeParser by lazy {
        if (parseTime.isEmpty()) AutoDetectTimeParser() else LocalTimeParser(parseTime)
    }


    private val info = ColumnInfo()
    private val pattern by lazy {  Pattern.compile(filePattern) }
    private var hasColumnsDefined = false

    val exchange
        get() = Exchange.getInstance(assetExchange)

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
        private val logger = Logging.getLogger("CSVConfig")

        fun fromFile(path: String): CSVConfig {
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
        private fun readConfigFile(path: String): Map<String, String> {
            val filePath = Paths.get(path, configFileName)
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

    fun getSymbol(fileName: String) = fileName.substringBefore(fileExtension).uppercase()


    /**
     * Should the provided file be parsed
     *
     * @param file
     */
    fun shouldParse(file: File): Boolean {
        val name = file.name
        return file.isFile && name.endsWith(fileExtension) && pattern.matcher(name).matches() && name !in fileSkip
    }


    /**
     * Merge a config map into this CSV config
     *
     * @param config
     */
    fun merge(config: Map<String, String>) {
        for ((key, value) in config) {
            when (key) {
                "file.extension" -> fileExtension = value
                "file.pattern" -> filePattern = value
                "file.skip" -> fileSkip = value.split(",")
                "asset.type" -> assetBuilder = AssetBuilder.getInstance(AssetType.valueOf(value))
                "asset.exchange" -> assetExchange = value
                "asset.currency" -> assetCurrency = value
                "price.adjust" -> priceAdjust = value.toBoolean()
                "price.validate" -> priceValidate = value.toBoolean()
                "price.threshold" -> priceThreshold = value.toDouble()
                else -> {
                    logger.warning("Found unknown property $key, skipping it.")
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

        val now = dateTimeParser.parse(line[info.time])
        val volume = if (info.hasVolume) line[info.volume].toFloat() else Float.NaN
        val action: PriceAction = when {
            priceAdjust -> PriceBar.fromAdjustedClose(
                asset,
                line[info.open].toFloat(),
                line[info.high].toFloat(),
                line[info.low].toFloat(),
                line[info.adjustedClose].toFloat(),
                line[info.volume].toFloat()
            )
            else -> PriceBar(
                asset,
                line[info.open].toFloat(),
                line[info.high].toFloat(),
                line[info.low].toFloat(),
                line[info.close].toFloat(),
                volume
            )
        }
        // Skip the price action if the open price is zero. This is an often occurring data issue
        if (skipZeroPrice && action.getPrice("OPEN") == 0.0) throw Exception("Found zero price")

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



