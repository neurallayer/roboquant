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

package org.roboquant.http

import com.sun.net.httpserver.HttpExchange

internal class MetricHandler(private val runs: Map<String, WebServer.RunInfo>) : BaseHttpHandler() {


    override fun getContent(exchange: HttpExchange): String {
        val elem = exchange.requestURI.path.split('/').drop(2)
        val run = elem.first()
        val metricName = elem.last()

        val data = StringBuilder("[")
        val info = runs.getValue(run)
        val metrics = info.roboquant.logger.getMetric(metricName, run)
        metrics.forEachIndexed { index, observation ->
            data.append("""{"time":${observation.time.epochSecond},"value":${observation.value}}""")
            if (index < (metrics.size -1)) data.append(",")
        }
        data.append("]")
        return data.toString()
    }

}