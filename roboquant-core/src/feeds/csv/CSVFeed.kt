@file:Suppress("SimplifiableCallChain")

package org.roboquant.feeds.csv

import de.siegmar.fastcsv.reader.CsvReader
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Background
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.io.File
import java.io.FileReader
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue


/**
 * Read historic data from CSV files in a directory. It will traverse down if it finds subdirectories.
 *
 * This implementation will store the data in memory, using the more memory efficient Float32 type to limit
 * overall memory consumption. If the JVM has enough memory allocated to it (f.e -Xmx8G), even larger volumes
 * (> 5000 files with daily prices) are no issue.
 *
 * If you don't have enough memory available, consider using [LazyCSVFeed] instead.
 *
 * @constructor
 */
class CSVFeed(
    path: String,
    val config: CSVConfig = CSVConfig.fromFile(path)
) : HistoricFeed {

    private var events = listOf<Event>()
    override val assets = mutableSetOf<Asset>()
    private val logger = Logging.getLogger("CSVFeed")

    override val timeline: List<Instant>
        get() = events.map { it.now }

    init {
        val dir = File(path)
        require(dir.isDirectory) { "Directory $path does not exist" }
        val startTime = Instant.now().toEpochMilli()

        runBlocking {
            events = readFiles(path)
        }

        val duration = Instant.now().toEpochMilli() - startTime
        logger.info { "Constructed feed with ${events.size} events, ${assets.size} assets and time-frame of [${timeFrame.toPrettyString()}] in ${duration}ms" }
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
     * Merge another CSVFeed into this feed. The timelines will be combined and the actions in events merged.
     * Duplicate events as a result of the merge won't be filtered out.
     *
     * @param feed
     */
    fun merge(feed: CSVFeed) {
        val newSteps = events.associate { it.now to it.actions }.toMutableMap()
        for ((actions, now) in feed.events) {
            newSteps[now] = newSteps.getOrDefault(now, listOf()) + actions
        }
        events = newSteps.toSortedMap().toList().map { Event(it.second, it.first) }
        assets.addAll(feed.assets)
        logger.info { "Merged feed has ${events.size} events and ${assets.size} assets" }
    }


    /**
     * Read and parse CSV files in parallel to ensure fast processing of large datasets.
     *
     * @param path
     * @return
     */
    private suspend fun readFiles(path: String): List<Event> {
        val files = readPath(path)
        if (files.isEmpty()) {
            logger.warning { "Found no CSV files at $path" }
            return listOf()
        }
        logger.fine { "Found ${files.size} CSV files" }

        val result = ConcurrentHashMap<Instant, MutableList<Action>>()
        val deferredList = mutableListOf<Deferred<Unit>>()
        for (file in files) {
            val asset = config.getAsset(file.name)
            assets.add(asset)
            val deferred = Background.async {
                val steps = readFile(asset, file)
                for (step in steps) {
                    val actions = result.getOrPut(step.now) { mutableListOf() }
                    synchronized(actions) {
                        actions.add(step.price)
                    }
                }
            }
            deferredList.add(deferred)
        }
        deferredList.awaitAll()
        return result.map { Event(it.value, it.key) }.sorted()
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
            config.priceValidate && !validateResult(result) && return listOf()
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
                    logger.warning { "Validation error ${priceAction.asset.symbol} ${step.now} $diff" }
                    return false
                }
            }
            last = price
        }
        return true
    }


    /**
     * @see Feed.play
     *
     */
    override suspend fun play(channel: EventChannel) {
        for (step in events) {
            channel.send(step)
        }
    }


}


internal class PriceEntry(val now: Instant, val price: PriceAction) : Comparable<PriceEntry> {

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: PriceEntry): Int {
        return now.compareTo(other.now)
    }

}

