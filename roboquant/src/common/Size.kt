package org.roboquant.common

import java.math.BigDecimal
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Size of orders and positions, using high precision calculations
 *
class Size2(private val value: BigDecimal)  : Comparable<Size> {

    constructor(value: Int) : this(BigDecimal.valueOf(value.toLong()))
    constructor(value: String) : this(BigDecimal(value))

    companion object {
        val ZERO = Size(BigDecimal.ZERO)
        val ONE = Size(BigDecimal.ONE)
    }

    /*
    override fun toByte(): Byte = value.toByte()

    override fun toChar(): Char  = value.toChar()

    override fun toInt(): Int = value.toInt()

    override fun toLong(): Long = value.toLong()

    override fun toFloat(): Float = value.toFloat()

    override fun toShort(): Short = value.toShort()
     */

    fun toDouble(): Double  = value.toDouble()

    fun toBigDecimal(): BigDecimal = value


    operator fun times(value: Double) : Double = toDouble() * value

    val iszero: Boolean
        get() = BigDecimal.ZERO.compareTo(this.value) == 0

    val nonzero: Boolean
        get() = ! iszero

    val absoluteValue
        get() = Size(value.abs())

    val sign
        get() = value.signum()

    operator fun plus(value: Size) = Size(this.value + value.value)

    operator fun minus(value: Size) = Size(this.value - value.value)

    operator fun compareTo(i: Number) = value.toDouble().compareTo(i.toDouble())

    operator fun unaryMinus(): Size = Size(value.unaryMinus())

    override fun toString(): String = value.toString()

    override fun hashCode(): Int = value.hashCode()

    override fun compareTo(other: Size): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean = if (other is Size) value.compareTo(other.value) == 0 else false

}
*/

/**
 * Size of orders and positions, using high precision storage with minimum overhead.
 */
@JvmInline
value class Size private constructor (private val value: Long) : Comparable<Size> {

    /**
     * Create a new instance based on an integer [value]
     */
    constructor(value: Int) : this(value * FRACTION)

    /**
     * Create a new instance based on an BigDecimal [value]
     */
    constructor(value: BigDecimal) : this(value.multiply(BD_FRACTION).toLong())

    /**
     * Create a new instance based on an string [value] that represents the number
     */
    constructor(value: String) : this(BigDecimal(value).multiply(BD_FRACTION).toLong())

    companion object {
        // We use max 8 digits scale
        private const val SCALE = 8
        private const val FRACTION = 100_000_000L
        private val BD_FRACTION = BigDecimal(FRACTION)

        val ZERO = Size(0)
        val ONE = Size(1)
    }

    /**
     * Get the size as a double
     */
    fun toDouble() = value / FRACTION.toDouble()

    /**
     * Get the size as a BigDecimal
     */
    fun toBigDecimal() = BigDecimal(value).setScale(SCALE).divide(BD_FRACTION)

    val iszero: Boolean
        get() = value == 0L

    val nonzero: Boolean
        get() = value != 0L

    val absoluteValue: Size
        get() = Size(value.absoluteValue)

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
