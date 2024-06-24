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

package org.roboquant.server

import org.roboquant.brokers.Broker
import org.roboquant.common.Timeframe
import org.roboquant.journals.MemoryJournal

/**
 * Stored information about a single run
 */
internal data class RunInfo(
    val journal: MemoryJournal,
    val timeframe: Timeframe,
    val policy: PausablePolicy,
    val broker: Broker,
    var done: Boolean = false
)
