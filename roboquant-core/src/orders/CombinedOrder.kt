package org.roboquant.orders

import org.roboquant.common.Asset

data class OneCancelsOtherOrder(
    val first: Order,
    val second: Order,
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : TradeOrder {

    init {
        require(first.asset == second.asset)
    }

    override val asset: Asset
        get() = first.asset
}


data class OneTriggersOtherOrder(
    val first: Order,
    val second: Order,
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : TradeOrder {

    init {
        require(first.asset == second.asset)
    }

    override val asset: Asset
        get() = first.asset
}


data class BracketOrder(
    val entry: SingleOrder,
    val takeProfit: SingleOrder,
    val stopLoss: SingleOrder,
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : TradeOrder {

    init {
        require(entry.asset == takeProfit.asset && stopLoss.asset == entry.asset)
        require(entry.quantity == -takeProfit.quantity && entry.quantity == -stopLoss.quantity)
    }

    override val asset: Asset
        get() = entry.asset

}

