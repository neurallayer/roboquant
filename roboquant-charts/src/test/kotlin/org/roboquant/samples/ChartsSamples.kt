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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.charts.MetricsReport
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.ReturnsMetric2
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Ignore
import kotlin.test.Test


internal class ChartsSamples {

    @Test
    @Ignore
    internal fun basic() {
        val rq = Roboquant(
            EMAStrategy(),
            ReturnsMetric2(),
            ScorecardMetric()
        )
        val feed = RandomWalkFeed.lastYears(5)
        rq.run(feed)
        val report = MetricsReport(rq)
        report.toHTMLFile("/tmp/test.html")
        println(rq.broker.account.summary())
    }
}
