/*
 * Copyright 2020-2025 Neural Layer
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
import io.questdb.cairo.security.AllowAllSecurityContext
import io.questdb.griffin.SqlExecutionContextImpl
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Event
import org.roboquant.common.Logging
import org.roboquant.common.PriceItem
import org.roboquant.common.Timeframe
import org.roboquant.feeds.*
import org.roboquant.questdb.PriceActionHandler.Companion.getHandler
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass

/**
 * @property tableName the name of table to use
 * @param dbPath the database directory to use, default is `~/.roboquant/questdb`
 */
class QuestDBFeed(private val tableName: String, dbPath: Path = Config.home / "questdb-prices" / "db") : AssetFeed {

    private var engine: CairoEngine


    init {
        require(dbPath.isDirectory()) { "dbPath needs to be an directory" }
        engine = getEngine(dbPath)
    }

    /**
     * @see AssetFeed.assets
     */
    override val assets: Set<Asset> by lazy {
        val result = mutableListOf<Asset>()
        engine.query("SELECT DISTINCT asset FROM $tableName;") {
            while (hasNext()) {
                val str = record.getSymA(0).toString()
                result.add(Asset.deserialize(str))
            }
        }
        result.toSet()
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
     * Companion object for partitioning options and engine management
     */
    @Suppress("unused")
    companion object Partition {

           private val logger = Logging.getLogger(this::class)

            /**
             * Don't partition
             */
            const val NONE = "NONE"

            /**
             * Partition per year
             */
            const val YEAR = "YEAR"

            /**
             * Partition per day
             */
            const val DAY = "DAY"

            /**
             * Partition per month
             */
            const val MONTH = "MONTH"

            /**
             * Partition per hour
             */
            const val HOUR = "HOUR"


        private var engines = mutableMapOf<Path, CairoEngine>()

        /**
         * Get or create a CairoEngine for the provided database path
         */
        @Synchronized
        fun getEngine(dbPath: Path): CairoEngine {
            if (dbPath !in  engines) {
                if (Files.notExists(dbPath)) {
                    Files.createDirectories(dbPath)
                }
                require(dbPath.isDirectory()) { "dbPath needs to be a directory" }
                val config = DefaultCairoConfiguration(dbPath.toString())
                val engine = CairoEngine(config)
                engines[dbPath] = engine

                // Add hook to close engine before JVM shutdown
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info { "Closing QuestDB engine path=$dbPath" }
                    engine.close()
                })
            }
            return engines.getValue(dbPath)
        }

        /**
         * Get the available runs (tables) in the database located at [dbPath]
         */
        fun getRuns(dbPath: Path): Set<String> {
            val engine = getEngine(dbPath)
            return engine.tables().toSet()
        }

        /**
         * Close the engine for the provided database path
         */
        @Synchronized
        fun close(dbPath: Path) {
            engines[dbPath]?.close()
        }

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

                val str = record.getSymA(0).toString()
                val asset = lookup.getOrPut(str) { Asset.deserialize(str) }
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

    /**
     * Generate a new QuestDB table based on the event in the feed and optional limited to the provided timeframe
     * Supported price-actions: [org.roboquant.common.PriceBar], [org.roboquant.common.PriceQuote] and [org.roboquant.common.TradePrice]
     *
     * @param feed the feed you want to record
     * @param type the type of PriceItem you want to record
     * @param timeframe the timeframe
     * @param append do you want to append to an existing table, default is false
     * @param partition partition the table using the specified value.
     * This is required when wanting to append timestamps out of order and might result in better overall performance.
     * The default value is [NONE]
     */
    fun record(
        feed: Feed,
        type: KClass<*>,
        timeframe: Timeframe = Timeframe.INFINITE,
        append: Boolean = false,
        partition: String = NONE,
    ) = runBlocking {

        require(partition in setOf("YEAR", "MONTH", "DAY", "HOUR", "NONE")) { "invalid partition value" }
        val handler = getHandler(type)
        val channel = EventChannel(timeframe = timeframe)

        // Create a new engine, so it can be released once the recording is done and release
        // any locks it has on the database
        if (!append) {
            engine.update("""DROP TABLE IF EXISTS $tableName""")
            handler.createTable(tableName, partition, engine)
        }

        val job = feed.playBackground(channel)

        val ctx = SqlExecutionContextImpl(engine, 1).with(AllowAllSecurityContext.INSTANCE, null, null)
        val writer = engine.getWriter(ctx.getTableToken(tableName), tableName)

        try {
            val lookupTable = mutableMapOf<Asset, String>()
            while (true) {
                val o = channel.receive()
                for (action in o.items.filterIsInstance<PriceItem>()) {
                    if (action::class == type) {
                        val row = writer.newRow(o.time.epochMicro)
                        val str = lookupTable.getOrPut(action.asset) { action.asset.serialize() }
                        row.putSym(0, str)
                        handler.updateRecord(row, action)
                        row.append()
                    }
                }
                writer.commit()
            }

        } catch (_: ClosedReceiveChannelException) {
            // On purpose left empty, expected exception
        } finally {
            writer.commit()
            channel.close()
            if (job.isActive) job.cancel()
            ctx.close()
            writer.close()
        }

    }

    /**
     * Generate a new QuestDB table based on the event in the feed and optional limited to the provided timeframe
     * Supported price-actions: [org.roboquant.common.PriceBar], [org.roboquant.common.PriceQuote] and [org.roboquant.common.TradePrice]
     *
     * @param feed the feed you want to record
     * @param timeframe the timeframe
     * @param append do you want to append to an existing table, default is false
     * @param partition partition the table using the specified value.
     * This is required when wanting to append timestamps out of order and might result in better overall performance.
     * The default value is [NONE]
     */
    inline fun <reified T : PriceItem> record(
        feed: Feed,
        timeframe: Timeframe = Timeframe.INFINITE,
        append: Boolean = false,
        partition: String = NONE,
    ) {
        record(feed = feed, T::class, timeframe, append, partition)
    }


}
