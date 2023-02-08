package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.feeds.AvroFeed
import org.roboquant.jupyter.MetricsReport
import org.roboquant.metrics.ReturnsMetric2
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.io.path.div


fun main() {
    val rq = Roboquant(
        EMAStrategy(),
        ReturnsMetric2(),
        ScorecardMetric()
    )
    val path = Config.home / "all_1962_2023.avro"
    val feed = AvroFeed(path)
    rq.run(feed)
    val report = MetricsReport(rq)
    report.toHTMLFile("/tmp/test.html")
    println(rq.broker.account.summary())
}