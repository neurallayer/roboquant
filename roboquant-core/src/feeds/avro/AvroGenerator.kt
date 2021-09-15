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
 * Utility to create an Avro file based on a feed. Typically, use case is to turn a large set of CSV files into a
 * single Avro file. It will overwrite an existing file with the same name.
 *
 * @constructor Create empty Avro generator
 */
class AvroGenerator(private val compressionLevel: Int = 1) {


    companion object {


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

    }




    /**
     * Generate a new Avro file based on the event in the feed and optional limited to the provided timeframe
     *
     * @param feed
     */
    fun capture(feed: Feed, outputFile: String, timeFrame: TimeFrame = TimeFrame.FULL) = runBlocking {
        val channel = EventChannel(timeFrame = timeFrame)
        val file = File(outputFile)
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
                for (action in event.actions) {
                    if (action is PriceBar) {
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
