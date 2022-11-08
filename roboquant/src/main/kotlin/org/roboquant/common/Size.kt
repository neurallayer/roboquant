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

import java.math.BigDecimal
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Represents the size of orders, positions and trades. This implementation is precise up to 8 decimals, ensuring that
 * order and position sizes are precise enough even when dealing with fractional orders.
 *
 * Since this implementation is a value class, there is almost no overhead compared to for example a Double or Long.
 */
@JvmInline
value class Size private constructor(private val value: Long) : Comparable<Size> {

    /**
     * Translates an [Int] [value] to a [Size]
     */
    constructor(value: Int) : this(value * FRACTION)

    /**
     * Translates a [BigDecimal] [value] to a [Size]
     */
    constructor(value: BigDecimal) : this(value.multiply(BD_FRACTION).toLong())

    /**
     * Translates a [Double] [value] to a [Size]
     */
    constructor(value: Double) : this(BigDecimal.valueOf(value))

    /**
     * Translates the [String] representation of a numeric [value] to a Size
     */
    constructor(value: String) : this(BigDecimal(value).multiply(BD_FRACTION).toLong())

    /**
     * @suppress
     */
    companion object {
        // We use 8 digits scale
        private const val SCALE = 8
        private const val FRACTION = 100_000_000L
        private val BD_FRACTION = BigDecimal(FRACTION)

        /**
         * Size of zero
         */
        val ZERO = Size(0)

        /**
         * Size of one
         */
        val ONE = Size(1)
    }

    /**
     * Converts this [Size] value to [Double]
     */
    fun toDouble() = value / FRACTION.toDouble()

    /**
     * Converts this [Size] value to [BigDecimal]
     */
    fun toBigDecimal(): BigDecimal = BigDecimal(value).setScale(SCALE).divide(BD_FRACTION)


    /**
     * Returns true if the size is zero, false otherwise.
     */
    val iszero: Boolean
        get() = value == 0L

    /**
     * Returns true if the size non-zero, false otherwise.
     */
    val nonzero: Boolean
        get() = value != 0L

    /**
     * Returns true is this represents a fractional size, false otherwise.
     */
    val isFractional: Boolean
        get() = (value % FRACTION) != 0L

    /**
     * Returns the absolute value of this size.
     */
    val absoluteValue: Size
        get() = Size(value.absoluteValue)

    /**
     * Returns the sign (direction) of this size.
     */
    val sign: Int
        get() = value.sign

    /**
     * Multiplies this size by the [other] value.
     */
    operator fun times(other: Number): Size = Size(toDouble() * other.toDouble())

    /**
     * Divides this size by the [other] value.
     */
    operator fun div(other: Number): Size = Size(toDouble() / other.toDouble())

    /**
     * Adds the [other] size to this size.
     */
    operator fun plus(other: Size): Size = Size(value + other.value)

    /**
     * Subtracts the [other] size from this size.
     */
    operator fun minus(other: Size): Size = Size(value - other.value)

    /**
     * Compare the [other] number to this size.
     */
    operator fun compareTo(other: Number): Int = value.toDouble().compareTo(other.toDouble())

    /**
     * Compare the [other] size to this size.
     */
    override operator fun compareTo(other: Size): Int = value.compareTo(other.value)

    /**
     * Return the unary minus
     */
    operator fun unaryMinus(): Size = Size(-value)

    /**
     * To string
     */
    override fun toString(): String = toBigDecimal().stripTrailingZeros().toPlainString()

}
