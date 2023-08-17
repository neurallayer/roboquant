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

package org.roboquant.charts

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test

class SignalChartTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(1)
        val strat = EMAStrategy()
        val chart = SignalChart(feed, strat)
        assertDoesNotThrow {
            chart.getOption()
        }

    }


}