/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.feeds.avro

import org.apache.avro.file.DataFileReader
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*
import kotlin.io.path.isRegularFile


/**
 * Read price data from a single file in Avro format. This feed loads data lazy and disposes of it afterwards, so
 * memory footprint is low. Compared to CSV files, Avro files are parsed more efficient, making it a good fit for large
 * back tests. Additionally, an Avro file can be compressed, reducing the overall disk space required.
 *
 * When the feed is instantiated, it will create an internal index for faster random access. Please note that
 * currently the internal resolution is milliseconds.
 *
 * @property path the path where the Avro file can be found
 *
 * @constructor Create new Avro Feed
 */
class AvroFeed(private val path: Path, useCache: Boolean = false) : AssetFeed {

    /**
     * Instantiate an Avro Feed based on the Avro file at [path]
     */
    constructor(path: String) : this(Path.of(path))

    /**
     * Contains mapping of a serialized Asset string to an Asset
     */
    private val assetLookup: Map<String, Asset>

    /**
     * MetadataProvider that holds time/position for quicker access rows
     * in Avro file.
     */
    private val index: List<Pair<Instant, Long>>

    /**
     * @see Feed.timeframe
     */
    override val timeframe: Timeframe

    /**
     * Get available assets.
     */
    override val assets: SortedSet<Asset>
        get() = assetLookup.values.toSortedSet()


    init {
        assert(path.isRegularFile()) { "$path is not a file" }
        val metadataProvider = MetadataProvider(path)

        val metadata = metadataProvider.build(useCache)
        this.index = metadata.index
        timeframe = metadata.timeframe
        assetLookup = metadata.assets

        logger.info { "loaded feed with timeframe=$timeframe" }
    }

    private fun position(r: DataFileReader<GenericRecord>, time: Instant) {
        val idx = index.binarySearch { it.first.compareTo(time) }
        when {
            idx > 0 -> r.seek(index[idx - 1].second)
            idx < -1 -> r.seek(index[-idx - 2].second)
        }
    }


    private fun getReader(): DataFileReader<GenericRecord> {
        return DataFileReader(path.toFile(), GenericDatumReader())
    }

    /**
     * Convert a generic Avro record to a [PriceAction]
     */
    private fun recToPriceAction(rec: GenericRecord, serializer: PriceActionSerializer): PriceAction {
        val assetStr = rec.get(1).toString()
        val asset = assetLookup.getValue(assetStr)
        val actionType = rec.get(2) as Int

        @Suppress("UNCHECKED_CAST")
        val values = rec.get(3) as List<Double>

        return if (rec.hasField("other")) {
            val other = rec.get("other") as Utf8?
            serializer.deserialize(asset, actionType, values, other?.toString())
        } else {
            serializer.deserialize(asset, actionType, values, null)
        }
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
        var actions = ArrayList<PriceAction>()
        val serializer = PriceActionSerializer()

        getReader().use {
            position(it, timeframe.start)

            while (it.hasNext()) {
                val rec = it.next()

                // Optimize unnecessary parsing of the whole record
                val now = Instant.ofEpochMilli(rec[0] as Long)
                if (now < timeframe) continue

                if (now != last) {
                    if (actions.isNotEmpty()) channel.send(Event(actions, last))
                    last = now
                    actions = ArrayList<PriceAction>(actions.size)
                }

                if (now > timeframe) break

                val action = recToPriceAction(rec, serializer)
                actions.add(action)
            }
            if (actions.isNotEmpty()) channel.send(Event(actions, last))
        }
    }

    /**
     * Standard set of Avro feeds that come with roboquant and will be downloaded the first time when invoked. They are
     * stored at <User.Home>/.roboquant and reused from there later on.
     */
    companion object {

        internal val logger = Logging.getLogger(AvroFeed::class)
        private const val SP500FILE = "sp500_pricebar_v5.1.avro"
        private const val SP500QUOTEFILE = "sp500_pricequote_v5.0.avro"
        private const val FOREXFILE = "forex_pricebar_v5.1.avro"


        /**
         * Get an AvroFeed containing end-of-day [PriceBar] data for the companies listed in the S&P 500. This feed
         * contains a few years of public data.
         *
         * Please note that not all US exchanges are included, so the prices are not 100% accurate.
         */
        fun sp500(): AvroFeed {
            val path = download(SP500FILE)
            return AvroFeed(path)
        }

        /**
         * Get an AvroFeed containing [PriceQuote] data for the companies listed in the S&P 500. This feed contains
         * a few minutes of public data.
         *
         * Please note that not all US exchanges are included, so the prices are not 100% accurate.
         */
        fun sp500Quotes(): AvroFeed {
            val path = download(SP500QUOTEFILE)
            return AvroFeed(path)
        }

        /**
         * Get an AvroFeed containing 1 minute [PriceBar] data for an EUR/USD currency pair.
         */
        fun forex(): AvroFeed {
            val path = download(FOREXFILE)
            return AvroFeed(path)
        }

        /**
         * Download a file from GitHub if now yet present on the local file system.
         */
        private fun download(fileName: String): Path {
            val path: Path = Paths.get(Config.home.toString(), fileName)
            if (Files.notExists(path)) {
                val url = "https://github.com/neurallayer/roboquant-data/blob/main/avro/$fileName?raw=true"
                logger.info("Downloading data from $url...")
                val website = URL(url)
                website.openStream().use { inputStream: InputStream ->
                    Files.copy(
                        inputStream, path, StandardCopyOption.REPLACE_EXISTING
                    )
                }
                require(Files.exists(path))
            }
            return path
        }


        /**
         * Record the price-actions in a [feed] and store them in an Avro [fileName] that can be later used as input for
         * an AvroFeed. The provided [feed] needs to implement the [AssetFeed] interface.
         *
         * [compression] can be enabled, which results in a smaller file. The `snappy` compression codec is used, that
         * achieves decent compression ratio while using limited CPU usage.
         *
         * Additionally, you can filter on a [timeframe] and [assetFilter]. Default is to apply no filtering.
         */
        @Suppress("LongParameterList")
        fun record(
            feed: Feed,
            fileName: String,
            compression: Boolean = true,
            timeframe: Timeframe = Timeframe.INFINITE,
            append: Boolean = false,
            assetFilter: AssetFilter = AssetFilter.all()
        )  {
            recordAvro(feed, fileName, compression, timeframe, append, assetFilter)
        }
    }

}

