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
import io.questdb.cairo.TableWriter
import io.questdb.griffin.SqlException
import io.questdb.griffin.SqlExecutionContext
import io.questdb.griffin.SqlExecutionContextImpl
import org.roboquant.common.Account
import org.roboquant.common.*
import org.roboquant.common.Event
import org.roboquant.journals.MetricsJournal
import org.roboquant.journals.metrics.Metric
import org.roboquant.common.Signal
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.div
import kotlin.io.path.isDirectory

/**
 * Log metrics to a QuestDB database
 */
class QuestDBJournal(
    private vararg val metrics: Metric,
    dbPath: Path = Config.home / "questdb-metrics" / "db",
    private val table: String = "metrics",
    workers: Int = 1,
    private val partition: String = QuestDBFeed.NONE,
    private val truncate: Boolean = false
) : MetricsJournal {

    private val logger = Logging.getLogger(this::class)
    private var engine: CairoEngine
    private val ctx: SqlExecutionContext

    init {
        require(partition in setOf("YEAR", "MONTH", "DAY", "HOUR", "NONE")) { "invalid partition value" }
        engine = getEngine(dbPath)
        ctx = SqlExecutionContextImpl(engine, workers)
        createTable(table)
        logger.info { "db=$dbPath table=$table" }
    }

    companion object {

        private var engines = mutableMapOf<Path, CairoEngine>()

        /**
         * Get a [CairoEngine] for the given [dbPath]. If it does not exist yet, it will be created.
         * This is a synchronized method to ensure thread safety.
         *
         * @param dbPath the path to the database directory
         * @return a CairoEngine instance
         */
        @Synchronized
        fun getEngine(dbPath: Path): CairoEngine {
            if (dbPath !in  engines) {
                if (Files.notExists(dbPath)) {
                    Files.createDirectories(dbPath)
                }
                require(dbPath.isDirectory()) { "dbPath needs to be a directory" }
                val config = DefaultCairoConfiguration(dbPath.toString())
                engines[dbPath] = CairoEngine(config)
            }
            return engines.getValue(dbPath)
        }

        fun getRuns(dbPath: Path): Set<String> {
            val engine = getEngine(dbPath)
            return engine.tables().toSet()
        }

        @Synchronized
        fun close(dbPath: Path) {
            engines[dbPath]?.close()
        }

    }

    private inline fun appendRows(block: TableWriter.() -> Unit) {
        val token = ctx.getTableToken(table)
        ctx.cairoEngine.getWriter(token, table).use {
            it.block()
            it.commit()
        }
    }

    /**
     * Get a metric for a specific [table]
     */
    override fun getMetric(name: String): TimeSeries {
        val result = mutableListOf<Observation>()

        engine.query("select time, value from '$table' where metric='$name'") {
            while (hasNext()) {
                val r = this.record
                val o = Observation(ofEpochMicro(r.getTimestamp(0)), r.getDouble(1))
                result.add(o)
            }
        }

        return TimeSeries(result)
    }

    /**
     * Remove a specific [run] from the database.
     * This will drop the table with the name of the run.
     */
    fun removeRun(run: String) {
        try {
            engine.dropTable(run)
        } catch (e: SqlException) {
            logger.error(e) { "error with drop table $run" }
        }
    }

    /**
     * Get the names of all metrics in this database.
     */
    override fun getMetricNames(): Set<String> {
        return engine.distictSymbol(table, "name").toSortedSet()
    }

    /**
     * Remove all runs from the database, both current and past runs.
     * Under the hood, this will drop all the tables in the database.
     */
    fun removeAllRuns() {
        engine.dropAllTables()
        logger.info { "removed all runs from ${engine.configuration.confRoot}" }
    }

    /**
     * Get all the runs in this database
     */
    fun getRuns(): Set<String> = engine.tables().toSet()

    private fun createTable(tableName: String) {
        engine.execute(
            """CREATE TABLE IF NOT EXISTS '$tableName' (
                |metric SYMBOL,
                |value DOUBLE,  
                |time TIMESTAMP
                |), INDEX(metric) timestamp(time) PARTITION BY $partition""".trimMargin(),
        )
        if (truncate) engine.update("TRUNCATE TABLE '$tableName'")
    }

    /**
     * Close the underlying context
     */
    fun close() {
        // engine.close()
        ctx.close()
    }

    /**
     * Track an [event] and calculate the metrics for it.
     * The [account] is used to provide context for the metrics.
     * The [signals] and [orders] are optional and can be used to provide additional context.
     */
    override fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>) {

        val result = mutableMapOf<String, Double>()
        for (metric in metrics) {
            val values = metric.calculate(event, account,signals, orders)
            result.putAll(values)
        }

        appendRows {
            val t = event.time.epochMicro
            for ((k, v) in result) {
                val row = newRow(t)
                row.putSym(0, k)
                row.putDouble(1, v)
                row.append()
            }
        }
    }

}
