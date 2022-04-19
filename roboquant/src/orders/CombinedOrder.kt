package org.roboquant.orders



class OCOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(first.asset, id, tag) {

    init {
        require(first.asset == second.asset) { "OCO orders can only contain orders for the same asset"}
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)
}


class OTOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(first.asset, id, tag)  {

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
    tag: String = ""
) : Order(entry.asset, id, tag) {

    init {
        require(entry.asset == takeProfit.asset && entry.asset == stopLoss.asset) { "Bracket orders can only contain orders for the same asset"}
        require(entry.size == -takeProfit.size && entry.size == -stopLoss.size) { "Bracket orders takeProfit and stopLoss orders need to close position"}
    }

    override fun info() = sortedMapOf("entry" to entry, "takeProfit" to takeProfit, "stopLoss" to "stopLoss")
}

