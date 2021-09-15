package org.roboquant.metrics


import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

internal class MetricSchedulerTest {


    private fun calc(metric: Metric, account: Account, event: Event): MetricResults {
        metric.calculate(account, event)
        return metric.getMetrics()
    }

    @Test
    fun test() {
        val pm = ProgressMetric()
        var metric = MetricScheduler(MetricScheduler.everyFriday, pm)

        val (account, event) = TestData.metricInput()
        var result = calc(metric, account, event)
        assertTrue(result.isNotEmpty())

        metric = MetricScheduler(MetricScheduler.lastFridayOfMonth, pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.lastFridayOfYear, pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.dailyAt(16), pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

    }
}