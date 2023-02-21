/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.strategies

import org.roboquant.feeds.Event

/**
 * Strategy that doesn't generate any signals. This is especially useful if you develop your Strategy as
 * a Policy (for example when you require access to the Account). In that case the NoSignalStrategy just serves as
 * a pass through and all logic can be handled by the policy.
 *
 * ## Example
 *      val roboquant =  Roboquant(NoSignalStrategy(), MyCustomPolicy())
 *
 * @constructor Create new NoSignalStrategy
 */
class NoSignalStrategy : Strategy {
    override fun generate(event: Event): List<Signal> {
        return emptyList()
    }
}