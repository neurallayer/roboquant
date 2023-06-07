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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.apache.avro.util.Utf8
import org.roboquant.common.*
import org.roboquant.feeds.AssetSerializer.deserialize
import org.roboquant.feeds.AssetSerializer.serialize
import java.io.File
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
class AvroFeed(private val path: Path) : AssetFeed {


    /**
     * Instantiate an Avro Feed based on the Avro file at [path]
     */
    constructor(path: String) : this(Path.of(path))

    /**
     * Contains mapping of an Avro UTF8 string to an Asset
     */
    private val assetLookup = mutableMapOf<Utf8, Asset>()

    /**
     * Index that holds time/position for quicker access rows
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
        val result = buildIndex()
        index = result.first
        timeframe = result.second

        logger.info { "loaded feed with timeframe=$timeframe" }
    }

    private fun position(r: DataFileReader<GenericRecord>, time: Instant) {
        val idx = index.binarySearch { it.first.compareTo(time) }
        when {
            idx > 0 -> r.seek(index[idx - 1].second)
            idx < -1 -> r.seek(index[-idx - 2].second)
        }
    }


    /**
     * Build an index of where each time starts. The index helps to achieve faster read access if not starting
     * from the beginning.
     */
    private fun buildIndex(): Pair<List<Pair<Instant, Long>>, Timeframe> {
        var last = Long.MIN_VALUE
        val index = mutableListOf<Pair<Instant, Long>>()
        var start = Long.MIN_VALUE
        var prevPos = Long.MIN_VALUE
        getReader().use {
            while (it.hasNext()) {
                val rec = it.next()
                val t = rec[0] as Long

                val asset = rec[1] as Utf8
                if (!assetLookup.containsKey(asset)) assetLookup[asset] = asset.toString().deserialize()
                if (t > last) {
                    if (start == Long.MIN_VALUE) start = t
                    val pos = it.previousSync()

                    if (pos != prevPos) {
                        val time = Instant.ofEpochMilli(t)
                        index.add(Pair(time, pos))
                        prevPos = pos
                    }
                    last = t
                }
            }
        }
        val timeframe = if (start == Long.MIN_VALUE)
            Timeframe.EMPTY
        else
            Timeframe(Instant.ofEpochMilli(start), Instant.ofEpochMilli(last))
        return Pair(index, timeframe)
    }

    private fun getReader(): DataFileReader<GenericRecord> {
        return DataFileReader(path.toFile(), GenericDatumReader())
    }

