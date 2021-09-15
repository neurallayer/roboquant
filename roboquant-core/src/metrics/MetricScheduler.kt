package org.roboquant.metrics

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.roboquant.Phase
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


    override fun calculate(account: Account, event: Event) {
        if (fire(event.now))
            for (metric in metrics) metric.calculate(account, event)
    }


    override fun getMetrics(): MetricResults {
        val result = mutableMapOf<String, Number>()
        for (metric in metrics) result += metric.getMetrics()
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

    override fun start(phase: Phase) {
        lastFire = false
        for (metric in metrics) metric.start(phase)
    }

    override fun end(phase: Phase) {
        for (metric in metrics) metric.end(phase)
    }

    override fun reset() {
        for (metric in metrics) metric.reset()
    }
}