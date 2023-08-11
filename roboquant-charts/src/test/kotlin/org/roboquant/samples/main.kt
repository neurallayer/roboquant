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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.charts.MetricsReport
import org.roboquant.common.Config
import org.roboquant.feeds.AvroFeed
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