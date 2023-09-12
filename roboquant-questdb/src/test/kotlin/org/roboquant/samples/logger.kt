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
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.questdb.QuestDBMetricsLogger
import org.roboquant.strategies.EMAStrategy


fun main() {
    val logger = QuestDBMetricsLogger()
    val feed = RandomWalkFeed.lastYears(1)
    val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = logger)
    rq.run(feed, name = "myrun")
    val m = logger.getMetric("account.equity", "myrun")
    println(m.size)
    feed.close()
}