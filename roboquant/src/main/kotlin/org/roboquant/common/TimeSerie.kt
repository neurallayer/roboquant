/*
 * Copyright 2020-2022 Neural Layer
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
 * Observation represents a double [value] at a certain point in [time]. Observations can be compared based on
 * their [time].
 *
 * @property time the time when the value was observed expressed as an [Instant]
 * @property value the value that was observed expressed as a [Double]
 */
class Observation(val time: Instant, val value: Double) : Comparable<Observation> {

    /**
     * Compare to [other] observation based on their [time]
     */
    override fun compareTo(other: Observation): Int = time.compareTo(other.time)
}

/**
 * TimeSerie is an ordered list of [Observations][Observation] sorted from oldest to newest. There can be no
 * observations with the same [Observation.time] in a timeserie.
 */
typealias TimeSerie = List<Observation>

/**
 * Return the values of the timeserie as a [DoubleArray]
 */
fun TimeSerie.toDoubleArray(): DoubleArray = map { it.value }.toDoubleArray()

/**
 * Return the [Timeline] of the [TimeSerie]
 */
fun TimeSerie.timeline(): Timeline = map { it.time }
