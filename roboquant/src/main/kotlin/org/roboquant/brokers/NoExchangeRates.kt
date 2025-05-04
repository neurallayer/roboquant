/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Currency
import org.roboquant.common.UnsupportedException
import java.time.Instant

/**
 * This implementation will not convert between different currencies and only return an exchange rate of 1.0 if from
 * and to are the same currency.
 * It will throw an [UnsupportedException] in all other use cases.
 *
 * @constructor Create a new instance
 */
class NoExchangeRates : ExchangeRates {

    /**
     * Return the conversion rate between two currencies.
     * @see ExchangeRates.getRate
     *
     * @param to
     * @param amount The total amount to be converted
     * @return The rate to use
     */
    override fun getRate(amount: Amount, to: Currency, time: Instant): Double {
        amount.currency === to && return 1.0
        throw UnsupportedException("Cannot convert $amount to $to")
    }

}
