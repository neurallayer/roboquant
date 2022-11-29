package org.roboquant.orders

/**
 * One Triggers Other order, if the primary order it executed, the secondary order will automatically be activated,
 *
 * @property primary
 * @property secondary
 * @constructor
 *
 * @param tag
 */
class OTOOrder(
    val primary: SingleOrder,
    val secondary: SingleOrder,
    tag: String = ""
) : CreateOrder(primary.asset, tag) {

    init {
        require(primary.asset == secondary.asset) { "OTO orders can only contain orders for the same asset" }
    }

    override fun info() = sortedMapOf("first" to primary, "second" to secondary)

}