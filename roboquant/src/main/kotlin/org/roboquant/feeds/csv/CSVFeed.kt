/*
 * Copyright 2020-2025 Neural Layer
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

import de.siegmar.fastcsv.reader.CsvReader
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.ParallelJobs
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.common.PriceItem
import java.io.File
import java.io.FileReader
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

/**
 * Read historic price data from CSV files in a directory. It will traverse down if it finds subdirectories. This
 * implementation is thread safe and can be shared across multiple runs at the same time.
 *
 * This implementation will store all the data in memory, using [Double] for the prices. If you don't have enough
 * memory available, consider using [LazyCSVFeed] instead.
 *
 * @param pathStr the directory that contains CSV files or a single CSV file
 * @property config the configuration to be run, default is no additional configuration. See also [CSVConfig]
 * @param configure optional modifications of the default configuration
 * @constructor
 */
class CSVFeed(
    pathStr: String,
    val config: CSVConfig,
    configure: CSVConfig.() -> Unit
) : HistoricPriceFeed() {

    private val logger = Logging.getLogger(CSVFeed::class)

    constructor(path: String, config: CSVConfig = CSVConfig.fromFile(path)) : this(path, config, {})

    constructor(path: String, configure: CSVConfig.() -> Unit) : this(path, CSVConfig.fromFile(path), configure)


    init {
        val path = Path.of(pathStr)
        require(path.isDirectory() || path.isRegularFile()) { "$path does not exist" }
        config.configure()
        runBlocking {
            readFiles(path)
        }

        logger.info { "events=${timeline.size} assets=${assets.size} timeframe=$timeframe" }
    }

    /**
     * Read a [path] (directory or single file) and all its descendants and return the found CSV files
     */
    private fun readPath(path: Path): List<File> {
        val entry = path.toFile()
        return if (entry.isFile) {
            listOf(entry)
        } else {
            entry
                .walk()
                .filter { config.shouldInclude(it) }
                .map { it.absoluteFile }
                .toList()
        }
    }

    /**
     * Read and parse CSV files in parallel from the [path] to ensure fast processing of large datasets.
     */
    private suspend fun readFiles(path: Path) {
        val files = readPath(path)
        if (files.isEmpty()) {
            logger.warn { "Found no CSV files at $path" }
            return
        }
        logger.debug { "Found ${files.size} CSV files" }

        val jobs = ParallelJobs()
        for (file in files) {
            val asset = config.assetBuilder.build(file.nameWithoutExtension)
            jobs.add {
                val steps = readFile(asset, file)
                for (step in steps) {
                    add(step.time, step.price)
                }
            }
        }
        jobs.joinAll()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun readFile(asset: Asset, file: File): List<PriceEntry> {
        val reader = CsvReader.builder()
            .fieldSeparator(config.separator)
            .skipEmptyLines(true)
            .ofCsvRecord(FileReader(file))
        reader.use {
            val result = mutableListOf<PriceEntry>()
            var errors = 0
            var isHeader = config.hasHeader
            for (row in it) {
                if (isHeader) {
                    config.configure(row.fields)
                    isHeader = false
                } else {
                    try {
                        val step = config.processLine(row.fields, asset)
                        result += step
                    } catch (t: Throwable) {
                        println(t)
                        logger.debug(t) { "${asset.symbol} $row" }
                        errors += 1
                    }
                }
            }
            if (errors > 0) logger.warn { "Skipped $errors lines due to errors in $file" }
            return result
        }
    }

}

class PriceEntry(val time: Instant, val price: PriceItem) : Comparable<PriceEntry> {

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: PriceEntry): Int {
        return time.compareTo(other.time)
    }

}

