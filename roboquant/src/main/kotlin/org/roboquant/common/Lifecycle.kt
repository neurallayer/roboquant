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

import org.roboquant.RunInfo

/**
 * Interface implemented by all components used in a run (Strategy, Policy, Broker, Metric, MetricLogger).
 *
 * The component will be informed that a phase has been started or ended. It provides the component with the opportunity
 * to manage its state so a phase can be started without any state remaining from previous runs.
 *
 */
interface Lifecycle {

    /**
     * Invoked at the start of a [run]. Default implementation is to invoke [reset], which is suitable for
     * many type of components.
     */
    fun start(run: RunInfo) {
        reset()
    }

    /**
     * Invoked at the end of a [run]. Default implementation is to take no action.
     */
    fun end(run: RunInfo) {
        // default is to do nothing
    }

    /**
     * Reset the state of the component to its initial state, default implementation is to take no action. This is
     * typically the only method to override when wanting to reset some internal state since by default the [start]
     * method invokes this method.
     */
    fun reset() {
        // default is to do nothing
    }

}
