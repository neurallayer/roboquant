package org.roboquant.common

import java.math.BigDecimal

/**
 * Size of orders and positions, using high precision calculations
 *
 * @TODO: faster implementation, perhaps use Long internally
 *
 * @property value
 * @constructor Create new Size
 */
class Size(private val value: BigDecimal) : Number()   {

    constructor(value: Int) : this(BigDecimal.valueOf(value.toLong()))
    constructor(value: String) : this(BigDecimal(value))

    companion object {
        val ZERO = Size(BigDecimal.ZERO)
        val ONE = Size(BigDecimal.ONE)
    }

    override fun toByte(): Byte = value.toByte()

    override fun toChar(): Char  = value.toChar()

    override fun toInt(): Int = value.toInt()

    override fun toLong(): Long = value.toLong()

    override fun toFloat(): Float = value.toFloat()

    override fun toShort(): Short = value.toShort()

    override fun toDouble(): Double  = value.toDouble()

    fun toBigDecimal(): BigDecimal = value


    operator fun times(value: Double) : Double = toDouble() * value

    val iszero: Boolean
        get() = BigDecimal.ZERO.compareTo(this.value) == 0

    val nonzero: Boolean
        get() = ! iszero

    val absoluteValue
        get() = value.abs()

    val sign
        get() = value.signum()

    operator fun plus(value: Size) = Size(this.value + value.value)

    operator fun minus(value: Size) = Size(this.value - value.value)

    operator fun compareTo(i: Number) = value.toDouble().compareTo(i.toDouble())

    operator fun unaryMinus(): Size = Size(value.unaryMinus())

    override fun toString(): String = value.toString()

    override fun hashCode(): Int = value.hashCode()

    override fun equals(other: Any?): Boolean = if (other is Size) value.compareTo(other.value) == 0 else false

}