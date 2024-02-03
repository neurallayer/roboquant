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

package org.roboquant.tiingo

import org.roboquant.common.Config

/**
 * Configuration for Tiingo connections
 *
 * @property key the Tiingo api key to use (property name is `tiingo.key`)
 */
data class TiingoConfig(
    var key: String = Config.getProperty("tiingo.key", ""),
)
