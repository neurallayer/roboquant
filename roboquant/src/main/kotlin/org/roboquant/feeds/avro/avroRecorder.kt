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

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.roboquant.common.Asset
import org.roboquant.common.AssetFilter
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.avro.AssetSerializer.serialize
import java.io.File
import kotlin.io.path.Path

/**
 * Schema used to store different types of [PriceAction]
 */
private const val SCHEMA = """
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

@Suppress("LongParameterList")
internal fun recordAvro(
    feed: Feed,
    fileName: String,
    compression: Boolean = true,
    timeframe: Timeframe = Timeframe.INFINITE,
    append: Boolean = false,
    assetFilter: AssetFilter = AssetFilter.all()
) = runBlocking {
    val channel = EventChannel(timeframe = timeframe)
    val schema = Schema.Parser().parse(SCHEMA)
    val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
    val dataFileWriter = DataFileWriter(datumWriter)
    val file = File(fileName)

    val index = MetadataProvider(Path(fileName))
    index.clearCache()

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