    /**
     * Convert a generic Avro record to a [PriceAction]
     */
    private fun recToPriceAction(rec: GenericRecord, serializer: PriceActionSerializer): PriceAction {
        val assetStr = rec.get(1) as Utf8
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

        private val logger = Logging.getLogger(AvroFeed::class)
        private const val sp500File = "sp500_pricebar_v5.1.avro"
        private const val sp500QuoteFile = "sp500_pricequote_v5.0.avro"
        private const val forexFile = "forex_pricebar_v5.1.avro"

        /**
         * Get an AvroFeed containing end-of-day [PriceBar] data for the companies listed in the S&P 500. This feed
         * contains a few years of public data.
         *
         * Please note that not all US exchanges are included, so the prices are not 100% accurate.
         */
        fun sp500(): AvroFeed {
            val path = download(sp500File)
            return AvroFeed(path)
        }

        /**
         * Get an AvroFeed containing [PriceQuote] data for the companies listed in the S&P 500. This feed contains
         * a few minutes of public data.
         *
         * Please note that not all US exchanges are included, so the prices are not 100% accurate.
         */
        fun sp500Quotes(): AvroFeed {
            val path = download(sp500QuoteFile)
            return AvroFeed(path)
        }

        /**
         * Get an AvroFeed containing 1 minute [PriceBar] data for an EUR/USD currency pair.
         */
        fun forex(): AvroFeed {
            val path = download(forexFile)
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
         * Schema used to store different types of [PriceAction]
         */
        private const val schemaDef = """
            {
             "namespace": "org.roboquant.avro.schema",
             "type": "record",
             "name": "PriceAction",
             "fields": [
                 {"name": "time", "type": "long"},
                 {"name": "asset", "type": "string"},
                 {"name": "type", "type": "int"},
                 {"name": "values",  "type": {"type": "array", "items" : "double"}},
                 {"name": "other", "type": ["null", "string"], "default": null}
             ] 
            }  
            """

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
        ) = runBlocking {
            val channel = EventChannel(timeframe = timeframe)
            val schema = Schema.Parser().parse(schemaDef)
            val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
            val dataFileWriter = DataFileWriter(datumWriter)
            val file = File(fileName)

            if (append) {
                dataFileWriter.appendTo(file)
            } else {
                if (compression) dataFileWriter.setCodec(CodecFactory.snappyCodec())
                dataFileWriter.create(schema, file)
            }


            val job = launch {
                feed.play(channel)
                channel.close()
            }

            val arraySchema = Schema.createArray(Schema.create(Schema.Type.DOUBLE))
            try {
                val cache = mutableMapOf<Asset, String>()
                val record = GenericData.Record(schema)
                val serializer = PriceActionSerializer()
                while (true) {
                    val event = channel.receive()
                    val now = event.time.toEpochMilli()
                    for (action in event.actions.filterIsInstance<PriceAction>()
                        .filter { assetFilter.filter(it.asset, event.time) }) {
                        val asset = action.asset
                        val assetStr = cache.getOrPut(asset) { asset.serialize() }
                        record.put(0, now)
                        record.put(1, assetStr)

                        val serialization = serializer.serialize(action)
                        record.put(2, serialization.type)

                        val arr = GenericData.Array<Double>(serialization.values.size, arraySchema)
                        arr.addAll(serialization.values)
                        record.put(3, arr)

                        record.put(4, serialization.other)
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
 * Used by AvroFeed to serialize and deserialize Assets to a string. This is optimized for size.
 */
internal object AssetSerializer {

    /**
     * Serialize an asset into a short string.
     */
    fun Asset.serialize(): String {
        val sb = StringBuilder(symbol).append(SEP)
        if (type != AssetType.STOCK) sb.append(type.name)
        sb.append(SEP)
        if (currency.currencyCode != "USD") sb.append(currency.currencyCode)
        sb.append(SEP)
        if (exchange.exchangeCode != "") sb.append(exchange.exchangeCode)
        sb.append(SEP)
        if (multiplier != 1.0) sb.append(multiplier)
        sb.append(SEP)
        if (id.isNotEmpty()) sb.append(id)
        sb.append(SEP)

        var cnt = 0
        for (ch in sb.reversed()) if (ch == SEP) cnt++ else break
        return sb.substring(0, sb.length - cnt)
    }


    /**
     * Use the ASCII Unit Separator character. Should not interfere with used strings for symbol, exchange and currency
     */
    private const val SEP = '\u001F'

    /**
     * Deserialize a string into an asset. The string needs to have been created using [serialize]
     *
     * @return
     */
    fun String.deserialize(): Asset {
        val e = split(SEP)
        val l = e.size
        require(l <= 6) { "Invalid format" }
        return Asset(
            e[0],
            if (l > 1 && e[1].isNotEmpty()) AssetType.valueOf(e[1]) else AssetType.STOCK,
            if (l > 2 && e[2].isNotEmpty()) e[2] else "USD",
            if (l > 3) e[3] else "",
            if (l > 4 && e[4].isNotEmpty()) e[4].toDouble() else 1.0,
            if (l > 5) e[5] else "",
        )

    }
}


/**
 * Used by AvroFeed to serialize and deserialize [PriceAction] to a DoubleArray, so it can be stored in an Avro file.
 */
internal class PriceActionSerializer {

    internal class Serialization(val type: Int, val values: List<Double>, val other: String? = null)

    private val timeSpans = mutableMapOf<String, TimeSpan>()

    private companion object {
        private const val PRICEBAR_IDX = 1
        private const val TRADEPRICE_IDX = 2
        private const val PRICEQUOTE_IDX = 3
        private const val ORDERBOOK_IDX = 4
    }

    fun serialize(action: PriceAction): Serialization {
        return when (action) {
            is PriceBar -> Serialization(PRICEBAR_IDX, action.ohlcv.toList(), action.timeSpan?.toString())
            is TradePrice -> Serialization(TRADEPRICE_IDX, listOf(action.price, action.volume))
            is PriceQuote -> Serialization(
                PRICEQUOTE_IDX,
                listOf(action.askPrice, action.askSize, action.bidPrice, action.bidSize)
            )

            is OrderBook -> Serialization(ORDERBOOK_IDX, orderBookToValues(action))
            else -> throw UnsupportedException("cannot serialize action=$action")
        }
    }


    private fun getPriceBar(asset: Asset, values: DoubleArray, other: String?): PriceBar {
        val timeSpan =  if (other != null) {
            timeSpans.getOrPut(other) {TimeSpan.parse(other)}
        } else {
            null
        }
        return PriceBar(asset, values, timeSpan)
    }

    fun deserialize(asset: Asset, idx: Int, values: List<Double>, other: String?): PriceAction {
        return when (idx) {
            PRICEBAR_IDX -> getPriceBar(asset, values.toDoubleArray(), other)
            TRADEPRICE_IDX -> TradePrice(asset, values[0], values[1])
            PRICEQUOTE_IDX -> PriceQuote(asset, values[0], values[1], values[2], values[3])
            ORDERBOOK_IDX -> getOrderBook(asset, values)
            else -> throw UnsupportedException("cannot deserialize asset=$asset type=$idx")
        }
    }

    private fun orderBookToValues(action: OrderBook): List<Double> {
        return listOf(action.asks.size.toDouble()) +
                action.asks.map { listOf(it.size, it.limit) }.flatten() +
                action.bids.map { listOf(it.size, it.limit) }.flatten()
    }


    private fun getOrderBook(asset: Asset, values: List<Double>): OrderBook {
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
