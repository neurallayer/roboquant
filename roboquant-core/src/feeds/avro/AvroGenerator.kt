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
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import java.io.File

/**
 * Utility to create an Avro file based on feed. It only records actions of the type [PriceBar].
 *
 * Typical use case is to turn a large set of CSV files into a single Avro file and use that for future back-tests.
 */
object AvroGenerator {

    private const val schemaDef = """
            {
             "namespace": "org.roboquant",
             "type": "record",
             "name": "priceBars",
             "fields": [
                 {"name": "time", "type": "long"},
                 {"name": "asset", "type": "string"},
                 {"name": "open",  "type": "float"},
                 {"name": "high",  "type": "float"},
                 {"name": "low",  "type": "float"},
                 {"name": "close",  "type": "float"},
                 {"name": "volume", "type": "float"}
             ] 
            }  
            """

    /**
     * Generate a new Avro file with the provided [fileName] based on the events in the [feed].
     * Optional the capture can be limited to the provided [timeFrame] and a [compressionLevel] can be set.
     *
     * It will overwrite an existing file with the same name.
     */
    fun capture(feed: Feed, fileName: String, timeFrame: TimeFrame = TimeFrame.FULL, compressionLevel: Int = 1) =
        runBlocking {

            val channel = EventChannel(timeFrame = timeFrame)
            val file = File(fileName)
            val schema = Schema.Parser().parse(schemaDef)
            val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
            val dataFileWriter = DataFileWriter(datumWriter)
            dataFileWriter.setCodec(CodecFactory.deflateCodec(compressionLevel))

            val tf = feed.timeFrame.intersect(timeFrame)
            dataFileWriter.setMeta("roboquant.start", tf.start.toEpochMilli())
            dataFileWriter.setMeta("roboquant.end", tf.end.toEpochMilli())

            dataFileWriter.create(schema, file)

            val cache = mutableMapOf<Asset, String>()

            val job = launch {
                feed.play(channel)
                channel.close()
            }

            try {
                val record = GenericData.Record(schema)
                while (true) {
                    val event = channel.receive()
                    val now = event.now.toEpochMilli()
                    for (action in event.actions.filterIsInstance<PriceBar>()) {
                        val assetStr = cache.getOrPut(action.asset) { action.asset.serialize() }
                        record.put(0, now)
                        record.put(1, assetStr)
                        record.put(2, action.open)
                        record.put(3, action.high)
                        record.put(4, action.low)
                        record.put(5, action.close)
                        record.put(6, action.volume)
                        dataFileWriter.append(record)
                    }

                    // We sync after each event, so we can later create an index if required
                    dataFileWriter.sync()
                }

            } catch (e: ClosedReceiveChannelException) {
                // On purpose left empty, expected exception
            } finally {
                channel.close()
                if (job.isActive) job.cancel()
                dataFileWriter.sync()
                dataFileWriter.close()
            }
        }
}
