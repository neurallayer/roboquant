/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.metrics

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant
import java.time.ZoneId

/**
 * Wraps other metrics and allows scheduling of those wrapped metrics. For example, you could
 * run a certain metric only on the last Friday of the year. The two main use-cases are:
 *
 * 1. being able to use CPU intensive metrics while not incurring the overhead at every single step
 * 2. run metrics that only make sense on certain dates or intervals
 *
 * It supports most of the Quartz format for defining the cron entry. For more details check the following page:
 *
 *   [Cron documentation](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html)
 *
 * # Usage
 *      metric = MetricScheduler("* * * ? * *", SomeMetric(). AnotherMetric())
 *
 *
 * @property metrics
 * @property schedule
 * @constructor Create new Metric Scheduler
 */
class MetricScheduler(
    private val schedule: String,
    private vararg val metrics: Metric,
    private val zoneId: ZoneId = ZoneId.of("UTC"),
    private val fireOnceInRow: Boolean = false
) : Metric {

    companion object Schedules {
        /**
         * Schedule to run something every Friday
         */
        const val everyFriday = "* * * ? * 6"

        /**
         * Schedule to run something last Friday of the month
         */
        const val lastFridayOfMonth = "* * * ? * 6L"

        /**
         * Schedule to run something last Friday of the year
         */
        const val lastFridayOfYear = "* * * ? 12 6L"

        /**
         * Schedule to run something at a specific hour of the day
         */
        fun dailyAt(hour: Int) = "0 0 $hour ? * *"
    }

    private val executionTime: ExecutionTime
    private var lastFire = false

    init {
        val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
        val parser = CronParser(cronDefinition)
        val cron = parser.parse(schedule)
        cron.validate()
        executionTime = ExecutionTime.forCron(cron)
    }

    override fun calculate(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Double>()
        if (fire(event.time)) {
            for (metric in metrics) result += metric.calculate(account, event)
        }
        return result
    }

    /**
     * Should the metrics be calculated given the provided time
     *
     * @return
     */
    private fun fire(now: Instant): Boolean {
        val zdt = now.atZone(zoneId)
        val fire = executionTime.isMatch(zdt)
        val result = if (fireOnceInRow) !lastFire && fire else fire
        lastFire = fire
        return result
    }

    override fun start(runPhase: RunPhase) {
        lastFire = false
        for (metric in metrics) metric.start(runPhase)
    }

    override fun end(runPhase: RunPhase) {
        for (metric in metrics) metric.end(runPhase)
    }

    override fun reset() {
        for (metric in metrics) metric.reset()
    }
}