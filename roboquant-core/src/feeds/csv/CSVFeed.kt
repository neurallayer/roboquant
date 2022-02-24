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

import de.siegmar.fastcsv.reader.CsvReader
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Background
import org.roboquant.common.Logging
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceAction
import java.io.File
import java.io.FileReader
import java.time.Instant
import kotlin.math.absoluteValue


/**
 * Read historic data from CSV files in a directory. It will traverse down if it finds subdirectories.
 *
 * This implementation will store the data in memory, using Double type to limit overall memory consumption. If you
 * don't have enough memory available, consider using [LazyCSVFeed] instead.
 *
 * @constructor
 */
class CSVFeed(
    path: String,
    val config: CSVConfig = CSVConfig.fromFile(path)
) : HistoricPriceFeed() {


    private val logger = Logging.getLogger(CSVFeed::class)


    init {
        val dir = File(path)
        require(dir.isDirectory) { "Directory $path does not exist" }
        val startTime = Instant.now().toEpochMilli()

        runBlocking {
            readFiles(path)
        }

        val duration = Instant.now().toEpochMilli() - startTime
        logger.info { "Constructed feed with ${timeline.size} events, ${assets.size} assets and timeframe of [${timeframe.toPrettyString()}] in ${duration}ms" }
    }


    /**
     * Read a directory or file and all its descendants and return the found CSV files
     *
     * @param path
     * @return
     */
    private fun readPath(path: String): List<File> {
        val entry = File(path)
        return if (entry.isFile) {
            listOf(entry)
        } else {
            entry
                .walk()
                .filter { config.shouldParse(it) }
                .map { it.absoluteFile }
                .toList()
        }
    }



    /**
     * Read and parse CSV files in parallel to ensure fast processing of large datasets.
     *
     * @param path
     * @return
     */
    private suspend fun readFiles(path: String) {
        val files = readPath(path)
        if (files.isEmpty()) {
            logger.warning { "Found no CSV files at $path" }
            return
        }
        logger.fine { "Found ${files.size} CSV files" }

        val deferredList = mutableListOf<Deferred<Unit>>()
        for (file in files) {
            val asset = config.getAsset(file.name)
            assets.add(asset)
            val deferred = Background.async {
                val steps = readFile(asset, file)
                for (step in steps) {
                    add(step.time, step.price)
                }
            }
            deferredList.add(deferred)
        }
        deferredList.awaitAll()
    }


    private fun readFile(asset: Asset, file: File): List<PriceEntry> {
        val reader = CsvReader.builder().skipEmptyRows(true).build(FileReader(file))
        reader.use {
            val result = mutableListOf<PriceEntry>()
            var errors = 0
            var first = true
            for (row in it) {
                if (first) {
                    config.detectColumns(row.fields)
                    first = false
                } else {
                    try {
                        val step = config.processLine(asset, row.fields)
                        result += step
                    } catch (e: Exception) {
                        logger.fine { "${asset.symbol} $row" }
                        errors += 1
                    }
                }
            }
            if (errors > 0) logger.info { "Skipped $errors lines due to errors in $file" }
            config.priceValidate && !validateResult(result) && return emptyList()
            return result
        }
    }


    /**
     * Validate if the result for an asset has unusual movements in price.
     * This could be an indication of incorrect or corrupt historic data.
     *
     * @param result
     * @return
     */
    private fun validateResult(result: List<PriceEntry>): Boolean {
        var last: Double? = null
        for (step in result) {
            val priceAction = step.price
            val price = priceAction.getPrice()
            if (last != null) {
                val diff = (price - last) / last
                if (diff.absoluteValue > config.priceThreshold) {
                    logger.warning { "Validation error ${priceAction.asset.symbol} ${step.time} $diff" }
                    return false
                }
            }
            last = price
        }
        return true
    }


}


internal class PriceEntry(val time: Instant, val price: PriceAction) : Comparable<PriceEntry> {

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: PriceEntry): Int {
        return time.compareTo(other.time)
    }

}

