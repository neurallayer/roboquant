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

import de.siegmar.fastcsv.reader.CsvReader
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.Action
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import java.io.File
import java.io.FileReader
import java.nio.file.Path
import java.time.Instant
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

/**
 * Feed that can handle large CSV files. The difference compared to the regular [CSVFeed] is that this feed only loads
 * data from disk when required and disposes of it afterwards. So it has lower memory consumption, at the cost of
 * lower overall throughput. This feed doesn't implement the HistoricFeed interface since it doesn't know upfront
 * what the timeline will be.
 *
 * The individual lines in the CSV files are expected to be ordered from oldest to newest according to the included
 * timestamp, and timestamps are unique within a single CSV file.
 *
 * The LazyCSVFeed keeps files open to ensure better performance. So make sure your OS has enough open file descriptors
 * configured. For example, on Linux you can check this with:
 *
 *      cat /proc/sys/fs/file-max
 *
 * If you use the same large sets of CSV files regular, you might consider converting them onetime to an
 * `AvroFeed` instead. This has also low-memory usage, but comes without negative performance impact.
 *
 * @constructor
 *
 * @param pathStr The directory that contains the CSV files or a single file
 * @property config the config to use for parsing
 * @param configure any additional configuration for this feed
 */
class LazyCSVFeed internal constructor(
    pathStr: String,
    val config: CSVConfig,
    configure: CSVConfig.() -> Unit
) : AssetFeed {

    constructor(path: String, config: CSVConfig = CSVConfig.fromFile(path)) : this(path, config, {})

    constructor(path: String, configure: CSVConfig.() -> Unit) : this(path, CSVConfig.fromFile(path), configure)


    private val logger = Logging.getLogger(LazyCSVFeed::class)
    private val filesMap: Map<Asset, File>

    /**
     * Return the assets that are available in this feed
     */
    override val assets
        get() = filesMap.keys.toSortedSet()

    init {
        val path = Path.of(pathStr)
        require(path.isDirectory() || path.isRegularFile()) { "$path does not exist" }
        config.configure()
        filesMap = path.toFile()
            .walk()
            .filter { config.shouldInclude(it) }
            .map { it.absoluteFile }
            .map { config.assetBuilder.build(it) to it }
            .toMap()

        logger.info { "Scanned $path found ${filesMap.size} files" }
        if (filesMap.isEmpty()) logger.warn { "No files to process" }
    }

    /**
     * @see AssetFeed.play
     */
    override suspend fun play(channel: EventChannel) {
        var last = Instant.MIN
        val readers = filesMap.mapValues { IncrementalReader(it.key, it.value, config) }

        try {
            val queue = PriorityQueue<PriceEntry>(readers.size)

            // Initialize the queue by filling it with 1 entry from each reader
            for (entries in readers.values) {
                val entry = entries.next()
                if (entry != null) queue.add(entry)
            }

            while (queue.isNotEmpty()) {
                val now = queue.first().time
                assert(now > last) { "Found unsorted time $now in ${queue.first().price.asset}" }
                val actions = mutableListOf<Action>()
                var done = false
                while (!done) {
                    val entry = queue.firstOrNull()
                    if (entry != null && entry.time == now) {
                        actions.add(entry.price)
                        queue.remove()
                        val asset = entry.price.asset
                        val next = readers[asset]?.next()
                        if (next != null) queue.add(next)
                    } else {
                        done = true
                    }
                }
                val event = Event(actions, now)
                channel.send(event)
                last = now
            }

        } finally {
            for (reader in readers.values) {
                if (reader.errors > 0) logger.debug { "${reader.asset} has ${reader.errors} error" }
                reader.close()
            }
        }
    }

}

/**
 * Will read a line at a time of a CSV file
 */
private class IncrementalReader(val asset: Asset, file: File, val config: CSVConfig) {

    private val reader = CsvReader.builder()
        .fieldSeparator(config.separator)
        .skipEmptyRows(true)
        .build(FileReader(file)).iterator()
    var errors = 0L

    init {
        if (config.hasHeader && reader.hasNext()) {
            val line = reader.next().fields
            config.configure(line)
        }
    }

    /**
     * Return the next [PriceEntry] or null if done processing
     */
    fun next(): PriceEntry? {
        while (reader.hasNext()) {
            val line = reader.next().fields
            try {
                return config.processLine(line, asset)
            } catch (_: Throwable) {
                errors++
            }
        }
        reader.close()
        return null
    }

    fun close() = reader.close()

}
