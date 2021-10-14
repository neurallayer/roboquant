/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import oanda.OANDAConnection
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.CurrencyConverter
import org.roboquant.brokers.Position
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

class OANDABroker(
    accountID: String? = null,
    token: String? = null,
    demoAccount: Boolean = true,
    currencyConverter: CurrencyConverter? = null,
) : Broker {

    private val ctx: Context = OANDAConnection.getContext(token, demoAccount)
    override val account: Account = Account(currencyConverter = currencyConverter)
    private val logger = Logging.getLogger("OANDABroker")

    val availableAssets = OANDAConnection.getAvailableAssets(ctx)
    private val accountID = OANDAConnection.getAccountID(accountID, ctx)

    init {
        initAccount()
        logger.info("Retrieved account with id $accountID")
    }


    private fun initAccount() {
        val summary = ctx.account.summary(AccountID(accountID)).account
        account.baseCurrency = Currency.getInstance(summary.currency.toString())
        account.cash.deposit(account.baseCurrency, summary.balance.doubleValue())
        val positions = ctx.position.listOpen(AccountID(accountID)).positions

        for (p in positions) {
            val symbol = p.instrument.toString()
            val asset = availableAssets[symbol]!!
            if (p.long.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.long.units.doubleValue(), p.long.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
            if (p.short.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.short.units.doubleValue(), p.short.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
        }

        account.time = Instant.now()
    }

    /**
     * TODO implement this logic, right now all orders will be rejected.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        orders.forEach { it.status = OrderStatus.REJECTED }
        account.orders.addAll(orders)
        account.portfolio.updateMarketPrices(event)
        return account
    }
}