package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.assets
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * Allow only one open order per asset. Signals generated when there is an open order for the same asset, will be
 * removed before handing it over to the wrapped policy
 *
 * @property policy The policy to wrap
 */
private class OneOrderPerAsset(private val policy: Policy) : Policy by policy {

    private val logger = Logging.getLogger(this::class)

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = policy.act(signals, account, event)
        val openOrderAssets = account.openOrders.assets
        val newOrders = orders.filter { ! openOrderAssets.contains(it.asset) }
        logger.fine { "orders in=${orders.size} out=${orders.size}" }
        return newOrders
    }
}


/**
 * Ensure there is only one open order per asset at any given time.
 */
fun Policy.oneOrderPerAsset() : Policy = OneOrderPerAsset(this)
