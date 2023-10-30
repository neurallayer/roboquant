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

package org.roboquant.common

/**
 * Interface implemented by all components used in a run (Strategy, Policy, Broker, Metric, MetricLogger).
 *
 * The component will be informed that a run has been started or ended. It provides the component with the opportunity
 * to manage its state, so a run can be started without any state remaining from previous runs.
 */
interface Lifecycle {

    /**
     * Invoked at the start of a [run]. The default implementation is to take no action.
     */
    fun start(run: String, timeframe: Timeframe) {
        // default is to do nothing
    }

    /**
     * Invoked at the end of a [run]. The default implementation is to take no action.
     */
    fun end(run: String) {
        // default is to do nothing
    }

    /**
     * Reset the state of the component to its initial state. The default implementation is to take no action.
     */
    fun reset() {
        // default is to do nothing
    }

}
