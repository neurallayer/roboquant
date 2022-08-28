/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant

import org.roboquant.brokers.Account
import org.roboquant.brokers.InternalAccount
import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.common.USD
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus

/**
 * Test data used in unit tests
 */
object TestData {

    fun usAccount(): Account {
        val asset1 = Asset("AAA")
        val asset2 = Asset("AAB")
        val account = InternalAccount()
        account.cash.deposit(100_000.USD)
        account.setPosition(Position(asset1, 100, 10.0))
        account.setPosition(Position(asset2, 100, 10.0))

        val order = OrderState(MarketOrder(asset1, 100), OrderStatus.INITIAL)
        account.putOrders(listOf(order))
        return account.toAccount()
    }

}