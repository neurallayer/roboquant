package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Size
import java.time.Instant


enum class TIF {
    DAY,
    GTC
}

/**
 * Base class for all types of create orders.
 *
 * The only thing they all have in common is they refer to a single [asset] and can optionally have a tag associated with them.
 */
data class Order(
    val asset: Asset,
    val size: Size,
    val limit: Double,
    val tif: TIF = TIF.DAY,
    val tag: String = ""
)  {

    val buy: Boolean
        get() = size > 0

    val sell: Boolean
        get() = size < 0

    /**
     * The order id is set by broker once placed, before that it contains an empty string.
     */
    var id = ""

    /**
     * How much is filled
     */
    var fill = Size.ZERO

    fun cancel(): Order {
        require(id != "")
        return copy(size = Size.ZERO)
    }

    fun modify(size: Size = this.size, limit: Double = this.limit) : Order {
        require(id != "")
        return copy(size = size, limit = limit)
    }

    fun isCancellation(): Boolean {
        return size == Size.ZERO && id != ""
    }

    fun isExecutable(price: Double): Boolean {
        return (buy and (price <= limit)) || (sell and (price >= limit))
    }

    fun isModify(): Boolean {
        return size != Size.ZERO && id != ""
    }

    /**
     * Returns a unified string representation for the different order types
     */
    override fun toString(): String {
        return "asset=$asset id=$id tag=$tag"
    }

}
