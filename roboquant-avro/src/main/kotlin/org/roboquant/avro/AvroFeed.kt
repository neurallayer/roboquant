/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.avro

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileConstants
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.apache.avro.util.Utf8
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.compareTo
import org.roboquant.feeds.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*

internal const val SCHEMA = """{
             "namespace": "org.roboquant.avro.schema",
             "type": "record",
             "name": "PriceItemV2",
             "fields": [
                 {"name": "timestamp", "type" : "string"},
                 {"name": "symbol", "type": "string"},
                 {"name": "type", "type": { "type": "enum", "name": "type", "symbols" : ["BAR", "TRADE", "QUOTE", "BOOK"]}},
                 {"name": "values",  "type": {"type": "array", "items" : "double"}},
                 {"name": "meta", "type": ["null", "string"], "default": null}
             ] 
        }"""

/**
 * Read price data from a single file in Avro format. This feed loads data lazy and disposes of it afterwards, so
 * memory footprint is low. Compared to CSV files, Avro files are parsed more efficient, making it a good fit for large
 * back tests. Additionally, an Avro file can be compressed, reducing the overall disk space required.
 *
 * The internal resolution is nanoseconds and stored as a single Long value
 *
 * @property path the path where the Avro file can be found
 * @property template template to use to convert the stored symbols into assets
 *
 * @constructor Create new Avro Feed
 */
class AvroFeed(private val path: Path, private val template: Asset = Asset("TEMPLATE")) : Feed {

    /**
     * Instantiate an Avro Feed based on the Avro file at [path]
     */
    constructor(path: String) : this(Path.of(path))

    private val logger = Logging.getLogger(AvroFeed::class)

    private val index by lazy { createIndex() }

    override val timeframe: Timeframe by lazy { calcTimeframe() }


    init {
        logger.info { "New AvroFeed path=$path exist=${exists()}" }
    }


    fun exists(): Boolean = Files.exists(path)

    private fun getReader(): DataFileReader<GenericRecord> {
        return DataFileReader(path.toFile(), GenericDatumReader())
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
        var items = ArrayList<PriceItem>()
        val serializer = PriceItemSerializer()
        val cache = mutableMapOf<String, Asset>()
        getReader().use {
            val name = it.schema.fullName
            assert(name == "org.roboquant.avro.schema.PriceItemV2") { "invalid avro schema $name"}
            if (timeframe.isFinite()) position(it, timeframe.start)
            while (it.hasNext()) {
                val rec = it.next()

                // Optimize unnecessary parsing of the whole record
                val now = Instant.parse(rec[0] as Utf8)
                if (now < timeframe) continue

                if (now != last) {
                    channel.sendNotEmpty(Event(last, items))
                    last = now
                    items = ArrayList<PriceItem>(items.size)
                }

                if (now > timeframe) break

                // Parse the remaining attributes
                val symbol = rec.get(1).toString()
                val asset = cache.getOrPut(symbol) { template.copy(symbol = symbol) }
                val priceItemType = PriceItemType.valueOf(rec.get(2).toString())

                @Suppress("UNCHECKED_CAST")
                val values = rec.get(3) as List<Double>
                val meta = rec.get(4) as Utf8?
                val item = serializer.deserialize(asset, priceItemType, values, meta?.toString())
                items.add(item)
            }
            channel.sendNotEmpty(Event(last, items))
        }
    }

    private fun position(r: DataFileReader<GenericRecord>, time: Instant) {
        val key = index.floorKey(time)
        if (key != null) r.seek(index.getValue(key))
    }

    private fun createIndex() : TreeMap<Instant, Long> {
        val index = TreeMap<Instant, Long>()
        getReader().use {
            while (it.hasNext()) {
                val position = it.tell()
                val t = it.next().get(0) as Utf8
                it.seek(position)
                if (it.hasNext()) {
                    index.putIfAbsent(Instant.parse(t),position)
                    it.nextBlock()
                }
            }
        }
        logger.info { "Index created" }
        return index
    }

    private fun calcTimeframe() : Timeframe {
        if (index.isEmpty()) return Timeframe.EMPTY
        val start = index.firstKey()
        getReader().use {
            position(it, index.lastKey())
            var timestamp = index.lastKey()
            while (it.hasNext()) {
                timestamp = Instant.parse(it.next().get(0) as Utf8)
            }
            logger.info { "Timeframe calculated" }
            return Timeframe(start, timestamp, true)
        }
    }

    /**
     * Record the price-actions in a [feed] and store them in an Avro file that can be later used as input for
     * an AvroFeed. The provided [feed] needs to implement the [AssetFeed] interface.
     *
     * [compress] can be enabled, which results in a smaller file. The `snappy` compression codec is used, that
     * achieves decent compression ratio while using limited CPU usage.
     *
     * Additionally, you can filter on a [timeframe]. Default is to apply no filtering.
     */
    @Suppress("LongParameterList")
    fun record(
        feed: Feed,
        compress: Boolean = true,
        timeframe: Timeframe = Timeframe.INFINITE,
        append: Boolean = false,
        syncInterval: Int = DataFileConstants.DEFAULT_SYNC_INTERVAL
    ) = runBlocking {

        val channel = EventChannel(timeframe = timeframe)
        val schema = Schema.Parser().parse(SCHEMA)
        val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
        val dataFileWriter = DataFileWriter(datumWriter)
        val file = path.toFile()

        if (append) {
            require(exists()) {"File $file doesn't exist yet, cannot append"}
            dataFileWriter.appendTo(file)
        } else {
            if (exists()) logger.info { "Overwriting existing Avro file $file" }
            if (compress) dataFileWriter.setCodec(CodecFactory.snappyCodec())
            dataFileWriter.setSyncInterval(syncInterval)
            dataFileWriter.create(schema, file)
        }

        val job = launch {
            feed.play(channel)
            channel.close()
        }

        val arraySchema = Schema.createArray(Schema.create(Schema.Type.DOUBLE))
        val enumSchema = Schema.createArray(Schema.create(Schema.Type.STRING))
        try {
            val record = GenericData.Record(schema)
            val serializer = PriceItemSerializer()

            while (true) {
                val event = channel.receive()

                for (action in event.items.filterIsInstance<PriceItem>()) {

                    val asset = action.asset
                    record.put(0, event.time.toString())
                    record.put(1, asset.symbol)

                    val serialization = serializer.serialize(action)
                    val t = GenericData.EnumSymbol(enumSchema, serialization.type)
                    record.put(2, t)

                    val arr = GenericData.Array<Double>(serialization.values.size, arraySchema)
                    arr.addAll(serialization.values)
                    record.put(3, arr)

                    record.put(4, serialization.meta)
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

