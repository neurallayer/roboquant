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

package org.roboquant.jupyter

import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.charts.Chart
import org.roboquant.charts.PriceBarChart
import org.roboquant.common.Config
import org.roboquant.strategies.EMAStrategy

/**
 * Provides current environment settings in HTML format suitable for displaying in a Jupyter Notebook.
 */
class Welcome {

    /**
     * Return the welcome message with the main environment settings as an HTML snippet.
     */
    @Suppress("MaxLineLength")
    fun asHTML(): String {

        with(Config.info) {
            return """
            <img src="https://roboquant.org/img/avatar.png" alt="roboquant logo" align="left" style="margin-right: 20px; max-height:160px;"/>
            <span>
                <b style="color: rgb(50,150,200);font-size: 150%;"> roboquant </b> $version<br>
                <b> build:</b> $build<br>
                <b> home:</b> ${Config.home}<br>
                <b> os:</b> $os<br>
                <b> jvm:</b> $jvm<br>
                <b> memory:</b> $memory MB<br>
                <b> cpu cores:</b> $cores<br>
            </span>
            """.trimIndent()
        }
    }

    /**
     * Generate a full HTML Welcome page.
     */
    fun asHTMLPage(): String {
        return """
        <html>
            <body>
                ${asHTML()}
            </body>
        </html>
        """.trimIndent()
    }

    /**
     * Run a small demo back test and display the resulting equity curve
     */
    fun demo1() {
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy)
        val feed = AvroFeed.sp500()
        println(
            """
            ┌───────────────┐
            │     INPUT     │
            └───────────────┘
            val strategy = EMAStrategy()
            val roboquant = Roboquant(strategy)
            
            val feed = AvroFeed.sp500()
            roboquant.run(feed)
            ┌───────────────┐
            │    Output     │
            └───────────────┘
        """.trimIndent()
        )

        roboquant.run(feed)
    }


    /**
     * View feed data demo
     */
    fun demo2(): Chart {
        println(
            """
            ┌───────────────┐
            │     INPUT     │
            └───────────────┘
            val feed = AvroFeed.sp500()
            PriceBarChart(feed, "AAPL")
            
            ┌───────────────┐
            │    Output     │
            └───────────────┘
        """.trimIndent()
        )

        val feed = AvroFeed.sp500()
        return PriceBarChart(feed, "AAPL")
    }

}
