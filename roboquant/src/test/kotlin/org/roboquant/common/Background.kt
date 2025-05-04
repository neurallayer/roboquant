/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Background object helps to deal with starting and running co-routines. Right now very light implementation, but
 * in future features can be added without impacting the rest of the code. This is used internally only,
 * see [ParallelJobs] for the public API.
 */
internal object Background {

    private val boundScope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * Launch an IO bound [block] and return the job
     */
    fun job(block: suspend CoroutineScope.() -> Unit): Job {
        return boundScope.launch(block = block)
    }

}
