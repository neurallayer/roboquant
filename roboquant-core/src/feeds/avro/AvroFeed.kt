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

@file:Suppress("BlockingMethodInNonBlockingContext")

package org.roboquant.feeds.avro

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.avro.file.DataFileReader
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant

/**
 * Read price data from a single file in Avro format. This feed loads data lazy and disposes of it afterwards, so
 * memory footprint is low.
 *
 * Compared to CSV files, Avro files contain typed values, making the parsing more efficient. Additionally, an Avro file
 * can be compressed efficiently, reducing the overall disk space required.
 *
 * Roboquant comes with a utility to create an Avro file based on another feed, see [AvroUtil]
 *
 * @constructor Create new Avro Feed
 */
class AvroFeed(private val path: String, useIndex: Boolean = true) : HistoricFeed {

    constructor(path:Path, useIndex: Boolean = true) : this(path.toString(), useIndex)

    private val assetLookup = mutableMapOf<String, Asset>()
    internal val index = mutableListOf<Pair<Instant, Long>>()

    override val assets
        get() = assetLookup.values.toSortedSet()

    override val timeline: Timeline
        get() = index.map { it.first }

    init {
        if (useIndex) buildIndex()
        logger.info { "Loaded data from $path with timefame $timeframe" }
    }

    private fun position(r: DataFileReader<GenericRecord>, time: Instant) {
        var idx = index.binarySearch { it.first.compareTo(time) }
        idx = if (idx < 0) -idx - 1 else idx
        if (idx >= index.size) idx = index.size - 1
        if (idx >= 0) r.seek(index[idx].second)
    }

    /**
     * Build an index of where each time starts and get all assets found
     */
    private fun buildIndex() {
        index.clear()
        var last = Long.MIN_VALUE
        
        getReader().use {
            while (it.hasNext()) {
                val rec = it.next()
                val t = rec[0] as Long
                val asset = rec[1].toString()
                // if (!assetLookup.containsKey(asset)) assetLookup[asset] = Asset.deserialize(asset)
                if (!assetLookup.containsKey(asset)) assetLookup[asset] = Json.decodeFromString(asset)
                if (t > last) {
                    val pos = it.previousSync()
                    val time = Instant.ofEpochMilli(t)
                    index.add(Pair(time, pos))
                    last = t
                }
            }
        }
    }

    private fun getReader(): DataFileReader<GenericRecord> {
        val file = File(path)
        return DataFileReader(file, GenericDatumReader())
    }


    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        val timeframe = channel.timeframe
        var last = Instant.MIN
        var actions = mutableListOf<PriceAction>()

        getReader().use {
            if (index.isNotEmpty()) position(it, timeframe.start)

            while (it.hasNext()) {
                val rec = it.next()

                // Optimize unnecessary parsing of whole record
                val now = Instant.ofEpochMilli(rec[0] as Long)
                if (now < timeframe) continue

                if (now != last) {
                    if (actions.isNotEmpty()) channel.send(Event(actions, last))
                    last = now
                    actions = mutableListOf()
                }

                if (now > timeframe) break

                val assetId = rec.get(1).toString()
                val asset = assetLookup.getOrPut(assetId) { Json.decodeFromString(assetId) }
                val actionType = rec.get(2) as Int

                @Suppress("UNCHECKED_CAST")
                val values = rec.get(3) as List<Double>

                val action = when (actionType) {
                    1 -> PriceBar.fromValues(asset, values)
                    2 -> TradePrice.fromValues(asset, values)
                    3 -> PriceQuote.fromValues(asset, values)
                    4 -> OrderBook.fromValues(asset, values)
                    else -> {
                        throw Exception("unsupported price action found")
                    }
                }

                actions.add(action)
            }
            if (actions.isNotEmpty()) channel.send(Event(actions, last))
        }
    }

    companion object {
        private val logger = Logging.getLogger(AvroFeed::class)
        private const val sp500File = "5yr_sp500_v2.0.avro"
        private const val smallFile = "us_small_daily_v2.0.avro"

        /**
         * 5 years worth of end of day [PriceBar] data for the companies listed in the S&P 500
         */
        fun sp500(): AvroFeed {
            val path = download(sp500File)
            return AvroFeed(path.toString())
        }

        /**
         * Small avro file with end of day [PriceBar] data 6 us stocks: AAPL, AMZN, TSLA, IBM, JNJ and JPM
         */
        fun usTest(): AvroFeed {
            val path = download(smallFile)
            return AvroFeed(path.toString())
        }

        private fun download(fileName: String): Path {
            val path: Path = Paths.get(Config.home.toString(), fileName)
            if (Files.notExists(path)) {
                val url = "https://github.com/neurallayer/roboquant-data/blob/main/avro/$fileName?raw=true"
                logger.info("Downloading data from $url...")
                val website = URL(url)
                website.openStream().use { inputStream: InputStream ->
                    Files.copy(
                        inputStream,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
                require(Files.exists(path))
            }
            return path
        }


    }

}

