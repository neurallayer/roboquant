@file:Suppress("BlockingMethodInNonBlockingContext")

package org.roboquant.feeds.avro

import org.apache.avro.file.DataFileReader
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceBar
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant


/**
 * Read price data from a single file in Avro format. Compared to CSV files, Avro files contain typed values, making the
 * parsing more efficient. Additionally, an Avro file can be compressed efficiently, reducing the overall disk space
 * required.
 *
 * This feed loads data lazy and disposes of it afterwards, so memory footprint is limited.
 *
 * Roboquant also comes with a utility to create an Avro file based on another feed, see [AvroGenerator]
 *
 * @constructor Create new Avro Feed
 */
class AvroFeed(private val path: String, private val useIndex: Boolean = true) : HistoricFeed {

    private val _timeFrame: TimeFrame
    private val assetLookup = mutableMapOf<String, Asset>()

    internal val index = mutableListOf<Pair<Instant, Long>>()

    override val assets
        get() = assetLookup.values.toSortedSet()


    override val timeFrame: TimeFrame
        get() = if (index.isEmpty()) _timeFrame else super.timeFrame


    override val timeline: List<Instant>
        get() = index.map { it.first }


    init {
        _timeFrame = getReader().use { reader ->
            val start = reader.getMetaLong("roboquant.start")
            val end = reader.getMetaLong("roboquant.end")
            TimeFrame(Instant.ofEpochMilli(start), (Instant.ofEpochMilli(end)))
        }

        if (useIndex) buildIndex()
        logger.info { "Loaded assets with timeframe $timeFrame from $path" }
    }


    private fun position(r: DataFileReader<GenericRecord>, now: Instant) {
        var idx = index.binarySearch { it.first.compareTo(now) }
        idx = if (idx < 0) -idx - 1 else idx
        if (idx >= index.size) idx = index.size -1
        if (idx >= 0) r.seek(index[idx].second)
    }


    private fun buildIndex() {
        index.clear()
        var last = Instant.MIN

        getReader().use {
            while (it.hasNext()) {
                val rec = it.next()
                val t = Instant.ofEpochMilli(rec[0] as Long)
                val asset = rec[1].toString()
                if (! assetLookup.containsKey(asset)) assetLookup[asset] = Asset.deserialize(asset)
                if (t > last) {
                    val pos = it.previousSync()
                    index.add(Pair(t, pos))
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
        val lookup = assetLookup
        val tf = channel.timeFrame
        var last = Instant.MIN
        var actions = mutableListOf<PriceBar>()

        getReader().use {
            if (useIndex) position(it, tf.start)

            while (it.hasNext()) {
                val rec = it.next()

                // Optimize unnecessary parsing of whole record
                val now = Instant.ofEpochMilli(rec[0] as Long)
                if (now < tf.start) continue

                if (now != last) {
                    if (actions.isNotEmpty()) channel.send(Event(actions, last))
                    last = now
                    actions = mutableListOf()
                }

                if (now >= tf.end) break

                val id = rec.get(1).toString()
                val asset = lookup.getOrPut(id) { Asset.deserialize(id) }

                val priceBar = PriceBar(
                    asset,
                    rec.get(2) as Float,
                    rec.get(3) as Float,
                    rec.get(4) as Float,
                    rec.get(5) as Float,
                    rec.get(6) as Float
                )
                actions.add(priceBar)
            }
            if (actions.isNotEmpty()) channel.send(Event(actions, last))
        }
    }

    companion object {
        private val logger = Logging.getLogger("AvroFeed")
        private const val sp500URL = "https://github.com/neurallayer/roboquant-data/blob/main/avro/5yr_sp500.avro?raw=true"

        /**
         * 5 years worth of end of day data for companies listed in the S&P 500
         */
        fun sp500(): AvroFeed {
            val path: Path = Paths.get(Config.home.toString(), "5yr_sp500.avro")
            if (Files.notExists(path)) {
                logger.info("Downloading S&P 500 price data ...")
                download(sp500URL, path)
                require(Files.exists(path))
            }
            return AvroFeed(path.toString())
        }


        private fun download(downloadURL: String, fileName: Path) {
            val website = URL(downloadURL)
            website.openStream().use { inputStream: InputStream ->
                Files.copy(
                    inputStream,
                    fileName,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }


    }

}

