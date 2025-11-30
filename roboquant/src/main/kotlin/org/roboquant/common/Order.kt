package org.roboquant.common

/**
 * Time-in-Force policies for orders
 * - DAY: valid for the trading day only
 * - GTC: Good-Til-Cancelled, remains active until explicitly cancelled
 */ 
enum class TIF {
    DAY,
    GTC
}

/**
 * Data class for all orders. The logic that is applied:
 * - New create orders don't have an id yet until it is set by the broker implementation.
 * - Cancel orders are orders with a known id and size zero. Only cancellation orders can have a size of zero
 * - Modify orders are orders with a known id but with updated properties like size and limit.
 *
 * @property asset the underlying asset
 * @property size the size the order
 * @property limit the limit price
 * @property tif the time-in-force policy, default is DAY
 * @property tag any optional tag string, default is empty string
 */
data class Order(
    val asset: Asset,
    val size: Size,
    val limit: Double,
    val tif: TIF = TIF.DAY,
    val tag: String = ""
)  {

    /**
     * Return the remaining order size to be filled. This is a derived property
     * based om the [size] minus the [fill]
     */
    val remaining: Size
        get() = size - fill

    /**
     * True if a BUY order, false otherwise
     */
    val buy: Boolean
        get() = size > 0

    /**
     * True is a SELL order, false otherwise
     */
    val sell: Boolean
        get() = size < 0

    /**
     * The broker manages the order-id once placed, before that it contains an empty string.
     * So don't manually set the id.
     */
    var id = ""

    /**
     * How much is filled
     */
    var fill = Size.ZERO

    /**
     * Cancel an existing order
     */
    fun cancel(): Order {
        require(id != "")
        return copy(size = Size.ZERO)
    }

    /**
     * Modify an existing order
     */
    fun modify(size: Size = this.size, limit: Double = this.limit) : Order {
        require(id != "")
        return copy(size = size, limit = limit)
    }

    /**
     * Return true if an cancellation order, false otherwise.
     */
    fun isCancellation(): Boolean {
        return size == Size.ZERO && id != ""
    }

    /**
     * Given the provided price, is this order executable
     */
    fun isExecutable(price: Double): Boolean {
        return (buy and (price <= limit)) || (sell and (price >= limit))
    }

    /**
     * Returns a unified string representation for the different order types
     */
    override fun toString(): String {
        return "asset=$asset id=$id tag=$tag"
    }

    /**
     * Calculate the remaining value
     */
    fun value(price: Double) : Amount {
        return asset.value(size, price)
    }

}
