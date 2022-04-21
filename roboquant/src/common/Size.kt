package org.roboquant.common

import java.math.BigDecimal
import kotlin.math.absoluteValue
import kotlin.math.sign


/**
 * Represents the size of orders, positions and trades. This implementation is precise up to 8 decimals, ensuring that order
 * sizes are always precise even with fractional orders.
 */
@JvmInline
value class Size private constructor (private val value: Long) : Comparable<Size> {

    /**
     * Translates an [Int] [value] to a [Size]
     */
    constructor(value: Int) : this(value * FRACTION)

    /**
     * Translates a [BigDecimal] [value] to a [Size]
     */
    constructor(value: BigDecimal) : this(value.multiply(BD_FRACTION).toLong())

    /**
     * Translaes the [String] representation of a numeric [value] to a Size
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


    val iszero: Boolean
        get() = value == 0L


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

    operator fun times(value: Number) : Double = toDouble() * value.toDouble()

    operator fun div(value: Number) : Double = toDouble() / value.toDouble()

    operator fun plus(other: Size) = Size(value + other.value)

    operator fun minus(other: Size) = Size(value - other.value)

    operator fun compareTo(other: Number) = value.toDouble().compareTo(other.toDouble())

    override operator fun compareTo(other: Size) = value.compareTo(other.value)

    operator fun unaryMinus(): Size = Size(-value)

    override fun toString(): String = toBigDecimal().stripTrailingZeros().toPlainString()

}
