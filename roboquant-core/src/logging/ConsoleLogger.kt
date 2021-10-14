/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.logging

import org.roboquant.RunInfo

/**
 * Use plain println to output metric results to the console. This works with Notebooks and standalone applications.
 * By default, it will log metrics in a single line, but by setting [splitMetrics] to true, every metric will be logged
 * to separate line.
 */
class ConsoleLogger(private val splitMetrics: Boolean = false) : MetricsLogger {

    override fun log(results: Map<String, Number>, info: RunInfo) {
        if (results.isEmpty()) return

        if (splitMetrics) {
            results.forEach {
                println("$info ${it.key}=${it.value}")
            }
        } else {
            println("$info $results")
        }
    }

}
