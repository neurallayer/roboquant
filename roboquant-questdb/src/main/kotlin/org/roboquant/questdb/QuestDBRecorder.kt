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

package org.roboquant.questdb


import io.questdb.cairo.CairoConfiguration
import io.questdb.cairo.CairoEngine
import io.questdb.cairo.DefaultCairoConfiguration
import io.questdb.cairo.security.AllowAllSecurityContext
import io.questdb.griffin.SqlExecutionContextImpl
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.feeds.*
import org.roboquant.feeds.util.AssetSerializer.serialize
import org.roboquant.questdb.PriceActionHandler.Companion.getHandler
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.isDirectory


/**
 * Record another feed into a QuestDB database.
 *
 * - Supports up to micro seconds resolution
 * - Supports very large datasets
 * - Fast random access
 * - Limited to a single [PriceAction] type per table
 *
 * @param dbPath the path to use for the database.
 * If it doesn't exist yet, it will be created.
 * The default value is `~/.roboquant/questdb-prices/db`
 *
 */
class QuestDBRecorder(dbPath: Path = Config.home / "questdb-prices" / "db") {

    private val config: CairoConfiguration
    private val logger = Logging.getLogger(this::class)

    init {
        if (Files.notExists(dbPath)) {
            logger.info { "Creating new database path=$dbPath" }
            Files.createDirectories(dbPath)
        }

        require(dbPath.isDirectory()) { "dbPath needs to be a directory" }
        config = DefaultCairoConfiguration(dbPath.toString())
    }

    /**
     * Create a new engine
     */
    fun createEngine() = CairoEngine(config)

    @Suppress("unused")
    companion object Partition {

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
    }

    /**
     * Remove all the feeds from the database. This cannot be undone, so use this method with care.
     */
    fun removeAllFeeds() {
        CairoEngine(config).use {
            it.dropAllTables()
            logger.info { "Dropped all feeds in db=${config.root}" }
        }
    }

    /**
     * Generate a new QuestDB table based on the event in the feed and optional limited to the provided timeframe
     * Supported price-actions: [PriceBar], [PriceQuote] and [TradePrice]
     *
     * @param feed the feed you want to record
     * @param tableName the table to use to store the data
     * @param timeframe the timeframe
     * @param append do you want to append to an existing table, default is false
     * @param partition partition the table using the specified value.
     * This is required when wanting to append timestamps out of order and might result in better overall performance.
     * The default value is [NONE]
     */
    inline fun <reified T : PriceAction> record(
        feed: Feed,
        tableName: String,
        timeframe: Timeframe = Timeframe.INFINITE,
        append: Boolean = false,
        partition: String = NONE
    ) = runBlocking {

        require(partition in setOf("YEAR", "MONTH", "DAY", "HOUR", "NONE")) { "invalid partition value" }

        @Suppress("UNCHECKED_CAST")
        val handler = getHandler(T::class) as PriceActionHandler<T>
        val channel = EventChannel(timeframe = timeframe)

        // Create a new engine, so it can be released once the recording is done and release
        // any locks it has on the database
        val engine = createEngine()
        handler.createTable(tableName, partition, engine)
        if (!append) engine.update("TRUNCATE TABLE $tableName")

        val job = launch {
            feed.play(channel)
            channel.close()
        }

        val ctx = SqlExecutionContextImpl(engine, 1).with(AllowAllSecurityContext.INSTANCE, null, null)
        val writer = engine.getWriter(ctx.getTableToken(tableName), tableName)

        try {
            val lookupTable = mutableMapOf<Asset, String>()
            while (true) {
                val o = channel.receive()
                for (action in o.actions.filterIsInstance<T>()) {
                    val row = writer.newRow(o.time.epochMicro)
                    val str = lookupTable.getOrPut(action.asset) { action.asset.serialize() }
                    row.putSym(0, str)
                    handler.updateRecord(row, action)
                    row.append()
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
            engine.close()
        }

    }
}




