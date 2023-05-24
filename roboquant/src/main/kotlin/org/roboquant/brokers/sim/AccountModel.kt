/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.brokers.sim

import org.roboquant.brokers.Account
import org.roboquant.brokers.sim.execution.InternalAccount

/**
 * Interface for modeling different types of Accounts used in the [SimBroker], like a [CashAccount] or [MarginAccount]
 *
 * Currently, the main functionality is that at the end of each step the buying power is re-calculated and made
 * available in the attribute [Account.buyingPower]. But in the future, the implementation could make other updates
 * to the account, for example, calculate borrowing fees or interest rates that might apply.
 */
interface AccountModel {

    /**
     * Update the [account] based on the rules within the account model. Currently only the buying-power is calculated
     * and set.
     */
    fun updateAccount(account: InternalAccount)

}

