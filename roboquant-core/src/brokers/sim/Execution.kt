package org.roboquant.brokers.sim

import org.roboquant.common.Asset

/**
 * Return how much (if any) of the order was filled and against what price. The price is in
 * the currency denoted by the asset underlying asset.
 *
 * @property asset
 * @property quantity
 * @property price
 * @constructor Create empty Execution
 */
class Execution(val asset: Asset, val quantity: Double, val price: Double) {

    init {
        require(quantity != 0.0) { "Execution should have a non-zero quantity" }
    }


    fun cost(): Double  = asset.multiplier * quantity * price
    fun size(): Double  = asset.multiplier * quantity

}