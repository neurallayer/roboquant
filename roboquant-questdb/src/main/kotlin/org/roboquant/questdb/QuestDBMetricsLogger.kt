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

import io.questdb.cairo.CairoEngine
import io.questdb.cairo.DefaultCairoConfiguration
import org.roboquant.common.*
import org.roboquant.loggers.MetricsLogger
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.io.path.div
import kotlin.io.path.isDirectory

/**
 * Log metrics to a QuestDB database
 */
class QuestDBMetricsLogger(dbPath: Path = Config.home / "questdb-metrics" / "db") : MetricsLogger {

    private val logger = Logging.getLogger(this::class)
    private var engine: CairoEngine

    private val tables = ConcurrentSkipListSet<String>()

    init {
        if (Files.notExists(dbPath)) {
            logger.info { "Creating new database path=$dbPath" }
            Files.createDirectories(dbPath)
        }

        require( dbPath.isDirectory() ) { "dbPath needs to be a directory"}
        val config = DefaultCairoConfiguration(dbPath.toString())
        engine = CairoEngine(config)
    }

    /**
     * Load previous runs already in the database, so they are accessible via [getMetric]
     */
    fun loadPreviousRuns() {
        tables.addAll(engine.tables())
    }

    override fun log(results: Map<String, Double>, time: Instant, run: String) {
        if (results.isEmpty()) return
        if (! tables.contains(run)) {
            createTable(run)
            tables.add(run)
        }

        engine.insert(run) {
            val t = time.epochMicro
            for ((k,v) in results) {
                val row = newRow(t)
                row.putSym(0, k)
                row.putDouble(1, v)
                row.append()
            }
        }

    }

    /**
     * Get a metric for a specific [run]
     */
    override fun getMetric(metricName: String, run: String): TimeSeries {
        val result = mutableListOf<Observation>()
        engine.query("select * from '$run' where metric='$metricName'") {
            while (hasNext()) {
                val r = this.record
                val o = Observation(ofEpochMicro(r.getTimestamp(1)), r.getDouble(0))
                result.add(o)
            }
        }
        return TimeSeries(result)
    }

    /**
     * get a specific metric for all runs
     */
    override fun getMetric(metricName: String): Map<String, TimeSeries> {
        val result = mutableMapOf<String, TimeSeries>()
        for (table in tables) {
            val v = getMetric(metricName, table)
            if (v.isNotEmpty()) result[table] = v
        }
        return result
    }

    override fun start(run: String, timeframe: Timeframe) {
        // engine.update("drop table $run")
        tables.remove(run)
    }


    override fun getMetricNames(run: String): Set<String> {
        return engine.distictSymbol(run, "name").toSortedSet()
    }


    override val runs: Set<String>
        get() = engine.tables().toSet()

    private fun createTable(name: String) {
        engine.update(
            """CREATE TABLE IF NOT EXISTS '$name' (
                |metric SYMBOL,
                |value DOUBLE,  
                |time TIMESTAMP
                |) timestamp(time)""".trimMargin(),
        )

        engine.update("TRUNCATE TABLE '$name'")
    }

    fun close() {
        engine.close()
    }

}