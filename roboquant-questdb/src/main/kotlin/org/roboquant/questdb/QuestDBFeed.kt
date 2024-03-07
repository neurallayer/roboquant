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

package org.roboquant.questdb


import io.questdb.cairo.CairoEngine
import io.questdb.cairo.DefaultCairoConfiguration
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.util.AssetSerializer.deserialize
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div
import kotlin.io.path.isDirectory

/**
 * @property tableName the name of table to use
 * @param dbPath the database directory to use, default is `~/.roboquant/questdb`
 */
class QuestDBFeed(private val tableName: String, dbPath: Path = Config.home / "questdb-prices" / "db") : AssetFeed {

    private var engine: CairoEngine
    private val logger = Logging.getLogger(this::class)

    init {
        require(dbPath.isDirectory()) { "dbPath needs to be an existing questdb directory" }

        val configuration = DefaultCairoConfiguration(dbPath.toString())
        engine = CairoEngine(configuration)

        require(engine.tables().contains(tableName)) { "Table not found" }

        // Add hook to close engine before JVM shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info { "Closing QuestDB engine" }
            engine.close()
        })
    }

    /**
     * @see AssetFeed.assets
     */
    override val assets: SortedSet<Asset> by lazy {
        val result = mutableListOf<Asset>()
        engine.query("SELECT DISTINCT asset FROM $tableName;") {
            while (hasNext()) {
                val str = record.getSym(0).toString()
                result.add(str.deserialize())
            }
        }
        result.toSortedSet()
    }

    /**
     * @see AssetFeed.timeframe
     */
    override val timeframe: Timeframe by lazy {
        val query = "SELECT MIN(time), MAX(time) FROM $tableName;"
        var tf = Timeframe.EMPTY
        engine.query(query) {
            if (hasNext()) {
                val start = record.getTimestamp(0)
                val end = record.getTimestamp(1)
                tf = Timeframe(ofEpochMicro(start), ofEpochMicro(end), true)
            }
        }
        tf
    }

    /**
     * (Re)play the events of the feed using the provided [channel]
     */
    override suspend fun play(channel: EventChannel) {
        val tf = channel.timeframe
        val sql = if (tf.isInfinite()) {
            tableName
        } else {
            "SELECT * FROM $tableName WHERE time >= '${tf.start}' AND TIME <= '${tf.end}'"
        }
        logger.debug { "using sql=$sql" }

        // get the right handler for processing the rows
        val handler = PriceActionHandler.detectHandler(engine, tableName)
        logger.debug { "selected handler is ${handler::class.simpleName}" }

        engine.query(sql) {
            val lookup = mutableMapOf<String, Asset>()
            var actions = mutableListOf<PriceItem>()

            var last = Long.MIN_VALUE
            while (hasNext()) {

                val time = record.getTimestamp(1)
                if (time != last) {
                    channel.sendNotEmpty(Event(ofEpochMicro(last), actions))
                    last = time
                    actions = mutableListOf()
                }

                val str = record.getSym(0).toString()
                val asset = lookup.getOrPut(str) { str.deserialize() }
                val price = handler.getPriceAction(asset, record)
                actions.add(price)
            }
            channel.sendNotEmpty(Event(ofEpochMicro(last), actions))
        }
    }

    /**
     * Close the database engine
     */
    override fun close() {
        engine.close()
    }

}
