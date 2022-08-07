package org.roboquant.common

import java.math.BigDecimal
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Represents the size of orders, positions and trades. This implementation is precise up to 8 decimals, ensuring that
 * order and position sizes are precise enough even when dealing with fractional orders.
 *
 * Since this implementation uses a value class, so there almost no overhead compared to the underlying
 * primitive, a [Long]
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
     * Translates the [String] representation of a numeric [value] to a Size
     */
    constructor(value: String) : this(BigDecimal(value).multiply(BD_FRACTION).toLong())

    companion object {
        // We use max 8 digits scale
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
    fun toBigDecimal() = BigDecimal(value).setScale(SCALE).divide(BD_FRACTION)

    /**
     * Returns true if the size is zero, false otherwise
     */
    val iszero: Boolean
        get() = value == 0L

    /**
     * Returns true if the size non-zero, false otherwise
     */
    val nonzero: Boolean
        get() = value != 0L

    /**
     * Returns the absolute value of this value
     */
    val absoluteValue: Size
        get() = Size(value.absoluteValue)

    /**
     * Returns the sign of this value
     */
    val sign: Int
        get() = value.sign

    /**
     * Multiplies this value by the [other] value.
     */
    operator fun times(other: Number): Double = toDouble() * other.toDouble()

    /**
     * Divides this value by the [other] value.
     */
    operator fun div(other: Number): Double = toDouble() / other.toDouble()

    /**
     * Adds the [other] value to this value.
     */
    operator fun plus(other: Size) = Size(value + other.value)

    /**
     * Subtracts the [other] value from this value.
     */
    operator fun minus(other: Size) = Size(value - other.value)

    /**
     * Compare the [other] number to this value.
     */
    operator fun compareTo(other: Number) = value.toDouble().compareTo(other.toDouble())

    /**
     * Compare the [other] size to this value.
     */
    override operator fun compareTo(other: Size) = value.compareTo(other.value)

    /**
     * Return the unary minus
     */
    operator fun unaryMinus(): Size = Size(-value)

    /**
     * To string
     */
    override fun toString(): String = toBigDecimal().stripTrailingZeros().toPlainString()

}
