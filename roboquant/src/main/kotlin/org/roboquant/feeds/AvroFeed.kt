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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
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
import java.util.SortedSet
import kotlin.io.path.isRegularFile

/**
 * Read price data from a single file in Avro format. This feed loads data lazy and disposes of it afterwards, so
 * memory footprint is low. Compared to CSV files, Avro files contain typed values, making the parsing more efficient.
 *
 * Additionally, an Avro file can be compressed, reducing the overall disk space required.
 *
 * @constructor Create new Avro Feed
 */
class AvroFeed(private val path: Path) : HistoricFeed {

    /**
     * Create a new Avro Feed
     */
    constructor(path: String) : this(Path.of(path))

    private val assetLookup: List<Asset>
    private val index  = buildIndex()

    override val assets: SortedSet<Asset>
        get() = assetLookup.toSortedSet()

    override val timeline: Timeline
        get() = index.map { it.first }

    init {
        assert(path.isRegularFile()) { "$path is not a file"}
        assetLookup = readAssets()
        logger.info { "Loaded data from $path with timeframe $timeframe" }
    }

    private fun position(r: DataFileReader<GenericRecord>, time: Instant) {
        var idx = index.binarySearch { it.first.compareTo(time) }
        idx = if (idx < 0) -idx - 1 else idx
        if (idx >= index.size) idx = index.size - 1
        if (idx >= 0) r.seek(index[idx].second)
    }


    private fun readAssets(): List<Asset> {
        getReader().use {
            val assetsJson = it.getMetaString(assetsMetaKey)
            return Json.decodeFromString(assetsJson)
        }
    }

    /**
     * Build an index of where each time starts. The index helps to achieve faster read
     * access if not starting from the beginning.
     */
    private fun buildIndex() : List<Pair<Instant, Long>> {
        var last = Long.MIN_VALUE
        val index = mutableListOf<Pair<Instant, Long>>()

        getReader().use {
            while (it.hasNext()) {
                val rec = it.next()
                val t = rec[0] as Long
                if (t > last) {
                    val pos = it.previousSync()
                    val time = Instant.ofEpochMilli(t)
                    index.add(Pair(time, pos))
                    last = t
                }
            }
        }
        return index
    }

    private fun getReader(): DataFileReader<GenericRecord> {
        return DataFileReader(path.toFile(), GenericDatumReader())
    }

    /**
     * Convert a generic Avro record to a [PriceAction]
     */
    private fun recToPriceAction(rec: GenericRecord): PriceAction {
        val assetIdx = rec.get(1) as Int
        val asset = assetLookup[assetIdx]
        val actionType = rec.get(2) as Int

        @Suppress("UNCHECKED_CAST")
        val values = rec.get(3) as List<Double>
        return PriceActionSerializer.deserialize(asset, actionType, values)
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
            position(it, timeframe.start)

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

                val action = recToPriceAction(rec)
                actions.add(action)
            }
            if (actions.isNotEmpty()) channel.send(Event(actions, last))
        }
    }

    /**
     * Standard set of Avro feeds that come with roboquant and will be downloaded first time when invoked. They are
     * stored at <User.Home>/.roboquant and reused from there later on.
     */
    companion object {

        private val logger = Logging.getLogger(AvroFeed::class)
        private const val sp500File = "sp500_pricebar_v4.0.avro"
        private const val sp500QuoteFile = "sp500_pricequote_v4.0.avro"
        private const val assetsMetaKey = "feed.assets"

        /**
         * Get an AvroFeed containing end-of-day [PriceBar] data for the companies listed in the S&P 500.
         */
        fun sp500(): AvroFeed {
            val path = download(sp500File)
            return AvroFeed(path)
        }

        /**
         * Get an AvroFeed containing [PriceQuote] data for the companies listed in the S&P 500.
         */
        fun sp500Quotes(): AvroFeed {
            val path = download(sp500QuoteFile)
            return AvroFeed(path)
        }


        /**
         * Download a file from GitHub if now yet present on local file system
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
         * Schema used to store different type of [PriceAction]
         */
        private const val schemaDef = """
            {
             "namespace": "org.roboquant",
             "type": "record",
             "name": "priceActions",
             "fields": [
                 {"name": "time", "type": "long"},
                 {"name": "asset", "type": "int"},
                 {"name": "type", "type": "int"},
                 {"name": "values",  "type": {"type": "array", "items" : "double"}}
             ] 
            }  
            """

        /**
         * Record the [PriceAction]s in a feed and store them in an Avro file that can be later used with an [AvroFeed].
         *
         * Compression can be used which results in a smaller files at the cost of performance when accessing them. The
         * Snappy compression codec is used, that achieves decent compression ratio while not limiting the performance
         * overhead.
         */
        fun record(
            feed: AssetFeed,
            fileName: String,
            compression: Boolean = true,
            timeframe: Timeframe = Timeframe.INFINITE,
            assetFilter: AssetFilter = AssetFilter.all()
        ) = runBlocking {
            val channel = EventChannel(timeframe = timeframe)
            val file = File(fileName)
            val schema = Schema.Parser().parse(schemaDef)
            val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
            val dataFileWriter = DataFileWriter(datumWriter)
            if (compression) dataFileWriter.setCodec(CodecFactory.snappyCodec())

            val assets = feed.assets.toList()
            val lookup = assets.withIndex().associate { it.value to it.index }
            val assetsMetaValue = Json.encodeToString(assets)
            dataFileWriter.setMeta(assetsMetaKey, assetsMetaValue)
            dataFileWriter.create(schema, file)

            val job = launch {
                feed.play(channel)
                channel.close()
            }

            val arraySchema = Schema.createArray(Schema.create(Schema.Type.DOUBLE))
            try {
                val record = GenericData.Record(schema)
                while (true) {
                    val event = channel.receive()
                    val now = event.time.toEpochMilli()
                    for (action in event.actions.filterIsInstance<PriceAction>()
                        .filter { assetFilter.filter(it.asset, event.time) }) {
                        val assetIdx = lookup[action.asset]
                        record.put(0, now)
                        record.put(1, assetIdx)

                        val (idx, values) = PriceActionSerializer.serialize(action)
                        record.put(2, idx)

                        val arr = GenericData.Array<Double>(values.size, arraySchema)
                        arr.addAll(values)
                        record.put(3, arr)
                        dataFileWriter.append(record)
                    }

                }

            } catch (_: ClosedReceiveChannelException) {
                // On purpose left empty, expected exception
            } finally {
                channel.close()
                if (job.isActive) job.cancel()
                dataFileWriter.sync()
                dataFileWriter.close()
            }
        }


    }


}


