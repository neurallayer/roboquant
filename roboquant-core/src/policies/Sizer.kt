package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import kotlin.math.floor

/**
 * Sizer helps a policy to determine the sizing of orders.
 *
 * @constructor Create empty Sizer
 */
interface Sizer {
    fun size(asset: Asset, account: Account, event: Event): Double
}

/**
 * Fixed value amount sizer allocates fixed percentage of the total value of the account. This sizer doesn't
 * take volatility into account
 *
 * @property percentage percentage of total value to use, default is 0.01 (= 1%)
 * @constructor Create bew Fixed amount sizer
 */
class FixedValueSizer(val percentage: Double = 0.01) : Sizer {

    override fun size(asset: Asset, account: Account, event: Event): Double {

        // TODO can cache the total value
        val available = account.convertToCurrency(account.getValue(), now = event.now) * percentage
        val price = event.getPrice(asset)!!

        return floor(available / price)
    }

}