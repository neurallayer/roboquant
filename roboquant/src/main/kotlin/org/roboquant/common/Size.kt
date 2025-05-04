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

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Represents the size of orders, positions and trades. This implementation is precise up to 8 decimals, ensuring that
 * order and position sizes are accurate enough when dealing with most fractional orders.
 *
 * Since this implementation is a value class, there is no overhead compared to a Double or Long.
 */
@JvmInline
value class Size private constructor(private val value: Long) : Comparable<Size> {

    /**
     * Creates a Size instance based a an [Int] [value]
     */
    constructor(value: Int) : this(value * FRACTION)

    /**
     * Creates a Size instance based a [BigDecimal] [value]. If the value cannot be represented 100% accurate, an
     * exception is thrown.
     */
    constructor(value: BigDecimal) : this(value.multiply(BD_FRACTION).longValueExact())

    /**
     * Creates a Size instance based a [Double] [value].
     * Be careful using this constructor since a Double is not always precise.
     * Also overflows and lost of precision don't lead to an exception.
     *
     * Better to use the Size constructor with a [String] or [BigDecimal] as its parameter instead:
     * ```
     * val size = Size("0.001")
     * ```
     */
    constructor(value: Double) : this(BigDecimal.valueOf(value).multiply(BD_FRACTION).toLong())

    /**
     * Creates a Size instance based on the [String] representation of a numeric [value]. If the value cannot be
     * represented 100% accurate, an exception is thrown.
     */
    constructor(value: String) : this(BigDecimal(value).multiply(BD_FRACTION).longValueExact())

    /**
     * @suppress
     */
    companion object {
        // We use 8 digits scale
        private const val SCALE = 8
        private const val FRACTION = 100_000_000L
        private const val DOUBLE_FRACTION = 100_000_000.0
        private val BD_FRACTION = BigDecimal(FRACTION)

        /**
         * This method should normally not be used, but allows to create a new Size from the underlying value
         */
        fun fromUnderlyingValue(x: Long): Size = Size(x)

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
     * Converts this [Size] value to a [Double], this conversion might lose precision
     */
    fun toDouble() = value / DOUBLE_FRACTION

    /**
     * Converts this [Size] value to a [BigDecimal]
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
     * Return true is positive size, false otherwise
     */
    val isPositive: Boolean
        get() = value > 0

    /**
     * Return true is negative size, false otherwise
     */
    val isNegative: Boolean
        get() = value < 0

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
     *
     * @see Long.sign
     */
    val sign: Int
        get() = value.sign

    /**
     * Round the size to nearest fraction
     */
    fun round(scale: Int) = Size(toBigDecimal().setScale(scale, RoundingMode.DOWN))

    /**
     * Multiplies this size by the [other] value. This method might lose precision.
     */
    operator fun times(other: Number): Size = Size(toDouble() * other.toDouble())

    /**
     * Divides this size by the [other] value. This method might lose precision.
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
     * Compare the [other] Int to this size.
     */
    operator fun compareTo(other: Int): Int = compareTo(Size(other))

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
