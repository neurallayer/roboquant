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

package org.roboquant.brokers

import de.siegmar.fastcsv.reader.CsvReader
import org.roboquant.common.Config
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.div
import kotlin.io.path.notExists

@Suppress("MaxLineLength")
/**
 * Currency converter that uses the exchange reference rates as published by the ECB and that are freely available at
 * [ECB website](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html)
 *
 * It contains many daily exchange rates, and with this file loaded it is possible to trade in most currencies
 * in the world. However, please note that:
 *
 * 1. The published rates are all in relationship to the Euro. So they only go back to the introduction date of
 * the Euro and don't cover earlier periods.
 * 2. Cryptocurrencies are not included in this file.
 *
 * @constructor Create a new ECB Exchange rates converter
 */
class ECBExchangeRates(url: String, compressed: Boolean = false, useCache: Boolean = false) :
    TimedExchangeRates(Currency.EUR) {

    private val logger = Logging.getLogger(ECBExchangeRates::class)

    /**
     * @suppress
     */
    companion object {

        private const val DEFAULT_ECB_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.zip"

        /**
         * Load the latest exchange rate file directly from the ECB website. This method [uses cache][useCache] by
         * default to avoid making unnecessary requests.
         */
        fun fromWeb(useCache: Boolean = true) = ECBExchangeRates(DEFAULT_ECB_URL, compressed = true, useCache)

        /**
         * Load the ECB exchange rates from a local CSV file
         *
         * @param path
         * @param compressed
         */
        fun fromFile(path: String, compressed: Boolean = false) =
            ECBExchangeRates("file:$path", compressed = compressed)
    }

    init {
        load(url, compressed, useCache)
        logger.info {
            "loaded conversion rates for ${exchangeRates.size} currencies"
        }
    }

    /**
     * Get the cache url of an asset. If not exist cache it now.
     *
     * @param url
     * @return
     */
    private fun cache(url: URL): URL {
        val fileName = File(url.path).name
        val day = LocalDate.now().toEpochDay()
        val cacheFile = Config.home / "$day-$fileName"

        if (cacheFile.notExists()) {
            logger.debug { "Caching $url as $cacheFile " }
            url.openStream().use {
                Files.copy(it, cacheFile, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        return cacheFile.toUri().toURL()
    }

    /**
     * Load the rates from a URL, either a local file or directly from the ECB website. When
     * compressed is true, this file will be handled assumed to be in zip format and first decompressed before
     * parsed.
     *
     * The entries have only a date stamp, but according to the ECB website, they are typically published around
     * 16:00 Brussels time. So that is used to generate the final timestamps.
     *
     * @param urlString
     * @param compressed
     * @return
     */
    private fun load(urlString: String, compressed: Boolean, cache: Boolean) {
        var url = URL(urlString)

        /**
         * See if we need to cache it. We get it max once per day in that case and don't put a high load on the website
         */
        if (cache && url.protocol.uppercase().startsWith("HTTP")) {
            url = cache(url)
        }

        var input: InputStream = url.openStream()

        // Unzip the file and use the first entry as the input stream.
        if (compressed) {
            val zis = ZipInputStream(input)
            val entry = zis.nextEntry
            input = if (entry != null) {
                logger.debug { "Found file ${entry.name} from $url" }
                zis
            } else {
                logger.error { "File $urlString is not compressed" }
                input.close()
                url.openStream()
            }
        }
        val inputReader = InputStreamReader(input)
        val reader = CsvReader.builder().build(inputReader)

        val lines = reader.map { it.fields } //.toTypedArray() }
        reader.close()
        val currencies = lines.first().drop(1).filter { it.isNotBlank() }.map { Currency.getInstance(it) }

        // The timezone of the ECB
        val zoneId = ZoneOffset.UTC
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId)
        for (line in lines.drop(1)) {

            // According to the ECB website, the rates are published around 16:00 local time
            val instant = ZonedDateTime.parse(line[0] + " 16:00:00", dtf).toInstant()
            for ((i, rateStr) in line.drop(1).withIndex()) {
                try {
                    val v = 1.0 / rateStr.toDouble()
                    val k = currencies[i]
                    val map = exchangeRates.getOrPut(k) { TreeMap() }
                    map[instant] = v
                } catch (ex: NumberFormatException) {
                    // Will happen due to N/A values and trailing comma in the CSV file
                    logger.debug { "Encounter number format exception for string $rateStr" }
                }
            }
        }
    }

}