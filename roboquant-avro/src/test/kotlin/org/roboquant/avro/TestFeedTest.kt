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

package org.roboquant.avro

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed

fun play(feed: Feed, timeframe: Timeframe = Timeframe.INFINITE): EventChannel {
    val channel = EventChannel(timeframe = timeframe)

    CoroutineScope(Dispatchers.IO + Job()).launch {
        feed.play(channel)
        channel.close()
    }
    return channel
}