/**
 * Used by AvroFeed to serialize and deserialize [PriceAction] to a DoubleArray, so it can be stored in an Avro file.
 */
private object PriceActionSerializer {

    private const val PRICEBAR_IDX = 1
    private const val TRADEPRICE_IDX = 2
    private const val PRICEQUOTE_IDX = 3
    private const val ORDERBOOK_IDX = 4

    fun serialize(action: PriceAction): Pair<Int, List<Double>> {
        return when (action) {
            is PriceBar -> Pair(PRICEBAR_IDX, action.ohlcv.toList())
            is TradePrice -> Pair(TRADEPRICE_IDX, listOf(action.price, action.volume))
            is PriceQuote -> Pair(
                PRICEQUOTE_IDX,
                listOf(action.askPrice, action.askSize, action.bidPrice, action.bidSize)
            )
            is OrderBook -> Pair(ORDERBOOK_IDX, orderBookToValues(action))
            else -> throw UnsupportedException("cannot serialize action=$action")
        }
    }

    fun deserialize(asset: Asset, idx: Int, values: List<Double>): PriceAction {
        return when (idx) {
            PRICEBAR_IDX -> PriceBar(asset, values.toDoubleArray())
            TRADEPRICE_IDX -> TradePrice(asset, values[0], values[1])
            PRICEQUOTE_IDX -> PriceQuote(asset, values[0], values[1], values[2], values[3])
            ORDERBOOK_IDX  -> orderBookFromValues(asset, values)
            else -> throw UnsupportedException("cannot deserialize asset=$asset type=$idx")
        }
    }

    private fun orderBookToValues(action: OrderBook) : List<Double> {
        return listOf(action.asks.size.toDouble()) +
                action.asks.map { listOf(it.size, it.limit) }.flatten() +
                action.bids.map { listOf(it.size, it.limit) }.flatten()
    }


    private fun orderBookFromValues(asset: Asset, values: List<Double>): OrderBook {
        val asks = mutableListOf<OrderBook.OrderBookEntry>()
        val bids = mutableListOf<OrderBook.OrderBookEntry>()
        val endAsks = 1 + 2 * values[0].toInt()
        for (i in 1 until endAsks step 2) {
            val entry = OrderBook.OrderBookEntry(values[i], values[i + 1])
            asks.add(entry)
        }

        for (i in endAsks until values.lastIndex step 2) {
            val entry = OrderBook.OrderBookEntry(values[i], values[i + 1])
            bids.add(entry)
        }
        return OrderBook(asset, asks, bids)
    }


}
