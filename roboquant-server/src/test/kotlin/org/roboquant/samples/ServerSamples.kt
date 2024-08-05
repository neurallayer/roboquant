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

import org.roboquant.common.*
import org.roboquant.journals.MemoryJournal
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PriceMetric
import org.roboquant.server.WebServer
import org.roboquant.strategies.EMACrossover
import kotlin.system.exitProcess
import kotlin.test.Ignore
import kotlin.test.Test


internal class ServerSamples {

    private fun getJournal() =
        MemoryJournal(PriceMetric("CLOSE"), AccountMetric())



}
