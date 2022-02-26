package org.roboquant.orders



class OCOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
) : Order(first.asset, id) {

    init {
        require(first.asset == second.asset) { "OCO orders can only contain orders for the same asset"}
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)
}


class OTOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
) : Order(first.asset, id)  {

    init {
        require(first.asset == second.asset) { "OTO orders can only contain orders for the same asset"}
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)

}


class BracketOrder(
    val entry: SingleOrder,
    val takeProfit: SingleOrder,
    val stopLoss: SingleOrder,
    id: Int = nextId(),
) : Order(entry.asset, id) {

    init {
        require(entry.asset == takeProfit.asset && entry.asset == stopLoss.asset) { "Bracket orders can only contain orders for the same asset"}
        require(entry.quantity == -takeProfit.quantity && entry.quantity == -stopLoss.quantity) { "Bracket orders takeProfit and stopLoss orders need to close position"}
    }

    override fun info() = sortedMapOf("entry" to entry, "takeProfit" to takeProfit, "stopLoss" to "stopLoss")
}

