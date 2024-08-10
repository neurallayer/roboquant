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

package org.roboquant.strategies

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * The Strategy is the interface that any trading strategy will need to implement. A strategy receives an
 * [Event] and [Account] and can generate zero or more Instructions/Orders.
 *
 * Roboquant makes no assumptions on the type of strategy. It can range from a technical indicator all the way
 * to sentiment analysis using machine learning.
 */
interface Strategy  {

    /**
     * Generate zero or more [signals][Signal] based on received [event].
     */
    fun createSignals(event: Event): List<Signal>

}

