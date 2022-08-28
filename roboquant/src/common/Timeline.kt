/*
 * Copyright 2022 Neural Layer
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

import java.time.Instant

/**
 * Timeline is an ordered list of [Instant] instances, sorted from old to new. Every [Instant] is unique.
 *
 * Currently, it is just a typealias for List<Instant>, but this might change in the future.
 */
typealias Timeline = List<Instant>


/**
 * Return the index of the time that is closets to the provided time but doesn't exceed it. So it is the most recent
 * time but without looking into the future.
 *
 * If no such time is found return null
 */
fun Timeline.latestNotAfter(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 2 else idx
    return if (idx >= 0) idx else null
}

/**
 * Return the index of the time that is closets to the provided time but doesn't proceed it.
 *
 * If no such time is found return null
 */
fun Timeline.earliestNotBefore(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 1 else idx
    return if (idx < size) idx else null
}

/**
 * Return the timeframe for this timeline. If the timeline is empty, an exception will be thrown.
 */
val Timeline.timeframe
    get() = Timeframe.inclusive(first(), last())

/**
 * Split the timeline in chunks of [size]
 */
fun Timeline.split(size: Int): List<Timeframe> {
    require(size > 1) { "Minimum requires 2 elements in timeline" }
    val chunks = chunked(size)
    val result = mutableListOf<Timeframe>()
    for (chunk in chunks) {
        if (size > 1) result.add(Timeframe(chunk.first(), chunk.last()))
    }
    result[result.lastIndex] = result.last().inclusive
    return result
}
